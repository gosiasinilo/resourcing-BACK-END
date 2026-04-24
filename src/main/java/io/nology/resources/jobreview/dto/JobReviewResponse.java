package io.nology.resources.jobreview.dto;

import java.time.LocalDateTime;

public record JobReviewResponse(
        Long id,
        Integer workQuality,
        Integer communication,
        Integer onTime,
        String comments,
        String reviewedBy,
        LocalDateTime createdAt) {
}