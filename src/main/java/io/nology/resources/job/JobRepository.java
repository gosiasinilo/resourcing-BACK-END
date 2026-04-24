package io.nology.resources.job;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.nology.resources.job.entity.Job;
import io.nology.resources.job.entity.Job.JobStatus;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    List<Job> findByStatus(JobStatus status);
}
