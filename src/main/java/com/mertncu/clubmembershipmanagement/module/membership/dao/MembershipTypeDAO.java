package com.mertncu.clubmembershipmanagement.module.membership.dao;

import com.mertncu.clubmembershipmanagement.common.base.BaseDAO;
import com.mertncu.clubmembershipmanagement.config.DatabaseConnection;
import com.mertncu.clubmembershipmanagement.module.membership.model.MembershipType;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MembershipTypeDAO implements BaseDAO<MembershipType> {

    public MembershipTypeDAO() {
        createTableIfNotExists();
        seedDefaultPlans();
    }

    private void createTableIfNotExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS membership_types (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                duration_months INTEGER NOT NULL,
                price REAL NOT NULL,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
            );
        """;
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sql);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void seedDefaultPlans() {
        if (findAll().isEmpty()) {
            save(new MembershipType("1 Month Plan", 1, 500.0));
            save(new MembershipType("3 Months Plan", 3, 1350.0));
            save(new MembershipType("6 Months Plan", 6, 2500.0));
            save(new MembershipType("12 Months VIP Plan", 12, 4500.0));
        }
    }

    @Override
    public MembershipType save(MembershipType mType) {
        String sql = "INSERT INTO membership_types (name, duration_months, price, created_at) VALUES (?, ?, ?, ?)";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                
                pstmt.setString(1, mType.getName());
                pstmt.setInt(2, mType.getDurationMonths());
                pstmt.setDouble(3, mType.getPrice());
                pstmt.setString(4, LocalDateTime.now().toString());
                
                if (pstmt.executeUpdate() > 0) {
                    try (ResultSet rs = pstmt.getGeneratedKeys()) {
                        if (rs.next()) mType.setId(rs.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return mType;
    }

    @Override
    public boolean update(MembershipType mType) {
        String sql = "UPDATE membership_types SET name = ?, duration_months = ?, price = ?, updated_at = ? WHERE id = ?";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, mType.getName());
                pstmt.setInt(2, mType.getDurationMonths());
                pstmt.setDouble(3, mType.getPrice());
                pstmt.setString(4, LocalDateTime.now().toString());
                pstmt.setInt(5, mType.getId());
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM membership_types WHERE id = ?";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    @Override
    public Optional<MembershipType> findById(int id) {
        String sql = "SELECT * FROM membership_types WHERE id = ?";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) return Optional.of(extractType(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return Optional.empty();
    }

    @Override
    public List<MembershipType> findAll() {
        List<MembershipType> types = new ArrayList<>();
        String sql = "SELECT * FROM membership_types";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) types.add(extractType(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return types;
    }

    private MembershipType extractType(ResultSet rs) throws SQLException {
        MembershipType t = new MembershipType();
        t.setId(rs.getInt("id"));
        t.setName(rs.getString("name"));
        t.setDurationMonths(rs.getInt("duration_months"));
        t.setPrice(rs.getDouble("price"));
        String c = rs.getString("created_at");
        if (c != null) t.setCreatedAt(LocalDateTime.parse(c));
        return t;
    }
}
