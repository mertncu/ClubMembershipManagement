package com.mertncu.clubmembershipmanagement.module.membership.model;

import com.mertncu.clubmembershipmanagement.common.base.BaseEntity;
import java.time.LocalDate;

public class Subscription extends BaseEntity {
    private int userId;
    private int membershipTypeId;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean isActive;

    public Subscription() {}

    public Subscription(int userId, int membershipTypeId, LocalDate startDate, LocalDate endDate, boolean isActive) {
        this.userId = userId;
        this.membershipTypeId = membershipTypeId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isActive = isActive;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public int getMembershipTypeId() { return membershipTypeId; }
    public void setMembershipTypeId(int membershipTypeId) { this.membershipTypeId = membershipTypeId; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    
    // Utility to get remaining days
    public long getRemainingDays() {
        if (!isActive || endDate.isBefore(LocalDate.now())) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), endDate);
    }
}
