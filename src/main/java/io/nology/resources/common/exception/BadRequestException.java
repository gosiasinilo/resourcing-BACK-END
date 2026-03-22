package io.nology.resources.common.exception;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import io.nology.resources.common.serviceErrors.ValidationErrors;

public class BadRequestException extends HTTPException {

    Map<String, List<String>> details;

    public BadRequestException(String message, HttpStatus status, String errorCode, Map<String, List<String>> details) {
        super(message, status, errorCode);
        this.details = details;
    }

    public static BadRequestException from(ValidationErrors err) {
        return new BadRequestException(err.getMessage(), HttpStatus.BAD_REQUEST, "BAD_REQUEST", err.getErrors());
    }

    public static BadRequestException from(MethodArgumentNotValidException ex) {
        Map<String, List<String>> details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.groupingBy(
                        FieldError::getField,
                        Collectors.mapping(FieldError::getDefaultMessage,
                                Collectors.toList())));
        return new BadRequestException("DTO validation failed", HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", details);
    }

    public Map<String, List<String>> getDetails() {
        return details;
    }

}
