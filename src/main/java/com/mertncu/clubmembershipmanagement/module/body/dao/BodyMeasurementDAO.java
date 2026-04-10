package com.mertncu.clubmembershipmanagement.module.body.dao;

import com.mertncu.clubmembershipmanagement.common.base.BaseDAO;
import com.mertncu.clubmembershipmanagement.config.DatabaseConnection;
import com.mertncu.clubmembershipmanagement.module.body.model.BodyMeasurement;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BodyMeasurementDAO implements BaseDAO<BodyMeasurement> {

    public BodyMeasurementDAO() { createTableIfNotExists(); }

    private void createTableIfNotExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS body_measurements (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                weight_kg REAL NOT NULL,
                height_cm REAL NOT NULL,
                age INTEGER NOT NULL,
                gender TEXT NOT NULL,
                bmi REAL NOT NULL,
                body_fat_percent REAL NOT NULL,
                measured_at DATE NOT NULL,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP
            );
            """;
        try (Statement stmt = DatabaseConnection.getConnection().createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public BodyMeasurement save(BodyMeasurement m) {
        String sql = "INSERT INTO body_measurements (user_id, weight_kg, height_cm, age, gender, bmi, body_fat_percent, measured_at) VALUES (?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, m.getUserId()); ps.setDouble(2, m.getWeightKg());
            ps.setDouble(3, m.getHeightCm()); ps.setInt(4, m.getAge());
            ps.setString(5, m.getGender()); ps.setDouble(6, m.getBmi());
            ps.setDouble(7, m.getBodyFatPercent()); ps.setString(8, m.getMeasuredAt().toString());
            if (ps.executeUpdate() > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) m.setId(rs.getInt(1));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return m;
    }

    @Override public boolean update(BodyMeasurement m) { return false; }

    @Override
    public boolean delete(int id) {
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("DELETE FROM body_measurements WHERE id=?")) {
            ps.setInt(1, id); return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    @Override
    public Optional<BodyMeasurement> findById(int id) {
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM body_measurements WHERE id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return Optional.empty();
    }

    @Override
    public List<BodyMeasurement> findAll() { return findByUserId(-1); }

    public List<BodyMeasurement> findByUserId(int userId) {
        String sql = userId == -1
            ? "SELECT * FROM body_measurements ORDER BY measured_at DESC"
            : "SELECT * FROM body_measurements WHERE user_id=? ORDER BY measured_at DESC";
        List<BodyMeasurement> list = new ArrayList<>();
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql);
            if (userId != -1) ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    private BodyMeasurement map(ResultSet rs) throws SQLException {
        BodyMeasurement m = new BodyMeasurement();
        m.setId(rs.getInt("id")); m.setUserId(rs.getInt("user_id"));
        m.setWeightKg(rs.getDouble("weight_kg")); m.setHeightCm(rs.getDouble("height_cm"));
        m.setAge(rs.getInt("age")); m.setGender(rs.getString("gender"));
        m.setBmi(rs.getDouble("bmi")); m.setBodyFatPercent(rs.getDouble("body_fat_percent"));
        m.setMeasuredAt(LocalDate.parse(rs.getString("measured_at")));
        return m;
    }
}
