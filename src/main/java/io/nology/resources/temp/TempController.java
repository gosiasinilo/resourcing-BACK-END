package io.nology.resources.temp;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.nology.resources.temp.dto.CreateTempReq;
import io.nology.resources.temp.dto.TempResponse;
import io.nology.resources.temp.dto.TempResponseById;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/temps")
public class TempController {

    private final TempService tempService;

    public TempController(TempService tempService) {
        this.tempService = tempService;
    }

    @GetMapping
    public Object getTemps(
            @RequestParam(required = false) Long jobId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (jobId != null) {
            return tempService.getAvailableTempsByJob(jobId);
        }

        if (startDate != null && endDate != null) {
            return tempService.getAvailableTempsByDate(startDate, endDate);
        }

        return tempService.getAllTemps();
    }

    @PostMapping
    public ResponseEntity<TempResponse> createTemp(@RequestBody @Valid CreateTempReq request) {
        TempResponse response = tempService.createTemp(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public TempResponseById getTempById(@PathVariable Long id) {
        return tempService.getTempById(id);
    }
}