package io.nology.resources.common.validations;

import java.time.LocalDate;

import io.nology.resources.common.serviceErrors.ValidationErrors;

public class Validations {

    public static void validateMinLength(String field, String value, int min, ValidationErrors err) {
        if (value != null && value.length() < min) {
            err.addError(field, field + " must be longer than " + (min - 1) + " characters");
        }
    }

    public static void validateEmail(String field, String value, ValidationErrors err) {
        if (value != null && !value.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            err.addError(field, "Please provide a valid email address");
        }
    }

    public static void validateDateNotInThePast(String field, LocalDate date, ValidationErrors err) {
        if (date != null && date.isBefore(LocalDate.now())) {
            err.addError(field, field + " cannot be in the past");
        }
    }
}