package com.chubb.assessment.api.exception;

import com.chubb.assessment.api.dto.response.ErrorResponse;
import com.chubb.assessment.common.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataAccessException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.OffsetDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String VALIDATION_FAILED = "Request validation failed.";
    private static final String SERVICE_UNAVAILABLE =
            "The policy service is temporarily unavailable. Please try again later.";

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleBodyValidation(HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, VALIDATION_FAILED, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleParamValidation(HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, VALIDATION_FAILED, request);
    }

    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class,
            PropertyReferenceException.class,
            IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, VALIDATION_FAILED, request);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDataAccess(HttpServletRequest request) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, SERVICE_UNAVAILABLE, request);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(
                OffsetDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
