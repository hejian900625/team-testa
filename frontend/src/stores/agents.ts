import { defineStore } from 'pinia'

import {
  agentApi,
  ApiError,
  type Agent,
  type AgentCreate,
  type AgentPage,
  type AgentRun,
  type AgentStatus,
  type AgentUpdate,
  type ListAgentRunsParams,
  type ListAgentsParams,
  type RunPage
} from '../api/client'

export const DEFAULT_PAGE_SIZE = 20

/** 视图层的筛选输入允许空串（表示“不过滤”），发请求前归一化掉。 */
export type ListQueryOverrides = Omit<Partial<ListAgentsParams>, 'status'> & {
  status?: AgentStatus | ''
}

export const useAgentsStore = defineStore('agents', {
  state: () => ({
    items: [] as Agent[],
    page: 1,
    size: DEFAULT_PAGE_SIZE,
    total: 0,
    keyword: '',
    status: '' as AgentStatus | '',
    loading: false,
    error: '',
    // 详情
    current: null as Agent | null,
    currentLoading: false,
    // 运行记录
    runs: [] as AgentRun[],
    runsPage: 1,
    runsSize: DEFAULT_PAGE_SIZE,
    runsTotal: 0,
    runsLoading: false
  }),

  actions: {
    async fetchList(overrides?: ListQueryOverrides): Promise<void> {
      const page = overrides?.page ?? this.page
      const size = overrides?.size ?? this.size
      const keyword = overrides && 'keyword' in overrides ? overrides.keyword ?? '' : this.keyword
      const status = overrides && 'status' in overrides ? overrides.status ?? '' : this.status

      this.page = page
      this.size = size
      this.keyword = keyword
      this.status = status
      this.loading = true
      this.error = ''

      try {
        const params: ListAgentsParams = { page, size }
        if (keyword) params.keyword = keyword
        if (status) params.status = status
        const result: AgentPage = await agentApi.listAgents(params)
        this.items = result.items
        this.total = result.total
      } catch (err) {
        this.items = []
        this.total = 0
        this.error = errorMessage(err, '加载智能体列表失败')
      } finally {
        this.loading = false
      }
    },

    async fetchOne(id: string): Promise<Agent | null> {
      this.currentLoading = true
      try {
        this.current = await agentApi.getAgent(id)
        return this.current
      } catch (err) {
        this.error = errorMessage(err, '加载智能体详情失败')
        return null
      } finally {
        this.currentLoading = false
      }
    },

    async create(body: AgentCreate): Promise<Agent> {
      return agentApi.createAgent(body)
    },

    async update(id: string, body: AgentUpdate): Promise<Agent> {
      return agentApi.updateAgent(id, body)
    },

    async remove(id: string): Promise<void> {
      await agentApi.deleteAgent(id)
    },

    async fetchRuns(id: string, page = 1): Promise<void> {
      this.runsPage = page
      this.runsLoading = true
      try {
        const params: ListAgentRunsParams = { page, size: this.runsSize }
        const result: RunPage = await agentApi.listAgentRuns(id, params)
        this.runs = result.items
        this.runsTotal = result.total
      } catch (err) {
        this.runs = []
        this.runsTotal = 0
        this.error = errorMessage(err, '加载运行记录失败')
      } finally {
        this.runsLoading = false
      }
    }
  }
})

export function errorMessage(err: unknown, fallback: string): string {
  if (err instanceof ApiError) return err.message
  if (err instanceof Error && err.message) return err.message
  return fallback
}
