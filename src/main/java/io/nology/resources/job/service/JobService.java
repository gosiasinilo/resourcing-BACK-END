package io.nology.resources.job.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.nology.resources.common.exception.BadRequestException;
import io.nology.resources.common.exception.NotFoundException;
import io.nology.resources.common.serviceErrors.NotFoundError;
import io.nology.resources.common.serviceErrors.ValidationErrors;
import io.nology.resources.common.validations.Validations;
import io.nology.resources.job.JobMapper;
import io.nology.resources.job.JobRepository;
import io.nology.resources.job.dto.CreateJobReq;
import io.nology.resources.job.dto.EditJobReq;
import io.nology.resources.job.dto.JobResponse;
import io.nology.resources.job.entity.Job;
import io.nology.resources.temp.entity.Temp;

@Service
public class JobService {

        private final JobRepository jobRepository;
        private final JobMapper jobMapper;
        private final JobRules jobRules;
        private final TempAvailabilityService tempAvailability;
        private final JobAssigning jobAssigning;

        public JobService(
                        JobRepository jobRepository,
                        JobMapper jobMapper,
                        JobRules jobRules,
                        TempAvailabilityService tempAvailability,
                        JobAssigning jobAssigning) {

                this.jobRepository = jobRepository;
                this.jobMapper = jobMapper;
                this.jobRules = jobRules;
                this.tempAvailability = tempAvailability;
                this.jobAssigning = jobAssigning;
        }

        public JobResponse createJob(CreateJobReq request) {

                ValidationErrors err = new ValidationErrors();

                if (!jobRules.dateRangeValid(request.startDate(), request.endDate())) {
                        err.addError("endDate", "End date must be after start date");
                }

                if (!err.getErrors().isEmpty()) {
                        throw io.nology.resources.common.exception.BadRequestException.from(err);
                }

                Job job = new Job();
                job.setName(request.name());
                job.setStartDate(request.startDate());
                job.setEndDate(request.endDate());

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

        public void deleteJob(Long id) {
                Job job = jobRepository.findById(id)
                                .orElseThrow(() -> new NotFoundException(new NotFoundError("Job", id)));

                jobAssigning.unassignTemp(id);
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

                if (!err.getErrors().isEmpty()) {
                        throw BadRequestException.from(err);
                }

                if (req.name() != null) {
                        job.setName(req.name());
                }

                job.setStartDate(newStart);
                job.setEndDate(newEnd);

                return jobMapper.toResponse(jobRepository.save(job));
        }
}