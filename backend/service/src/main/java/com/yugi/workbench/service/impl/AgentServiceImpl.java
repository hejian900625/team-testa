package com.yugi.workbench.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yugi.workbench.domain.dto.AgentCreate;
import com.yugi.workbench.domain.dto.AgentQuery;
import com.yugi.workbench.domain.dto.AgentUpdate;
import com.yugi.workbench.domain.dto.AgentView;
import com.yugi.workbench.domain.dto.PageResult;
import com.yugi.workbench.domain.entity.Agent;
import com.yugi.workbench.domain.enums.AgentStatus;
import com.yugi.workbench.domain.exception.DuplicateResourceException;
import com.yugi.workbench.domain.exception.ResourceNotFoundException;
import com.yugi.workbench.domain.exception.ValidationException;
import com.yugi.workbench.mapper.AgentMapper;
import com.yugi.workbench.service.AgentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class AgentServiceImpl implements AgentService {

    /** agent.tools 列上限（见 docs/architecture.md 第 4 节）。 */
    private static final int TOOLS_MAX_LENGTH = 1024;

    private final AgentMapper agentMapper;
    private final ObjectMapper objectMapper;

    public AgentServiceImpl(AgentMapper agentMapper, ObjectMapper objectMapper) {
        this.agentMapper = agentMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public PageResult<AgentView> list(AgentQuery query) {
        LambdaQueryWrapper<Agent> wrapper = Wrappers.lambdaQuery(Agent.class);
        if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
            wrapper.like(Agent::getName, query.getKeyword().trim());
        }
        if (query.getStatus() != null) {
            wrapper.eq(Agent::getStatus, query.getStatus().name());
        }
        wrapper.orderByDesc(Agent::getCreatedAt);

        Page<Agent> page = new Page<>(query.getPage(), query.getSize());
        Page<Agent> result = agentMapper.selectPage(page, wrapper);
        List<AgentView> items = result.getRecords().stream().map(this::toView).toList();
        return new PageResult<>(query.getPage(), query.getSize(), result.getTotal(), items);
    }

    @Override
    public AgentView get(String id) {
        return toView(require(id));
    }

    @Override
    @Transactional
    public AgentView create(AgentCreate request) {
        String name = request.getName().trim();
        if (existsByName(name, null)) {
            throw new DuplicateResourceException("名称已存在: " + name);
        }
        String tools = writeTools(request.getTools());
        Instant now = Instant.now();

        Agent agent = new Agent();
        agent.setId(UUID.randomUUID().toString());
        agent.setName(name);
        agent.setDescription(request.getDescription());
        agent.setModel(request.getModel().trim());
        agent.setSystemPrompt(request.getSystemPrompt());
        agent.setTools(tools);
        agent.setStatus((request.getStatus() == null ? AgentStatus.DRAFT : request.getStatus()).name());
        agent.setCreatedAt(now);
        agent.setUpdatedAt(now);
        agentMapper.insert(agent);
        return toView(agent);
    }

    @Override
    @Transactional
    public AgentView update(String id, AgentUpdate request) {
        Agent agent = require(id);

        if (request.getName() != null) {
            String name = request.getName().trim();
            if (name.isEmpty()) {
                throw new ValidationException("名称不能为空");
            }
            if (!name.equals(agent.getName())) {
                if (existsByName(name, id)) {
                    throw new ValidationException("名称已存在: " + name);
                }
                agent.setName(name);
            }
        }
        if (request.getModel() != null) {
            String model = request.getModel().trim();
            if (model.isEmpty()) {
                throw new ValidationException("模型不能为空");
            }
            agent.setModel(model);
        }
        if (request.getDescription() != null) {
            agent.setDescription(request.getDescription());
        }
        if (request.getSystemPrompt() != null) {
            agent.setSystemPrompt(request.getSystemPrompt());
        }
        if (request.getTools() != null) {
            agent.setTools(writeTools(request.getTools()));
        }
        if (request.getStatus() != null) {
            agent.setStatus(request.getStatus().name());
        }
        agent.setUpdatedAt(Instant.now());
        agentMapper.updateById(agent);
        return toView(agent);
    }

    @Override
    @Transactional
    public void delete(String id) {
        require(id);
        agentMapper.deleteById(id);
    }

    @Override
    public boolean exists(String id) {
        return id != null && agentMapper.selectById(id) != null;
    }

    private Agent require(String id) {
        Agent agent = agentMapper.selectById(id);
        if (agent == null) {
            throw new ResourceNotFoundException("智能体不存在: " + id);
        }
        return agent;
    }

    private boolean existsByName(String name, String excludeId) {
        LambdaQueryWrapper<Agent> wrapper = Wrappers.lambdaQuery(Agent.class).eq(Agent::getName, name);
        if (excludeId != null) {
            wrapper.ne(Agent::getId, excludeId);
        }
        return agentMapper.selectCount(wrapper) > 0;
    }

    private AgentView toView(Agent agent) {
        AgentView view = new AgentView();
        view.setId(agent.getId());
        view.setName(agent.getName());
        view.setDescription(agent.getDescription());
        view.setModel(agent.getModel());
        view.setSystemPrompt(agent.getSystemPrompt());
        view.setTools(readTools(agent.getTools()));
        view.setStatus(agent.getStatus() == null ? null : AgentStatus.valueOf(agent.getStatus()));
        view.setCreatedAt(agent.getCreatedAt());
        view.setUpdatedAt(agent.getUpdatedAt());
        return view;
    }

    private String writeTools(List<String> tools) {
        if (tools == null) {
            return null;
        }
        String json;
        try {
            json = objectMapper.writeValueAsString(tools);
        } catch (JsonProcessingException e) {
            throw new ValidationException("tools 无法序列化");
        }
        if (json.length() > TOOLS_MAX_LENGTH) {
            throw new ValidationException("tools 内容过长");
        }
        return json;
    }

    private List<String> readTools(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(raw, new TypeReference<List<String>>() {
            });
        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }
    }
}
