package io.nology.resources.job;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.nology.resources.job.entity.Job;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

}
