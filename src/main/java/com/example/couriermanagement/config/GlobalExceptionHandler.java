package com.example.couriermanagement.config;

import com.example.couriermanagement.dto.response.ErrorInfo;
import com.example.couriermanagement.dto.response.ErrorResponse;
import com.example.couriermanagement.dto.response.ValidationErrorInfo;
import com.example.couriermanagement.dto.response.ValidationErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        ErrorResponse error = ErrorResponse.builder()
                .error(ErrorInfo.builder()
                        .code("BAD_REQUEST")
                        .message(ex.getMessage() != null ? ex.getMessage() : "Bad request")
                        .build())
                .build();
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException ex) {
        ErrorResponse error = ErrorResponse.builder()
                .error(ErrorInfo.builder()
                        .code("INTERNAL_ERROR")
                        .message(ex.getMessage() != null ? ex.getMessage() : "Internal error")
                        .build())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        ErrorResponse error = ErrorResponse.builder()
                .error(ErrorInfo.builder()
                        .code("FORBIDDEN")
                        .message("Access denied")
                        .build())
                .build();
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> details = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = error instanceof FieldError
                    ? ((FieldError) error).getField()
                    : error.getObjectName();
            String message = error.getDefaultMessage() != null
                    ? error.getDefaultMessage()
                    : "Validation error";
            details.put(fieldName, message);
        });

        ValidationErrorResponse error = ValidationErrorResponse.builder()
                .error(ValidationErrorInfo.builder()
                        .code("VALIDATION_FAILED")
                        .message("Validation failed")
                        .details(details)
                        .build())
                .build();
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ValidationErrorResponse> handleBindException(BindException ex) {
        Map<String, String> details = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = error instanceof FieldError
                    ? ((FieldError) error).getField()
                    : error.getObjectName();
            String message = error.getDefaultMessage() != null
                    ? error.getDefaultMessage()
                    : "Validation error";
            details.put(fieldName, message);
        });

        ValidationErrorResponse error = ValidationErrorResponse.builder()
                .error(ValidationErrorInfo.builder()
                        .code("VALIDATION_FAILED")
                        .message("Validation failed")
                        .details(details)
                        .build())
                .build();
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse error = ErrorResponse.builder()
                .error(ErrorInfo.builder()
                        .code("INTERNAL_ERROR")
                        .message("Internal server error")
                        .build())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
