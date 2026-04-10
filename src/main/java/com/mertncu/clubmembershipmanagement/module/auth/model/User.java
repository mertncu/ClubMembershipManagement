package com.mertncu.clubmembershipmanagement.module.auth.model;

import com.mertncu.clubmembershipmanagement.common.base.BaseEntity;
import com.mertncu.clubmembershipmanagement.common.enums.UserRole;

public class User extends BaseEntity {
    private String name;
    private String email;
    private String passwordHash;
    private UserRole role;

    public User() {}

    public User(String name, String email, String passwordHash, UserRole role) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }
}
