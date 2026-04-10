package com.mertncu.clubmembershipmanagement.module.workout.dao;

import com.mertncu.clubmembershipmanagement.common.base.BaseDAO;
import com.mertncu.clubmembershipmanagement.config.DatabaseConnection;
import com.mertncu.clubmembershipmanagement.module.workout.model.WorkoutProgram;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WorkoutProgramDAO implements BaseDAO<WorkoutProgram> {

    public WorkoutProgramDAO() { createTableIfNotExists(); }

    private void createTableIfNotExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS workout_programs (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                trainer_id INTEGER NOT NULL,
                name TEXT NOT NULL,
                goal TEXT,
                level TEXT DEFAULT 'BEGINNER',
                duration_weeks INTEGER DEFAULT 4,
                start_date DATE,
                notes TEXT,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP
            );
            """;
        try (Statement stmt = DatabaseConnection.getConnection().createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public WorkoutProgram save(WorkoutProgram w) {
        String sql = "INSERT INTO workout_programs (user_id, trainer_id, name, goal, level, duration_weeks, start_date, notes) VALUES (?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, w.getUserId()); ps.setInt(2, w.getTrainerId());
            ps.setString(3, w.getName()); ps.setString(4, w.getGoal());
            ps.setString(5, w.getLevel()); ps.setInt(6, w.getDurationWeeks());
            ps.setString(7, w.getStartDate() != null ? w.getStartDate().toString() : null);
            ps.setString(8, w.getNotes());
            if (ps.executeUpdate() > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) w.setId(rs.getInt(1));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return w;
    }

    @Override public boolean update(WorkoutProgram w) { return false; }

    @Override
    public boolean delete(int id) {
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("DELETE FROM workout_programs WHERE id=?")) {
            ps.setInt(1, id); return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    @Override
    public Optional<WorkoutProgram> findById(int id) {
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM workout_programs WHERE id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return Optional.empty();
    }

    @Override public List<WorkoutProgram> findAll() { return findByUserId(-1); }

    public List<WorkoutProgram> findByUserId(int userId) {
        List<WorkoutProgram> list = new ArrayList<>();
        String sql = userId < 0 ? "SELECT * FROM workout_programs ORDER BY id DESC"
                                : "SELECT * FROM workout_programs WHERE user_id=? ORDER BY id DESC";
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql);
            if (userId >= 0) ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<WorkoutProgram> findByTrainerId(int trainerId) {
        List<WorkoutProgram> list = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(
                "SELECT * FROM workout_programs WHERE trainer_id=? ORDER BY id DESC")) {
            ps.setInt(1, trainerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    private WorkoutProgram map(ResultSet rs) throws SQLException {
        WorkoutProgram w = new WorkoutProgram();
        w.setId(rs.getInt("id")); w.setUserId(rs.getInt("user_id"));
        w.setTrainerId(rs.getInt("trainer_id")); w.setName(rs.getString("name"));
        w.setGoal(rs.getString("goal")); w.setLevel(rs.getString("level"));
        w.setDurationWeeks(rs.getInt("duration_weeks"));
        String sd = rs.getString("start_date"); if (sd != null) w.setStartDate(LocalDate.parse(sd));
        w.setNotes(rs.getString("notes"));
        return w;
    }
}
