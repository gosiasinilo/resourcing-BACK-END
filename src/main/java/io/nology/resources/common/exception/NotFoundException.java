package io.nology.resources.common.exception;

import org.springframework.http.HttpStatus;

import io.nology.resources.common.serviceErrors.NotFoundError;

public class NotFoundException extends HTTPException {

    public NotFoundException(NotFoundError error) {
        super(error.getMessage(), HttpStatus.NOT_FOUND, error.getErrorType());
        this.details = null;

    }

}
