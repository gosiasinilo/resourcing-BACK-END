package io.nology.resources.config.seeder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
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

/**
 * Populates demo data.
 *
 * <ul>
 *   <li><b>dev</b> profile: wipes and reseeds on every startup.</li>
 *   <li><b>prod</b> profile: seeds only when {@code app.seed-demo-data=true}
 *       AND the database is empty. Never deletes anything.</li>
 * </ul>
 */
@Component
@Profile({ "dev", "prod" })
public class DataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final TempFactory tempFactory;
    private final JobFactory jobFactory;
    private final SkillFactory skillFactory;
    private final TempRepository tempRepo;
    private final JobRepository jobRepo;
    private final SkillRepository skillRepo;
    private final JobReviewRepository reviewRepo;
    private final Environment environment;
    private final Random random = new Random();

    @Value("${app.seed-demo-data:false}")
    private boolean seedDemoData;

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

    public DataSeeder(
            TempFactory tempFactory,
            JobFactory jobFactory,
            SkillFactory skillFactory,
            TempRepository tempRepo,
            JobRepository jobRepo,
            SkillRepository skillRepo,
            JobReviewRepository reviewRepo,
            Environment environment) {
        this.tempFactory = tempFactory;
        this.jobFactory = jobFactory;
        this.skillFactory = skillFactory;
        this.tempRepo = tempRepo;
        this.jobRepo = jobRepo;
        this.skillRepo = skillRepo;
        this.reviewRepo = reviewRepo;
        this.environment = environment;
    }

    @Bean
    public CommandLineRunner seedData() {
        return args -> {
            boolean prod = environment.matchesProfiles("prod");

            if (prod) {
                if (!seedDemoData) {
                    log.info("Demo seeding disabled (set app.seed-demo-data=true to seed an empty DB). Skipping.");
                    return;
                }
                if (skillRepo.count() > 0) {
                    log.info("Database already contains data ({} skills) — skipping demo seed.", skillRepo.count());
                    return;
                }
                log.info("Seeding demo data into an empty database...");
            } else {
                log.info("Dev profile — resetting and reseeding the database...");
                reviewRepo.deleteAll();
                jobRepo.deleteAll();
                tempRepo.deleteAll();
                skillRepo.deleteAll();
            }

            seed();
        };
    }

    private void seed() {
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
            activeJob.setStatus(Job.JobStatus.IN_PROGRESS);
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

        // Overdue jobs — past end date, still active
        Job[] overdueJobs = {
            jobFactory.createJob(skills),
            jobFactory.createJob(skills, allTemps.get(0)),
            jobFactory.createJob(skills, allTemps.get(1)),
        };
        Job.JobStatus[] overdueStatuses = {
            Job.JobStatus.INITIATED,
            Job.JobStatus.IN_PROGRESS,
            Job.JobStatus.IN_PROGRESS,
        };
        for (int i = 0; i < overdueJobs.length; i++) {
            LocalDate overdueStart = LocalDate.now().minusDays(15 + random.nextInt(10));
            overdueJobs[i].setStartDate(overdueStart);
            overdueJobs[i].setEndDate(overdueStart.plusDays(3 + random.nextInt(5)));
            overdueJobs[i].setStatus(overdueStatuses[i]);
            jobRepo.save(overdueJobs[i]);
        }

        log.info("Seeding complete: {} temps, {} jobs, {} reviews",
                allTemps.size(), jobRepo.count(), reviewRepo.count());
    }

    private void seedReview(Job job, Temp temp) {
        JobReview review = new JobReview();
        review.setJob(job);
        review.setTemp(temp);
        int wq = 3 + random.nextInt(3);
        int comm = 3 + random.nextInt(3);
        int onTime = 3 + random.nextInt(3);
        review.setWorkQuality(wq);
        review.setCommunication(comm);
        review.setOnTime(onTime);
        review.setReviewedBy(REVIEWERS[random.nextInt(REVIEWERS.length)]);

        boolean positive = random.nextBoolean();
        review.setComments(positive
                ? POSITIVE_COMMENTS[random.nextInt(POSITIVE_COMMENTS.length)]
                : MIXED_COMMENTS[random.nextInt(MIXED_COMMENTS.length)]);

        reviewRepo.save(review);

        double avg = (wq + comm + onTime) / 3.0;
        temp.setRating(new BigDecimal(avg).setScale(2, RoundingMode.HALF_UP));
    }
}
