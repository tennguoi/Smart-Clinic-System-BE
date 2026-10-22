package com.example.demo.exception;

import com.example.demo.dto.ApiErrorResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Bắt lỗi Bean Validation khi submit request body không hợp lệ (@Valid @RequestBody)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = error instanceof FieldError ? ((FieldError) error).getField() : error.getObjectName();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.warn("⚠️ Validation error: {}", errors);

        ApiErrorResponse response = ApiErrorResponse.validation("Dữ liệu gửi lên không hợp lệ", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Bắt lỗi ConstraintViolation (ví dụ validation trên param/path variable)
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String propertyPath = violation.getPropertyPath().toString();
            errors.put(propertyPath, violation.getMessage());
        });

        log.warn("⚠️ Constraint violation: {}", errors);

        ApiErrorResponse response = ApiErrorResponse.validation("Dữ liệu tham số không hợp lệ", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Bắt lỗi JSON payload gửi lên sai định dạng
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleMalformedJson(HttpMessageNotReadableException ex) {
        log.warn("⚠️ Malformed JSON: {}", ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("MALFORMED_JSON", "Dữ liệu JSON gửi lên không đúng định dạng hoặc thiếu trường bắt buộc");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Bắt lỗi phân quyền không đủ quyền hạn (403 Forbidden)
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        log.warn("⛔ Access Denied: {}", ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("FORBIDDEN", ex.getMessage() != null && !ex.getMessage().isBlank()
                ? ex.getMessage()
                : "Bạn không có quyền thực hiện thao tác này");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    /**
     * Bắt lỗi xác thực tài khoản / mật khẩu (401 Unauthorized)
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthenticationException(AuthenticationException ex) {
        log.warn("🔒 Authentication failed: {}", ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("UNAUTHORIZED", "Thông tin xác thực không chính xác hoặc đã hết hạn");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * Bắt ResponseStatusException do controller / service throw chủ động
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatus(ResponseStatusException ex) {
        log.warn("⚠️ ResponseStatusException: status={}, reason={}", ex.getStatusCode(), ex.getReason());
        ApiErrorResponse response = ApiErrorResponse.of(ex.getStatusCode().toString(), ex.getReason() != null ? ex.getReason() : ex.getMessage());
        return ResponseEntity.status(ex.getStatusCode()).body(response);
    }

    /**
     * Bắt IllegalArgumentException hoặc IllegalStateException
     */
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(RuntimeException ex) {
        log.warn("⚠️ Bad argument: {}", ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of("BAD_REQUEST", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Bắt các RuntimeException chưa phân loại
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiErrorResponse> handleRuntimeException(RuntimeException ex) {
        log.error("💥 Unhandled RuntimeException: ", ex);
        String msg = ex.getMessage();
        if (msg != null && msg.toLowerCase().contains("không tìm thấy")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiErrorResponse.of("NOT_FOUND", msg));
        }
        ApiErrorResponse response = ApiErrorResponse.of("SYSTEM_ERROR", msg != null ? msg : "Đã xảy ra lỗi xử lý");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Chốt chặn cuối cùng cho các lỗi không mong muốn (500)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneralException(Exception ex) {
        log.error("🚨 Internal Server Error: ", ex);
        ApiErrorResponse response = ApiErrorResponse.of("INTERNAL_ERROR", "Đã xảy ra lỗi hệ thống, vui lòng liên hệ quản trị viên");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
