package io.nology.resources.temp.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record AvailableTempInfo(
        Long id,
        String firstName,
        String lastName,
        String email,
        String city,
        BigDecimal rating,
        boolean available,
        LocalDate nextAvailableStart,
        List<AlternativeTempInfo> alternativeTemps) {
    public record AlternativeTempInfo(
            Long id,
            String firstName,
            String lastName,
            String city,
            BigDecimal rating) {
    }
}