package com.mertncu.clubmembershipmanagement.module.event.model;

import com.mertncu.clubmembershipmanagement.common.base.BaseEntity;
import java.time.LocalDateTime;

public class EventRegistration extends BaseEntity {
    private int eventId;
    private int userId;
    private LocalDateTime registeredAt;

    public EventRegistration() {}

    public EventRegistration(int eventId, int userId) {
        this.eventId = eventId;
        this.userId = userId;
        this.registeredAt = LocalDateTime.now();
    }

    public int getEventId() { return eventId; }
    public void setEventId(int eventId) { this.eventId = eventId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public LocalDateTime getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(LocalDateTime registeredAt) { this.registeredAt = registeredAt; }
}
