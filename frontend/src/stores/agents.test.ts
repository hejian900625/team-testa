import { beforeEach, describe, expect, it, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'

import { useAgentsStore } from './agents'
import { agentApi } from '../api/client'
import type { Agent, AgentPage, RunPage } from '../api/client'

// 只替换 agentApi，保留真实的 ApiError，否则下面的 instanceof 判断会失效。
vi.mock('../api/client', async (importOriginal) => ({
  ...(await importOriginal<typeof import('../api/client')>()),
  agentApi: {
    listAgents: vi.fn(),
    getAgent: vi.fn(),
    createAgent: vi.fn(),
    updateAgent: vi.fn(),
    deleteAgent: vi.fn(),
    listAgentRuns: vi.fn(),
    getRun: vi.fn()
  }
}))

const agent: Agent = {
  id: '11111111-1111-1111-1111-111111111111',
  name: 'Alpha',
  model: 'gpt-4o-mini',
  status: 'DRAFT',
  createdAt: '2026-10-14T00:00:00Z',
  updatedAt: '2026-10-14T00:00:00Z'
}

const page: AgentPage = { page: 1, size: 20, total: 1, items: [agent] }

describe('useAgentsStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('fetchList stores items and total', async () => {
    vi.mocked(agentApi.listAgents).mockResolvedValue(page)
    const store = useAgentsStore()

    await store.fetchList({ page: 1, size: 20, keyword: 'Al', status: 'DRAFT' })

    expect(store.items).toHaveLength(1)
    expect(store.total).toBe(1)
    expect(store.error).toBe('')
    expect(agentApi.listAgents).toHaveBeenCalledWith({
      page: 1,
      size: 20,
      keyword: 'Al',
      status: 'DRAFT'
    })
  })

  it('fetchList drops empty filters instead of reusing the previous ones', async () => {
    vi.mocked(agentApi.listAgents).mockResolvedValue(page)
    const store = useAgentsStore()

    await store.fetchList({ keyword: 'Al', status: 'DRAFT' })
    await store.fetchList({ keyword: '', status: '' })

    expect(store.keyword).toBe('')
    expect(store.status).toBe('')
    expect(agentApi.listAgents).toHaveBeenLastCalledWith({ page: 1, size: 20 })
  })

  it('fetchList records error message on failure', async () => {
    const { ApiError } = await import('../api/client')
    vi.mocked(agentApi.listAgents).mockRejectedValue(
      new ApiError(500, { code: 'INTERNAL', message: '服务异常' })
    )
    const store = useAgentsStore()

    await store.fetchList()

    expect(store.items).toHaveLength(0)
    expect(store.error).toBe('服务异常')
  })

  it('fetchRuns stores run items', async () => {
    const runPage: RunPage = {
      page: 1,
      size: 20,
      total: 1,
      items: [
        {
          id: 'run-1',
          agentId: agent.id,
          status: 'SUCCEEDED',
          createdAt: '2026-10-14T00:00:00Z'
        }
      ]
    }
    vi.mocked(agentApi.listAgentRuns).mockResolvedValue(runPage)
    const store = useAgentsStore()

    await store.fetchRuns(agent.id, 1)

    expect(store.runs).toHaveLength(1)
    expect(store.runsTotal).toBe(1)
  })
})
