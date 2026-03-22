package io.nology.resources.temp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTempReq(
                @NotBlank(message = "Name must not be blank") @Size(min = 3, message = "Name must be longer than 2 characters") String firstName,
                @NotBlank(message = "Surname must not be blank") String lastName) {

}
