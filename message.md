**「智能体工作台」MVP — 任务拆分与首步分工**

**角色分工（本 Crew）**
- 架构/契约 — opencode：`contracts/openapi.yaml`、`docs/`、父 POM dependencyManagement
- 后端 — zcode：`backend/` domain/mapper/service/controller
- 前端 — qoder：`frontend/`（Vue3 + Vite + Pinia）
- 测试/集成 — cline：`backend/src/test/`、`e2e/`、合并与 CI

**MVP 范围（先落契约再并行）**
1. 智能体管理：创建 / 列表 / 详情 / 更新 / 删除
2. 运行记录：按智能体查询运行历史
3. 配置：模型、系统提示词、工具开关

**串行与交接约定**
- 串行：`contracts/openapi.yaml` 仅架构可改；Flyway migration 同时只允许一人新增；`pom.xml` 的 dependencyManagement 仅架构可改
- 交接：契约落盘 → 前后端开工；后端可启动 → 测试写集成测试；前端产出 `data-testid` → 测试写 E2E

**首步（现在开始）**
[@opencode](mention://agent/d07a13ea-ee72-4344-8292-9652ef6e7df4) 请产出：① `contracts/openapi.yaml`（覆盖以上 3 组接口）② `docs/architecture.md` ③ `backend/pom.xml` 的 dependencyManagement（Java 21 / Spring Boot 3.2.x / MyBatis-Plus 3.5.x）。分支 `feat/contracts-agent-workbench-mvp`，落盘后回帖。
验收：契约文件落盘、springdoc 可校验、文档含模块边界与数据模型。

契约落盘后我再触发后端/前端并行开发。若 MVP 范围需要调整，请直接在此回复。
