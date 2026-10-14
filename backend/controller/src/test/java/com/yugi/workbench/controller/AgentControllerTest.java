package com.yugi.workbench.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.yugi.workbench.controller.support.GlobalExceptionHandler;
import com.yugi.workbench.domain.dto.AgentRunView;
import com.yugi.workbench.domain.dto.AgentView;
import com.yugi.workbench.domain.dto.PageResult;
import com.yugi.workbench.domain.enums.AgentStatus;
import com.yugi.workbench.domain.enums.RunStatus;
import com.yugi.workbench.domain.exception.ResourceNotFoundException;
import com.yugi.workbench.service.AgentService;
import com.yugi.workbench.service.RunService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AgentControllerTest {

    private final AgentService agentService = mock(AgentService.class);
    private final RunService runService = mock(RunService.class);
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new AgentController(agentService, runService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
            .build();

    @Test
    void create_returns201WithBody() throws Exception {
        when(agentService.create(any())).thenReturn(agentView());

        mockMvc.perform(post("/api/v1/agents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Alpha\",\"model\":\"gpt-4o-mini\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("11111111-1111-1111-1111-111111111111"))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.createdAt").value("2026-10-14T00:00:00Z"));
    }

    @Test
    void create_invalidBody_returns400Problem() throws Exception {
        mockMvc.perform(post("/api/v1/agents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @Test
    void get_missing_returns404Problem() throws Exception {
        when(agentService.get("nope")).thenThrow(new ResourceNotFoundException("智能体不存在: nope"));

        mockMvc.perform(get("/api/v1/agents/nope"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void list_returnsPageEnvelope() throws Exception {
        when(agentService.list(any())).thenReturn(new PageResult<>(1, 20, 1L, List.of(agentView())));

        mockMvc.perform(get("/api/v1/agents").param("keyword", "alp"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.items[0].name").value("Alpha"));
    }

    @Test
    void list_invalidSize_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/agents").param("size", "1000"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @Test
    void delete_returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/agents/abc"))
                .andExpect(status().isNoContent());

        verify(agentService).delete("abc");
    }

    @Test
    void listAgentRuns_returnsPage() throws Exception {
        when(runService.listByAgent(anyString(), anyInt(), anyInt()))
                .thenReturn(new PageResult<>(1, 20, 1L, List.of(runView())));

        mockMvc.perform(get("/api/v1/agents/id-1/runs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value("run-1"))
                .andExpect(jsonPath("$.items[0].status").value("SUCCEEDED"));
    }

    private AgentView agentView() {
        AgentView view = new AgentView();
        view.setId("11111111-1111-1111-1111-111111111111");
        view.setName("Alpha");
        view.setModel("gpt-4o-mini");
        view.setStatus(AgentStatus.DRAFT);
        view.setCreatedAt(Instant.parse("2026-10-14T00:00:00Z"));
        view.setUpdatedAt(Instant.parse("2026-10-14T00:00:00Z"));
        return view;
    }

    private AgentRunView runView() {
        AgentRunView view = new AgentRunView();
        view.setId("run-1");
        view.setAgentId("id-1");
        view.setStatus(RunStatus.SUCCEEDED);
        view.setCreatedAt(Instant.parse("2026-10-14T00:00:00Z"));
        return view;
    }
}
