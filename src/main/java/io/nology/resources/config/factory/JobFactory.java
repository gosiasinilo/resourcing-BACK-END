package io.nology.resources.config.factory;

import java.time.LocalDate;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.github.javafaker.Faker;

import io.nology.resources.job.entity.Job;
import io.nology.resources.temp.entity.Temp;

@Component
@Profile({ "dev", "test" })
public class JobFactory {

    private final Faker faker = new Faker();

    public Job createJob(Temp temp) {
        Job job = new Job();
        job.setName(faker.lorem().sentence(3));
        job.setStartDate(LocalDate.now().plusDays(3));
        job.setEndDate(LocalDate.now().plusDays(14));

        if (temp != null) {
            job.setTemp(temp);
            temp.getJobs().add(job);
        }

        return job;
    }

    public Job createJob() {
        return createJob(null);
    }
}