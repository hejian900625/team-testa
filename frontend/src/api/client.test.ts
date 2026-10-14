import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

import { agentApi, ApiError } from './client'
import type { Agent, AgentPage } from './client'

function jsonResponse(body: unknown, status = 200): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { 'Content-Type': 'application/json' }
  })
}

const agent: Agent = {
  id: '11111111-1111-1111-1111-111111111111',
  name: 'Alpha',
  model: 'gpt-4o-mini',
  status: 'ACTIVE',
  createdAt: '2026-10-14T00:00:00Z',
  updatedAt: '2026-10-14T00:00:00Z'
}

describe('agentApi', () => {
  beforeEach(() => {
    vi.stubGlobal(
      'fetch',
      vi.fn(() => Promise.resolve(jsonResponse({ ok: true })))
    )
  })

  afterEach(() => {
    vi.unstubAllGlobals()
    vi.restoreAllMocks()
  })

  it('listAgents builds query string and parses page', async () => {
    const page: AgentPage = { page: 1, size: 20, total: 1, items: [agent] }
    vi.stubGlobal('fetch', vi.fn(() => Promise.resolve(jsonResponse(page))))

    const result = await agentApi.listAgents({ page: 1, size: 20, keyword: 'Al' })

    expect(fetch).toHaveBeenCalledOnce()
    const url = (fetch as unknown as ReturnType<typeof vi.fn>).mock.calls[0][0] as string
    expect(url).toContain('/api/v1/agents')
    expect(url).toContain('page=1')
    expect(url).toContain('size=20')
    expect(url).toContain('keyword=Al')
    expect(result.items[0].name).toBe('Alpha')
  })

  it('createAgent posts JSON body', async () => {
    vi.stubGlobal('fetch', vi.fn(() => Promise.resolve(jsonResponse(agent, 201))))

    const result = await agentApi.createAgent({ name: 'Alpha', model: 'gpt-4o-mini' })

    expect(fetch).toHaveBeenCalledOnce()
    const [, init] = (fetch as unknown as ReturnType<typeof vi.fn>).mock.calls[0]
    expect(init.method).toBe('POST')
    expect(JSON.parse(init.body)).toMatchObject({ name: 'Alpha', model: 'gpt-4o-mini' })
    expect(result.id).toBe(agent.id)
  })

  it('deleteAgent resolves on 204 without body', async () => {
    vi.stubGlobal('fetch', vi.fn(() => Promise.resolve(new Response(null, { status: 204 }))))

    await expect(agentApi.deleteAgent(agent.id)).resolves.toBeUndefined()
    const [, init] = (fetch as unknown as ReturnType<typeof vi.fn>).mock.calls[0]
    expect(init.method).toBe('DELETE')
  })

  it('throws ApiError with Problem message on error response', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn(() =>
        Promise.resolve(
          jsonResponse({ code: 'NOT_FOUND', message: '资源不存在' }, 404)
        )
      )
    )

    await expect(agentApi.getAgent(agent.id)).rejects.toMatchObject({
      name: 'ApiError',
      status: 404,
      message: '资源不存在'
    })
  })

  it('throws network ApiError when fetch rejects', async () => {
    vi.stubGlobal('fetch', vi.fn(() => Promise.reject(new Error('boom'))))

    await expect(agentApi.getAgent(agent.id)).rejects.toBeInstanceOf(ApiError)
  })
})
