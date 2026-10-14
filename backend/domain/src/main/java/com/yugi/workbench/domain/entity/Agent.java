package com.yugi.workbench.domain.entity;

import java.time.Instant;

/**
 * agent 表实体。纯 POJO，不加持久化框架注解：
 * 表名 agent、字段由 MyBatis-Plus 驼峰/下划线自动映射（见 application.yml）。
 */
public class Agent {

    private String id;
    private String name;
    private String description;
    private String model;
    private String systemPrompt;
    /** 工具标识数组的 JSON 文本，如 ["search"]。 */
    private String tools;
    /** 存储 AgentStatus 的名称。 */
    private String status;
    private Instant createdAt;
    private Instant updatedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public String getTools() {
        return tools;
    }

    public void setTools(String tools) {
        this.tools = tools;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
