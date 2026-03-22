package io.nology.resources.common;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import io.nology.resources.common.dto.ExceptionResponse;
import io.nology.resources.common.exception.BadRequestException;
import io.nology.resources.common.exception.HTTPException;
import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> handleValidationException(MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        BadRequestException exception = BadRequestException.from(ex);
        return handleHttpException(exception, request);
    }

    @ExceptionHandler(HTTPException.class)
    public ResponseEntity<ExceptionResponse> handleHttpException(HTTPException ex, HttpServletRequest request) {
        String path = request.getRequestURI();
        ExceptionResponse response = new ExceptionResponse(ex.getMessage(), ex.getStatus().value(), ex.getErrorCode(),
                ex.getDetails(), path);
        return new ResponseEntity<>(response, ex.getStatus());
    }

}
