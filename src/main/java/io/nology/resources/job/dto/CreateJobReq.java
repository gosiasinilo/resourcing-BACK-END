package io.nology.resources.job.dto;

import java.time.LocalDate;
import java.util.List;

import io.nology.resources.job.entity.Job;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateJobReq(
                @NotBlank(message = "Name is required") @Size(min = 4, message = "Name must be longer than 3 characters") String name,

                String description,

                @NotNull(message = "Job type is required") Job.JobType jobType,
                @NotNull(message = "Start date is required") LocalDate startDate,

                @NotNull(message = "End date is required") LocalDate endDate,
                String city,

                List<Long> requiredSkillIds) {

}
