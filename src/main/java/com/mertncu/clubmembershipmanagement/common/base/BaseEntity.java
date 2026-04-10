package com.mertncu.clubmembershipmanagement.common.base;

import java.time.LocalDateTime;

/**
 * BaseEntity class that holds common properties for all database entities.
 */
public abstract class BaseEntity {
    private Integer id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public BaseEntity() {
        this.createdAt = LocalDateTime.now();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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
}
