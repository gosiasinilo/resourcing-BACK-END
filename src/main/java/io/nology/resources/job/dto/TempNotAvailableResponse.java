package io.nology.resources.job.dto;

import java.time.LocalDate;
import java.util.List;

import io.nology.resources.temp.dto.TempResponse;

public record TempNotAvailableResponse(
                String message,
                LocalDate nextAvailableStart,
                List<TempResponse> availableTemps) {
}