package com.yugi.workbench.domain.exception;

/**
 * 业务参数校验失败（映射为 HTTP 400 / code=VALIDATION_FAILED）。
 */
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }
}
