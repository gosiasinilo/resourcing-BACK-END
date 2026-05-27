package io.nology.resources.temp.dto;

import java.util.List;

public record EditTempReq(String firstName,
        String lastName,
        String email,
        String city,
        String notes,
        List<Long> skillIds) {

}
