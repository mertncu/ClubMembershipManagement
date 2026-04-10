package com.mertncu.clubmembershipmanagement.module.event.dao;

import com.mertncu.clubmembershipmanagement.common.base.BaseDAO;
import com.mertncu.clubmembershipmanagement.config.DatabaseConnection;
import com.mertncu.clubmembershipmanagement.module.event.model.Event;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EventDAO implements BaseDAO<Event> {

    public EventDAO() {
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS events (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                category_id INTEGER NOT NULL REFERENCES event_categories(id),
                trainer_id INTEGER NOT NULL REFERENCES users(id),
                title TEXT NOT NULL,
                description TEXT,
                event_date DATETIME NOT NULL,
                quota INTEGER NOT NULL,
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
    public Event save(Event evt) {
        String sql = "INSERT INTO events (category_id, trainer_id, title, description, event_date, quota, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, evt.getCategoryId());
                pstmt.setInt(2, evt.getTrainerId());
                pstmt.setString(3, evt.getTitle());
                pstmt.setString(4, evt.getDescription());
                pstmt.setString(5, evt.getEventDate().toString());
                pstmt.setInt(6, evt.getQuota());
                pstmt.setString(7, LocalDateTime.now().toString());
                if (pstmt.executeUpdate() > 0) {
                    try (ResultSet rs = pstmt.getGeneratedKeys()) {
                        if (rs.next()) evt.setId(rs.getInt(1));
                    }
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return evt;
    }

    @Override
    public boolean update(Event evt) { return false; }
    @Override
    public boolean delete(int id) { return false; }

    @Override
    public Optional<Event> findById(int id) {
        String sql = "SELECT * FROM events WHERE id = ?";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) return Optional.of(extractEvent(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return Optional.empty();
    }

    @Override
    public List<Event> findAll() {
        List<Event> list = new ArrayList<>();
        String sql = "SELECT * FROM events ORDER BY event_date ASC";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) list.add(extractEvent(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
    
    // Finds upcoming events only
    public List<Event> findUpcomingEvents() {
        List<Event> list = new ArrayList<>();
        String sql = "SELECT * FROM events WHERE event_date >= ? ORDER BY event_date ASC";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, LocalDateTime.now().toString());
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) list.add(extractEvent(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    private Event extractEvent(ResultSet rs) throws SQLException {
        Event evt = new Event();
        evt.setId(rs.getInt("id"));
        evt.setCategoryId(rs.getInt("category_id"));
        evt.setTrainerId(rs.getInt("trainer_id"));
        evt.setTitle(rs.getString("title"));
        evt.setDescription(rs.getString("description"));
        evt.setEventDate(LocalDateTime.parse(rs.getString("event_date")));
        evt.setQuota(rs.getInt("quota"));
        return evt;
    }
}
