package com.yugi.workbench.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yugi.workbench.domain.entity.Agent;

/**
 * 智能体持久化。由启动类 @MapperScan("com.yugi.workbench.mapper") 注册。
 */
public interface AgentMapper extends BaseMapper<Agent> {
}
