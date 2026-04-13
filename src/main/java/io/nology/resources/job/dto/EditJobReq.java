package io.nology.resources.job.dto;

import java.time.LocalDate;

public record EditJobReq(
                String name,
                LocalDate startDate,
                LocalDate endDate) {

}
