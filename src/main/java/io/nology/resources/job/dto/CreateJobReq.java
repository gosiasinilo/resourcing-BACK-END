package io.nology.resources.job.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateJobReq(
        @Size(min = 4, message = "Name must be longer than 3 characters") String name,
        @NotNull(message = "Start date is required") @FutureOrPresent(message = "Start date cannot be in the past") LocalDate startDate,
        @NotNull(message = "End date is required") @FutureOrPresent(message = "End date cannot be in the past") LocalDate endDate) {

}
