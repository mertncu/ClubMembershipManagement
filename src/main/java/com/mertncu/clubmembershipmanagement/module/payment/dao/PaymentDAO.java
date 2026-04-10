package com.mertncu.clubmembershipmanagement.module.payment.dao;

import com.mertncu.clubmembershipmanagement.common.base.BaseDAO;
import com.mertncu.clubmembershipmanagement.config.DatabaseConnection;
import com.mertncu.clubmembershipmanagement.module.payment.model.Payment;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PaymentDAO implements BaseDAO<Payment> {

    public PaymentDAO() { createTableIfNotExists(); }

    private void createTableIfNotExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS payments (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                subscription_id INTEGER NOT NULL,
                amount REAL NOT NULL,
                discount_amount REAL DEFAULT 0,
                coupon_id INTEGER,
                payment_method TEXT NOT NULL,
                status TEXT NOT NULL DEFAULT 'COMPLETED',
                paid_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(id)
            );
            """;
        try (Statement stmt = DatabaseConnection.getConnection().createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public Payment save(Payment p) {
        String sql = "INSERT INTO payments (user_id, subscription_id, amount, discount_amount, coupon_id, payment_method, status, paid_at, created_at) VALUES (?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, p.getUserId());
            ps.setInt(2, p.getSubscriptionId());
            ps.setDouble(3, p.getAmount());
            ps.setDouble(4, p.getDiscountAmount());
            if (p.getCouponId() != null) ps.setInt(5, p.getCouponId()); else ps.setNull(5, Types.INTEGER);
            ps.setString(6, p.getPaymentMethod());
            ps.setString(7, p.getStatus());
            ps.setString(8, LocalDateTime.now().toString());
            ps.setString(9, LocalDateTime.now().toString());
            if (ps.executeUpdate() > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) p.setId(rs.getInt(1));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return p;
    }

    @Override
    public boolean update(Payment p) { return false; /* not needed */ }

    @Override
    public boolean delete(int id) {
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("DELETE FROM payments WHERE id=?")) {
            ps.setInt(1, id); return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    @Override
    public Optional<Payment> findById(int id) {
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM payments WHERE id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return Optional.empty();
    }

    @Override
    public List<Payment> findAll() {
        List<Payment> list = new ArrayList<>();
        try (Statement stmt = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM payments ORDER BY paid_at DESC")) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Payment> findByUserId(int userId) {
        List<Payment> list = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(
                "SELECT * FROM payments WHERE user_id=? ORDER BY paid_at DESC")) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /** Total revenue from all completed payments */
    public double getTotalRevenue() {
        try (Statement stmt = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT SUM(amount) FROM payments WHERE status='COMPLETED'")) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    private Payment map(ResultSet rs) throws SQLException {
        Payment p = new Payment();
        p.setId(rs.getInt("id"));
        p.setUserId(rs.getInt("user_id"));
        p.setSubscriptionId(rs.getInt("subscription_id"));
        p.setAmount(rs.getDouble("amount"));
        p.setDiscountAmount(rs.getDouble("discount_amount"));
        int couponId = rs.getInt("coupon_id");
        p.setCouponId(rs.wasNull() ? null : couponId);
        p.setPaymentMethod(rs.getString("payment_method"));
        p.setStatus(rs.getString("status"));
        String paidAt = rs.getString("paid_at");
        if (paidAt != null) p.setPaidAt(LocalDateTime.parse(paidAt.replace(" ", "T")));
        return p;
    }
}
