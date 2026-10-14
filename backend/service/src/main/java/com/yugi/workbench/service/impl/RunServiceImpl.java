package com.yugi.workbench.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yugi.workbench.domain.dto.AgentRunView;
import com.yugi.workbench.domain.dto.PageResult;
import com.yugi.workbench.domain.entity.AgentRun;
import com.yugi.workbench.domain.enums.RunStatus;
import com.yugi.workbench.domain.exception.ResourceNotFoundException;
import com.yugi.workbench.mapper.AgentMapper;
import com.yugi.workbench.mapper.AgentRunMapper;
import com.yugi.workbench.service.RunService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RunServiceImpl implements RunService {

    private final AgentMapper agentMapper;
    private final AgentRunMapper agentRunMapper;

    public RunServiceImpl(AgentMapper agentMapper, AgentRunMapper agentRunMapper) {
        this.agentMapper = agentMapper;
        this.agentRunMapper = agentRunMapper;
    }

    @Override
    public PageResult<AgentRunView> listByAgent(String agentId, int page, int size) {
        if (agentMapper.selectById(agentId) == null) {
            throw new ResourceNotFoundException("智能体不存在: " + agentId);
        }
        LambdaQueryWrapper<AgentRun> wrapper = Wrappers.lambdaQuery(AgentRun.class)
                .eq(AgentRun::getAgentId, agentId)
                .orderByDesc(AgentRun::getCreatedAt);

        Page<AgentRun> result = agentRunMapper.selectPage(new Page<>(page, size), wrapper);
        List<AgentRunView> items = result.getRecords().stream().map(this::toView).toList();
        return new PageResult<>(page, size, result.getTotal(), items);
    }

    @Override
    public AgentRunView get(String runId) {
        AgentRun run = agentRunMapper.selectById(runId);
        if (run == null) {
            throw new ResourceNotFoundException("运行记录不存在: " + runId);
        }
        return toView(run);
    }

    private AgentRunView toView(AgentRun run) {
        AgentRunView view = new AgentRunView();
        view.setId(run.getId());
        view.setAgentId(run.getAgentId());
        view.setStatus(run.getStatus() == null ? null : RunStatus.valueOf(run.getStatus()));
        view.setInput(run.getInput());
        view.setOutput(run.getOutput());
        view.setErrorMessage(run.getErrorMessage());
        view.setStartedAt(run.getStartedAt());
        view.setFinishedAt(run.getFinishedAt());
        view.setCreatedAt(run.getCreatedAt());
        return view;
    }
}
