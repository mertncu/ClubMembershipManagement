package com.mertncu.clubmembershipmanagement.module.event.dao;

import com.mertncu.clubmembershipmanagement.common.base.BaseDAO;
import com.mertncu.clubmembershipmanagement.config.DatabaseConnection;
import com.mertncu.clubmembershipmanagement.module.event.model.TrainerAvailability;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TrainerAvailabilityDAO implements BaseDAO<TrainerAvailability> {

    public TrainerAvailabilityDAO() { createTableIfNotExists(); }

    private void createTableIfNotExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS trainer_availability (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                trainer_id INTEGER NOT NULL REFERENCES users(id),
                slot_start DATETIME NOT NULL,
                slot_end DATETIME NOT NULL,
                is_booked INTEGER DEFAULT 0,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP
            );
            """;
        try (Statement stmt = DatabaseConnection.getConnection().createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public TrainerAvailability save(TrainerAvailability ta) {
        String sql = "INSERT INTO trainer_availability (trainer_id, slot_start, slot_end, is_booked) VALUES (?,?,?,?)";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, ta.getTrainerId());
            ps.setString(2, ta.getSlotStart().toString());
            ps.setString(3, ta.getSlotEnd().toString());
            ps.setBoolean(4, ta.isBooked());
            if (ps.executeUpdate() > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) ta.setId(rs.getInt(1));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return ta;
    }

    @Override
    public boolean update(TrainerAvailability ta) {
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(
                "UPDATE trainer_availability SET is_booked=? WHERE id=?")) {
            ps.setBoolean(1, ta.isBooked()); ps.setInt(2, ta.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    @Override
    public boolean delete(int id) {
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(
                "DELETE FROM trainer_availability WHERE id=?")) {
            ps.setInt(1, id); return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    @Override
    public Optional<TrainerAvailability> findById(int id) { return Optional.empty(); }

    @Override
    public List<TrainerAvailability> findAll() { return new ArrayList<>(); }

    /** Available (not booked) future slots for a given trainer. */
    public List<TrainerAvailability> findAvailableByTrainer(int trainerId) {
        List<TrainerAvailability> list = new ArrayList<>();
        String sql = "SELECT * FROM trainer_availability WHERE trainer_id=? AND is_booked=0 AND slot_start >= ? ORDER BY slot_start";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, trainerId);
            ps.setString(2, LocalDateTime.now().toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /** All slots (booked and free) for a trainer — used in trainer's own view. */
    public List<TrainerAvailability> findByTrainerId(int trainerId) {
        List<TrainerAvailability> list = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(
                "SELECT * FROM trainer_availability WHERE trainer_id=? ORDER BY slot_start")) {
            ps.setInt(1, trainerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    private TrainerAvailability map(ResultSet rs) throws SQLException {
        TrainerAvailability ta = new TrainerAvailability();
        ta.setId(rs.getInt("id"));
        ta.setTrainerId(rs.getInt("trainer_id"));
        ta.setSlotStart(LocalDateTime.parse(rs.getString("slot_start").replace(" ", "T")));
        ta.setSlotEnd(LocalDateTime.parse(rs.getString("slot_end").replace(" ", "T")));
        ta.setBooked(rs.getBoolean("is_booked"));
        return ta;
    }
}
