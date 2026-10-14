package com.yugi.workbench.domain.dto;

import com.yugi.workbench.domain.enums.AgentStatus;

/**
 * 智能体列表查询条件（keyword 模糊匹配 name，status 精确匹配，分页）。
 */
public class AgentQuery {

    private String keyword;
    private AgentStatus status;
    private int page = 1;
    private int size = 20;

    public AgentQuery() {
    }

    public AgentQuery(String keyword, AgentStatus status, int page, int size) {
        this.keyword = keyword;
        this.status = status;
        this.page = page;
        this.size = size;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public AgentStatus getStatus() {
        return status;
    }

    public void setStatus(AgentStatus status) {
        this.status = status;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
