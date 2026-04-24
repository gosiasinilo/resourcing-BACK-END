package io.nology.resources.job.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CompleteJobReq(
        @NotNull(message = "Quality rating is required") @Min(0) @Max(5) Integer workQuality,

        @NotNull(message = "Communication rating is required") @Min(0) @Max(5) Integer communication,

        @NotNull(message = "On time finish rating is required") @Min(0) @Max(5) Integer onTime,

        String comments) {
}