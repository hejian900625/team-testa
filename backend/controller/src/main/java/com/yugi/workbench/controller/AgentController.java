package com.yugi.workbench.controller;

import com.yugi.workbench.domain.dto.AgentCreate;
import com.yugi.workbench.domain.dto.AgentQuery;
import com.yugi.workbench.domain.dto.AgentRunView;
import com.yugi.workbench.domain.dto.AgentUpdate;
import com.yugi.workbench.domain.dto.AgentView;
import com.yugi.workbench.domain.dto.PageResult;
import com.yugi.workbench.domain.enums.AgentStatus;
import com.yugi.workbench.domain.exception.ValidationException;
import com.yugi.workbench.service.AgentService;
import com.yugi.workbench.service.RunService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 智能体接口（契约 tags: agents / runs）。基路径 /api/v1。
 */
@RestController
@RequestMapping("/api/v1/agents")
public class AgentController {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    private final AgentService agentService;
    private final RunService runService;

    public AgentController(AgentService agentService, RunService runService) {
        this.agentService = agentService;
        this.runService = runService;
    }

    @GetMapping
    public PageResult<AgentView> listAgents(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "status", required = false) AgentStatus status,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size) {
        int pageNo = page == null ? DEFAULT_PAGE : page;
        int pageSize = size == null ? DEFAULT_SIZE : size;
        validatePaging(pageNo, pageSize);
        return agentService.list(new AgentQuery(keyword, status, pageNo, pageSize));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AgentView createAgent(@Valid @RequestBody AgentCreate request) {
        return agentService.create(request);
    }

    @GetMapping("/{agentId}")
    public AgentView getAgent(@PathVariable("agentId") String agentId) {
        return agentService.get(agentId);
    }

    @PutMapping("/{agentId}")
    public AgentView updateAgent(@PathVariable("agentId") String agentId,
                                 @Valid @RequestBody AgentUpdate request) {
        return agentService.update(agentId, request);
    }

    @DeleteMapping("/{agentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAgent(@PathVariable("agentId") String agentId) {
        agentService.delete(agentId);
    }

    @GetMapping("/{agentId}/runs")
    public PageResult<AgentRunView> listAgentRuns(
            @PathVariable("agentId") String agentId,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size) {
        int pageNo = page == null ? DEFAULT_PAGE : page;
        int pageSize = size == null ? DEFAULT_SIZE : size;
        validatePaging(pageNo, pageSize);
        return runService.listByAgent(agentId, pageNo, pageSize);
    }

    private void validatePaging(int page, int size) {
        if (page < 1) {
            throw new ValidationException("page 必须 >= 1");
        }
        if (size < 1 || size > MAX_SIZE) {
            throw new ValidationException("size 必须在 1.." + MAX_SIZE + " 之间");
        }
    }
}
