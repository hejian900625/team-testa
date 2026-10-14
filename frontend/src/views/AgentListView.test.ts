import { mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import AgentListView from './AgentListView.vue'
import { agentApi } from '../api/client'
import type { AgentPage } from '../api/client'
import { makeRouter, useTestPinia } from '../test/utils'

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

const page: AgentPage = {
  page: 1,
  size: 20,
  total: 2,
  items: [
    {
      id: 'a1',
      name: 'Alpha',
      model: 'gpt-4o-mini',
      status: 'ACTIVE',
      createdAt: '2026-10-14T00:00:00Z',
      updatedAt: '2026-10-14T00:00:00Z'
    },
    {
      id: 'a2',
      name: 'Beta',
      model: 'gpt-4o',
      status: 'DRAFT',
      createdAt: '2026-10-14T00:00:00Z',
      updatedAt: '2026-10-14T00:00:00Z'
    }
  ]
}

describe('AgentListView', () => {
  useTestPinia()

  beforeEach(() => {
    vi.clearAllMocks()
  })

  function mountView() {
    const router = makeRouter()
    return mount(AgentListView, { global: { plugins: [router] } })
  }

  it('renders agents from the store after fetch', async () => {
    vi.mocked(agentApi.listAgents).mockResolvedValue(page)

    const wrapper = mountView()
    await vi.waitFor(() => expect(agentApi.listAgents).toHaveBeenCalled())

    await wrapper.vm.$nextTick()
    expect(wrapper.find('[data-testid="agent-table"]').exists()).toBe(true)
    expect(wrapper.text()).toContain('Alpha')
    expect(wrapper.text()).toContain('Beta')
    expect(wrapper.find('[data-testid="page-info"]').text()).toContain('共 2 条')
  })

  it('shows empty state when no agents', async () => {
    vi.mocked(agentApi.listAgents).mockResolvedValue({ page: 1, size: 20, total: 0, items: [] })

    const wrapper = mountView()
    await vi.waitFor(() => expect(agentApi.listAgents).toHaveBeenCalled())
    await wrapper.vm.$nextTick()

    expect(wrapper.find('[data-testid="empty-state"]').exists()).toBe(true)
  })
})
