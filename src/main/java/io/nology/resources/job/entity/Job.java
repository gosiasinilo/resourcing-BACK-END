package io.nology.resources.job.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import io.nology.resources.common.TimeStampEntityListener;
import io.nology.resources.common.entity.BaseEntity;
import io.nology.resources.common.entity.traits.Timestampable;
import io.nology.resources.jobreview.entity.JobReview;
import io.nology.resources.skill.entity.Skill;
import io.nology.resources.temp.entity.Temp;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "jobs")
@EntityListeners(TimeStampEntityListener.class)
public class Job extends BaseEntity implements Timestampable {

    public enum JobType {
        LOCATION, ONLINE
    }

    public enum JobStatus {
        INITIATED, ASSIGNED, ACTIVE, COMPLETED
    }

    private String name;
    private String description;

    @Enumerated(EnumType.STRING)
    private JobType jobType;

    @Enumerated(EnumType.STRING)
    private JobStatus status = JobStatus.INITIATED;

    private LocalDate startDate;
    private LocalDate endDate;

    private String city;
    private Double latitude;
    private Double longitude;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "temp_id")
    private Temp temp;

    @ManyToMany
    @JoinTable(name = "job_required_skills", joinColumns = @JoinColumn(name = "job_id"), inverseJoinColumns = @JoinColumn(name = "skill_id"))
    private List<Skill> requiredSkills = new ArrayList<>();

    @OneToMany(mappedBy = "job")
    private List<JobReview> reviews = new ArrayList<>();

    public Job() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public JobType getJobType() {
        return jobType;
    }

    public void setJobType(JobType jobType) {
        this.jobType = jobType;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
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

    public Temp getTemp() {
        return temp;
    }

    public void setTemp(Temp temp) {
        this.temp = temp;
    }

    public List<Skill> getRequiredSkills() {
        return requiredSkills;
    }

    public void setRequiredSkills(List<Skill> requiredSkills) {
        this.requiredSkills = requiredSkills;
    }

    public List<JobReview> getReviews() {
        return reviews;
    }

    public void setReviews(List<JobReview> reviews) {
        this.reviews = reviews;
    }
}