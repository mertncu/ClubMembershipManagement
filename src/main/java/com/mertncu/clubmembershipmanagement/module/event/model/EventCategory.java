package com.mertncu.clubmembershipmanagement.module.event.model;

import com.mertncu.clubmembershipmanagement.common.base.BaseEntity;

public class EventCategory extends BaseEntity {
    private String name;
    private String description;

    public EventCategory() {}

    public EventCategory(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
