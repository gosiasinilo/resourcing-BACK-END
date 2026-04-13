package io.nology.resources.temp;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Component;

import io.nology.resources.job.entity.Job;
import io.nology.resources.temp.entity.Temp;

@Component
public class TempAssigning {

    public boolean isTempAvailable(Temp temp, LocalDate start, LocalDate end) {
        return temp.getJobs().stream()
                .noneMatch(j -> !(j.getEndDate().isBefore(start)
                        || j.getStartDate().isAfter(end)));
    }

    public LocalDate getNextAvailableDate(Temp temp, LocalDate start) {
        return temp.getJobs().stream()
                .map(Job::getEndDate)
                .filter(d -> !d.isBefore(start))
                .max(LocalDate::compareTo)
                .map(d -> d.plusDays(1))
                .orElse(start);
    }

    public List<String> getAlternativeTemps(
            List<Temp> allTemps,
            LocalDate start,
            LocalDate end,
            Long excludedTempId) {

        return allTemps.stream()
                .filter(t -> !t.getId().equals(excludedTempId))
                .filter(t -> t.getJobs().stream()
                        .noneMatch(j -> !(j.getEndDate().isBefore(start)
                                || j.getStartDate().isAfter(end))))
                .map(t -> t.getId() + " - " + t.getFirstName() + " " + t.getLastName())
                .toList();
    }

    public void removeTempFromJob(Job job) {
        Temp temp = job.getTemp();

        if (temp != null) {
            temp.getJobs().remove(job);
            job.setTemp(null);
        }
    }
}