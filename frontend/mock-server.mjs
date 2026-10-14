import { createServer } from 'node:http'

// 契约（../contracts/openapi.yaml）的内存实现，供前端在后端就绪前自测与 e2e 使用。
const PORT = Number(process.env.MOCK_PORT ?? 8080)
const BASE = '/api/v1'

let seq = 0
const now = () => new Date().toISOString()
const id = () => `00000000-0000-4000-8000-${String(++seq).padStart(12, '0')}`

const agents = [
  {
    id: id(),
    name: 'Alpha',
    description: '客服助手',
    model: 'gpt-4o-mini',
    systemPrompt: '你是客服助手。',
    tools: ['search'],
    status: 'ACTIVE',
    createdAt: now(),
    updatedAt: now()
  },
  {
    id: id(),
    name: 'Beta',
    model: 'claude-3-5-sonnet',
    status: 'DRAFT',
    createdAt: now(),
    updatedAt: now()
  }
]

const runs = [
  {
    id: id(),
    agentId: agents[0].id,
    status: 'SUCCEEDED',
    input: '你好',
    output: '你好，有什么可以帮您？',
    startedAt: now(),
    finishedAt: now(),
    createdAt: now()
  },
  {
    id: id(),
    agentId: agents[0].id,
    status: 'FAILED',
    input: '查订单',
    errorMessage: '工具调用超时',
    startedAt: now(),
    finishedAt: null,
    createdAt: now()
  }
]

function send(res, status, body) {
  const json = body === undefined ? '' : JSON.stringify(body)
  res.writeHead(status, { 'Content-Type': 'application/json; charset=utf-8' })
  res.end(json)
}

const problem = (res, status, code, message) => send(res, status, { code, message })

function paginate(list, query) {
  const page = Number(query.get('page') ?? 1)
  const size = Number(query.get('size') ?? 20)
  const start = (page - 1) * size
  return { page, size, total: list.length, items: list.slice(start, start + size) }
}

function readBody(req) {
  return new Promise((resolve) => {
    let raw = ''
    req.on('data', (chunk) => (raw += chunk))
    req.on('end', () => {
      try {
        resolve(JSON.parse(raw || '{}'))
      } catch {
        resolve(null)
      }
    })
  })
}

const server = createServer(async (req, res) => {
  const url = new URL(req.url, 'http://localhost')
  const path = url.pathname

  if (!path.startsWith(BASE)) return problem(res, 404, 'NOT_FOUND', '接口不存在')
  const rest = path.slice(BASE.length)
  const query = url.searchParams

  res.setHeader('Access-Control-Allow-Origin', '*')
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type')
  res.setHeader('Access-Control-Allow-Methods', 'GET,POST,PUT,DELETE,OPTIONS')
  if (req.method === 'OPTIONS') return res.writeHead(204).end()

  if (rest === '/agents' && req.method === 'GET') {
    const keyword = (query.get('keyword') ?? '').trim().toLowerCase()
    const status = query.get('status')
    const filtered = agents.filter(
      (a) =>
        (!keyword || a.name.toLowerCase().includes(keyword)) &&
        (!status || a.status === status)
    )
    return send(res, 200, paginate(filtered, query))
  }

  if (rest === '/agents' && req.method === 'POST') {
    const body = await readBody(req)
    if (!body) return problem(res, 400, 'BAD_REQUEST', '请求体不是合法 JSON')
    if (!body.name?.trim()) return problem(res, 400, 'VALIDATION_FAILED', '名称不能为空')
    if (!body.model?.trim()) return problem(res, 400, 'VALIDATION_FAILED', '模型不能为空')
    const agent = {
      id: id(),
      name: body.name,
      description: body.description,
      model: body.model,
      systemPrompt: body.systemPrompt,
      tools: body.tools,
      status: body.status ?? 'DRAFT',
      createdAt: now(),
      updatedAt: now()
    }
    agents.unshift(agent)
    return send(res, 201, agent)
  }

  const agentMatch = /^\/agents\/([^/]+)$/.exec(rest)
  if (agentMatch) {
    const index = agents.findIndex((a) => a.id === agentMatch[1])
    if (index === -1) return problem(res, 404, 'NOT_FOUND', '智能体不存在')
    if (req.method === 'GET') return send(res, 200, agents[index])
    if (req.method === 'PUT') {
      const body = await readBody(req)
      if (!body) return problem(res, 400, 'BAD_REQUEST', '请求体不是合法 JSON')
      if (body.name !== undefined && !body.name.trim())
        return problem(res, 400, 'VALIDATION_FAILED', '名称不能为空')
      agents[index] = { ...agents[index], ...body, id: agents[index].id, updatedAt: now() }
      return send(res, 200, agents[index])
    }
    if (req.method === 'DELETE') {
      agents.splice(index, 1)
      return res.writeHead(204).end()
    }
  }

  const runsMatch = /^\/agents\/([^/]+)\/runs$/.exec(rest)
  if (runsMatch && req.method === 'GET') {
    if (!agents.some((a) => a.id === runsMatch[1]))
      return problem(res, 404, 'NOT_FOUND', '智能体不存在')
    const filtered = runs.filter((r) => r.agentId === runsMatch[1])
    return send(res, 200, paginate(filtered, query))
  }

  const runMatch = /^\/runs\/([^/]+)$/.exec(rest)
  if (runMatch && req.method === 'GET') {
    const run = runs.find((r) => r.id === runMatch[1])
    if (!run) return problem(res, 404, 'NOT_FOUND', '运行记录不存在')
    return send(res, 200, run)
  }

  return problem(res, 405, 'METHOD_NOT_ALLOWED', '不支持的方法或路径')
})

// 只绑 127.0.0.1，避免与本机其他占用同名端口的服务在 IPv6 上串味。
server.listen(PORT, '127.0.0.1', () => {
  console.log(`contract mock listening on http://127.0.0.1:${PORT}${BASE}`)
})
