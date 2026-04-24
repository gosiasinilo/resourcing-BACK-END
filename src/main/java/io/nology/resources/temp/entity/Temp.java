package io.nology.resources.temp.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import io.nology.resources.common.TimeStampEntityListener;
import io.nology.resources.common.entity.BaseEntity;
import io.nology.resources.common.entity.traits.Timestampable;
import io.nology.resources.job.entity.Job;
import io.nology.resources.jobreview.entity.JobReview;
import io.nology.resources.skill.entity.Skill;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "temps")
@EntityListeners(TimeStampEntityListener.class)
public class Temp extends BaseEntity implements Timestampable {

    private String firstName;
    private String lastName;
    private String email;
    private String city;
    private Double latitude;
    private Double longitude;
    private BigDecimal rating;
    private String notes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "temp")
    private List<Job> jobs = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "temp_skills", joinColumns = @JoinColumn(name = "temp_id"), inverseJoinColumns = @JoinColumn(name = "skill_id"))
    private List<Skill> skills = new ArrayList<>();

    @OneToMany(mappedBy = "temp")
    private List<JobReview> reviews = new ArrayList<>();

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public BigDecimal getRating() {
        return rating;
    }

    public void setRating(BigDecimal rating) {
        this.rating = rating;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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

    public List<Skill> getSkills() {
        return skills;
    }

    public void setSkills(List<Skill> skills) {
        this.skills = skills;
    }

    public List<JobReview> getReviews() {
        return reviews;
    }

    public void setReviews(List<JobReview> reviews) {
        this.reviews = reviews;
    }
}