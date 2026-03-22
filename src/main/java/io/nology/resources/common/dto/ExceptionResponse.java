package io.nology.resources.common.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class ExceptionResponse {
    private String path;
    private String message;

    private int status;
    private String errorCode;
    private LocalDateTime timestamp = LocalDateTime.now();
    private Map<String, List<String>> details;

    public ExceptionResponse(String message, int status, String errorCode, Map<String, List<String>> details,
            String path) {
        this.message = message;
        this.status = status;
        this.errorCode = errorCode;
        this.path = path;
        this.details = details;
    }

    public String getPath() {
        return path;
    }

    public String getMessage() {
        return message;
    }

    public int getStatus() {
        return status;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public Map<String, List<String>> getDetails() {
        return details;
    }
}
