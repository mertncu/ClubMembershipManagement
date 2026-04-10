package com.mertncu.clubmembershipmanagement.module.body.dao;

import com.mertncu.clubmembershipmanagement.common.base.BaseDAO;
import com.mertncu.clubmembershipmanagement.config.DatabaseConnection;
import com.mertncu.clubmembershipmanagement.module.body.model.TrainerProfile;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TrainerProfileDAO implements BaseDAO<TrainerProfile> {

    public TrainerProfileDAO() { createTableIfNotExists(); }

    private void createTableIfNotExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS trainer_profiles (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL UNIQUE REFERENCES users(id),
                specialization TEXT,
                bio TEXT,
                years_experience INTEGER DEFAULT 0,
                certifications TEXT,
                photo_url TEXT,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP
            );
            """;
        try (Statement stmt = DatabaseConnection.getConnection().createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public TrainerProfile save(TrainerProfile p) {
        String sql = "INSERT OR REPLACE INTO trainer_profiles (user_id, specialization, bio, years_experience, certifications, photo_url) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, p.getUserId()); ps.setString(2, p.getSpecialization());
            ps.setString(3, p.getBio()); ps.setInt(4, p.getYearsExperience());
            ps.setString(5, p.getCertifications()); ps.setString(6, p.getPhotoUrl());
            if (ps.executeUpdate() > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) p.setId(rs.getInt(1));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return p;
    }

    @Override
    public boolean update(TrainerProfile p) {
        String sql = "UPDATE trainer_profiles SET specialization=?, bio=?, years_experience=?, certifications=? WHERE user_id=?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, p.getSpecialization()); ps.setString(2, p.getBio());
            ps.setInt(3, p.getYearsExperience()); ps.setString(4, p.getCertifications());
            ps.setInt(5, p.getUserId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    @Override
    public boolean delete(int id) {
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("DELETE FROM trainer_profiles WHERE id=?")) {
            ps.setInt(1, id); return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    @Override
    public Optional<TrainerProfile> findById(int id) {
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM trainer_profiles WHERE id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return Optional.empty();
    }

    public Optional<TrainerProfile> findByUserId(int userId) {
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM trainer_profiles WHERE user_id=?")) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return Optional.empty();
    }

    @Override
    public List<TrainerProfile> findAll() {
        List<TrainerProfile> list = new ArrayList<>();
        try (Statement stmt = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM trainer_profiles")) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    private TrainerProfile map(ResultSet rs) throws SQLException {
        TrainerProfile p = new TrainerProfile();
        p.setId(rs.getInt("id")); p.setUserId(rs.getInt("user_id"));
        p.setSpecialization(rs.getString("specialization")); p.setBio(rs.getString("bio"));
        p.setYearsExperience(rs.getInt("years_experience")); p.setCertifications(rs.getString("certifications"));
        p.setPhotoUrl(rs.getString("photo_url"));
        return p;
    }
}
