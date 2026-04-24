package io.nology.resources.job;

import java.time.LocalDate;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.nology.resources.job.entity.Job;
import io.nology.resources.job.entity.Job.JobStatus;

@Service
public class JobStatusScheduler {

    private final JobRepository jobRepo;

    public JobStatusScheduler(JobRepository jobRepo) {
        this.jobRepo = jobRepo;
    }

    @Scheduled(cron = "0 0 1 * * *") // runs at 1am every day
    @Transactional
    public void updateJobStatuses() {
        LocalDate today = LocalDate.now();

        // ASSIGNED -> ACTIVE: job has started but not yet ended
        List<Job> toActivate = jobRepo.findByStatus(JobStatus.ASSIGNED)
                .stream()
                .filter(job -> !job.getStartDate().isAfter(today)
                        && !job.getEndDate().isBefore(today))
                .toList();

        toActivate.forEach(job -> job.setStatus(JobStatus.ACTIVE));
        jobRepo.saveAll(toActivate);

        // ACTIVE -> COMPLETED: end date has passed
        List<Job> toComplete = jobRepo.findByStatus(JobStatus.ACTIVE)
                .stream()
                .filter(job -> job.getEndDate().isBefore(today))
                .toList();

        toComplete.forEach(job -> job.setStatus(JobStatus.COMPLETED));
        jobRepo.saveAll(toComplete);

        System.out.println("Job status update: " +
                toActivate.size() + " activated, " +
                toComplete.size() + " completed.");
    }
}