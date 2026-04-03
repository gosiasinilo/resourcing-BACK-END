package io.nology.resources.temp.dto;

import java.time.LocalDate;
import java.util.List;

public record AvailableTempInfo(
                Long tempId,
                String firstName,
                String lastName,
                String email,
                LocalDate nextAvailableStart,
                List<String> alternativeTemps) {
}