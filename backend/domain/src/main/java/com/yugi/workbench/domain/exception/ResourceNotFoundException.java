package com.yugi.workbench.domain.exception;

/**
 * 资源不存在（映射为 HTTP 404 / code=NOT_FOUND）。
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
