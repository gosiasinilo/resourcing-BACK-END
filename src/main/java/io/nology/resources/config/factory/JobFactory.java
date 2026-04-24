package io.nology.resources.config.factory;

import java.time.LocalDate;
import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.github.javafaker.Faker;

import io.nology.resources.job.entity.Job;
import io.nology.resources.skill.entity.Skill;
import io.nology.resources.temp.entity.Temp;

@Component
@Profile({ "dev", "test" })
public class JobFactory {

    private final Faker faker = new Faker();
    private final LocationFactory locationFactory;

    public JobFactory(LocationFactory locationFactory) {
        this.locationFactory = locationFactory;
    }

    public Job createJob(List<Skill> skills, Temp temp) {
        LocalDate start = LocalDate.now().plusDays(faker.number().numberBetween(1, 20));
        LocalDate end = start.plusDays(faker.number().numberBetween(1, 10));

        boolean isOnline = locationFactory.online();
        LocationFactory.CityData cityData = locationFactory.randomCity();

        Job job = new Job();
        job.setName(faker.job().title());
        job.setDescription(faker.lorem().sentence());
        job.setStartDate(start);
        job.setEndDate(end);
        job.setStatus(Job.JobStatus.INITIATED);
        job.setJobType(isOnline ? Job.JobType.ONLINE : Job.JobType.LOCATION);

        if (!isOnline) {
            job.setCity(cityData.city());
            job.setLatitude(cityData.lat());
            job.setLongitude(cityData.lng());
        }

        job.setRequiredSkills(skills.stream()
                .limit(1 + faker.random().nextInt(2))
                .toList());

        if (temp != null) {
            job.setTemp(temp);
            temp.getJobs().add(job);
            job.setStatus(Job.JobStatus.ASSIGNED);
        }

        return job;
    }

    public Job createJob(List<Skill> skills) {
        return createJob(skills, null);
    }
}