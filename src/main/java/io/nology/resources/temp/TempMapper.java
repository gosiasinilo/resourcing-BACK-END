package io.nology.resources.temp;

import java.util.List;

import org.springframework.stereotype.Component;

import io.nology.resources.temp.dto.TempResponse;
import io.nology.resources.temp.dto.TempResponseById;
import io.nology.resources.temp.entity.Temp;

@Component
public class TempMapper {

    public TempResponse toResponse(Temp temp) {
        return new TempResponse(
                temp.getId(),
                temp.getFirstName(),
                temp.getLastName(),
                temp.getEmail(),
                temp.getCity(),
                temp.getRating());
    }

    public TempResponseById toDetailResponse(Temp temp) {
        List<String> skills = temp.getSkills().stream()
                .map(s -> s.getName())
                .toList();

        List<TempResponseById.JobSummary> jobs = temp.getJobs().stream()
                .map(j -> new TempResponseById.JobSummary(
                        j.getId(),
                        j.getName(),
                        j.getStartDate().toString(),
                        j.getEndDate().toString(),
                        j.getStatus()))
                .toList();

        return new TempResponseById(
                temp.getId(),
                temp.getFirstName(),
                temp.getLastName(),
                temp.getEmail(),
                temp.getCity(),
                temp.getNotes(),
                temp.getRating(),
                skills,
                jobs);
    }
}