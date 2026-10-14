package com.yugi.workbench.domain.dto;

import com.yugi.workbench.domain.enums.AgentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 创建智能体请求体（契约 AgentCreate）。
 */
public class AgentCreate {

    @NotBlank(message = "名称不能为空")
    @Size(max = 64, message = "名称长度不能超过 64")
    private String name;

    @Size(max = 512, message = "描述长度不能超过 512")
    private String description;

    @NotBlank(message = "模型不能为空")
    @Size(max = 128, message = "模型长度不能超过 128")
    private String model;

    @Size(max = 8000, message = "系统提示词长度不能超过 8000")
    private String systemPrompt;

    private List<String> tools;

    private AgentStatus status;

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

    public List<String> getTools() {
        return tools;
    }

    public void setTools(List<String> tools) {
        this.tools = tools;
    }

    public AgentStatus getStatus() {
        return status;
    }

    public void setStatus(AgentStatus status) {
        this.status = status;
    }
}
