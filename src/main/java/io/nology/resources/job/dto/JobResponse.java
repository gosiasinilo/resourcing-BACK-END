package io.nology.resources.job.dto;

import java.time.LocalDate;
import java.util.List;

import io.nology.resources.job.entity.Job;
import io.nology.resources.temp.dto.TempResponse;

public record JobResponse(Long id, String name, String description,
        Job.JobType jobType,
        Job.JobStatus status,
        LocalDate startDate,
        LocalDate endDate,
        String city,
        List<String> requiredSkills,
        TempResponse temp) {
}
