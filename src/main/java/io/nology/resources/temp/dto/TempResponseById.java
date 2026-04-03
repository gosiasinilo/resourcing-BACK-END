package io.nology.resources.temp.dto;

import java.util.List;

public record TempResponseById(Long id, String firstName, String lastName, String email, List<JobDetails> jobs) {

}
