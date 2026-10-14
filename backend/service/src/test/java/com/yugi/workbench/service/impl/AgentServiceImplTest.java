package com.yugi.workbench.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import com.yugi.workbench.mapper.AgentMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgentServiceImplTest {

    @Mock
    private AgentMapper agentMapper;

    private AgentServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AgentServiceImpl(agentMapper, new ObjectMapper());
    }

    @Test
    void create_persistsWithGeneratedIdAndDefaultStatus() {
        when(agentMapper.selectCount(any())).thenReturn(0L);

        AgentCreate request = new AgentCreate();
        request.setName("  Alpha  ");
        request.setModel("gpt-4o-mini");
        request.setTools(List.of("search", "http"));

        AgentView view = service.create(request);

        assertThat(view.getId()).isNotBlank();
        assertThat(view.getName()).isEqualTo("Alpha");
        assertThat(view.getModel()).isEqualTo("gpt-4o-mini");
        assertThat(view.getStatus()).isEqualTo(AgentStatus.DRAFT);
        assertThat(view.getTools()).containsExactly("search", "http");
        assertThat(view.getCreatedAt()).isNotNull();

        ArgumentCaptor<Agent> captor = ArgumentCaptor.forClass(Agent.class);
        verify(agentMapper).insert(captor.capture());
        assertThat(captor.getValue().getTools()).isEqualTo("[\"search\",\"http\"]");
    }

    @Test
    void create_rejectsDuplicateName() {
        when(agentMapper.selectCount(any())).thenReturn(1L);

        AgentCreate request = new AgentCreate();
        request.setName("Alpha");
        request.setModel("gpt-4o-mini");

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void get_throwsWhenMissing() {
        when(agentMapper.selectById("missing")).thenReturn(null);

        assertThatThrownBy(() -> service.get("missing"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_appliesOnlyProvidedFields() {
        Agent existing = agent("id-1", "Alpha", AgentStatus.ACTIVE);
        when(agentMapper.selectById("id-1")).thenReturn(existing);

        AgentUpdate request = new AgentUpdate();
        request.setName("Alpha2");
        request.setStatus(AgentStatus.DISABLED);

        AgentView view = service.update("id-1", request);

        assertThat(view.getName()).isEqualTo("Alpha2");
        assertThat(view.getStatus()).isEqualTo(AgentStatus.DISABLED);
        assertThat(view.getModel()).isEqualTo("gpt-4o-mini");
        verify(agentMapper).updateById(existing);
    }

    @Test
    void update_throwsWhenMissing() {
        when(agentMapper.selectById("nope")).thenReturn(null);

        assertThatThrownBy(() -> service.update("nope", new AgentUpdate()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_throwsWhenMissing() {
        when(agentMapper.selectById("nope")).thenReturn(null);

        assertThatThrownBy(() -> service.delete("nope"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void list_returnsPagedItems() {
        when(agentMapper.selectPage(any(), any())).thenAnswer(invocation -> {
            Page<Agent> page = invocation.getArgument(0);
            page.setRecords(List.of(agent("id-1", "Alpha", AgentStatus.ACTIVE),
                    agent("id-2", "Beta", AgentStatus.DRAFT)));
            page.setTotal(5L);
            return page;
        });

        PageResult<AgentView> result = service.list(new AgentQuery("alp", AgentStatus.ACTIVE, 1, 20));

        assertThat(result.getPage()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(20);
        assertThat(result.getTotal()).isEqualTo(5L);
        assertThat(result.getItems()).extracting(AgentView::getName).containsExactly("Alpha", "Beta");
    }

    private Agent agent(String id, String name, AgentStatus status) {
        Agent agent = new Agent();
        agent.setId(id);
        agent.setName(name);
        agent.setModel("gpt-4o-mini");
        agent.setStatus(status.name());
        agent.setCreatedAt(Instant.parse("2026-10-14T00:00:00Z"));
        agent.setUpdatedAt(Instant.parse("2026-10-14T00:00:00Z"));
        return agent;
    }
}
