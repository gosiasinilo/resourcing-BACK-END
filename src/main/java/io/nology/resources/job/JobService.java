package io.nology.resources.job;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.nology.resources.common.exception.BadRequestException;
import io.nology.resources.common.exception.NotFoundException;
import io.nology.resources.common.serviceErrors.NotFoundError;
import io.nology.resources.common.serviceErrors.ValidationErrors;
import io.nology.resources.job.dto.CreateJobReq;
import io.nology.resources.job.dto.JobResponse;
import io.nology.resources.job.entity.Job;
import io.nology.resources.temp.TempRepository;
import io.nology.resources.temp.dto.TempResponse;
import io.nology.resources.temp.entity.Temp;

@Service
public class JobService {

        private final JobRepository jobRepository;
        private final TempRepository tempRepository;
        private final JobMapper jobMapper;

        public JobService(JobRepository jobRepository, TempRepository tempRepository, JobMapper jobMapper) {
                this.jobRepository = jobRepository;
                this.tempRepository = tempRepository;
                this.jobMapper = jobMapper;
        }

        public JobResponse createJob(CreateJobReq request) {

                if (!request.endDate().isAfter(request.startDate())) {
                        ValidationErrors err = new ValidationErrors();
                        err.addError("endDate", "End date must be after start date");
                        throw BadRequestException.from(err);
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

                return jobs.stream()
                                .map(jobMapper::toResponse)
                                .toList();
        }

        public JobResponse getJobById(Long id) {
                Job job = jobRepository.findById(id)
                                .orElseThrow(() -> new NotFoundException(new NotFoundError("Job", id)));

                return jobMapper.toResponse(job);
        }

        @Transactional
        public JobResponse assignTemp(Long jobId, Long tempId) {

                Job job = jobRepository.findById(jobId)
                                .orElseThrow(() -> new NotFoundException(new NotFoundError("Job", jobId)));

                Temp temp = tempRepository.findById(tempId)
                                .orElseThrow(() -> new NotFoundException(new NotFoundError("Temp", tempId)));

                boolean overlaps = temp.getJobs().stream()
                                .anyMatch(j -> !(j.getEndDate().isBefore(job.getStartDate())
                                                || j.getStartDate().isAfter(job.getEndDate())));

                if (overlaps) {

                        LocalDate nextAvailableDate = temp.getJobs().stream()
                                        .map(Job::getEndDate)
                                        .filter(d -> !d.isBefore(job.getStartDate()))
                                        .max(LocalDate::compareTo)
                                        .map(d -> d.plusDays(1))
                                        .orElse(job.getStartDate());

                        List<TempResponse> availableTempResponse = tempRepository.getAvailableTemps(
                                        job.getStartDate(), job.getEndDate())
                                        .stream()
                                        .map(t -> new TempResponse(t.getId(), t.getFirstName(), t.getLastName(),
                                                        t.getEmail()))
                                        .toList();

                        throw new BadRequestException(
                                        "Requested temp is busy",
                                        HttpStatus.BAD_REQUEST,
                                        "TEMP_BUSY",
                                        Map.of(
                                                        "nextAvailableDate", List.of(nextAvailableDate.toString()),
                                                        "availableTemps", availableTempResponse.stream()
                                                                        .map(t -> t.id() + " - " + t.firstName() + " "
                                                                                        + t.lastName())
                                                                        .toList()));
                }

                job.setTemp(temp);
                temp.getJobs().add(job);

                return jobMapper.toResponse(jobRepository.save(job));
        }

        public JobResponse unassignTemp(Long jobId) {
                Job job = jobRepository.findById(jobId)
                                .orElseThrow(() -> new NotFoundException(new NotFoundError("Job", jobId)));

                Temp temp = job.getTemp();

                if (temp != null) {
                        temp.getJobs().remove(job);
                }

                job.setTemp(null);

                return jobMapper.toResponse(jobRepository.save(job));
        }

        @Transactional
        public void deleteJob(Long id) {
                Job job = jobRepository.findById(id)
                                .orElseThrow(() -> new NotFoundException(new NotFoundError("Job", id)));

                if (job.getTemp() != null) {
                        unassignTemp(id);
                }

                jobRepository.delete(job);
        }
}