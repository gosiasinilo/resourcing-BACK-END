package io.nology.resources.common.serviceErrors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ValidationErrors implements ServiceError {

    private Map<String, List<String>> errors = new HashMap<>();

    @Override
    public String getErrorType() {
        return "Service Validation Error";
    }

    @Override
    public String getMessage() {
        return "Service validation failed";
    }

    public boolean hasErrors() {
        return this.errors.isEmpty() == false;
    }

    public void addError(String field, String message) {
        this.errors.computeIfAbsent(field, f -> new ArrayList<>()).add(message);
    }

    public Map<String, List<String>> getErrors() {
        return this.errors;
    }

}
