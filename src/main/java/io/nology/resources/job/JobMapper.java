package io.nology.resources.job;

import java.util.List;

import org.springframework.stereotype.Component;

import io.nology.resources.job.dto.JobResponse;
import io.nology.resources.job.entity.Job;
import io.nology.resources.jobreview.dto.JobReviewResponse;
import io.nology.resources.jobreview.entity.JobReview;
import io.nology.resources.temp.dto.TempResponse;
import io.nology.resources.temp.entity.Temp;

@Component
public class JobMapper {

    public JobResponse toResponse(Job job) {
        List<String> requiredSkills = job.getRequiredSkills().stream()
                .map(s -> s.getName())
                .toList();

        TempResponse tempResponse = null;
        Temp temp = job.getTemp();
        if (temp != null) {
            tempResponse = new TempResponse(
                    temp.getId(),
                    temp.getFirstName(),
                    temp.getLastName(),
                    temp.getEmail(),
                    temp.getCity(),
                    temp.getRating(),
                    temp.getJobs().size());
        }

        JobReviewResponse reviewResponse = null;
        if (!job.getReviews().isEmpty()) {
            JobReview r = job.getReviews().get(0);
            reviewResponse = new JobReviewResponse(
                    r.getId(), r.getWorkQuality(), r.getCommunication(),
                    r.getOnTime(), r.getComments(), r.getReviewedBy(), r.getCreatedAt());
        }

        return new JobResponse(
                job.getId(),
                job.getName(),
                job.getDescription(),
                job.getJobType(),
                job.getStatus(),
                job.getStartDate(),
                job.getEndDate(),
                job.getCity(),
                requiredSkills,
                tempResponse,
                reviewResponse);
    }
}