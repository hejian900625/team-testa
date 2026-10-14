# 智能体工作台 · 前端

Vue 3 + Vite + TypeScript + Pinia + Vue Router。接口类型由契约生成，禁止手改 `src/types/api.ts`。

## 命令

| 命令 | 说明 |
|------|------|
| `pnpm install` | 安装依赖（`pnpm-workspace.yaml` 已放行 esbuild 的构建脚本） |
| `pnpm dev` | 开发服务器 :5176，`/api` 代理到后端 |
| `pnpm mock` | 契约的内存 mock（:8080），后端未就绪时自测/e2e 用 |
| `pnpm gen:api` | 由 `../contracts/openapi.yaml` 重新生成 `src/types/api.ts` |
| `pnpm test` / `typecheck` / `lint` / `build` | 质量门禁 |

后端不在 8080 时用 `VITE_PROXY_TARGET=http://127.0.0.1:8082 pnpm dev` 改代理目标；mock 同理用
`MOCK_PORT=8082 pnpm mock`（mock 只绑 127.0.0.1，避免与本机同端口服务在 IPv6 上串味）。
注意后端**默认起在 8082**、而 vite 代理默认指向 8080，全栈联调务必显式设置 `VITE_PROXY_TARGET`。

> 完整本地运行说明（后端启动、数据库、端口/代理一览、e2e）见仓库根 [`README.md`](../README.md)。

## 分层

- `src/api/client.ts` — 契约类型 + fetch 封装，统一错误结构 `Problem`
- `src/stores/agents.ts` — 列表/详情/运行记录的取数与状态，空筛选值在此归一化
- `src/views/*` — 页面（列表、详情、新建/编辑、404）
- `src/components/*` — `StatusBadge` / `ErrorBanner` / `EmptyState`

## 给测试

每个可交互元素都有稳定的 `data-testid`（行：`agent-row-<id>`、`run-row-<id>`；状态徽标：`status-<STATUS>`）。
e2e 建议直接对着 `pnpm mock` 跑：数据固定（Alpha/ACTIVE、Beta/DRAFT，Alpha 有 2 条运行记录），
且 mock 与契约同源，后端上线后同一套断言可复用。
