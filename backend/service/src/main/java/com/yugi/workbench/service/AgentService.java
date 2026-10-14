package com.yugi.workbench.service;

import com.yugi.workbench.domain.dto.AgentCreate;
import com.yugi.workbench.domain.dto.AgentQuery;
import com.yugi.workbench.domain.dto.AgentUpdate;
import com.yugi.workbench.domain.dto.AgentView;
import com.yugi.workbench.domain.dto.PageResult;

/**
 * 智能体业务接口（契约 agents 资源）。
 */
public interface AgentService {

    PageResult<AgentView> list(AgentQuery query);

    AgentView get(String id);

    AgentView create(AgentCreate request);

    AgentView update(String id, AgentUpdate request);

    void delete(String id);

    boolean exists(String id);
}
