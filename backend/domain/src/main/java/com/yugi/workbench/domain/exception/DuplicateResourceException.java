package com.yugi.workbench.domain.exception;

/**
 * 资源冲突，如名称重复（映射为 HTTP 409 / code=CONFLICT）。
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
