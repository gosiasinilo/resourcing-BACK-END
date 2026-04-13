package io.nology.resources.temp;

import org.springframework.stereotype.Component;

import io.nology.resources.job.entity.Job;
import io.nology.resources.temp.entity.Temp;

@Component
public class TempAssigning {

    public void removeTempFromJob(Job job) {
        Temp temp = job.getTemp();

        if (temp != null) {
            temp.getJobs().remove(job);
            job.setTemp(null);
        }
    }
}