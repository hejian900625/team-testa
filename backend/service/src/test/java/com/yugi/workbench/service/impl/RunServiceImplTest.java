package com.yugi.workbench.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yugi.workbench.domain.dto.AgentRunView;
import com.yugi.workbench.domain.dto.PageResult;
import com.yugi.workbench.domain.entity.Agent;
import com.yugi.workbench.domain.entity.AgentRun;
import com.yugi.workbench.domain.enums.RunStatus;
import com.yugi.workbench.domain.exception.ResourceNotFoundException;
import com.yugi.workbench.mapper.AgentMapper;
import com.yugi.workbench.mapper.AgentRunMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RunServiceImplTest {

    @Mock
    private AgentMapper agentMapper;

    @Mock
    private AgentRunMapper agentRunMapper;

    private RunServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new RunServiceImpl(agentMapper, agentRunMapper);
    }

    @Test
    void listByAgent_throwsWhenAgentMissing() {
        when(agentMapper.selectById("missing")).thenReturn(null);

        assertThatThrownBy(() -> service.listByAgent("missing", 1, 20))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listByAgent_returnsPagedItems() {
        when(agentMapper.selectById("id-1")).thenReturn(new Agent());
        when(agentRunMapper.selectPage(any(), any())).thenAnswer(invocation -> {
            Page<AgentRun> page = invocation.getArgument(0);
            page.setRecords(List.of(run("run-1", "id-1", RunStatus.SUCCEEDED),
                    run("run-2", "id-1", RunStatus.FAILED)));
            page.setTotal(2L);
            return page;
        });

        PageResult<AgentRunView> result = service.listByAgent("id-1", 1, 20);

        assertThat(result.getTotal()).isEqualTo(2L);
        assertThat(result.getItems()).extracting(AgentRunView::getId).containsExactly("run-1", "run-2");
    }

    @Test
    void get_throwsWhenMissing() {
        when(agentRunMapper.selectById("run-x")).thenReturn(null);

        assertThatThrownBy(() -> service.get("run-x"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void get_returnsView() {
        when(agentRunMapper.selectById("run-1")).thenReturn(run("run-1", "id-1", RunStatus.RUNNING));

        AgentRunView view = service.get("run-1");

        assertThat(view.getId()).isEqualTo("run-1");
        assertThat(view.getAgentId()).isEqualTo("id-1");
        assertThat(view.getStatus()).isEqualTo(RunStatus.RUNNING);
    }

    private AgentRun run(String id, String agentId, RunStatus status) {
        AgentRun run = new AgentRun();
        run.setId(id);
        run.setAgentId(agentId);
        run.setStatus(status.name());
        run.setCreatedAt(Instant.parse("2026-10-14T00:00:00Z"));
        return run;
    }
}
