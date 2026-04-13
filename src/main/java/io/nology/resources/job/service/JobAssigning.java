package io.nology.resources.job.service;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.nology.resources.common.exception.BadRequestException;
import io.nology.resources.common.exception.NotFoundException;
import io.nology.resources.common.serviceErrors.NotFoundError;
import io.nology.resources.job.JobMapper;
import io.nology.resources.job.JobRepository;
import io.nology.resources.job.dto.JobResponse;
import io.nology.resources.job.entity.Job;
import io.nology.resources.temp.TempAssigning;
import io.nology.resources.temp.TempRepository;
import io.nology.resources.temp.entity.Temp;

@Service
public class JobAssigning {

        private final JobRepository jobRepository;
        private final TempRepository tempRepository;
        private final JobMapper jobMapper;
        private final TempAvailabilityService tempAvailability;
        private final TempAssigning tempAssigning;

        public JobAssigning(
                        JobRepository jobRepository,
                        TempRepository tempRepository,
                        JobMapper jobMapper,
                        TempAvailabilityService tempAvailability,
                        TempAssigning tempAssigning) {
                this.jobRepository = jobRepository;
                this.tempRepository = tempRepository;
                this.jobMapper = jobMapper;
                this.tempAvailability = tempAvailability;
                this.tempAssigning = tempAssigning;
        }

        @Transactional
        public JobResponse assignTemp(Long jobId, Long tempId) {

                Job job = jobRepository.findById(jobId)
                                .orElseThrow(() -> new NotFoundException(
                                                new NotFoundError("Job", jobId)));

                Temp temp = tempRepository.findById(tempId)
                                .orElseThrow(() -> new NotFoundException(
                                                new NotFoundError("Temp", tempId)));

                if (!tempAvailability.isTempAvailable(
                                temp, job.getStartDate(), job.getEndDate())) {

                        List<Temp> allTemps = tempRepository.findAll();

                        throw new BadRequestException(
                                        "Temp not available",
                                        HttpStatus.BAD_REQUEST,
                                        "TEMP_BUSY",
                                        Map.of(
                                                        "nextAvailableDate",
                                                        List.of(tempAvailability
                                                                        .getNextAvailableDate(
                                                                                        temp, job.getStartDate())
                                                                        .toString()),
                                                        "availableTemps",
                                                        tempAvailability.getAlternativeTemps(
                                                                        allTemps,
                                                                        job.getStartDate(),
                                                                        job.getEndDate(),
                                                                        tempId)));
                }

                job.setTemp(temp);
                temp.getJobs().add(job);

                return jobMapper.toResponse(jobRepository.save(job));
        }

        @Transactional
        public JobResponse unassignTemp(Long jobId) {

                Job job = jobRepository.findById(jobId)
                                .orElseThrow(() -> new NotFoundException(
                                                new NotFoundError("Job", jobId)));

                tempAssigning.removeTempFromJob(job);

                return jobMapper.toResponse(jobRepository.save(job));
        }
}
