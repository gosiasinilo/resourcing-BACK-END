package io.nology.resources.common.serviceErrors;

public class NotFoundError implements ServiceError {
    private final String entityName;
    private final Long id;

    public NotFoundError(String entityName, Long id) {
        this.entityName = entityName;
        this.id = id;
    }

    @Override
    public String getErrorType() {
        return "NOT_FOUND";
    }

    @Override
    public String getMessage() {
        return String.format("%s with id '%s' not found", entityName, id);
    }

    public String getEntityName() {
        return entityName;
    }

    public Long getId() {
        return id;
    }

}