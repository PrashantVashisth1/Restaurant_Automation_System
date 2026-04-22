package ras.db;

import ras.model.MenuItem;
import java.sql.*;
import java.util.*;

public class MenuItemDAO {

    public static List<MenuItem> getAll() throws SQLException {
        List<MenuItem> list = new ArrayList<>();
        String sql = "SELECT * FROM menu_items ORDER BY category, name";
        try (Statement st = DatabaseManager.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public static MenuItem findByCode(String code) throws SQLException {
        String sql = "SELECT * FROM menu_items WHERE code = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, code);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? map(rs) : null;
        }
    }

    public static void add(MenuItem item) throws SQLException {
        String sql = "INSERT INTO menu_items (code, name, category, price, is_available) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, item.getCode());
            ps.setString(2, item.getName());
            ps.setString(3, item.getCategory());
            ps.setDouble(4, item.getPrice());
            ps.setInt(5, item.isAvailable() ? 1 : 0);
            ps.executeUpdate();
        }
    }

    public static void update(MenuItem item) throws SQLException {
        String sql = "UPDATE menu_items SET code=?, name=?, category=?, price=?, is_available=? WHERE id=?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, item.getCode());
            ps.setString(2, item.getName());
            ps.setString(3, item.getCategory());
            ps.setDouble(4, item.getPrice());
            ps.setInt(5, item.isAvailable() ? 1 : 0);
            ps.setInt(6, item.getId());
            ps.executeUpdate();
        }
    }

    public static void updatePrice(int itemId, double newPrice, int managerId, double oldPrice) throws SQLException {
        Connection conn = DatabaseManager.getConnection();
        // Update price
        PreparedStatement ps = conn.prepareStatement("UPDATE menu_items SET price=? WHERE id=?");
        ps.setDouble(1, newPrice);
        ps.setInt(2, itemId);
        ps.executeUpdate();
        ps.close();
        // Log change (FR-010)
        ps = conn.prepareStatement(
            "INSERT INTO price_change_log (menu_item_id, old_price, new_price, changed_at, manager_id) VALUES (?,?,?,datetime('now','localtime'),?)");
        ps.setInt(1, itemId);
        ps.setDouble(2, oldPrice);
        ps.setDouble(3, newPrice);
        ps.setInt(4, managerId);
        ps.executeUpdate();
        ps.close();
    }

    public static void delete(int id) throws SQLException {
        String sql = "DELETE FROM menu_items WHERE id=?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public static List<String[]> getPriceChangeLog() throws SQLException {
        List<String[]> log = new ArrayList<>();
        String sql = "SELECT pcl.changed_at, mi.name, mi.code, pcl.old_price, pcl.new_price, u.username " +
                     "FROM price_change_log pcl " +
                     "JOIN menu_items mi ON pcl.menu_item_id = mi.id " +
                     "JOIN users u ON pcl.manager_id = u.id " +
                     "ORDER BY pcl.changed_at DESC";
        try (Statement st = DatabaseManager.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                log.add(new String[]{
                    rs.getString("changed_at"),
                    rs.getString("name"),
                    rs.getString("code"),
                    String.format("%.2f", rs.getDouble("old_price")),
                    String.format("%.2f", rs.getDouble("new_price")),
                    rs.getString("username")
                });
            }
        }
        return log;
    }

    private static MenuItem map(ResultSet rs) throws SQLException {
        return new MenuItem(
            rs.getInt("id"), rs.getString("code"), rs.getString("name"),
            rs.getString("category"), rs.getDouble("price"), rs.getInt("is_available") == 1
        );
    }
}
