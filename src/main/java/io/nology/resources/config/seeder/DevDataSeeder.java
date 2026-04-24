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
import io.nology.resources.config.factory.SkillFactory;
import io.nology.resources.config.factory.TempFactory;
import io.nology.resources.job.JobRepository;
import io.nology.resources.job.entity.Job;
import io.nology.resources.skill.SkillRepository;
import io.nology.resources.skill.entity.Skill;
import io.nology.resources.temp.TempRepository;
import io.nology.resources.temp.entity.Temp;

@Component
@Profile("dev")
public class DevDataSeeder {

    private final TempFactory tempFactory;
    private final JobFactory jobFactory;
    private final SkillFactory skillFactory;
    private final TempRepository tempRepo;
    private final JobRepository jobRepo;
    private final SkillRepository skillRepo;
    private final Random random = new Random();

    public DevDataSeeder(
            TempFactory tempFactory,
            JobFactory jobFactory,
            SkillFactory skillFactory,
            TempRepository tempRepo,
            JobRepository jobRepo,
            SkillRepository skillRepo) {
        this.tempFactory = tempFactory;
        this.jobFactory = jobFactory;
        this.skillFactory = skillFactory;
        this.tempRepo = tempRepo;
        this.jobRepo = jobRepo;
        this.skillRepo = skillRepo;
    }

    @Bean
    public CommandLineRunner seedData() {
        return args -> {
            jobRepo.deleteAll();
            tempRepo.deleteAll();
            skillRepo.deleteAll();

            System.out.println("Seeding...");

            List<Skill> skills = skillRepo.saveAll(skillFactory.createSkills(10));

            List<Temp> allTemps = new ArrayList<>();

            int tempCount = 5 + random.nextInt(6);
            for (int i = 0; i < tempCount; i++) {
                Temp temp = tempFactory.createTemp(skills);
                tempRepo.save(temp);
                allTemps.add(temp);

                int jobCount = random.nextInt(4);
                LocalDate lastEndDate = null;

                for (int j = 0; j < jobCount; j++) {
                    Job job = jobFactory.createJob(skills, temp);

                    if (lastEndDate != null) {
                        LocalDate start = lastEndDate.plusDays(1 + random.nextInt(5));
                        job.setStartDate(start);
                        job.setEndDate(start.plusDays(random.nextInt(7) + 1));
                    }

                    lastEndDate = job.getEndDate();
                    jobRepo.save(job);
                }

                tempRepo.save(temp);
            }

            // a few unassigned jobs
            for (int i = 0; i < 4; i++) {
                jobRepo.save(jobFactory.createJob(skills));
            }

            System.out.println("Seeding completed: " + allTemps.size() + " temps");
            System.out.println("Total jobs: " + jobRepo.count());
        };
    }
}