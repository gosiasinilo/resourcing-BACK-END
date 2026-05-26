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

    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void updateJobStatuses() {
        LocalDate today = LocalDate.now();

        List<Job> toStart = jobRepo.findByStatus(JobStatus.ASSIGNED)
                .stream()
                .filter(job -> !job.getStartDate().isAfter(today))
                .toList();
        toStart.forEach(job -> job.setStatus(JobStatus.IN_PROGRESS));
        jobRepo.saveAll(toStart);

    }
}
