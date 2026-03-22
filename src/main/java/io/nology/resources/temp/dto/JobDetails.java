package io.nology.resources.temp.dto;

import java.time.LocalDate;

public record JobDetails(Long id, String name, LocalDate startDate, LocalDate endDate) {
}
