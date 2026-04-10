package com.mertncu.clubmembershipmanagement.module.diet.dao;

import com.mertncu.clubmembershipmanagement.common.base.BaseDAO;
import com.mertncu.clubmembershipmanagement.config.DatabaseConnection;
import com.mertncu.clubmembershipmanagement.module.diet.model.DietProgram;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DietProgramDAO implements BaseDAO<DietProgram> {

    public DietProgramDAO() { createTableIfNotExists(); }

    private void createTableIfNotExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS diet_programs (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                trainer_id INTEGER NOT NULL,
                name TEXT NOT NULL,
                goal TEXT,
                start_date DATE,
                end_date DATE,
                daily_calories INTEGER DEFAULT 2000,
                notes TEXT,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP
            );
            """;
        try (Statement stmt = DatabaseConnection.getConnection().createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public DietProgram save(DietProgram d) {
        String sql = "INSERT INTO diet_programs (user_id, trainer_id, name, goal, start_date, end_date, daily_calories, notes) VALUES (?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, d.getUserId()); ps.setInt(2, d.getTrainerId());
            ps.setString(3, d.getName()); ps.setString(4, d.getGoal());
            ps.setString(5, d.getStartDate() != null ? d.getStartDate().toString() : null);
            ps.setString(6, d.getEndDate() != null ? d.getEndDate().toString() : null);
            ps.setInt(7, d.getDailyCalories()); ps.setString(8, d.getNotes());
            if (ps.executeUpdate() > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) d.setId(rs.getInt(1));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return d;
    }

    @Override public boolean update(DietProgram d) { return false; }

    @Override
    public boolean delete(int id) {
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("DELETE FROM diet_programs WHERE id=?")) {
            ps.setInt(1, id); return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    @Override
    public Optional<DietProgram> findById(int id) {
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM diet_programs WHERE id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return Optional.empty();
    }

    @Override
    public List<DietProgram> findAll() { return findByUserId(-1); }

    public List<DietProgram> findByUserId(int userId) {
        List<DietProgram> list = new ArrayList<>();
        String sql = userId < 0 ? "SELECT * FROM diet_programs ORDER BY id DESC"
                                : "SELECT * FROM diet_programs WHERE user_id=? ORDER BY id DESC";
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql);
            if (userId >= 0) ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<DietProgram> findByTrainerId(int trainerId) {
        List<DietProgram> list = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(
                "SELECT * FROM diet_programs WHERE trainer_id=? ORDER BY id DESC")) {
            ps.setInt(1, trainerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    private DietProgram map(ResultSet rs) throws SQLException {
        DietProgram d = new DietProgram();
        d.setId(rs.getInt("id")); d.setUserId(rs.getInt("user_id"));
        d.setTrainerId(rs.getInt("trainer_id")); d.setName(rs.getString("name"));
        d.setGoal(rs.getString("goal"));
        String sd = rs.getString("start_date"); if (sd != null) d.setStartDate(LocalDate.parse(sd));
        String ed = rs.getString("end_date");   if (ed != null) d.setEndDate(LocalDate.parse(ed));
        d.setDailyCalories(rs.getInt("daily_calories")); d.setNotes(rs.getString("notes"));
        return d;
    }
}
