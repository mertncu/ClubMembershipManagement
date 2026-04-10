package com.mertncu.clubmembershipmanagement.module.branch.dao;

import com.mertncu.clubmembershipmanagement.common.base.BaseDAO;
import com.mertncu.clubmembershipmanagement.config.DatabaseConnection;
import com.mertncu.clubmembershipmanagement.module.branch.model.GymBranch;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GymBranchDAO implements BaseDAO<GymBranch> {

    public GymBranchDAO() { createTableIfNotExists(); }

    private void createTableIfNotExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS gym_branches (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                address TEXT,
                city TEXT,
                phone TEXT,
                manager_name TEXT,
                capacity INTEGER DEFAULT 0,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP
            );
            """;
        try (Statement stmt = DatabaseConnection.getConnection().createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public GymBranch save(GymBranch b) {
        String sql = "INSERT INTO gym_branches (name, address, city, phone, manager_name, capacity) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, b.getName()); ps.setString(2, b.getAddress());
            ps.setString(3, b.getCity()); ps.setString(4, b.getPhone());
            ps.setString(5, b.getManagerName()); ps.setInt(6, b.getCapacity());
            if (ps.executeUpdate() > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) b.setId(rs.getInt(1));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return b;
    }

    @Override
    public boolean update(GymBranch b) {
        String sql = "UPDATE gym_branches SET name=?, address=?, city=?, phone=?, manager_name=?, capacity=? WHERE id=?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, b.getName()); ps.setString(2, b.getAddress());
            ps.setString(3, b.getCity()); ps.setString(4, b.getPhone());
            ps.setString(5, b.getManagerName()); ps.setInt(6, b.getCapacity());
            ps.setInt(7, b.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    @Override
    public boolean delete(int id) {
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("DELETE FROM gym_branches WHERE id=?")) {
            ps.setInt(1, id); return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    @Override
    public Optional<GymBranch> findById(int id) {
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM gym_branches WHERE id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return Optional.empty();
    }

    @Override
    public List<GymBranch> findAll() {
        List<GymBranch> list = new ArrayList<>();
        try (Statement stmt = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM gym_branches ORDER BY name")) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    private GymBranch map(ResultSet rs) throws SQLException {
        GymBranch b = new GymBranch();
        b.setId(rs.getInt("id")); b.setName(rs.getString("name"));
        b.setAddress(rs.getString("address")); b.setCity(rs.getString("city"));
        b.setPhone(rs.getString("phone")); b.setManagerName(rs.getString("manager_name"));
        b.setCapacity(rs.getInt("capacity"));
        return b;
    }
}
