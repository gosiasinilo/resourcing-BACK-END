package io.nology.resources.temp.dto;

import java.util.List;

public record TempResponseById(Long id, String firstName, String lastName, List<JobDetails> jobs) {

}
