// Copyright (C) 2026 Vibhor Goel; SPDX-License-Identifier: GPL-3.0-or-later
import { InvalidReport, MAX_BYTES, sanitizedJson, validateReport } from "./report";

export interface Env {
  DB: D1Database;
  SOURCE_LIMIT: RateLimit;
  INGEST_LIMIT: RateLimit;
  // Server-only secret, never shipped in the APK. Required even during local development.
  RATE_LIMIT_SECRET: string;
  DISCORD_WEBHOOK_URL?: string;
}
const DAY = 86400;
const RETENTION = 30 * DAY;
const DAILY_REPORTS = 1000;
const DAILY_BYTES = 32 * 1024 * 1024;
const encoder = new TextEncoder();
class HttpError extends Error { constructor(readonly status: number, readonly code: string) { super(code); } }
function reply(status: number, code: string) {
  return Response.json({ status: code }, { status, headers: {
    "Cache-Control": "no-store", "X-Content-Type-Options": "nosniff",
    ...(status === 405 ? { Allow: "POST" } : {}),
    ...([429, 503].includes(status) ? { "Retry-After": "60" } : {}),
  } });
}
function hex(bytes: ArrayBuffer) { return [...new Uint8Array(bytes)].map(b => b.toString(16).padStart(2, "0")).join(""); }
async function sourceKey(request: Request, secret: string, day: number) {
  if (!secret || secret.length < 32) throw new Error("Missing rate limit secret");
  const key = await crypto.subtle.importKey("raw", encoder.encode(secret), { name: "HMAC", hash: "SHA-256" }, false, ["sign"]);
  // Cloudflare supplies this header at its edge. Never trust forwarded-for/client IDs.
  // No raw IP or keyed digest is written to D1; the limiter key changes each day.
  return hex(await crypto.subtle.sign("HMAC", key, encoder.encode(`${day}:${request.headers.get("CF-Connecting-IP") ?? "unknown"}`)));
}
async function readBody(request: Request) {
  const declared = request.headers.get("content-length");
  if (declared !== null && (!/^\d+$/.test(declared) || Number(declared) > MAX_BYTES)) throw new HttpError(413, "body_too_large");
  if (!request.body) throw new HttpError(400, "invalid_report");
  const reader = request.body.getReader();
  const buffer = new Uint8Array(MAX_BYTES);
  let size = 0;
  // A slow/incomplete upload cannot hold this handler open indefinitely.
  let timer: ReturnType<typeof setTimeout> | undefined;
  const timeout = new Promise<never>((_, reject) => {
    timer = setTimeout(() => { reject(new HttpError(408, "request_timeout")); void reader.cancel().catch(() => {}); }, 10000);
  });
  try {
    while (true) {
      const { done, value } = await Promise.race([reader.read(), timeout]);
      if (done) break;
      if (size + value.length > MAX_BYTES) { void reader.cancel().catch(() => {}); throw new HttpError(413, "body_too_large"); }
      buffer.set(value, size);
      size += value.length;
    }
    try { return JSON.parse(new TextDecoder("utf-8", { fatal: true, ignoreBOM: false }).decode(buffer.subarray(0, size))); }
    catch { throw new HttpError(400, "invalid_report"); }
  } finally { if (timer !== undefined) clearTimeout(timer); reader.releaseLock(); }
}

export async function expireReports(db: D1Database, now: number) {
  await db.batch([
    db.prepare("DELETE FROM crash_reports WHERE expires_at <= ?").bind(now),
    db.prepare("DELETE FROM daily_usage WHERE day < ?").bind(Math.floor(now / DAY) - 1),
  ]);
}

export async function notifyDiscord(env: Env, report: import("./report").Report): Promise<void> {
  const webhook = env.DISCORD_WEBHOOK_URL;
  if (!webhook) return;
  let url: URL;
  try { url = new URL(webhook); } catch { return; }
  if (url.protocol !== "https:" || url.hostname !== "discord.com" || !/^\/api\/webhooks\/\d+\/[^/]+$/.test(url.pathname)) return;
  // Deliberately omit exception messages, frames and detection context from third-party notifications.
  const device = report.device as Record<string, unknown>;
  const safe = (value: unknown) => String(value).replaceAll("`", "'").replace(/[\r\n]+/g, " ");
  const content = [
    "**New Macrion crash report**",
    `Report: \`${report.reportId}\``,
    `Exception: \`${safe(report.crash.type)}\``,
    `Build: \`${safe(report.build.versionName)} (${report.build.versionCode}), ${safe(report.build.flavor)}/${safe(report.build.buildType)}\``,
    `Device: \`${safe(device.manufacturer)} ${safe(device.model)}, Android ${safe(device.androidVersion)} (API ${safe(device.api)})\``,
  ].join("\n").slice(0, 2000);
  try {
    const response = await fetch(url, {
      method: "POST",
      headers: { "Content-Type": "application/json", "User-Agent": "Macrion-Crash-Reports/1.0" },
      body: JSON.stringify({ content, username: "Macrion crash reports", allowed_mentions: { parse: [] } }),
    });
    // Do not read or log Discord's body; alerts are supplementary to durable D1 storage.
    if (response.body) await response.body.cancel();
  } catch { /* Best effort: report retrieval does not depend on Discord. */ }
}

export default {
  async fetch(request: Request, env: Env, ctx: ExecutionContext): Promise<Response> {
    const url = new URL(request.url);
    if (url.pathname !== "/v1/reports" || url.search) return reply(404, "not_found");
    if (request.method !== "POST") return reply(405, "method_not_allowed");
    try {
      const now = Math.floor(Date.now() / 1000), day = Math.floor(now / DAY);
      if (!(await env.INGEST_LIMIT.limit({ key: "ingestion" })).success) return reply(429, "rate_limited");
      if (!(await env.SOURCE_LIMIT.limit({ key: await sourceKey(request, env.RATE_LIMIT_SECRET, day) })).success) return reply(429, "rate_limited");
      if (!/^application\/json(?:\s*;\s*charset=utf-8)?$/i.test(request.headers.get("content-type") ?? "") ||
          (request.headers.has("content-encoding") && request.headers.get("content-encoding") !== "identity")) return reply(415, "unsupported_media_type");
      const report = validateReport(await readBody(request));
      const payload = sanitizedJson(report), bytes = encoder.encode(payload);
      if (bytes.length > MAX_BYTES) return reply(413, "body_too_large");
      const hash = hex(await crypto.subtle.digest("SHA-256", bytes));
      // One atomic statement + insert trigger: parallel requests cannot race the daily cap.
      // Deduplication never overwrites a report or extends its retention deadline.
      const inserted = await env.DB.prepare(`
        INSERT INTO crash_reports(report_id, received_at, expires_at, payload_hash, payload,
          payload_bytes, schema_version, version_code, exception_type)
        SELECT ?, ?, ?, ?, ?, ?, 1, ?, ?
        WHERE COALESCE((SELECT reports FROM daily_usage WHERE day = ?), 0) < ?
          AND COALESCE((SELECT bytes FROM daily_usage WHERE day = ?), 0) + ? <= ?
        ON CONFLICT(report_id) DO NOTHING RETURNING report_id
      `).bind(report.reportId, now, now + RETENTION, hash, payload, bytes.length,
        report.build.versionCode, JSON.parse(payload).crash.type, day, DAILY_REPORTS, day, bytes.length, DAILY_BYTES).first();
      if (inserted) {
        ctx.waitUntil(notifyDiscord(env, report));
        return reply(201, "accepted");
      }
      const existing = await env.DB.prepare("SELECT payload_hash FROM crash_reports WHERE report_id = ?").bind(report.reportId).first<{ payload_hash: string }>();
      if (existing) return existing.payload_hash === hash ? reply(200, "already_received") : reply(409, "report_id_conflict");
      return reply(503, "capacity_reached");
    } catch (error) {
      if (error instanceof HttpError) return reply(error.status, error.code);
      if (error instanceof InvalidReport) return reply(400, "invalid_report");
      // Never echo or log exceptions: SQL/runtime errors may contain report content.
      return reply(503, "temporarily_unavailable");
    }
  },
  async scheduled(_controller: ScheduledController, env: Env): Promise<void> {
    try { await expireReports(env.DB, Math.floor(Date.now() / 1000)); }
    catch { throw new Error("Crash report retention failed"); }
  },
} satisfies ExportedHandler<Env>;
