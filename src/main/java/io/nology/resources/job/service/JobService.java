package io.nology.resources.job.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.nology.resources.common.exception.BadRequestException;
import io.nology.resources.common.exception.NotFoundException;
import io.nology.resources.common.serviceErrors.NotFoundError;
import io.nology.resources.common.serviceErrors.ValidationErrors;
import io.nology.resources.common.services.AnthropicService;
import io.nology.resources.common.services.LocationService;
import io.nology.resources.common.validations.Validations;
import io.nology.resources.job.JobMapper;
import io.nology.resources.job.JobRepository;
import io.nology.resources.job.dto.CompleteJobReq;
import io.nology.resources.job.dto.CreateJobReq;
import io.nology.resources.job.dto.EditJobReq;
import io.nology.resources.job.dto.JobResponse;
import io.nology.resources.job.dto.TempRecommendationResponse;
import io.nology.resources.job.entity.Job;
import io.nology.resources.jobreview.JobReviewRepository;
import io.nology.resources.jobreview.entity.JobReview;
import io.nology.resources.skill.SkillRepository;
import io.nology.resources.temp.entity.Temp;

@Service
public class JobService {

    private final JobRepository jobRepository;
    private final JobMapper jobMapper;
    private final JobRules jobRules;
    private final TempAvailabilityService tempAvailability;
    private final JobAssigning jobAssigning;
    private final LocationService locationService;
    private final SkillRepository skillRepository;
    private final JobReviewRepository jobReviewRepository;
    private final AnthropicService anthropicService;
    private final io.nology.resources.temp.TempRepository tempRepository;

    public JobService(
            JobRepository jobRepository,
            JobMapper jobMapper,
            JobRules jobRules,
            TempAvailabilityService tempAvailability,
            JobAssigning jobAssigning,
            LocationService locationService,
            SkillRepository skillRepository,
            JobReviewRepository jobReviewRepository,
            AnthropicService anthropicService,
            io.nology.resources.temp.TempRepository tempRepository) {
        this.jobRepository = jobRepository;
        this.jobMapper = jobMapper;
        this.jobRules = jobRules;
        this.tempAvailability = tempAvailability;
        this.jobAssigning = jobAssigning;
        this.locationService = locationService;
        this.skillRepository = skillRepository;
        this.jobReviewRepository = jobReviewRepository;
        this.anthropicService = anthropicService;
        this.tempRepository = tempRepository;
    }

    @Transactional
    public JobResponse createJob(CreateJobReq request) {
        ValidationErrors err = new ValidationErrors();

        if (!jobRules.dateRangeValid(request.startDate(), request.endDate())) {
            err.addError("endDate", "End date must be after start date");
        }

        if (request.jobType() == Job.JobType.LOCATION) {
            if (request.city() == null || request.city().isBlank()) {
                err.addError("city", "City is required for location-based jobs");
            }
        }

        if (!err.getErrors().isEmpty()) {
            throw BadRequestException.from(err);
        }

        Job job = new Job();
        job.setName(request.name());
        job.setDescription(request.description());
        job.setJobType(request.jobType());
        job.setStartDate(request.startDate());
        job.setEndDate(request.endDate());
        job.setStatus(Job.JobStatus.INITIATED);

        if (request.city() != null && !request.city().isBlank()) {
            double[] coords = resolveCoordinates(request.city(), err);
            if (!err.getErrors().isEmpty()) {
                throw BadRequestException.from(err);
            }
            job.setCity(request.city());
            job.setLatitude(coords[0]);
            job.setLongitude(coords[1]);
        }

        if (request.requiredSkillIds() != null && !request.requiredSkillIds().isEmpty()) {
            job.setRequiredSkills(skillRepository.findAllById(request.requiredSkillIds()));
        }

        return jobMapper.toResponse(jobRepository.save(job));
    }

    public Page<JobResponse> getAllJobs(
            Optional<Boolean> assigned,
            Optional<Job.JobStatus> status,
            boolean overdue,
            Job.JobType jobType,
            String sort,
            int page,
            int size) {

        List<Job> jobs = jobRepository.findAll();

        if (assigned.isPresent()) {
            boolean isAssigned = assigned.get();
            jobs = jobs.stream()
                    .filter(j -> isAssigned ? j.getTemp() != null : j.getTemp() == null)
                    .toList();
        }
        if (status.isPresent()) {
            jobs = jobs.stream().filter(j -> j.getStatus() == status.get()).toList();
        }
        if (overdue) {
            LocalDate today = LocalDate.now();
            jobs = jobs.stream()
                    .filter(j -> j.getEndDate().isBefore(today) &&
                            (j.getStatus() == Job.JobStatus.INITIATED ||
                             j.getStatus() == Job.JobStatus.ASSIGNED ||
                             j.getStatus() == Job.JobStatus.IN_PROGRESS))
                    .toList();
        }
        if (jobType != null) {
            jobs = jobs.stream().filter(j -> j.getJobType() == jobType).toList();
        }

        java.util.Comparator<Job> comparator = switch (sort != null ? sort : "date-desc") {
            case "az"       -> java.util.Comparator.comparing(Job::getName, String.CASE_INSENSITIVE_ORDER);
            case "za"       -> java.util.Comparator.comparing(Job::getName, String.CASE_INSENSITIVE_ORDER).reversed();
            case "date-asc" -> java.util.Comparator.comparing(Job::getStartDate);
            case "location" -> java.util.Comparator.comparing(
                    (Job j) -> j.getCity() != null ? j.getCity() : "￿",
                    String.CASE_INSENSITIVE_ORDER);
            default         -> java.util.Comparator.comparing(Job::getStartDate).reversed();
        };

        List<JobResponse> responses = jobs.stream()
                .sorted(comparator)
                .map(jobMapper::toResponse)
                .toList();

        Pageable pageable = PageRequest.of(page, size);
        int start = (int) pageable.getOffset();
        int end = Math.min(start + size, responses.size());
        List<JobResponse> pageContent = start > responses.size() ? List.of() : responses.subList(start, end);

        return new PageImpl<>(pageContent, pageable, responses.size());
    }

    public JobResponse getJobById(Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(new NotFoundError("Job", id)));
        return jobMapper.toResponse(job);
    }

    public JobResponse assignTemp(Long jobId, Long tempId) {
        return jobAssigning.assignTemp(jobId, tempId);
    }

    public JobResponse unassignTemp(Long jobId) {
        return jobAssigning.unassignTemp(jobId);
    }

    @Transactional
    public JobResponse closeJob(Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(new NotFoundError("Job", id)));

        ValidationErrors err = new ValidationErrors();

        if (job.getStatus() != Job.JobStatus.INITIATED) {
            err.addError("status", "Only unassigned jobs can be closed without a review");
        }
        if (!job.getEndDate().isBefore(LocalDate.now())) {
            err.addError("endDate", "Job end date has not passed yet");
        }
        if (!err.getErrors().isEmpty()) {
            throw BadRequestException.from(err);
        }

        job.setStatus(Job.JobStatus.COMPLETED);
        return jobMapper.toResponse(jobRepository.save(job));
    }

    @Transactional
    public void deleteJob(Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(new NotFoundError("Job", id)));

        if (job.getTemp() != null) {
            job.getTemp().getJobs().remove(job);
            job.setTemp(null);
        }

        jobRepository.delete(job);
    }

    @Transactional
    public JobResponse editJob(Long id, EditJobReq req) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(new NotFoundError("Job", id)));

        ValidationErrors err = new ValidationErrors();

        if (req.name() != null) {
            Validations.validateMinLength("name", req.name(), 4, err);
        }

        if (req.startDate() != null) {
            Validations.validateDateNotInThePast("startDate", req.startDate(), err);
        }

        if (req.endDate() != null) {
            Validations.validateDateNotInThePast("endDate", req.endDate(), err);
        }

        LocalDate newStart = req.startDate() != null ? req.startDate() : job.getStartDate();
        LocalDate newEnd   = req.endDate()   != null ? req.endDate()   : job.getEndDate();

        if (!jobRules.dateRangeValid(newStart, newEnd)) {
            err.addError("endDate", "End date must be after start date");
        }

        Temp temp = job.getTemp();
        if (temp != null && !tempAvailability.isTempAvailableExcluding(temp, newStart, newEnd, job.getId())) {
            err.addError("temp", "Assigned temp is not available for updated dates");
        }

        if (req.city() != null && !req.city().equals(job.getCity())) {
            double[] coords = resolveCoordinates(req.city(), err);
            if (err.getErrors().isEmpty()) {
                job.setCity(req.city());
                job.setLatitude(coords[0]);
                job.setLongitude(coords[1]);
            }
        }

        if (!err.getErrors().isEmpty()) {
            throw BadRequestException.from(err);
        }

        if (req.name() != null) job.setName(req.name());
        if (req.description() != null) job.setDescription(req.description());
        if (req.jobType() != null) job.setJobType(req.jobType());
        if (req.requiredSkillIds() != null) {
            job.setRequiredSkills(skillRepository.findAllById(req.requiredSkillIds()));
        }

        job.setStartDate(newStart);
        job.setEndDate(newEnd);

        return jobMapper.toResponse(jobRepository.save(job));
    }

    @Transactional
    public JobResponse completeJob(Long id, CompleteJobReq req, Authentication authentication) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(new NotFoundError("Job", id)));

        ValidationErrors err = new ValidationErrors();

        if (job.getStatus() != Job.JobStatus.ASSIGNED
                && job.getStatus() != Job.JobStatus.IN_PROGRESS) {
            err.addError("status", "Only assigned or in-progress jobs can be marked as complete");
        }

        if (job.getStartDate().isAfter(LocalDate.now())) {
            err.addError("startDate", "Job has not started yet");
        }

        if (!err.getErrors().isEmpty()) {
            throw BadRequestException.from(err);
        }

        JobReview review = new JobReview();
        review.setJob(job);
        review.setTemp(job.getTemp());
        review.setWorkQuality(req.workQuality());
        review.setCommunication(req.communication());
        review.setOnTime(req.onTime());
        review.setComments(req.comments());
        review.setReviewedBy(authentication.getName());
        jobReviewRepository.save(review);

        Temp temp = job.getTemp();
        List<io.nology.resources.jobreview.entity.JobReview> allReviews =
                jobReviewRepository.findByTempId(temp.getId());
        double avg = allReviews.stream()
                .mapToDouble(r -> (r.getWorkQuality() + r.getCommunication() + r.getOnTime()) / 3.0)
                .average()
                .orElse(0);
        temp.setRating(new java.math.BigDecimal(avg)
                .setScale(2, java.math.RoundingMode.HALF_UP));

        job.setStatus(Job.JobStatus.COMPLETED);

        return jobMapper.toResponse(jobRepository.save(job));
    }

    public TempRecommendationResponse recommendTemp(Long jobId) {
        if (!anthropicService.isConfigured()) {
            throw BadRequestException.from(new ValidationErrors() {{
                addError("api", "Anthropic API key not configured");
            }});
        }

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new NotFoundException(new NotFoundError("Job", jobId)));

        List<String> requiredSkills = job.getRequiredSkills().stream()
                .map(s -> s.getName()).toList();

        List<Temp> allTemps = tempRepository.findAll();
        var available = allTemps.stream()
                .filter(t -> t.getJobs().stream().noneMatch(j ->
                        !(j.getEndDate().isBefore(job.getStartDate()) || j.getStartDate().isAfter(job.getEndDate()))))
                .toList();

        if (available.isEmpty()) {
            throw BadRequestException.from(new ValidationErrors() {{
                addError("temps", "No available temps to recommend");
            }});
        }

        var sb = new StringBuilder();
        sb.append("You are a staffing assistant. Pick the single best temp for this job.\n\n");
        sb.append("Job: ").append(job.getName()).append("\n");
        sb.append("Dates: ").append(job.getStartDate()).append(" to ").append(job.getEndDate()).append("\n");
        sb.append("Location: ").append(job.getCity() != null ? job.getCity() : "Online").append("\n");
        sb.append("Required skills: ").append(requiredSkills.isEmpty() ? "None" : String.join(", ", requiredSkills)).append("\n\n");
        sb.append("Available temps:\n");

        for (Temp t : available) {
            List<String> tempSkills = t.getSkills().stream().map(s -> s.getName()).toList();
            long matched = requiredSkills.stream().filter(tempSkills::contains).count();
            sb.append("- ID ").append(t.getId())
              .append(": ").append(t.getFirstName()).append(" ").append(t.getLastName())
              .append(" | City: ").append(t.getCity())
              .append(" | Rating: ").append(t.getRating() != null ? t.getRating() : "none")
              .append(" | Skills: ").append(String.join(", ", tempSkills))
              .append(" | Matching: ").append(matched).append("/").append(requiredSkills.size())
              .append("\n");
        }

        sb.append("\nRespond with JSON only, no markdown: {\"tempId\": <number>, \"reasoning\": \"<1-2 sentences>\"}");

        var rec = anthropicService.recommendTemp(sb.toString());

        return available.stream()
                .filter(t -> t.getId().equals(rec.tempId()))
                .findFirst()
                .map(t -> new TempRecommendationResponse(t.getId(), t.getFirstName(), t.getLastName(), rec.reasoning()))
                .orElseThrow(() -> new RuntimeException("Recommended temp not found in available list"));
    }

    private double[] resolveCoordinates(String city, ValidationErrors err) {
        Optional<double[]> coords = locationService.getCoordinates(city);
        if (coords.isEmpty()) {
            err.addError("city", "City not found: " + city);
            return new double[]{0, 0};
        }
        return coords.get();
    }
}
