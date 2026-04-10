package com.mertncu.clubmembershipmanagement.module.workout.dao;

import com.mertncu.clubmembershipmanagement.common.base.BaseDAO;
import com.mertncu.clubmembershipmanagement.config.DatabaseConnection;
import com.mertncu.clubmembershipmanagement.module.workout.model.WorkoutExercise;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WorkoutExerciseDAO implements BaseDAO<WorkoutExercise> {

    public WorkoutExerciseDAO() { createTableIfNotExists(); }

    private void createTableIfNotExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS workout_exercises (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                program_id INTEGER NOT NULL REFERENCES workout_programs(id) ON DELETE CASCADE,
                name TEXT NOT NULL,
                muscle_group TEXT,
                sets INTEGER DEFAULT 3,
                reps INTEGER DEFAULT 12,
                weight_kg REAL DEFAULT 0,
                rest_seconds INTEGER DEFAULT 60,
                day_of_week TEXT DEFAULT 'ALL',
                notes TEXT
            );
            """;
        try (Statement stmt = DatabaseConnection.getConnection().createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public WorkoutExercise save(WorkoutExercise e) {
        String sql = "INSERT INTO workout_exercises (program_id, name, muscle_group, sets, reps, weight_kg, rest_seconds, day_of_week, notes) VALUES (?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, e.getProgramId()); ps.setString(2, e.getName());
            ps.setString(3, e.getMuscleGroup()); ps.setInt(4, e.getSets());
            ps.setInt(5, e.getReps()); ps.setDouble(6, e.getWeightKg());
            ps.setInt(7, e.getRestSeconds()); ps.setString(8, e.getDayOfWeek());
            ps.setString(9, e.getNotes());
            if (ps.executeUpdate() > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) e.setId(rs.getInt(1));
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
        return e;
    }

    @Override public boolean update(WorkoutExercise e) { return false; }

    @Override
    public boolean delete(int id) {
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("DELETE FROM workout_exercises WHERE id=?")) {
            ps.setInt(1, id); return ps.executeUpdate() > 0;
        } catch (SQLException ex) { ex.printStackTrace(); return false; }
    }

    @Override public Optional<WorkoutExercise> findById(int id) { return Optional.empty(); }
    @Override public List<WorkoutExercise> findAll() { return new ArrayList<>(); }

    public List<WorkoutExercise> findByProgramId(int programId) {
        List<WorkoutExercise> list = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(
                "SELECT * FROM workout_exercises WHERE program_id=? ORDER BY day_of_week, id")) {
            ps.setInt(1, programId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException ex) { ex.printStackTrace(); }
        return list;
    }

    private WorkoutExercise map(ResultSet rs) throws SQLException {
        WorkoutExercise e = new WorkoutExercise();
        e.setId(rs.getInt("id")); e.setProgramId(rs.getInt("program_id"));
        e.setName(rs.getString("name")); e.setMuscleGroup(rs.getString("muscle_group"));
        e.setSets(rs.getInt("sets")); e.setReps(rs.getInt("reps"));
        e.setWeightKg(rs.getDouble("weight_kg")); e.setRestSeconds(rs.getInt("rest_seconds"));
        e.setDayOfWeek(rs.getString("day_of_week")); e.setNotes(rs.getString("notes"));
        return e;
    }
}
