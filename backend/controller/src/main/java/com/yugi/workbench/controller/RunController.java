package com.yugi.workbench.controller;

import com.yugi.workbench.domain.dto.AgentRunView;
import com.yugi.workbench.service.RunService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 运行记录详情接口（契约 tags: runs）。
 */
@RestController
@RequestMapping("/api/v1/runs")
public class RunController {

    private final RunService runService;

    public RunController(RunService runService) {
        this.runService = runService;
    }

    @GetMapping("/{runId}")
    public AgentRunView getRun(@PathVariable("runId") String runId) {
        return runService.get(runId);
    }
}
