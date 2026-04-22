package ras.db;

import ras.model.Ingredient;
import ras.model.IngredientUsage;
import java.sql.*;
import java.util.*;

public class IngredientDAO {

    public static List<Ingredient> getAll() throws SQLException {
        List<Ingredient> list = new ArrayList<>();
        String sql = "SELECT * FROM ingredients ORDER BY name";
        try (Statement st = DatabaseManager.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapIngredient(rs));
        }
        return list;
    }

    public static Ingredient findByCode(String code) throws SQLException {
        String sql = "SELECT * FROM ingredients WHERE code=?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, code);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? mapIngredient(rs) : null;
        }
    }

    public static void add(Ingredient ingredient) throws SQLException {
        String sql = "INSERT INTO ingredients (code, name, unit, current_stock, threshold_value) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, ingredient.getCode());
            ps.setString(2, ingredient.getName());
            ps.setString(3, ingredient.getUnit());
            ps.setDouble(4, ingredient.getCurrentStock());
            ps.setDouble(5, ingredient.getThresholdValue());
            ps.executeUpdate();
        }
    }

    public static void update(Ingredient ingredient) throws SQLException {
        String sql = "UPDATE ingredients SET code=?, name=?, unit=?, current_stock=?, threshold_value=? WHERE id=?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, ingredient.getCode());
            ps.setString(2, ingredient.getName());
            ps.setString(3, ingredient.getUnit());
            ps.setDouble(4, ingredient.getCurrentStock());
            ps.setDouble(5, ingredient.getThresholdValue());
            ps.setInt(6, ingredient.getId());
            ps.executeUpdate();
        }
    }

    // FR-013: Deduct issued quantity from stock
    public static void deductStock(int ingredientId, double qty) throws SQLException {
        String sql = "UPDATE ingredients SET current_stock = MAX(0, current_stock - ?) WHERE id=?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setDouble(1, qty);
            ps.setInt(2, ingredientId);
            ps.executeUpdate();
        }
    }

    // FR-021: Add received stock from invoice
    public static void addStock(int ingredientId, double qty) throws SQLException {
        String sql = "UPDATE ingredients SET current_stock = current_stock + ? WHERE id=?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setDouble(1, qty);
            ps.setInt(2, ingredientId);
            ps.executeUpdate();
        }
    }

    // FR-012: Record ingredient issuance
    public static void recordUsage(IngredientUsage usage) throws SQLException {
        String sql = "INSERT INTO ingredient_usage (ingredient_id, menu_item_id, quantity_used, usage_date) VALUES (?,?,?,?)";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, usage.getIngredientId());
            ps.setInt(2, usage.getMenuItemId());
            ps.setDouble(3, usage.getQuantityUsed());
            ps.setString(4, usage.getUsageDate());
            ps.executeUpdate();
        }
    }

    // FR-015: Compute threshold = ceil(avg 3-day usage) * 2
    public static double computeThreshold(int ingredientId) throws SQLException {
        String sql = "SELECT usage_date, SUM(quantity_used) as daily " +
                     "FROM ingredient_usage WHERE ingredient_id=? " +
                     "GROUP BY usage_date ORDER BY usage_date DESC LIMIT 3";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, ingredientId);
            ResultSet rs = ps.executeQuery();
            double total = 0; int days = 0;
            while (rs.next()) { total += rs.getDouble("daily"); days++; }
            if (days == 0) return 5.0; // default minimum threshold
            double avg = total / days;
            return Math.ceil(avg) * 2;
        }
    }

    // FR-015: Update threshold for all ingredients
    public static void updateAllThresholds() throws SQLException {
        List<Ingredient> all = getAll();
        for (Ingredient ing : all) {
            double threshold = computeThreshold(ing.getId());
            PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(
                "UPDATE ingredients SET threshold_value=? WHERE id=?");
            ps.setDouble(1, threshold);
            ps.setInt(2, ing.getId());
            ps.executeUpdate();
            ps.close();
        }
    }

    public static List<IngredientUsage> getRecentUsage(int ingredientId, int days) throws SQLException {
        List<IngredientUsage> list = new ArrayList<>();
        String sql = "SELECT iu.*, i.name as iname, COALESCE(mi.name, 'General') as miname " +
                     "FROM ingredient_usage iu " +
                     "JOIN ingredients i ON iu.ingredient_id = i.id " +
                     "LEFT JOIN menu_items mi ON iu.menu_item_id = mi.id " +
                     "WHERE iu.ingredient_id=? AND iu.usage_date >= date('now','-" + days + " days','localtime') " +
                     "ORDER BY iu.usage_date DESC";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, ingredientId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new IngredientUsage(
                    rs.getInt("id"), rs.getInt("ingredient_id"), rs.getString("iname"),
                    rs.getInt("menu_item_id"), rs.getString("miname"),
                    rs.getDouble("quantity_used"), rs.getString("usage_date")
                ));
            }
        }
        return list;
    }

    public static List<Ingredient> getLowStockIngredients() throws SQLException {
        List<Ingredient> list = new ArrayList<>();
        String sql = "SELECT * FROM ingredients WHERE current_stock < threshold_value ORDER BY name";
        try (Statement st = DatabaseManager.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapIngredient(rs));
        }
        return list;
    }

    public static void delete(int id) throws SQLException {
        String sql = "DELETE FROM ingredients WHERE id=?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private static Ingredient mapIngredient(ResultSet rs) throws SQLException {
        return new Ingredient(
            rs.getInt("id"), rs.getString("code"), rs.getString("name"),
            rs.getString("unit"), rs.getDouble("current_stock"), rs.getDouble("threshold_value")
        );
    }
}
