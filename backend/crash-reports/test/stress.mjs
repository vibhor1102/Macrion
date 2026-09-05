// Copyright (C) 2026 Vibhor Goel; SPDX-License-Identifier: GPL-3.0-or-later
import assert from 'node:assert/strict';
import { performance } from 'node:perf_hooks';
import { fixture, harness } from './harness.mjs';

// Intentionally no target-URL option: load tests can only hit an ephemeral loopback server.
async function run(name, count, concurrency, makeBody, expected, limits = { sourceLimit: 100000, ingestLimit: 100000 }, closeConnection = false) {
  const h = await harness(limits);
  try {
    const base = await h.mf.ready;
    assert.equal(base.hostname, '127.0.0.1');
    const target = new URL('/v1/reports', base);
    let next = 0;
    const latencies = [], statuses = {};
    const start = performance.now();
    await Promise.all(Array.from({ length: concurrency }, async () => {
      while (next < count) {
        const index = next++;
        const body = makeBody(index), began = performance.now();
        const response = await fetch(target, {
          method: 'POST', headers: { 'content-type': 'application/json', ...(closeConnection ? { connection: 'close' } : {}) }, body,
          signal: AbortSignal.timeout(30000),
        });
        await response.arrayBuffer();
        latencies.push(performance.now() - began);
        statuses[response.status] = (statuses[response.status] ?? 0) + 1;
      }
    }));
    const elapsed = performance.now() - start;
    latencies.sort((a, b) => a - b);
    const stored = await h.db.prepare('SELECT count(*) AS reports, COALESCE(sum(payload_bytes), 0) AS bytes FROM crash_reports').first();
    const accounting = await h.db.prepare('SELECT COALESCE(sum(reports), 0) AS reports, COALESCE(sum(bytes), 0) AS bytes FROM daily_usage').first();
    assert.deepEqual(accounting, stored, 'Storage and accounting must agree after concurrent load');
    assert.deepEqual(statuses, expected);
    console.log(JSON.stringify({ profile: name, requests: count, concurrency, statuses, stored,
      requestsPerSecond: Math.round(count / (elapsed / 1000)),
      p50Ms: Math.round(latencies[Math.floor(count * 0.5)]),
      p95Ms: Math.round(latencies[Math.floor(count * 0.95)]),
      p99Ms: Math.round(latencies[Math.floor(count * 0.99)]),
    }));
  } finally { await h.close(); }
}

await run('valid unique reports', 600, 32, () => JSON.stringify(fixture()), { 201: 600 });
const duplicate = JSON.stringify(fixture());
await run('retry storm', 600, 64, () => duplicate, { 200: 599, 201: 1 });
await run('daily quota race', 1200, 64, () => JSON.stringify(fixture()), { 201: 1000, 503: 200 });
await run('malformed flood', 1000, 32, i => i % 2 ? '{' : JSON.stringify({ ...fixture(), userEmail: 'private@example.org' }), { 400: 1000 });
await run('near-limit valid reports', 100, 16, () => {
  const r = fixture();
  const frame = { class: 'C'.repeat(256), method: 'm'.repeat(256), file: 'F'.repeat(128), line: 42, native: false };
  r.crash.frames = Array(256).fill(frame);
  const compact = JSON.stringify(r);
  assert(compact.length < 262144);
  return compact.padEnd(262144, ' ');
}, { 201: 100 });
// Early rejection can leave an unfinished HTTP/1 upload: do not pool that socket.
await run('oversized flood', 200, 16, () => 'x'.repeat(262145), { 413: 200 }, undefined, true);
await run('configured rate limits', 600, 32, () => JSON.stringify(fixture()), { 201: 30, 429: 570 }, {});
