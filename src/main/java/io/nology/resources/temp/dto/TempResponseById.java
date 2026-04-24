package io.nology.resources.temp.dto;

import java.math.BigDecimal;
import java.util.List;

import io.nology.resources.job.entity.Job;

public record TempResponseById(Long id, String firstName, String lastName, String email,
        String city,
        String notes,
        BigDecimal rating,
        List<String> skills, List<JobSummary> jobs) {
    public record JobSummary(
            Long id,
            String name,
            String startDate,
            String endDate,
            Job.JobStatus status) {
    }
}