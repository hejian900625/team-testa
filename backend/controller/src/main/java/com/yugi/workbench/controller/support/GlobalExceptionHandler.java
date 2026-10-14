package com.yugi.workbench.controller.support;

import com.yugi.workbench.domain.dto.Problem;
import com.yugi.workbench.domain.exception.DuplicateResourceException;
import com.yugi.workbench.domain.exception.ResourceNotFoundException;
import com.yugi.workbench.domain.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 统一异常 → Problem { code, message, details? }，与契约错误响应一致。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Problem> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new Problem("NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Problem> handleConflict(DuplicateResourceException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new Problem("CONFLICT", ex.getMessage()));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Problem> handleValidation(ValidationException ex) {
        return badRequest(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Problem> handleBodyValidation(MethodArgumentNotValidException ex) {
        Map<String, Object> details = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> details.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest()
                .body(new Problem("VALIDATION_FAILED", "参数校验失败", details));
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<Problem> handleMethodValidation(HandlerMethodValidationException ex) {
        return badRequest("参数校验失败");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Problem> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return badRequest("参数非法: " + ex.getName());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Problem> handleUnreadable(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(new Problem("BAD_REQUEST", "请求体不是合法 JSON"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Problem> handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new Problem("INTERNAL_ERROR", "服务器内部错误"));
    }

    private ResponseEntity<Problem> badRequest(String message) {
        return ResponseEntity.badRequest().body(new Problem("VALIDATION_FAILED", message));
    }
}
