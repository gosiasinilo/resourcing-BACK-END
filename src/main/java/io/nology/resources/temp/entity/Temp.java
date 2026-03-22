package io.nology.resources.temp.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import io.nology.resources.common.TimeStampEntityListener;
import io.nology.resources.common.entity.BaseEntity;
import io.nology.resources.common.entity.traits.Timestampable;
import io.nology.resources.job.entity.Job;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "temps")
@EntityListeners(TimeStampEntityListener.class)
public class Temp extends BaseEntity implements Timestampable {
    private String firstName;
    private String lastName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "temp")
    private List<Job> jobs = new ArrayList<>();

    public Temp() {
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<Job> getJobs() {
        return jobs;
    }

    public void setJobs(List<Job> jobs) {
        this.jobs = jobs;
    }
}