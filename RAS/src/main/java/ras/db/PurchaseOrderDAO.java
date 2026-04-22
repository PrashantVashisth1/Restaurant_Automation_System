package ras.db;

import ras.model.PurchaseOrder;
import java.sql.*;
import java.util.*;

public class PurchaseOrderDAO {

    public static void add(PurchaseOrder po) throws SQLException {
        String sql = "INSERT INTO purchase_orders (ingredient_id, ingredient_code, ingredient_name, " +
                     "current_stock, threshold_value, quantity_ordered, order_date, status) VALUES (?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, po.getIngredientId());
            ps.setString(2, po.getIngredientCode());
            ps.setString(3, po.getIngredientName());
            ps.setDouble(4, po.getCurrentStock());
            ps.setDouble(5, po.getThresholdValue());
            ps.setDouble(6, po.getQuantityOrdered());
            ps.setString(7, po.getOrderDate());
            ps.setString(8, po.getStatus());
            ps.executeUpdate();
        }
    }

    public static List<PurchaseOrder> getAll() throws SQLException {
        List<PurchaseOrder> list = new ArrayList<>();
        String sql = "SELECT * FROM purchase_orders ORDER BY order_date DESC";
        try (Statement st = DatabaseManager.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public static List<PurchaseOrder> getByStatus(String status) throws SQLException {
        List<PurchaseOrder> list = new ArrayList<>();
        String sql = "SELECT * FROM purchase_orders WHERE status=? ORDER BY order_date DESC";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, status);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public static void updateStatus(int id, String status) throws SQLException {
        String sql = "UPDATE purchase_orders SET status=? WHERE id=?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public static void updateQuantity(int id, double qty) throws SQLException {
        String sql = "UPDATE purchase_orders SET quantity_ordered=? WHERE id=?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setDouble(1, qty);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public static int getPendingCount() throws SQLException {
        ResultSet rs = DatabaseManager.getConnection().createStatement()
            .executeQuery("SELECT COUNT(*) FROM purchase_orders WHERE status='PENDING'");
        return rs.next() ? rs.getInt(1) : 0;
    }

    private static PurchaseOrder map(ResultSet rs) throws SQLException {
        return new PurchaseOrder(
            rs.getInt("id"), rs.getInt("ingredient_id"),
            rs.getString("ingredient_code"), rs.getString("ingredient_name"),
            rs.getDouble("current_stock"), rs.getDouble("threshold_value"),
            rs.getDouble("quantity_ordered"), rs.getString("order_date"),
            rs.getString("status")
        );
    }
}
