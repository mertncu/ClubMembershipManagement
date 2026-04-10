package com.mertncu.clubmembershipmanagement.module.event.model;

import com.mertncu.clubmembershipmanagement.common.base.BaseEntity;
import java.time.LocalDateTime;

public class Event extends BaseEntity {
    private int categoryId;
    private int trainerId;
    private String title;
    private String description;
    private LocalDateTime eventDate;
    private int quota;

    public Event() {}

    public Event(int categoryId, int trainerId, String title, String description, LocalDateTime eventDate, int quota) {
        this.categoryId = categoryId;
        this.trainerId = trainerId;
        this.title = title;
        this.description = description;
        this.eventDate = eventDate;
        this.quota = quota;
    }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    public int getTrainerId() { return trainerId; }
    public void setTrainerId(int trainerId) { this.trainerId = trainerId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getEventDate() { return eventDate; }
    public void setEventDate(LocalDateTime eventDate) { this.eventDate = eventDate; }
    public int getQuota() { return quota; }
    public void setQuota(int quota) { this.quota = quota; }
}
