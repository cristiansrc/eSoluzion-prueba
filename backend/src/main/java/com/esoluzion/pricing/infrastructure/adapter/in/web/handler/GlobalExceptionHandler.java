package com.esoluzion.pricing.infrastructure.adapter.in.web.handler;

import com.esoluzion.pricing.domain.exception.PriceNotFoundException;
import com.esoluzion.pricing.infrastructure.adapter.in.web.dto.ApiErrorDetail;
import com.esoluzion.pricing.infrastructure.adapter.in.web.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.openapitools.jackson.nullable.JsonNullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(PriceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handlePriceNotFound(
            PriceNotFoundException exception, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "PRICE_NOT_FOUND",
                "No applicable price found for the given parameters.", request, List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException exception, HttpServletRequest request) {
        var details = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> {
                    var detail = new ApiErrorDetail();
                    detail.setField(error.getField());
                    detail.setCode("FIELD_INVALID");
                    detail.setMessage(error.getDefaultMessage());
                    if (error.getRejectedValue() != null) {
                        detail.setRejectedValue(JsonNullable.of(error.getRejectedValue().toString()));
                    } else {
                        detail.setRejectedValue(JsonNullable.<String>undefined());
                    }
                    return detail;
                })
                .toList();
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                "The request contains invalid fields.", request, details);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingParam(
            MissingServletRequestParameterException exception, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                "Required parameter '" + exception.getParameterName() + "' is missing", request, List.of());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                "Invalid parameter type: " + exception.getName(), request, List.of());
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(
            ValidationException exception, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                "Validation error: " + exception.getMessage(), request, List.of());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(
            IllegalArgumentException exception, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                exception.getMessage(), request, List.of());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            ConstraintViolationException exception, HttpServletRequest request) {
        var details = exception.getConstraintViolations().stream()
                .map(violation -> {
                    var detail = new ApiErrorDetail();
                    detail.setField(violation.getPropertyPath().toString());
                    detail.setCode("FIELD_INVALID");
                    detail.setMessage(violation.getMessage());
                    detail.setRejectedValue(violation.getInvalidValue() != null
                            ? JsonNullable.of(violation.getInvalidValue().toString())
                            : JsonNullable.<String>undefined());
                    return detail;
                })
                .toList();
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                "The request contains invalid fields.", request, details);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleMessageNotReadable(
            HttpMessageNotReadableException exception, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "INVALID_REQUEST_BODY",
                "The request body is invalid or malformed.", request, List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(
            Exception exception, HttpServletRequest request) {
        String traceId = UUID.randomUUID().toString();
        log.error("Unhandled exception. trace_id={}", traceId, exception);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "An unexpected error occurred.", request, List.of());
    }

    private ResponseEntity<ApiErrorResponse> build(
            HttpStatus status, String code, String message,
            HttpServletRequest request, List<ApiErrorDetail> details) {

        var response = new ApiErrorResponse();
        response.setTimestamp(LocalDateTime.now());
        response.setStatus(status.value());
        response.setError(status.getReasonPhrase());
        response.setCode(code);
        response.setMessage(message);
        response.setPath(request.getRequestURI());
        response.setTraceId(UUID.randomUUID().toString());
        response.setDetails(details);

        return new ResponseEntity<>(response, status);
    }
}
