package io.nology.resources.job;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.nology.resources.job.dto.CompleteJobReq;
import io.nology.resources.job.dto.CreateJobReq;
import io.nology.resources.job.dto.EditJobReq;
import io.nology.resources.job.dto.JobResponse;
import io.nology.resources.job.service.JobService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public JobResponse createJob(@RequestBody @Valid CreateJobReq request) {
        return jobService.createJob(request);
    }

    @GetMapping
    public List<JobResponse> getJobs(@RequestParam Optional<Boolean> assigned) {
        return jobService.getAllJobs(assigned);
    }

    @GetMapping("/{id}")
    public JobResponse getJob(@PathVariable Long id) {
        return jobService.getJobById(id);
    }

    @PatchMapping("/{id}")
    public JobResponse editJob(@PathVariable Long id, @RequestBody EditJobReq request) {
        return jobService.editJob(id, request);
    }

    @PatchMapping("/{id}/assign")
    public JobResponse assignTemp(
            @PathVariable Long id,
            @RequestParam Long tempId) {
        return jobService.assignTemp(id, tempId);
    }

    @PatchMapping("/{id}/unassign")
    public JobResponse unassignTemp(@PathVariable Long id) {
        return jobService.unassignTemp(id);
    }

    @PostMapping("/{id}/complete")
    public JobResponse completeJob(
            @PathVariable Long id,
            @RequestBody @Valid CompleteJobReq request,
            Authentication authentication) {
        return jobService.completeJob(id, request, authentication);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteJob(@PathVariable Long id) {
        jobService.deleteJob(id);
    }
}