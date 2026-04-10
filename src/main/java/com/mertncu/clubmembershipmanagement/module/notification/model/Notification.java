package com.mertncu.clubmembershipmanagement.module.notification.model;

import com.mertncu.clubmembershipmanagement.common.base.BaseEntity;
import java.time.LocalDateTime;

public class Notification extends BaseEntity {
    private int userId;         // target user (0 = broadcast to all)
    private String title;
    private String message;
    private boolean isRead;
    private String type;        // INFO, WARNING, SUCCESS
    private LocalDateTime createdAt;

    public Notification() {}

    public Notification(int userId, String title, String message, String type) {
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.isRead = false;
        this.createdAt = LocalDateTime.now();
    }

    public int getUserId()                   { return userId; }
    public void setUserId(int v)             { this.userId = v; }
    public String getTitle()                 { return title; }
    public void setTitle(String v)           { this.title = v; }
    public String getMessage()               { return message; }
    public void setMessage(String v)         { this.message = v; }
    public boolean isRead()                  { return isRead; }
    public void setRead(boolean v)           { this.isRead = v; }
    public String getType()                  { return type; }
    public void setType(String v)            { this.type = v; }
    public LocalDateTime getCreatedAt()      { return createdAt; }
    public void setCreatedAt(LocalDateTime v){ this.createdAt = v; }
}
