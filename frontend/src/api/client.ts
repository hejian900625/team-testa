import type { components, operations } from '../types/api'

/**
 * 基于契约 contracts/openapi.yaml 生成的类型（`pnpm run gen:api`）。
 * 本文件为手写的轻量 fetch 封装，禁止手改 src/types/api.ts。
 */

export type Agent = components['schemas']['Agent']
export type AgentCreate = components['schemas']['AgentCreate']
export type AgentUpdate = components['schemas']['AgentUpdate']
export type AgentPage = components['schemas']['AgentPage']
export type AgentRun = components['schemas']['AgentRun']
export type RunPage = components['schemas']['RunPage']
export type AgentStatus = components['schemas']['AgentStatus']
export type RunStatus = components['schemas']['RunStatus']
export type Problem = components['schemas']['Problem']

export type ListAgentsParams = NonNullable<operations['listAgents']['parameters']['query']>
export type ListAgentRunsParams = NonNullable<operations['listAgentRuns']['parameters']['query']>

/** 契约规定基路径为 /api/v1，可通过 VITE_API_BASE_URL 覆盖（含 /api/v1）。 */
const BASE_URL: string = (import.meta.env.VITE_API_BASE_URL as string | undefined) ?? '/api/v1'

/** 契约统一的错误结构 Problem { code, message, details? }。 */
export class ApiError extends Error {
  readonly status: number
  readonly problem?: Problem

  constructor(status: number, problem?: Problem) {
    super(problem?.message ?? `请求失败（HTTP ${status}）`)
    this.name = 'ApiError'
    this.status = status
    this.problem = problem
  }
}

function buildQuery(params?: Record<string, unknown>): string {
  if (!params) return ''
  const search = new URLSearchParams()
  for (const [key, value] of Object.entries(params)) {
    if (value === undefined || value === null || value === '') continue
    search.append(key, String(value))
  }
  const qs = search.toString()
  return qs ? `?${qs}` : ''
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  let response: Response
  try {
    response = await fetch(`${BASE_URL}${path}`, {
      headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
      ...init
    })
  } catch {
    throw new ApiError(0, {
      code: 'NETWORK_ERROR',
      message: '无法连接到服务，请检查网络或稍后重试'
    })
  }

  if (response.status === 204) {
    return undefined as T
  }

  const text = await response.text()
  const data: unknown = text ? safeJsonParse(text) : undefined

  if (!response.ok) {
    const problem = isProblem(data) ? data : undefined
    throw new ApiError(response.status, problem)
  }

  return data as T
}

function safeJsonParse(text: string): unknown {
  try {
    return JSON.parse(text)
  } catch {
    return text
  }
}

function isProblem(data: unknown): data is Problem {
  return (
    typeof data === 'object' &&
    data !== null &&
    'message' in data &&
    typeof (data as { message: unknown }).message === 'string'
  )
}

export interface AgentApi {
  listAgents(params?: ListAgentsParams): Promise<AgentPage>
  getAgent(agentId: string): Promise<Agent>
  createAgent(body: AgentCreate): Promise<Agent>
  updateAgent(agentId: string, body: AgentUpdate): Promise<Agent>
  deleteAgent(agentId: string): Promise<void>
  listAgentRuns(agentId: string, params?: ListAgentRunsParams): Promise<RunPage>
  getRun(runId: string): Promise<AgentRun>
}

export const agentApi: AgentApi = {
  listAgents(params) {
    return request<AgentPage>(`/agents${buildQuery(params as Record<string, unknown>)}`)
  },
  getAgent(agentId) {
    return request<Agent>(`/agents/${agentId}`)
  },
  createAgent(body) {
    return request<Agent>('/agents', { method: 'POST', body: JSON.stringify(body) })
  },
  updateAgent(agentId, body) {
    return request<Agent>(`/agents/${agentId}`, { method: 'PUT', body: JSON.stringify(body) })
  },
  deleteAgent(agentId) {
    return request<void>(`/agents/${agentId}`, { method: 'DELETE' })
  },
  listAgentRuns(agentId, params) {
    return request<RunPage>(
      `/agents/${agentId}/runs${buildQuery(params as Record<string, unknown>)}`
    )
  },
  getRun(runId) {
    return request<AgentRun>(`/runs/${runId}`)
  }
}
