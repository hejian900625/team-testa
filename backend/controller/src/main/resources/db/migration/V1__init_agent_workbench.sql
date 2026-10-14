-- 智能体工作台 MVP 初始表结构（表定义见 docs/architecture.md 第 4 节）。
-- 严格串行：同一时间仅一个 Agent 可新增 V 脚本；已执行的脚本禁止修改。

CREATE TABLE IF NOT EXISTS agent (
    id            CHAR(36)     NOT NULL COMMENT '主键，UUID',
    name          VARCHAR(64)  NOT NULL COMMENT '智能体名称，唯一',
    description   VARCHAR(512) NULL     COMMENT '描述',
    model         VARCHAR(128) NOT NULL COMMENT '模型标识',
    system_prompt TEXT         NULL     COMMENT '系统提示词',
    tools         VARCHAR(1024) NULL    COMMENT '工具标识数组（JSON）',
    status        VARCHAR(16)  NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT/ACTIVE/DISABLED',
    created_at    DATETIME     NOT NULL COMMENT '创建时间（UTC）',
    updated_at    DATETIME     NOT NULL COMMENT '更新时间（UTC）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_agent_name (name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '智能体';

CREATE TABLE IF NOT EXISTS agent_run (
    id            CHAR(36)    NOT NULL COMMENT '主键，UUID',
    agent_id      CHAR(36)    NOT NULL COMMENT '所属智能体',
    status        VARCHAR(16) NOT NULL COMMENT 'PENDING/RUNNING/SUCCEEDED/FAILED',
    input         TEXT        NULL     COMMENT '输入',
    output        TEXT        NULL     COMMENT '输出',
    error_message TEXT        NULL     COMMENT '错误信息',
    started_at    DATETIME    NULL     COMMENT '开始时间（UTC）',
    finished_at   DATETIME    NULL     COMMENT '结束时间（UTC）',
    created_at    DATETIME    NOT NULL COMMENT '创建时间（UTC，倒序分页用）',
    PRIMARY KEY (id),
    KEY idx_agent_run_agent_id (agent_id),
    KEY idx_agent_run_created_at (created_at),
    CONSTRAINT fk_agent_run_agent FOREIGN KEY (agent_id) REFERENCES agent (id) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '智能体运行记录';
