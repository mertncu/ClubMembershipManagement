package com.mertncu.clubmembershipmanagement.module.event.dao;

import com.mertncu.clubmembershipmanagement.common.base.BaseDAO;
import com.mertncu.clubmembershipmanagement.config.DatabaseConnection;
import com.mertncu.clubmembershipmanagement.module.event.model.EventCategory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EventCategoryDAO implements BaseDAO<EventCategory> {

    public EventCategoryDAO() {
        createTableIfNotExists();
        seedDefaultCategories();
    }

    private void createTableIfNotExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS event_categories (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL UNIQUE,
                description TEXT,
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

    private void seedDefaultCategories() {
        if (findAll().isEmpty()) {
            save(new EventCategory("Yoga", "Relaxing mindful stretching exercises."));
            save(new EventCategory("Spinning", "High intensity stationary cycling."));
            save(new EventCategory("CrossFit", "Strength and conditioning workout."));
            save(new EventCategory("Pilates", "Core strengthening and stability."));
        }
    }

    @Override
    public EventCategory save(EventCategory cat) {
        String sql = "INSERT INTO event_categories (name, description, created_at) VALUES (?, ?, ?)";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, cat.getName());
                pstmt.setString(2, cat.getDescription());
                pstmt.setString(3, LocalDateTime.now().toString());
                if (pstmt.executeUpdate() > 0) {
                    try (ResultSet rs = pstmt.getGeneratedKeys()) {
                        if (rs.next()) cat.setId(rs.getInt(1));
                    }
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return cat;
    }

    @Override
    public boolean update(EventCategory cat) {
        return false; 
    }
    @Override
    public boolean delete(int id) {
        return false; 
    }
    @Override
    public Optional<EventCategory> findById(int id) {
        return Optional.empty(); 
    }

    @Override
    public List<EventCategory> findAll() {
        List<EventCategory> list = new ArrayList<>();
        String sql = "SELECT * FROM event_categories";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    EventCategory cat = new EventCategory();
                    cat.setId(rs.getInt("id"));
                    cat.setName(rs.getString("name"));
                    cat.setDescription(rs.getString("description"));
                    list.add(cat);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}
