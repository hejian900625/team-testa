# 智能体工作台 — 架构说明（MVP）

> 所有者：架构/契约 Agent。前后端只读本目录；变更请提 issue。

## 1. 目标与范围（MVP）

一个用于管理智能体及其运行记录的工作台，最小可用范围：

1. **智能体管理**：创建 / 列表（分页/筛选）/ 详情 / 更新 / 删除
2. **运行记录**：按智能体查询运行历史、查看单次运行详情
3. **配置**：模型、系统提示词、工具开关（tools）

非目标（本期不做）：真实调用大模型执行、鉴权与多租户、可视化编排。

## 2. 技术栈

| 层 | 选型 |
|----|------|
| 后端 | Java 21 (LTS)、Spring Boot 3.2.x、MyBatis-Plus 3.5.x、Flyway、MySQL 8 |
| 前端 | Node 20 LTS、pnpm 8+、Vue 3.4+、TypeScript 5.x、Vite 5+、Pinia、Vue Router 4 |
| 契约 | OpenAPI 3.0.3（`contracts/openapi.yaml`）为唯一事实来源 |
| 测试 | JUnit / Spring Boot Test（后端）、Playwright（e2e） |

## 3. 仓库结构与模块边界

```
contracts/            # 接口契约（唯一所有者：架构 Agent）
docs/                 # 架构/设计文档
backend/              # Maven 多模块
  pom.xml             # 父 POM：dependencyManagement（唯一所有者：架构 Agent）
  domain/             # 实体、枚举、DTO（业务逻辑无）
  mapper/             # MyBatis-Plus Mapper
  service/            # 业务逻辑
  controller/         # REST Controller + 启动类 + application.yml + Flyway 脚本
    src/main/resources/db/migration/
frontend/             # Vue 3 应用
  src/types/api.ts    # 由 OpenAPI 生成，禁止手改
e2e/                  # 端到端测试
```

模块依赖方向：`controller → service → mapper → domain`（单向，禁止反向依赖）。

## 4. 数据模型

### agent（智能体）
| 字段 | 类型 | 约束 |
|------|------|------|
| id | char(36) | PK，UUID |
| name | varchar(64) | NOT NULL，唯一 |
| description | varchar(512) | |
| model | varchar(128) | NOT NULL |
| system_prompt | text | |
| tools | json / varchar(1024) | 工具标识数组 |
| status | varchar(16) | DRAFT/ACTIVE/DISABLED，默认 DRAFT |
| created_at | datetime | NOT NULL |
| updated_at | datetime | NOT NULL |

### agent_run（运行记录）
| 字段 | 类型 | 约束 |
|------|------|------|
| id | char(36) | PK，UUID |
| agent_id | char(36) | NOT NULL，FK → agent.id，索引 |
| status | varchar(16) | PENDING/RUNNING/SUCCEEDED/FAILED |
| input | text | |
| output | text | |
| error_message | text | |
| started_at | datetime | |
| finished_at | datetime | |
| created_at | datetime | NOT NULL，索引（用于倒序分页） |

**数据库变更必须通过 Flyway**，脚本落在 `controller/src/main/resources/db/migration/`，
命名 `V{版本}__{描述}.sql`（双下划线），且**严格串行**——同一时间仅一个 Agent 可新增脚本；
已执行的 V 脚本禁止修改。初始脚本 `V1__init_agent_workbench.sql` 由后端 Agent 编写。

## 5. 接口约定

- 基路径 `/api/v1`，资源：`agents`、`runs`
- 分页返回 `{ page, size, total, items }`
- 错误统一 `Problem { code, message, details? }`
- 时间统一 ISO-8601 UTC
- 完整定义见 `contracts/openapi.yaml`

## 6. 交接与并行约定

- **契约优先**：`contracts/openapi.yaml` 落盘后，前后端方可开工
- 前端类型由 `pnpm -C frontend run gen:api` 从契约生成，写入 `frontend/src/types/api.ts`
- 后端可启动（`mvn -pl controller spring-boot:run`）后，测试 Agent 才能写集成测试
- 前端产出 `data-testid` 后，测试 Agent 才能写 e2e
- 串行资源：`contracts/`、`backend/pom.xml` 的 dependencyManagement、Flyway migration、`frontend/pnpm-lock.yaml`
- 分支：`feat/<模块>-<子任务>`，经 PR 合并，禁止直接 push main

## 7. 待决项（TODO）

- // TODO(agent): 运行记录写入来源（谁触发 run）未定义，MVP 先只读 @2026-10-14
- // TODO(agent): tools 的取值枚举待定，先自由字符串 @2026-10-14
