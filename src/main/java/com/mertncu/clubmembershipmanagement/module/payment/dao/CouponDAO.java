package com.mertncu.clubmembershipmanagement.module.payment.dao;

import com.mertncu.clubmembershipmanagement.common.base.BaseDAO;
import com.mertncu.clubmembershipmanagement.config.DatabaseConnection;
import com.mertncu.clubmembershipmanagement.module.payment.model.Coupon;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CouponDAO implements BaseDAO<Coupon> {

    public CouponDAO() { createTableIfNotExists(); }

    private void createTableIfNotExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS coupons (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                code TEXT NOT NULL UNIQUE,
                discount_percent REAL NOT NULL,
                valid_until DATE,
                is_active INTEGER DEFAULT 1,
                description TEXT,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP
            );
            """;
        try (Statement stmt = DatabaseConnection.getConnection().createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public Coupon save(Coupon c) {
        String sql = "INSERT OR IGNORE INTO coupons (code, discount_percent, valid_until, is_active, description) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getCode());
            ps.setDouble(2, c.getDiscountPercent());
            ps.setString(3, c.getValidUntil() != null ? c.getValidUntil().toString() : null);
            ps.setBoolean(4, c.isActive());
            ps.setString(5, c.getDescription());
            if (ps.executeUpdate() > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) c.setId(rs.getInt(1));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return c;
    }

    @Override
    public boolean update(Coupon c) {
        String sql = "UPDATE coupons SET discount_percent=?, valid_until=?, is_active=?, description=? WHERE id=?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setDouble(1, c.getDiscountPercent());
            ps.setString(2, c.getValidUntil() != null ? c.getValidUntil().toString() : null);
            ps.setBoolean(3, c.isActive());
            ps.setString(4, c.getDescription());
            ps.setInt(5, c.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    @Override
    public boolean delete(int id) {
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("DELETE FROM coupons WHERE id=?")) {
            ps.setInt(1, id); return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    @Override
    public Optional<Coupon> findById(int id) {
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM coupons WHERE id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return Optional.empty();
    }

    @Override
    public List<Coupon> findAll() {
        List<Coupon> list = new ArrayList<>();
        try (Statement stmt = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM coupons ORDER BY id DESC")) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /** Finds a coupon by its code string — used for validation during payment. */
    public Optional<Coupon> findByCode(String code) {
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(
                "SELECT * FROM coupons WHERE UPPER(code)=UPPER(?)")) {
            ps.setString(1, code);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return Optional.empty();
    }

    private Coupon map(ResultSet rs) throws SQLException {
        Coupon c = new Coupon();
        c.setId(rs.getInt("id"));
        c.setCode(rs.getString("code"));
        c.setDiscountPercent(rs.getDouble("discount_percent"));
        String d = rs.getString("valid_until");
        c.setValidUntil(d != null ? LocalDate.parse(d) : null);
        c.setActive(rs.getBoolean("is_active"));
        c.setDescription(rs.getString("description"));
        return c;
    }
}
