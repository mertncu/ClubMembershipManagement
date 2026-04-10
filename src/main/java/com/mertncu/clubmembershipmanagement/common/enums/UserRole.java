package com.mertncu.clubmembershipmanagement.common.enums;

public enum UserRole {
    ADMIN("Administrator"),
    MEMBER("Gym Member"),
    TRAINER("Fitness Trainer");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
