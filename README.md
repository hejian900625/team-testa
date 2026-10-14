# 智能体工作台

MVP：智能体管理（CRUD）+ 运行记录 + 配置。接口契约见 [`contracts/openapi.yaml`](contracts/openapi.yaml)，
架构与模块边界见 [`docs/architecture.md`](docs/architecture.md)。

## 目录结构

```
contracts/            # OpenAPI 契约（接口唯一事实来源）
docs/                 # 架构说明
backend/              # Maven 多模块：domain / mapper / service / controller
frontend/             # Vue 3 + Vite + TS + Pinia
```

## 环境要求

| 依赖 | 版本 | 本机实测 |
|------|------|----------|
| JDK | 21 (LTS) | 21.0.12 |
| Maven | 3.9+ | 3.9.16 |
| Node | ≥ 20.19 | v24.20.0 |
| pnpm | 8+（`packageManager` 固定 `pnpm@12.6.0`） | 12.6.0 |
| MySQL | 8.x | 后端启动需要 |

> 仓库没有 Maven Wrapper，请用本机 `mvn`。
> pnpm 12 需要放行依赖构建脚本：`frontend/pnpm-workspace.yaml` 已含 `allowBuilds: { esbuild: true }`，
> 直接 `pnpm install` 即可；不要改用 `dangerouslyAllowAllBuilds`。

---

## 快速开始

### 方式 A：只跑前端（用契约 mock，不需要后端）

适合看 UI、跑联调前的自测。

```bash
cd frontend
pnpm install

# 终端 1：起契约同源的 mock（默认 :8080）
pnpm mock

# 终端 2：起开发服务器（默认 http://localhost:5176）
pnpm dev
```

打开 <http://localhost:5176> 即可。mock 预置数据：`Alpha`（ACTIVE，含 2 条运行记录）、`Beta`（DRAFT）。

### 方式 B：全栈（前端 + 真实后端 + MySQL）

**1) 起后端**（在 `backend/` 目录下）：

```bash
cd backend
mvn -pl controller -am spring-boot:run
```

- 默认端口 **8082**（`SERVER_PORT` 可覆盖）。
- 数据库默认 `jdbc:mysql://127.0.0.1:3306/hifox_workbench`，账号 `root/root`；
  可用 `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` 覆盖。
- 连接串带 `createDatabaseIfNotExist=true`，**首次启动会自动建库**；Flyway 自动建表。
- 启动后接口文档：<http://localhost:8082/swagger-ui.html>，OpenAPI JSON：`/v3/api-docs`。

**2) 起前端并指向后端**（在 `frontend/` 目录下）：

```bash
cd frontend
pnpm install

# 后端在 8082，但 vite 的代理默认指向 8080，所以这里必须显式指定
VITE_PROXY_TARGET=http://localhost:8082 pnpm dev
```

Windows PowerShell 写法：

```powershell
$env:VITE_PROXY_TARGET="http://localhost:8082"; pnpm dev
```

然后打开 <http://localhost:5176>。

> ⚠️ **端口不一致提醒**：前端代理默认目标是 `http://localhost:8080`，而后端默认监听 `8082`。
> 跑全栈时记得设 `VITE_PROXY_TARGET=http://localhost:8082`（或反过来用 `SERVER_PORT=8080 mvn -pl controller -am spring-boot:run`
> 把后端起在 8080 —— 但 8080 在本机常被其它服务占用，推荐前者）。

---

## 常用命令

### 后端（在 `backend/` 目录执行）

| 命令 | 说明 |
|------|------|
| `mvn -pl controller -am spring-boot:run` | 启动服务（`-am` 一并构建依赖模块） |
| `mvn -pl controller -am test` | 跑测试 |
| `mvn -pl controller -am verify` | 测试 + 校验（提交前门禁） |

### 前端（在 `frontend/` 目录执行）

| 命令 | 说明 |
|------|------|
| `pnpm dev` | 开发服务器（默认 :5176） |
| `pnpm mock` | 契约同源的内存 mock（默认 :8080） |
| `pnpm build` | `vue-tsc --noEmit` + `vite build` |
| `pnpm typecheck` | 类型检查 |
| `pnpm lint` | ESLint |
| `pnpm test` | 单元测试（Vitest） |
| `pnpm gen:api` | 由 `../contracts/openapi.yaml` 生成 `src/types/api.ts` |
| `pnpm e2e` | Playwright 端到端测试 |

### 端到端测试

```bash
cd frontend
pnpm e2e            # 自动拉起 mock(:8765) + vite(:5176)，然后跑 Chromium
```

> e2e 目前跑在 **mock** 上（`tests/e2e/fixtures.ts` 里硬编码启动 mock），不依赖真实后端。
> 首次运行若缺浏览器：`pnpm exec playwright install chromium`。

---

## 端口与代理一览

| 服务 | 默认端口 | 覆盖方式 |
|------|----------|----------|
| 后端 Spring Boot | 8082 | `SERVER_PORT` |
| 前端 dev server | 5176 | `VITE_PORT` |
| 前端 `/api` 代理目标 | `http://localhost:8080` | `VITE_PROXY_TARGET` |
| 契约 mock | 8080 | `MOCK_PORT` |
| e2e 用 mock / vite | 8765 / 5176 | `MOCK_PORT` / `VITE_PORT` |

---

## 数据库变更

- 变更**只能通过 Flyway 脚本**，放在 `backend/controller/src/main/resources/db/migration/`，
  命名 `V{版本}__{描述}.sql`（双下划线）。
- 已执行过的 `V` 脚本**禁止修改**（checksum 校验会失败）。
- 新增脚本需**串行**进行，避免版本号冲突。

## 接口契约

- [`contracts/openapi.yaml`](contracts/openapi.yaml) 是接口的**唯一事实来源**；`docs`/代码与它冲突时以它为准。
- 前端类型 `frontend/src/types/api.ts` 由 `pnpm -C frontend gen:api` 生成，**禁止手改**。
