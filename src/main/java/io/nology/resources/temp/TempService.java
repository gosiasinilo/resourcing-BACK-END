package io.nology.resources.temp;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.nology.resources.common.exception.BadRequestException;
import io.nology.resources.common.exception.NotFoundException;
import io.nology.resources.common.serviceErrors.NotFoundError;
import io.nology.resources.common.serviceErrors.ValidationErrors;
import io.nology.resources.common.services.LocationService;
import io.nology.resources.common.validations.Validations;
import io.nology.resources.job.JobRepository;
import io.nology.resources.job.entity.Job;
import io.nology.resources.temp.dto.AvailableTempInfo;
import io.nology.resources.temp.dto.CreateTempReq;
import io.nology.resources.temp.dto.EditTempReq;
import io.nology.resources.temp.dto.TempResponse;
import io.nology.resources.temp.dto.TempResponseById;
import io.nology.resources.temp.entity.Temp;

@Service
public class TempService {

    private final TempRepository tempRepository;
    private final JobRepository jobRepository;
    private final TempMapper tempMapper;
    private final TempAssigning tempAssigning;
    private final LocationService locationService;

    public TempService(
            TempRepository tempRepository,
            JobRepository jobRepository,
            TempMapper tempMapper,
            TempAssigning tempAssigning,
            LocationService locationService) {
        this.tempRepository = tempRepository;
        this.jobRepository = jobRepository;
        this.tempMapper = tempMapper;
        this.tempAssigning = tempAssigning;
        this.locationService = locationService;
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

    public TempResponse createTemp(CreateTempReq request) {
        ValidationErrors err = new ValidationErrors();

        double[] coords = resolveCoordinates(request.city(), err);

        if (!err.getErrors().isEmpty()) {
            throw BadRequestException.from(err);
        }

        Temp temp = new Temp();
        temp.setFirstName(request.firstName());
        temp.setLastName(request.lastName());
        temp.setEmail(request.email());
        temp.setCity(request.city());
        temp.setLatitude(coords[0]);
        temp.setLongitude(coords[1]);
        temp.setNotes(request.notes());

        return tempMapper.toResponse(tempRepository.save(temp));
    }

    @Transactional
    public void deleteTemp(Long id) {
        Temp temp = tempRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(new NotFoundError("Temp", id)));

        List<Job> jobs = new ArrayList<>(temp.getJobs());
        for (Job job : jobs) {
            job.setTemp(null);
        }
        temp.getJobs().clear();
        tempRepository.delete(temp);
    }

    @Transactional
    public TempResponse editTemp(Long id, EditTempReq req) {
        Temp temp = tempRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(new NotFoundError("Temp", id)));

        ValidationErrors err = new ValidationErrors();

        if (req.firstName() != null && !req.firstName().equals(temp.getFirstName())) {
            Validations.validateMinLength("firstName", req.firstName(), 3, err);
            temp.setFirstName(req.firstName());
        }

        if (req.lastName() != null && !req.lastName().equals(temp.getLastName())) {
            Validations.validateMinLength("lastName", req.lastName(), 3, err);
            temp.setLastName(req.lastName());
        }

        if (req.email() != null && !req.email().equals(temp.getEmail())) {
            Validations.validateEmail("email", req.email(), err);
        }

        if (req.city() != null && !req.city().equals(temp.getCity())) {
            double[] coords = resolveCoordinates(req.city(), err);
            if (err.getErrors().isEmpty()) {
                temp.setCity(req.city());
                temp.setLatitude(coords[0]);
                temp.setLongitude(coords[1]);
            }
        }

        if (!err.getErrors().isEmpty()) {
            throw BadRequestException.from(err);
        }

        if (req.email() != null && !req.email().equals(temp.getEmail())) {
            temp.setEmail(req.email());
        }

        if (req.notes() != null) {
            temp.setNotes(req.notes());
        }

        return tempMapper.toResponse(tempRepository.save(temp));
    }

    public List<AvailableTempInfo> getAvailableTempsByDate(LocalDate startDate, LocalDate endDate) {
        if (!endDate.isAfter(startDate)) {
            ValidationErrors err = new ValidationErrors();
            err.addError("endDate", "End date must be after start date");
            throw BadRequestException.from(err);
        }

        List<Temp> allTemps = tempRepository.findAll();

        return allTemps.stream().map(temp -> {
            boolean available = tempAssigning.isTempAvailable(temp, startDate, endDate);

            List<AvailableTempInfo.AlternativeTempInfo> alternatives = available
                    ? List.of()
                    : allTemps.stream()
                            .filter(t -> !t.getId().equals(temp.getId()))
                            .filter(t -> tempAssigning.isTempAvailable(t, startDate, endDate))
                            .map(t -> new AvailableTempInfo.AlternativeTempInfo(
                                    t.getId(),
                                    t.getFirstName(),
                                    t.getLastName(),
                                    t.getCity(),
                                    t.getRating()))
                            .toList();

            return new AvailableTempInfo(
                    temp.getId(),
                    temp.getFirstName(),
                    temp.getLastName(),
                    temp.getEmail(),
                    temp.getCity(),
                    temp.getRating(),
                    available,
                    available ? startDate : tempAssigning.getNextAvailableDate(temp, startDate),
                    alternatives);
        }).toList();
    }

    public List<AvailableTempInfo> getAvailableTempsByJob(Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new NotFoundException(new NotFoundError("Job", jobId)));
        return getAvailableTempsByDate(job.getStartDate(), job.getEndDate());
    }

    private double[] resolveCoordinates(String city, ValidationErrors err) {
        if (city == null || city.isBlank()) {
            return new double[] { 0, 0 };
        }
        Optional<double[]> coords = locationService.getCoordinates(city);
        if (coords.isEmpty()) {
            err.addError("city", "City not found: " + city);
            return new double[] { 0, 0 };
        }
        return coords.get();
    }
}