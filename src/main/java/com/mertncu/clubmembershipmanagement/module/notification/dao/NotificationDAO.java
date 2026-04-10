package com.mertncu.clubmembershipmanagement.module.notification.dao;

import com.mertncu.clubmembershipmanagement.common.base.BaseDAO;
import com.mertncu.clubmembershipmanagement.config.DatabaseConnection;
import com.mertncu.clubmembershipmanagement.module.notification.model.Notification;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NotificationDAO implements BaseDAO<Notification> {

    public NotificationDAO() { createTableIfNotExists(); }

    private void createTableIfNotExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS notifications (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                title TEXT NOT NULL,
                message TEXT NOT NULL,
                is_read INTEGER DEFAULT 0,
                type TEXT DEFAULT 'INFO',
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP
            );
            """;
        try (Statement stmt = DatabaseConnection.getConnection().createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public Notification save(Notification n) {
        String sql = "INSERT INTO notifications (user_id, title, message, is_read, type) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, n.getUserId()); ps.setString(2, n.getTitle());
            ps.setString(3, n.getMessage()); ps.setBoolean(4, n.isRead());
            ps.setString(5, n.getType());
            if (ps.executeUpdate() > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) n.setId(rs.getInt(1));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return n;
    }

    @Override
    public boolean update(Notification n) {
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(
                "UPDATE notifications SET is_read=? WHERE id=?")) {
            ps.setBoolean(1, n.isRead()); ps.setInt(2, n.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    @Override
    public boolean delete(int id) {
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("DELETE FROM notifications WHERE id=?")) {
            ps.setInt(1, id); return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    @Override
    public Optional<Notification> findById(int id) { return Optional.empty(); }

    @Override
    public List<Notification> findAll() { return findByUserId(-1); }

    public List<Notification> findByUserId(int userId) {
        // userId == -1 -> fetch all (admin view)
        String sql = userId == -1
            ? "SELECT * FROM notifications ORDER BY created_at DESC"
            : "SELECT * FROM notifications WHERE user_id=0 OR user_id=? ORDER BY created_at DESC";
        List<Notification> list = new ArrayList<>();
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql);
            if (userId != -1) ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean markAsRead(int notificationId) {
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(
                "UPDATE notifications SET is_read=1 WHERE id=?")) {
            ps.setInt(1, notificationId); return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    private Notification map(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setId(rs.getInt("id")); n.setUserId(rs.getInt("user_id"));
        n.setTitle(rs.getString("title")); n.setMessage(rs.getString("message"));
        n.setRead(rs.getBoolean("is_read")); n.setType(rs.getString("type"));
        String ca = rs.getString("created_at");
        if (ca != null) n.setCreatedAt(LocalDateTime.parse(ca.replace(" ", "T")));
        return n;
    }
}
