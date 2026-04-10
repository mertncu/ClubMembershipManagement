package com.mertncu.clubmembershipmanagement.module.diet.dao;

import com.mertncu.clubmembershipmanagement.common.base.BaseDAO;
import com.mertncu.clubmembershipmanagement.config.DatabaseConnection;
import com.mertncu.clubmembershipmanagement.module.diet.model.DietItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DietItemDAO implements BaseDAO<DietItem> {

    public DietItemDAO() { createTableIfNotExists(); }

    private void createTableIfNotExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS diet_items (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                program_id INTEGER NOT NULL REFERENCES diet_programs(id) ON DELETE CASCADE,
                meal_type TEXT NOT NULL,
                food_name TEXT NOT NULL,
                quantity REAL DEFAULT 100,
                calories INTEGER DEFAULT 0,
                protein REAL DEFAULT 0,
                carbs REAL DEFAULT 0,
                fat REAL DEFAULT 0,
                day_of_week TEXT DEFAULT 'ALL'
            );
            """;
        try (Statement stmt = DatabaseConnection.getConnection().createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public DietItem save(DietItem item) {
        String sql = "INSERT INTO diet_items (program_id, meal_type, food_name, quantity, calories, protein, carbs, fat, day_of_week) VALUES (?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, item.getProgramId()); ps.setString(2, item.getMealType());
            ps.setString(3, item.getFoodName()); ps.setDouble(4, item.getQuantity());
            ps.setInt(5, item.getCalories()); ps.setDouble(6, item.getProtein());
            ps.setDouble(7, item.getCarbs()); ps.setDouble(8, item.getFat());
            ps.setString(9, item.getDayOfWeek());
            if (ps.executeUpdate() > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) item.setId(rs.getInt(1));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return item;
    }

    @Override public boolean update(DietItem d) { return false; }

    @Override
    public boolean delete(int id) {
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("DELETE FROM diet_items WHERE id=?")) {
            ps.setInt(1, id); return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    @Override public Optional<DietItem> findById(int id) { return Optional.empty(); }

    @Override
    public List<DietItem> findAll() { return new ArrayList<>(); }

    public List<DietItem> findByProgramId(int programId) {
        List<DietItem> list = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(
                "SELECT * FROM diet_items WHERE program_id=? ORDER BY meal_type, id")) {
            ps.setInt(1, programId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    private DietItem map(ResultSet rs) throws SQLException {
        DietItem i = new DietItem();
        i.setId(rs.getInt("id")); i.setProgramId(rs.getInt("program_id"));
        i.setMealType(rs.getString("meal_type")); i.setFoodName(rs.getString("food_name"));
        i.setQuantity(rs.getDouble("quantity")); i.setCalories(rs.getInt("calories"));
        i.setProtein(rs.getDouble("protein")); i.setCarbs(rs.getDouble("carbs"));
        i.setFat(rs.getDouble("fat")); i.setDayOfWeek(rs.getString("day_of_week"));
        return i;
    }
}
