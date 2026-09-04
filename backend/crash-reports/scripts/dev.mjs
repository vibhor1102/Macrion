// Copyright (C) 2026 Vibhor Goel; SPDX-License-Identifier: GPL-3.0-or-later
import { spawn } from 'node:child_process';
import { randomBytes } from 'node:crypto';
// Ephemeral local-only limiter key; never a production secret or client credential.
const child = spawn(process.execPath, [
  new URL('../node_modules/wrangler/bin/wrangler.js', import.meta.url).pathname,
  'dev', '--local', '--ip', '127.0.0.1',
  '--var', `RATE_LIMIT_SECRET:${randomBytes(32).toString('hex')}`,
], { stdio: 'inherit', env: { ...process.env, WRANGLER_SEND_METRICS: 'false' } });
for (const signal of ['SIGINT', 'SIGTERM']) process.on(signal, () => child.kill(signal));
child.on('exit', code => { process.exitCode = code ?? 1; });
child.on('error', () => { console.error('Could not start local Wrangler'); process.exitCode = 1; });
