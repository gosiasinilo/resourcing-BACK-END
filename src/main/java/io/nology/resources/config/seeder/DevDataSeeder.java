package io.nology.resources.config.seeder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import io.nology.resources.config.factory.JobFactory;
import io.nology.resources.config.factory.TempFactory;
import io.nology.resources.job.JobRepository;
import io.nology.resources.job.entity.Job;
import io.nology.resources.temp.TempRepository;
import io.nology.resources.temp.entity.Temp;

@Component
@Profile({ "dev", "test" })
public class DevDataSeeder {

    private final TempFactory tempFactory;
    private final JobFactory jobFactory;
    private final TempRepository tempRepo;
    private final JobRepository jobRepo;
    private final Random random = new Random();

    public DevDataSeeder(TempFactory tempFactory, JobFactory jobFactory,
            TempRepository tempRepo, JobRepository jobRepo) {
        this.tempFactory = tempFactory;
        this.jobFactory = jobFactory;
        this.tempRepo = tempRepo;
        this.jobRepo = jobRepo;

    }

    @Bean
    public CommandLineRunner seedData() {
        return args -> {

            jobRepo.deleteAll();
            tempRepo.deleteAll();

            System.out.println("Seeding ...");

            List<Temp> allTemps = new ArrayList<>();

            int Temps = 5 + random.nextInt(10);
            for (int i = 0; i < Temps; i++) {
                Temp temp = tempFactory.createTemp();
                tempRepo.save(temp);
                allTemps.add(temp);

                int Jobs = random.nextInt(5);
                LocalDate lastEndDate = null;

                for (int j = 0; j < Jobs; j++) {
                    Job job = jobFactory.createJob(null);

                    if (lastEndDate != null) {
                        LocalDate start = lastEndDate.plusDays(1 + random.nextInt(5));
                        job.setStartDate(start);
                        job.setEndDate(start.plusDays(random.nextInt(7) + 1));
                    }

                    job.setTemp(temp);
                    temp.getJobs().add(job);
                    lastEndDate = job.getEndDate();

                    jobRepo.save(job);
                }

                tempRepo.save(temp);
            }

            for (int i = 0; i < 3; i++) {
                Job job = jobFactory.createJob(null);
                jobRepo.save(job);
            }

            System.out.println(
                    "Seeding completed: " + allTemps.size() + " temps with 0-3 jobs each, plus 3 unassigned jobs.");
            System.out.println("Total jobs created: " + jobRepo.count());

        };
    }
}