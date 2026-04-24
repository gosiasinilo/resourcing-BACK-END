package io.nology.resources.jobreview;

import org.springframework.stereotype.Component;

import io.nology.resources.jobreview.dto.JobReviewResponse;
import io.nology.resources.jobreview.entity.JobReview;

@Component
public class JobReviewMapper {

    public JobReviewResponse toResponse(JobReview review) {
        return new JobReviewResponse(
                review.getId(),
                review.getWorkQuality(),
                review.getCommunication(),
                review.getOnTime(),
                review.getComments(),
                review.getReviewedBy(),
                review.getCreatedAt());
    }
}