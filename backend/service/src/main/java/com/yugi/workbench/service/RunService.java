package com.yugi.workbench.service;

import com.yugi.workbench.domain.dto.AgentRunView;
import com.yugi.workbench.domain.dto.PageResult;

/**
 * 运行记录业务接口（契约 runs 资源，MVP 只读）。
 */
public interface RunService {

    PageResult<AgentRunView> listByAgent(String agentId, int page, int size);

    AgentRunView get(String runId);
}
