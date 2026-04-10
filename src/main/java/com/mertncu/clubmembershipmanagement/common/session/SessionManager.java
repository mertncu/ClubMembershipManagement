package com.mertncu.clubmembershipmanagement.common.session;

import com.mertncu.clubmembershipmanagement.module.auth.model.User;

/**
 * Manages the current logged-in user session globally.
 */
public class SessionManager {
    private static SessionManager instance;
    private User currentUser;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void loginUser(User user) {
        this.currentUser = user;
    }

    public void logout() {
        this.currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    public boolean hasRole(com.mertncu.clubmembershipmanagement.common.enums.UserRole role) {
        if (currentUser == null) return false;
        return currentUser.getRole() == role;
    }
}
