package io.nology.resources.job.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.nology.resources.common.exception.BadRequestException;
import io.nology.resources.common.exception.NotFoundException;
import io.nology.resources.common.serviceErrors.NotFoundError;
import io.nology.resources.common.serviceErrors.ValidationErrors;
import io.nology.resources.common.services.LocationService;
import io.nology.resources.common.validations.Validations;
import io.nology.resources.job.JobMapper;
import io.nology.resources.job.JobRepository;
import io.nology.resources.job.dto.CompleteJobReq;
import io.nology.resources.job.dto.CreateJobReq;
import io.nology.resources.job.dto.EditJobReq;
import io.nology.resources.job.dto.JobResponse;
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

        public JobService(
                        JobRepository jobRepository,
                        JobMapper jobMapper,
                        JobRules jobRules,
                        TempAvailabilityService tempAvailability,
                        JobAssigning jobAssigning,
                        LocationService locationService,
                        SkillRepository skillRepository,
                        JobReviewRepository jobReviewRepository) {
                this.jobRepository = jobRepository;
                this.jobMapper = jobMapper;
                this.jobRules = jobRules;
                this.tempAvailability = tempAvailability;
                this.jobAssigning = jobAssigning;
                this.locationService = locationService;
                this.skillRepository = skillRepository;
                this.jobReviewRepository = jobReviewRepository;
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

        public List<JobResponse> getAllJobs(Optional<Boolean> assigned) {
                List<Job> jobs = jobRepository.findAll();

                if (assigned.isPresent()) {
                        boolean isAssigned = assigned.get();
                        jobs = jobs.stream()
                                        .filter(j -> isAssigned ? j.getTemp() != null : j.getTemp() == null)
                                        .toList();
                }

                return jobs.stream().map(jobMapper::toResponse).toList();
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
                LocalDate newEnd = req.endDate() != null ? req.endDate() : job.getEndDate();

                if (!jobRules.dateRangeValid(newStart, newEnd)) {
                        err.addError("endDate", "End date must be after start date");
                }

                Temp temp = job.getTemp();
                if (temp != null && !tempAvailability.isTempAvailable(temp, newStart, newEnd)) {
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

                if (req.name() != null)
                        job.setName(req.name());
                if (req.description() != null)
                        job.setDescription(req.description());
                if (req.jobType() != null)
                        job.setJobType(req.jobType());
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

                if (job.getStatus() != Job.JobStatus.ASSIGNED) {
                        err.addError("status", "Only assigned jobs can be marked as complete");
                }

                if (job.getStartDate().isAfter(LocalDate.now())) {
                        err.addError("startDate", "Job cannot be completed before it has started");
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

                // update temp rating — average across all their reviews
                Temp temp = job.getTemp();
                double avg = temp.getReviews().stream()
                                .mapToInt(r -> (r.getWorkQuality() + r.getCommunication() + r.getOnTime()) / 3)
                                .average()
                                .orElse(0);
                temp.setRating(new java.math.BigDecimal(avg).setScale(2, java.math.RoundingMode.HALF_UP));

                job.setStatus(Job.JobStatus.COMPLETED);

                return jobMapper.toResponse(jobRepository.save(job));
        }

        private double[] resolveCoordinates(String city, ValidationErrors err) {
                Optional<double[]> coords = locationService.getCoordinates(city);
                if (coords.isEmpty()) {
                        err.addError("city", "City not found: " + city);
                        return new double[] { 0, 0 };
                }
                return coords.get();
        }
}