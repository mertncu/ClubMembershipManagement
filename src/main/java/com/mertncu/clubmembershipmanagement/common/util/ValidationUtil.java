package com.mertncu.clubmembershipmanagement.common.util;

import java.util.regex.Pattern;

/**
 * Utility class to validate common inputs.
 */
public class ValidationUtil {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) return false;
        return EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isStrongPassword(String password) {
        if (password == null || password.length() < 6) return false;
        // Can add more complex rules (uppercase, special char) if needed
        return true;
    }
    
    public static boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }
}
