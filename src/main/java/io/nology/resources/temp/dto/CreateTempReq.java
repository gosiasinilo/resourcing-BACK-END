package io.nology.resources.temp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTempReq(
        @NotBlank(message = "First name is required") @Size(min = 3, message = "Name hast to be longer than 2 characters") String firstName,
        @NotBlank(message = "Surname is required") @Size(min = 3, message = "Surname hast to be longer than 2 characters") String lastName,
        @NotBlank(message = "Email is required") @Email(message = "Please provide a valid email address") String email,
        @NotBlank(message = "City is required") String city,
        String notes) {
}
