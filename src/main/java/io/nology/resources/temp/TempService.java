package io.nology.resources.temp;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import io.nology.resources.common.exception.BadRequestException;
import io.nology.resources.common.exception.NotFoundException;
import io.nology.resources.common.serviceErrors.NotFoundError;
import io.nology.resources.common.serviceErrors.ValidationErrors;
import io.nology.resources.job.JobRepository;
import io.nology.resources.job.entity.Job;
import io.nology.resources.temp.dto.AvailableTempInfo;
import io.nology.resources.temp.dto.CreateTempReq;
import io.nology.resources.temp.dto.TempResponse;
import io.nology.resources.temp.dto.TempResponseById;
import io.nology.resources.temp.entity.Temp;

@Service
public class TempService {

    private final TempRepository tempRepository;
    private final JobRepository jobRepository;
    private final TempMapper tempMapper;

    public TempService(TempRepository tempRepository, JobRepository jobRepository, TempMapper tempMapper) {
        this.tempRepository = tempRepository;
        this.jobRepository = jobRepository;
        this.tempMapper = tempMapper;
    }

    public List<TempResponse> getAllTemps() {
        return tempRepository.findAll()
                .stream()
                .map(tempMapper::toResponse)
                .toList();
    }

    public TempResponseById getTempById(Long id) {
        Temp temp = tempRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(new NotFoundError("Temp", id)));

        return tempMapper.toDetailResponse(temp);
    }

    public List<AvailableTempInfo> getAvailableTempsByDate(LocalDate startDate, LocalDate endDate) {

        if (!endDate.isAfter(startDate)) {
            ValidationErrors err = new ValidationErrors();
            err.addError("endDate", "End date must be after start date");
            throw BadRequestException.from(err);
        }

        List<Temp> allTemps = tempRepository.findAll();

        return allTemps.stream().map(temp -> {

            boolean isBusy = temp.getJobs().stream()
                    .anyMatch(j -> !(j.getEndDate().isBefore(startDate) || j.getStartDate().isAfter(endDate)));

            if (isBusy) {
                LocalDate nextAvailable = temp.getJobs().stream()
                        .map(Job::getEndDate)
                        .filter(d -> !d.isBefore(startDate))
                        .max(LocalDate::compareTo)
                        .map(d -> d.plusDays(1))
                        .orElse(startDate);

                List<String> alternatives = allTemps.stream()
                        .filter(t -> !t.getId().equals(temp.getId()))
                        .filter(t -> t.getJobs().stream()
                                .noneMatch(j -> !(j.getEndDate().isBefore(startDate)
                                        || j.getStartDate().isAfter(endDate))))
                        .map(t -> t.getFirstName() + " " + t.getLastName())
                        .toList();

                return new AvailableTempInfo(
                        temp.getId(),
                        temp.getFirstName(),
                        temp.getLastName(),
                        nextAvailable,
                        alternatives);
            }

            return new AvailableTempInfo(
                    temp.getId(),
                    temp.getFirstName(),
                    temp.getLastName(),
                    startDate,
                    List.of());

        }).toList();
    }

    public List<AvailableTempInfo> getAvailableTempsByJob(Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new NotFoundException(new NotFoundError("Job", jobId)));

        return getAvailableTempsByDate(job.getStartDate(), job.getEndDate());
    }

    public TempResponse createTemp(CreateTempReq request) {
        Temp temp = new Temp();
        temp.setFirstName(request.firstName());
        temp.setLastName(request.lastName());

        return tempMapper.toResponse(tempRepository.save(temp));
    }
}