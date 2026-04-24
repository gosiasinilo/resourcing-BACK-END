package io.nology.resources.temp.dto;

import java.math.BigDecimal;

public record TempResponse(Long id, String firstName, String lastName, String email, String city, BigDecimal rating) {

}