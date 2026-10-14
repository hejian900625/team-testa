import { mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import AgentDetailView from './AgentDetailView.vue'
import { agentApi } from '../api/client'
import type { Agent } from '../api/client'
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

const agent: Agent = {
  id: 'a1',
  name: 'Alpha',
  model: 'gpt-4o-mini',
  status: 'ACTIVE',
  createdAt: '2026-10-14T00:00:00Z',
  updatedAt: '2026-10-14T00:00:00Z'
}

describe('AgentDetailView', () => {
  useTestPinia()

  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(agentApi.getAgent).mockResolvedValue(agent)
    vi.mocked(agentApi.listAgentRuns).mockResolvedValue({ page: 1, size: 20, total: 0, items: [] })
  })

  async function mountView() {
    const router = makeRouter()
    await router.push('/agents/a1')
    await router.isReady()
    const wrapper = mount(AgentDetailView, {
      props: { id: 'a1' },
      global: { plugins: [router] }
    })
    await vi.waitFor(() => expect(wrapper.find('[data-testid="agent-detail-page"]').exists()).toBe(true))
    return { wrapper, router }
  }

  it('删除确认后调用接口并回到列表', async () => {
    const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(true)
    vi.mocked(agentApi.deleteAgent).mockResolvedValue(undefined)
    const { wrapper, router } = await mountView()

    await wrapper.find('[data-testid="delete-agent"]').trigger('click')

    await vi.waitFor(() => expect(agentApi.deleteAgent).toHaveBeenCalledWith('a1'))
    expect(confirmSpy).toHaveBeenCalled()
    await vi.waitFor(() => expect(router.currentRoute.value.name).toBe('agents'))
    confirmSpy.mockRestore()
  })

  it('取消确认则不删除', async () => {
    const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(false)
    const { wrapper } = await mountView()

    await wrapper.find('[data-testid="delete-agent"]').trigger('click')
    await new Promise((r) => setTimeout(r, 20))

    expect(agentApi.deleteAgent).not.toHaveBeenCalled()
    expect(wrapper.find('[data-testid="agent-detail-page"]').exists()).toBe(true)
    confirmSpy.mockRestore()
  })

  it('删除失败时展示错误提示', async () => {
    const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(true)
    vi.mocked(agentApi.deleteAgent).mockRejectedValue(new Error('boom'))
    const { wrapper, router } = await mountView()

    await wrapper.find('[data-testid="delete-agent"]').trigger('click')

    await vi.waitFor(() => expect(wrapper.find('[data-testid="error-banner"]').exists()).toBe(true))
    expect(router.currentRoute.value.name).not.toBe('agents')
    confirmSpy.mockRestore()
  })
})
