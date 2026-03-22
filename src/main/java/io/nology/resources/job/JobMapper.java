package io.nology.resources.job;

import org.springframework.stereotype.Component;

import io.nology.resources.job.dto.JobResponse;
import io.nology.resources.job.dto.TempDetails;
import io.nology.resources.job.entity.Job;
import io.nology.resources.temp.entity.Temp;

@Component
public class JobMapper {

    public TempDetails TempDetails(Temp temp) {
        if (temp == null)
            return null;
        return new TempDetails(temp.getId(), temp.getFirstName(), temp.getLastName());
    }

    public JobResponse toResponse(Job job) {

        return new JobResponse(
                job.getId(),
                job.getName(),
                job.getStartDate(),
                job.getEndDate(),
                TempDetails(job.getTemp()));
    }

}
