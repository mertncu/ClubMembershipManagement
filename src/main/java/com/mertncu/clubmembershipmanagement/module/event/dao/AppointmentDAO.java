package com.mertncu.clubmembershipmanagement.module.event.dao;

import com.mertncu.clubmembershipmanagement.common.base.BaseDAO;
import com.mertncu.clubmembershipmanagement.config.DatabaseConnection;
import com.mertncu.clubmembershipmanagement.module.event.model.Appointment;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AppointmentDAO implements BaseDAO<Appointment> {

    public AppointmentDAO() {
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS appointments (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL REFERENCES users(id),
                trainer_id INTEGER NOT NULL REFERENCES users(id),
                appointment_date DATETIME NOT NULL,
                status TEXT DEFAULT 'PENDING',
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
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
    public Appointment save(Appointment appt) {
        String sql = "INSERT INTO appointments (user_id, trainer_id, appointment_date, status, created_at) VALUES (?, ?, ?, ?, ?)";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, appt.getUserId());
                pstmt.setInt(2, appt.getTrainerId());
                pstmt.setString(3, appt.getAppointmentDate().toString());
                pstmt.setString(4, appt.getStatus());
                pstmt.setString(5, LocalDateTime.now().toString());
                if (pstmt.executeUpdate() > 0) {
                    try (ResultSet rs = pstmt.getGeneratedKeys()) {
                        if (rs.next()) appt.setId(rs.getInt(1));
                    }
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return appt;
    }

    @Override
    public boolean update(Appointment appt) {
        String sql = "UPDATE appointments SET status = ?, updated_at = ? WHERE id = ?";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, appt.getStatus());
                pstmt.setString(2, LocalDateTime.now().toString());
                pstmt.setInt(3, appt.getId());
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM appointments WHERE id = ?";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    @Override
    public Optional<Appointment> findById(int id) {
        String sql = "SELECT * FROM appointments WHERE id = ?";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) return Optional.of(extractAppointment(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return Optional.empty();
    }
    @Override
    public List<Appointment> findAll() { return new ArrayList<>(); }
    
    public List<Appointment> findByUserId(int userId) {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT * FROM appointments WHERE user_id = ? ORDER BY appointment_date DESC";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, userId);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) list.add(extractAppointment(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Appointment> findByTrainerId(int trainerId) {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT * FROM appointments WHERE trainer_id = ? ORDER BY appointment_date DESC";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, trainerId);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) list.add(extractAppointment(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    private Appointment extractAppointment(ResultSet rs) throws SQLException {
        Appointment a = new Appointment();
        a.setId(rs.getInt("id"));
        a.setUserId(rs.getInt("user_id"));
        a.setTrainerId(rs.getInt("trainer_id"));
        a.setAppointmentDate(LocalDateTime.parse(rs.getString("appointment_date")));
        a.setStatus(rs.getString("status"));
        return a;
    }
}
