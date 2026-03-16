package com.banking.exception;

import com.banking.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest req) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(err -> {
            String field = (err instanceof FieldError fe) ? fe.getField() : err.getObjectName();
            errors.put(field, err.getDefaultMessage());
        });
        log.warn("Validation failed: {}", errors);
        return ResponseEntity.badRequest()
                .body(ApiResponse.fail("Validation failed", errors));
    }

    @ExceptionHandler(CustomException.AccountNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(
            CustomException.AccountNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail(ex.getMessage(), null));
    }

    @ExceptionHandler(CustomException.InsufficientFundsException.class)
    public ResponseEntity<ApiResponse<Void>> handleInsufficientFunds(
            CustomException.InsufficientFundsException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiResponse.fail(ex.getMessage(), null));
    }

    @ExceptionHandler(CustomException.DuplicateAccountException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateAccount(
            CustomException.DuplicateAccountException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.fail(ex.getMessage(), null));
    }

    @ExceptionHandler(CustomException.DuplicateTransactionException.class)
    public ResponseEntity<ApiResponse<Object>> handleDuplicateTx(
            CustomException.DuplicateTransactionException ex) {
        return ResponseEntity.ok(ApiResponse.ok(ex.getMessage(), ex.getExistingResult()));
    }

    @ExceptionHandler(CustomException.InvalidTransactionException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidTx(
            CustomException.InvalidTransactionException ex) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.fail(ex.getMessage(), null));
    }

    @ExceptionHandler(CustomException.AccountSuspendedException.class)
    public ResponseEntity<ApiResponse<Void>> handleSuspended(
            CustomException.AccountSuspendedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.fail(ex.getMessage(), null));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.fail("Invalid username or password", null));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.fail("Access denied", null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(
            Exception ex, HttpServletRequest req) {
        log.error("Unhandled error on {}: {}", req.getRequestURI(), ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail("An unexpected error occurred", null));
    }
}