package com.mertncu.clubmembershipmanagement.module.event.model;

import com.mertncu.clubmembershipmanagement.common.base.BaseEntity;
import java.time.LocalDateTime;

public class Appointment extends BaseEntity {
    private int userId;
    private int trainerId;
    private LocalDateTime appointmentDate;
    private String status; // PENDING, APPROVED, CANCELLED

    public Appointment() {}

    public Appointment(int userId, int trainerId, LocalDateTime appointmentDate, String status) {
        this.userId = userId;
        this.trainerId = trainerId;
        this.appointmentDate = appointmentDate;
        this.status = status;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public int getTrainerId() { return trainerId; }
    public void setTrainerId(int trainerId) { this.trainerId = trainerId; }
    public LocalDateTime getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(LocalDateTime appointmentDate) { this.appointmentDate = appointmentDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
