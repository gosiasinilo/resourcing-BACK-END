package io.nology.resources.job.dto;

import java.time.LocalDate;

import io.nology.resources.temp.dto.TempResponse;

public record JobResponse(Long id, String name, LocalDate startDate, LocalDate endDate, TempResponse temp) {
}
