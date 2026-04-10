package com.mertncu.clubmembershipmanagement.module.message.model;

import com.mertncu.clubmembershipmanagement.common.base.BaseEntity;
import java.time.LocalDateTime;

public class Message extends BaseEntity {
    private int senderId;
    private int receiverId;
    private String content;
    private boolean isRead;
    private LocalDateTime sentAt;

    public Message() {}

    public Message(int senderId, int receiverId, String content) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.isRead = false;
        this.sentAt = LocalDateTime.now();
    }

    public int getSenderId()                 { return senderId; }
    public void setSenderId(int v)           { this.senderId = v; }
    public int getReceiverId()               { return receiverId; }
    public void setReceiverId(int v)         { this.receiverId = v; }
    public String getContent()               { return content; }
    public void setContent(String v)         { this.content = v; }
    public boolean isRead()                  { return isRead; }
    public void setRead(boolean v)           { this.isRead = v; }
    public LocalDateTime getSentAt()         { return sentAt; }
    public void setSentAt(LocalDateTime v)   { this.sentAt = v; }
}
