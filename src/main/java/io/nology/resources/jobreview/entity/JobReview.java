package io.nology.resources.jobreview.entity;

import java.time.LocalDateTime;

import io.nology.resources.common.TimeStampEntityListener;
import io.nology.resources.common.entity.BaseEntity;
import io.nology.resources.common.entity.traits.Timestampable;
import io.nology.resources.job.entity.Job;
import io.nology.resources.temp.entity.Temp;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "job_review")
@EntityListeners(TimeStampEntityListener.class)
public class JobReview extends BaseEntity implements Timestampable {

    @ManyToOne
    @JoinColumn(name = "job_id")
    private Job job;

    @ManyToOne
    @JoinColumn(name = "temp_id")
    private Temp temp;
    private Integer workQuality;
    private Integer communication;
    private Integer onTime;
    private String comments;
    private String reviewedBy;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public JobReview() {
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public Temp getTemp() {
        return temp;
    }

    public void setTemp(Temp temp) {
        this.temp = temp;
    }

    public Integer getWorkQuality() {
        return workQuality;
    }

    public void setWorkQuality(Integer workQuality) {
        this.workQuality = workQuality;
    }

    public Integer getCommunication() {
        return communication;
    }

    public void setCommunication(Integer communication) {
        this.communication = communication;
    }

    public Integer getOnTime() {
        return onTime;
    }

    public void setOnTime(Integer onTime) {
        this.onTime = onTime;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(String reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    @Override
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;

    }

    @Override
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}