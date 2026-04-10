package com.mertncu.clubmembershipmanagement.module.message.dao;

import com.mertncu.clubmembershipmanagement.common.base.BaseDAO;
import com.mertncu.clubmembershipmanagement.config.DatabaseConnection;
import com.mertncu.clubmembershipmanagement.module.message.model.Message;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MessageDAO implements BaseDAO<Message> {

    public MessageDAO() { createTableIfNotExists(); }

    private void createTableIfNotExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS messages (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                sender_id INTEGER NOT NULL,
                receiver_id INTEGER NOT NULL,
                content TEXT NOT NULL,
                is_read INTEGER DEFAULT 0,
                sent_at DATETIME DEFAULT CURRENT_TIMESTAMP
            );
            """;
        try (Statement stmt = DatabaseConnection.getConnection().createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public Message save(Message m) {
        String sql = "INSERT INTO messages (sender_id, receiver_id, content, is_read, sent_at) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, m.getSenderId()); ps.setInt(2, m.getReceiverId());
            ps.setString(3, m.getContent()); ps.setBoolean(4, m.isRead());
            ps.setString(5, LocalDateTime.now().toString());
            if (ps.executeUpdate() > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) m.setId(rs.getInt(1));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return m;
    }

    @Override public boolean update(Message m) { return false; }
    @Override
    public boolean delete(int id) {
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("DELETE FROM messages WHERE id=?")) {
            ps.setInt(1, id); return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
    @Override public Optional<Message> findById(int id) { return Optional.empty(); }
    @Override public List<Message> findAll() { return new ArrayList<>(); }

    /** Fetch conversation between two users (both directions). */
    public List<Message> findConversation(int userA, int userB) {
        String sql = "SELECT * FROM messages WHERE (sender_id=? AND receiver_id=?) OR (sender_id=? AND receiver_id=?) ORDER BY sent_at ASC";
        List<Message> list = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userA); ps.setInt(2, userB);
            ps.setInt(3, userB); ps.setInt(4, userA);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /** Get distinct user IDs that have exchanged messages with the given userId. */
    public List<Integer> findContactUserIds(int userId) {
        String sql = "SELECT DISTINCT CASE WHEN sender_id=? THEN receiver_id ELSE sender_id END AS contact_id FROM messages WHERE sender_id=? OR receiver_id=?";
        List<Integer> ids = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId); ps.setInt(2, userId); ps.setInt(3, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) ids.add(rs.getInt("contact_id"));
        } catch (SQLException e) { e.printStackTrace(); }
        return ids;
    }

    private Message map(ResultSet rs) throws SQLException {
        Message m = new Message();
        m.setId(rs.getInt("id")); m.setSenderId(rs.getInt("sender_id"));
        m.setReceiverId(rs.getInt("receiver_id")); m.setContent(rs.getString("content"));
        m.setRead(rs.getBoolean("is_read"));
        String sa = rs.getString("sent_at");
        if (sa != null) m.setSentAt(LocalDateTime.parse(sa.replace(" ", "T")));
        return m;
    }
}
