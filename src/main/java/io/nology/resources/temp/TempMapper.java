package io.nology.resources.temp;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import io.nology.resources.job.entity.Job;
import io.nology.resources.temp.dto.JobDetails;
import io.nology.resources.temp.dto.TempResponse;
import io.nology.resources.temp.dto.TempResponseById;
import io.nology.resources.temp.entity.Temp;

@Component
public class TempMapper {

    public TempResponse toResponse(Temp temp) {
        return new TempResponse(
                temp.getId(),
                temp.getFirstName(),
                temp.getLastName());
    }

    public TempResponseById toDetailResponse(Temp temp) {

        List<JobDetails> jobs = temp.getJobs().stream().map(this::mapJob).collect(Collectors.toList());

        return new TempResponseById(
                temp.getId(),
                temp.getFirstName(),
                temp.getLastName(),
                jobs);
    }

    private JobDetails mapJob(Job job) {
        return new JobDetails(
                job.getId(),
                job.getName(),
                job.getStartDate(),
                job.getEndDate());
    }
}