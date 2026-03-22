package io.nology.resources.common.entity.traits;

import java.time.LocalDateTime;

public interface Timestampable {
    public LocalDateTime getCreatedAt();

    public void setCreatedAt(LocalDateTime timestamp);

    public LocalDateTime getUpdatedAt();

    public void setUpdatedAt(LocalDateTime timestamp);

}
