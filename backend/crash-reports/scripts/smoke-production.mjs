// Copyright (C) 2026 Vibhor Goel; SPDX-License-Identifier: GPL-3.0-or-later
import { spawnSync } from 'node:child_process';
import { fixture } from '../test/harness.mjs';

const endpoint = 'https://macrion-crash-reports.vibhor1102.workers.dev/v1/reports';
const reports = [fixture(), fixture()];
reports[1].crash = {
  type: 'android.native_crash',
  frames: [],
  suppressed: [],
  nativeExit: { occurredAtMs: Date.now(), status: 11, importance: 100, pssKb: 1234, rssKb: 5678 },
};
reports[1].mainThread = false;
reports[1].recentOperations = [];
try {
  for (const report of reports) {
    const send = () => fetch(endpoint, { method: 'POST', headers: { 'content-type': 'application/json' },
      body: JSON.stringify(report), signal: AbortSignal.timeout(15000) });
    const first = await send(), retry = await send();
    if (first.status !== 201 || retry.status !== 200) throw new Error(`Unexpected responses: ${first.status}, ${retry.status}`);
  }
  console.log('Production acceptance and idempotency checks passed.');
} finally {
  const wrangler = new URL('../node_modules/wrangler/bin/wrangler.js', import.meta.url).pathname;
  const ids = reports.map(report => `'${report.reportId}'`).join(', ');
  const cleanupSql = `
    UPDATE daily_usage SET
      reports = reports - (SELECT COUNT(*) FROM crash_reports WHERE report_id IN (${ids})),
      bytes = bytes - COALESCE((SELECT SUM(payload_bytes) FROM crash_reports WHERE report_id IN (${ids})), 0)
    WHERE day IN (SELECT received_at / 86400 FROM crash_reports WHERE report_id IN (${ids}));
    DELETE FROM crash_reports WHERE report_id IN (${ids});
    DELETE FROM daily_usage WHERE reports <= 0;
  `.replace(/\s+/g, ' ');
  const cleanup = spawnSync(process.execPath, [wrangler, 'd1', 'execute', 'DB', '--remote', '--yes', '--command', cleanupSql], {
    stdio: 'ignore', env: { ...process.env, WRANGLER_SEND_METRICS: 'false' },
  });
  if (cleanup.status !== 0) throw new Error(`Test report cleanup failed; report IDs: ${reports.map(r => r.reportId).join(', ')}`);
}
