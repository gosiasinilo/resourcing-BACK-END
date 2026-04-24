package io.nology.resources.job.dto;

import java.time.LocalDate;
import java.util.List;

import io.nology.resources.job.entity.Job;

public record EditJobReq(
        String name,
        String description,
        Job.JobType jobType,
        LocalDate startDate,
        LocalDate endDate,
        String city,
        List<Long> requiredSkillIds

) {

}
