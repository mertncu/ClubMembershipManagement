package com.mertncu.clubmembershipmanagement.module.event.dao;

import com.mertncu.clubmembershipmanagement.common.base.BaseDAO;
import com.mertncu.clubmembershipmanagement.config.DatabaseConnection;
import com.mertncu.clubmembershipmanagement.module.event.model.EventRegistration;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EventRegistrationDAO implements BaseDAO<EventRegistration> {

    public EventRegistrationDAO() {
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS event_registrations (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                event_id INTEGER NOT NULL REFERENCES events(id),
                user_id INTEGER NOT NULL REFERENCES users(id),
                registered_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                UNIQUE(event_id, user_id)
            );
        """;
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sql);
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public EventRegistration save(EventRegistration reg) {
        String sql = "INSERT INTO event_registrations (event_id, user_id, registered_at, created_at) VALUES (?, ?, ?, ?)";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, reg.getEventId());
                pstmt.setInt(2, reg.getUserId());
                pstmt.setString(3, reg.getRegisteredAt().toString());
                pstmt.setString(4, LocalDateTime.now().toString());
                if (pstmt.executeUpdate() > 0) {
                    try (ResultSet rs = pstmt.getGeneratedKeys()) {
                        if (rs.next()) reg.setId(rs.getInt(1));
                    }
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return reg;
    }

    @Override
    public boolean update(EventRegistration obj) { return false; }
    
    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM event_registrations WHERE id = ?";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
    
    public boolean deleteByUserAndEvent(int userId, int eventId) {
        String sql = "DELETE FROM event_registrations WHERE user_id = ? AND event_id = ?";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, userId);
                pstmt.setInt(2, eventId);
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    @Override
    public Optional<EventRegistration> findById(int id) { return Optional.empty(); }
    @Override
    public List<EventRegistration> findAll() { return new ArrayList<>(); }
    
    public int getRegistrationCountForEvent(int eventId) {
        String sql = "SELECT COUNT(*) FROM event_registrations WHERE event_id = ?";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, eventId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }
    
    public boolean isUserRegisteredForEvent(int userId, int eventId) {
        String sql = "SELECT 1 FROM event_registrations WHERE user_id = ? AND event_id = ?";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, userId);
                pstmt.setInt(2, eventId);
                ResultSet rs = pstmt.executeQuery();
                return rs.next();
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }
}
