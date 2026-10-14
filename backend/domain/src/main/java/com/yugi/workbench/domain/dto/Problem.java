package com.yugi.workbench.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/**
 * 统一错误结构（契约 Problem）。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Problem {

    private String code;
    private String message;
    private Map<String, Object> details;

    public Problem() {
    }

    public Problem(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public Problem(String code, String message, Map<String, Object> details) {
        this.code = code;
        this.message = message;
        this.details = details;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }
}
