package com.mertncu.clubmembershipmanagement.module.auth.service;

import com.mertncu.clubmembershipmanagement.common.enums.UserRole;
import com.mertncu.clubmembershipmanagement.common.session.SessionManager;
import com.mertncu.clubmembershipmanagement.common.util.HashUtil;
import com.mertncu.clubmembershipmanagement.common.util.ValidationUtil;
import com.mertncu.clubmembershipmanagement.module.auth.dao.UserDAO;
import com.mertncu.clubmembershipmanagement.module.auth.model.User;

import java.util.Optional;

public class AuthService {
    private final UserDAO userDAO;

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    public boolean login(String email, String password) throws Exception {
        if (!ValidationUtil.isValidEmail(email)) {
            throw new Exception("Geçersiz e-posta formatı.");
        }

        Optional<User> optionalUser = userDAO.findByEmail(email);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (HashUtil.verifyPassword(password, user.getPasswordHash())) {
                SessionManager.getInstance().loginUser(user);
                return true;
            } else {
                throw new Exception("Hatalı şifre.");
            }
        } else {
            throw new Exception("Böyle bir kullanıcı bulunamadı.");
        }
    }

    public void register(String name, String email, String password, UserRole role) throws Exception {
        if (!ValidationUtil.isNotEmpty(name)) {
            throw new Exception("İsim boş bırakılamaz.");
        }
        if (!ValidationUtil.isValidEmail(email)) {
            throw new Exception("Geçersiz e-posta formatı.");
        }
        if (!ValidationUtil.isStrongPassword(password)) {
            throw new Exception("Şifre en az 6 karakter olmalıdır.");
        }

        if (userDAO.findByEmail(email).isPresent()) {
            throw new Exception("Bu e-posta adresi ile kayıtlı bir kullanıcı zaten var.");
        }

        String hashedPw = HashUtil.hashPassword(password);
        User newUser = new User(name, email, hashedPw, role);
        userDAO.save(newUser);
    }
    
    public void logout() {
        SessionManager.getInstance().logout();
    }
}
