package io.nology.resources.job.dto;

import java.time.LocalDate;
import java.util.List;

public record TempNotAvailableResponse(
        String message,
        LocalDate nextAvailableStart,
        List<TempDetails> availableTemps) {
}