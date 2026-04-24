package io.nology.resources.jobreview;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import io.nology.resources.jobreview.entity.JobReview;

public interface JobReviewRepository extends JpaRepository<JobReview, Long> {
    List<JobReview> findByTempId(Long tempId);

    List<JobReview> findByJobId(Long jobId);
}