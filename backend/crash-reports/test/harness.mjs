// Copyright (C) 2026 Vibhor Goel; SPDX-License-Identifier: GPL-3.0-or-later
import { build } from 'esbuild';
import { Miniflare, convertV4MiniflareOptions } from 'miniflare';
import { readFile } from 'node:fs/promises';
import { randomUUID, randomBytes } from 'node:crypto';

const config = JSON.parse((await readFile(new URL('../wrangler.jsonc', import.meta.url), 'utf8'))
  .replace(/^\s*\/\/.*$/gm, ''));
const bundle = await build({
  entryPoints: [new URL('../src/index.ts', import.meta.url).pathname],
  bundle: true, format: 'esm', platform: 'browser', write: false,
});
export async function harness({ sourceLimit, ingestLimit, secret = randomBytes(32).toString('hex'), migrate = true, discordWebhookUrl, outboundService } = {}) {
  const ratelimits = Object.fromEntries(config.ratelimits.map(({ name, ...options }) => [name, structuredClone(options)]));
  if (sourceLimit) ratelimits.SOURCE_LIMIT.simple.limit = sourceLimit;
  if (ingestLimit) ratelimits.INGEST_LIMIT.simple.limit = ingestLimit;
  const mf = new Miniflare(convertV4MiniflareOptions({
    telemetry: { enabled: false },
    host: '127.0.0.1', port: 0,
    workers: [{
    name: 'crash-reports',
    modules: true, script: bundle.outputFiles[0].text,
    compatibilityDate: config.compatibility_date,
    d1Databases: { DB: config.d1_databases[0].database_id },
    bindings: { RATE_LIMIT_SECRET: secret, ...(discordWebhookUrl ? { DISCORD_WEBHOOK_URL: discordWebhookUrl } : {}) }, ratelimits,
    // Fail closed if implementation ever tries to make an outbound request.
    outboundService: outboundService ?? (() => { throw new Error('Unexpected outbound network request'); }),
    }],
  }));
  try {
    const db = await mf.getD1Database('DB');
    if (migrate) {
      const sql = await readFile(new URL('../migrations/0001_reports.sql', import.meta.url), 'utf8');
      // D1 exec accepts multiple statements; flatten multiline CREATE TRIGGER intact.
      await db.exec(sql.replace(/--[^\n]*/g, '').replace(/\s+/g, ' '));
    }
    return { mf, db, close: () => mf.dispose() };
  } catch (error) { await mf.dispose(); throw error; }
}
export function fixture() {
  return {
    schemaVersion: 1, redactionVersion: 1, reportId: randomUUID(),
    build: { versionName: '0.2.0', versionCode: 20002, flavor: 'fDroid', buildType: 'debug' },
    device: { androidVersion: '14', api: 34, manufacturer: 'Synthetic', model: 'Test', abi: 'arm64-v8a' },
    mainThread: true,
    crash: { type: 'java.lang.IllegalStateException', message: 'Synthetic crash',
      frames: [{ class: 'io.github.vibhor1102.macrion.Test', method: 'run', file: 'Test.kt', line: 42, native: false }], suppressed: [] },
    recentOperations: [{ event: 'HOME_OPENED', millisecondsBeforeCrash: 10 }], redactionCount: 0, truncated: false,
  };
}
export function send(mf, report = fixture(), overrides = {}) {
  return mf.dispatchFetch('http://localhost/v1/reports', {
    method: 'POST', body: JSON.stringify(report),
    headers: { 'content-type': 'application/json', 'cf-connecting-ip': '192.0.2.1' }, ...overrides,
  });
}
