package io.nology.resources.common.exception;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;

public abstract class HTTPException extends RuntimeException {

    private HttpStatus status;
    private String errorCode;
    public Map<String, List<String>> details;

    public HTTPException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    public void setDetails(Map<String, List<String>> details) {
        this.details = details;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public Map<String, List<String>> getDetails() {
        return details;
    }

}
