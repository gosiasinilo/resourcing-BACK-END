package io.nology.resources.job.service;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

import io.nology.resources.job.entity.Job;
import io.nology.resources.temp.entity.Temp;

@Component
public class JobRules {
    public boolean overlaps(LocalDate start1, LocalDate end1,
            LocalDate start2, LocalDate end2) {
        return !(end1.isBefore(start2) || start1.isAfter(end2));
    }

    public boolean dateOverlap(Temp temp, LocalDate start, LocalDate end, Long ignoreJobId) {

        if (temp == null)
            return false;

        return temp.getJobs().stream()
                .filter(j -> ignoreJobId == null || !j.getId().equals(ignoreJobId))
                .anyMatch(j -> overlaps(j.getStartDate(), j.getEndDate(), start, end));
    }

    public boolean dateNotInThePast(LocalDate date) {
        return date != null && date.isBefore(LocalDate.now());
    }

    public boolean dateRangeValid(LocalDate start, LocalDate end) {
        return start != null && end != null && end.isAfter(start);
    }

    public boolean tempHasConflict(Temp temp, Job job, LocalDate start, LocalDate end) {
        if (temp == null)
            return false;

        return temp.getJobs().stream()
                .filter(j -> !j.getId().equals(job.getId()))
                .anyMatch(j -> overlaps(j.getStartDate(), j.getEndDate(), start, end));
    }
}