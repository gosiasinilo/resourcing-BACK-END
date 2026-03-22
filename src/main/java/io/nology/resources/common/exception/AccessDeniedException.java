package io.nology.resources.common.exception;

import org.springframework.http.HttpStatus;

public class AccessDeniedException extends HTTPException {

    public AccessDeniedException(String message, HttpStatus status, String errorCode) {
        super(message, status, errorCode);

    }

}
