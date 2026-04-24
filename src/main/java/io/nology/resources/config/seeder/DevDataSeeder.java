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
import io.nology.resources.jobreview.JobReviewRepository;
import io.nology.resources.jobreview.entity.JobReview;
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
    private final JobReviewRepository reviewRepo;
    private final Random random = new Random();

    private static final String[] REVIEWERS = {
            "Alice Manager", "Bob Supervisor", "Carol HR", "Dave Coordinator"
    };

    private static final String[] POSITIVE_COMMENTS = {
            "Excellent work, very professional.",
            "Great attitude, would hire again.",
            "Completed all tasks on time and to a high standard.",
            "Very reliable and easy to work with."
    };

    private static final String[] MIXED_COMMENTS = {
            "Good work overall, some delays but acceptable.",
            "Decent performance, room for improvement on communication.",
            "Arrived on time, quality could be better.",
            "Generally good but needed supervision."
    };

    public DevDataSeeder(
            TempFactory tempFactory,
            JobFactory jobFactory,
            SkillFactory skillFactory,
            TempRepository tempRepo,
            JobRepository jobRepo,
            SkillRepository skillRepo,
            JobReviewRepository reviewRepo) {
        this.tempFactory = tempFactory;
        this.jobFactory = jobFactory;
        this.skillFactory = skillFactory;
        this.tempRepo = tempRepo;
        this.jobRepo = jobRepo;
        this.skillRepo = skillRepo;
        this.reviewRepo = reviewRepo;
    }

    @Bean
    public CommandLineRunner seedData() {
        return args -> {
            reviewRepo.deleteAll();
            jobRepo.deleteAll();
            tempRepo.deleteAll();
            skillRepo.deleteAll();

            System.out.println("Seeding...");

            List<Skill> skills = skillRepo.saveAll(skillFactory.createSkills(10));
            List<Temp> allTemps = new ArrayList<>();

            int tempCount = 6 + random.nextInt(4);
            for (int i = 0; i < tempCount; i++) {
                Temp temp = tempFactory.createTemp(skills);
                tempRepo.save(temp);
                allTemps.add(temp);

                Job completedJob = jobFactory.createJob(skills, temp);
                LocalDate completedStart = LocalDate.now().minusDays(20 + random.nextInt(10));
                completedJob.setStartDate(completedStart);
                completedJob.setEndDate(completedStart.plusDays(3 + random.nextInt(5)));
                completedJob.setStatus(Job.JobStatus.COMPLETED);
                jobRepo.save(completedJob);
                seedReview(completedJob, temp);

                Job activeJob = jobFactory.createJob(skills, temp);
                LocalDate activeStart = LocalDate.now().minusDays(1 + random.nextInt(3));
                activeJob.setStartDate(activeStart);
                activeJob.setEndDate(LocalDate.now().plusDays(1 + random.nextInt(5)));
                activeJob.setStatus(Job.JobStatus.ACTIVE);
                jobRepo.save(activeJob);

                if (random.nextBoolean()) {
                    Job assignedJob = jobFactory.createJob(skills, temp);
                    LocalDate assignedStart = LocalDate.now().plusDays(2 + random.nextInt(10));
                    assignedJob.setStartDate(assignedStart);
                    assignedJob.setEndDate(assignedStart.plusDays(2 + random.nextInt(7)));
                    assignedJob.setStatus(Job.JobStatus.ASSIGNED);
                    jobRepo.save(assignedJob);
                }

                tempRepo.save(temp);
            }

            for (int i = 0; i < 5; i++) {
                jobRepo.save(jobFactory.createJob(skills));
            }

            System.out.println("Seeding completed: " + allTemps.size() + " temps");
            System.out.println("Total jobs: " + jobRepo.count());
            System.out.println("Total reviews: " + reviewRepo.count());
        };
    }

    private void seedReview(Job job, Temp temp) {
        JobReview review = new JobReview();
        review.setJob(job);
        review.setTemp(temp);
        review.setWorkQuality(3 + random.nextInt(3));
        review.setCommunication(3 + random.nextInt(3));
        review.setOnTime(3 + random.nextInt(3));
        review.setReviewedBy(REVIEWERS[random.nextInt(REVIEWERS.length)]);

        boolean positive = random.nextBoolean();
        review.setComments(positive
                ? POSITIVE_COMMENTS[random.nextInt(POSITIVE_COMMENTS.length)]
                : MIXED_COMMENTS[random.nextInt(MIXED_COMMENTS.length)]);

        reviewRepo.save(review);
    }
}