package io.nology.resources.job.service;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

import io.nology.resources.job.entity.Job;
import io.nology.resources.temp.entity.Temp;

@Component
public class JobRules {

    public boolean dateOverlap(Job job, Temp temp, LocalDate start, LocalDate end) {
        return temp.getJobs().stream()
                .filter(j -> !j.getId().equals(job.getId()))
                .anyMatch(j -> !(j.getEndDate().isBefore(start)
                        || j.getStartDate().isAfter(end)));
    }

    public boolean dateRangeValid(LocalDate start, LocalDate end) {
        return start != null && end != null && end.isAfter(start);
    }
}