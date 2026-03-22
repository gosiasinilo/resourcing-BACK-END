package io.nology.resources.job.dto;

import java.time.LocalDate;

public record JobResponse(Long id, String name, LocalDate startDate, LocalDate endDate, TempDetails temp) {
}
