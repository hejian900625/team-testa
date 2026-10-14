import { test as base } from '@playwright/test'
import { spawn } from 'node:child_process'
import { join, dirname } from 'node:path'
import { fileURLToPath } from 'node:url'
import * as net from 'node:net'

const MOCK_PORT = Number(process.env.MOCK_PORT ?? 8765)
const VITE_PORT = Number(process.env.VITE_PORT) || 5176
const ROOT = join(dirname(fileURLToPath(import.meta.url)), '../..')
const MOCK_SCRIPT = join(ROOT, 'mock-server.mjs')
const VITE_BIN = join(ROOT, 'node_modules', 'vite', 'bin', 'vite.js')

interface Fixtures {}

/**
 * Poll until a TCP port accepts connections.
 * Uses 'localhost' (not '127.0.0.1') so it works whether the server
 * binds to IPv4, IPv6, or both — matches what the browser sees.
 */
async function waitForPort(port: number, label: string, timeoutMs = 15000): Promise<void> {
  const deadline = Date.now() + timeoutMs
  while (Date.now() < deadline) {
    const ok = await new Promise<boolean>((resolve) => {
      const sock = new net.Socket()
      sock.setTimeout(800)
      sock.once('connect', () => { sock.destroy(); resolve(true) })
      sock.once('error', () => { sock.destroy(); resolve(false) })
      sock.connect(port, 'localhost')
    })
    if (ok) return
    await new Promise((r) => setTimeout(r, 400))
  }
  throw new Error(`${label} did not become ready on localhost:${port} within ${timeoutMs}ms`)
}

export const test = base.extend<Fixtures>({
  _servers: [
    async ({}, use) => {
      // 1) start mock
      const mock = spawn(process.execPath, [MOCK_SCRIPT], {
        cwd: ROOT,
        env: { ...process.env, MOCK_PORT: String(MOCK_PORT) },
        stdio: 'ignore',
      })
      mock.on('error', (e) => console.error('[mock] spawn error:', e.message))
      await waitForPort(MOCK_PORT, 'mock-server', 10000)
      console.log(`[e2e] mock ready on localhost:${MOCK_PORT}`)

      // 2) start vite
      const vite = spawn(process.execPath, [VITE_BIN, 'dev'], {
        cwd: ROOT,
        env: {
          ...process.env,
          VITE_PROXY_TARGET: `http://localhost:${MOCK_PORT}`,
          VITE_PORT: String(VITE_PORT),
        },
        stdio: 'ignore',
      })
      vite.on('error', (e) => console.error('[vite] spawn error:', e.message))
      await waitForPort(VITE_PORT, 'vite dev server', 20000)
      console.log(`[e2e] vite ready on localhost:${VITE_PORT}`)

      await use({})
      mock.kill()
      vite.kill()
    },
    { scope: 'worker', auto: true },
  ],
})

export { expect } from '@playwright/test'
