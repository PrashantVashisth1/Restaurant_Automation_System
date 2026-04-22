package ras.db;

import ras.model.Order;
import ras.model.OrderItem;
import java.sql.*;
import java.util.*;

public class OrderDAO {

    public static int createOrder(Order order) throws SQLException {
        String sql = "INSERT INTO orders (bill_no, order_date, total_amount, status, clerk_id) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, order.getBillNo());
            ps.setString(2, order.getOrderDate());
            ps.setDouble(3, order.getTotalAmount());
            ps.setString(4, order.getStatus());
            ps.setInt(5, order.getClerkId());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        }
        return -1;
    }

    public static void addOrderItem(OrderItem item) throws SQLException {
        String sql = "INSERT INTO order_items (order_id, menu_item_id, menu_item_code, menu_item_name, quantity, unit_price) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, item.getOrderId());
            ps.setInt(2, item.getMenuItemId());
            ps.setString(3, item.getMenuItemCode());
            ps.setString(4, item.getMenuItemName());
            ps.setInt(5, item.getQuantity());
            ps.setDouble(6, item.getUnitPrice());
            ps.executeUpdate();
        }
    }

    public static List<Order> getAllOrders() throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders ORDER BY order_date DESC";
        try (Statement st = DatabaseManager.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                orders.add(new Order(rs.getInt("id"), rs.getString("bill_no"),
                    rs.getString("order_date"), rs.getDouble("total_amount"),
                    rs.getString("status"), rs.getInt("clerk_id")));
            }
        }
        return orders;
    }

    public static List<OrderItem> getItemsForOrder(int orderId) throws SQLException {
        List<OrderItem> items = new ArrayList<>();
        String sql = "SELECT * FROM order_items WHERE order_id=?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                items.add(new OrderItem(
                    rs.getInt("id"), rs.getInt("order_id"), rs.getInt("menu_item_id"),
                    rs.getString("menu_item_code"), rs.getString("menu_item_name"),
                    rs.getInt("quantity"), rs.getDouble("unit_price")
                ));
            }
        }
        return items;
    }

    // FR-027: Monthly sales report
    public static List<String[]> getMonthlySalesReport(String yearMonth) throws SQLException {
        List<String[]> rows = new ArrayList<>();
        String sql = "SELECT oi.menu_item_code, oi.menu_item_name, SUM(oi.quantity) as total_qty, " +
                     "oi.unit_price, SUM(oi.quantity * oi.unit_price) as revenue " +
                     "FROM order_items oi JOIN orders o ON oi.order_id = o.id " +
                     "WHERE strftime('%Y-%m', o.order_date) = ? " +
                     "GROUP BY oi.menu_item_id ORDER BY revenue DESC";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, yearMonth);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                rows.add(new String[]{
                    rs.getString("menu_item_code"), rs.getString("menu_item_name"),
                    String.valueOf(rs.getInt("total_qty")),
                    String.format("%.2f", rs.getDouble("unit_price")),
                    String.format("%.2f", rs.getDouble("revenue"))
                });
            }
        }
        return rows;
    }

    // FR-029: Statistical sales report for a period
    public static List<String[]> getStatisticalSalesReport(String fromDate, String toDate) throws SQLException {
        List<String[]> rows = new ArrayList<>();
        String sql = "SELECT oi.menu_item_code, oi.menu_item_name, SUM(oi.quantity) as total_qty, " +
                     "SUM(oi.quantity * oi.unit_price) as revenue " +
                     "FROM order_items oi JOIN orders o ON oi.order_id = o.id " +
                     "WHERE date(o.order_date) BETWEEN ? AND ? " +
                     "GROUP BY oi.menu_item_id ORDER BY total_qty DESC";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, fromDate);
            ps.setString(2, toDate);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                rows.add(new String[]{
                    rs.getString("menu_item_code"), rs.getString("menu_item_name"),
                    String.valueOf(rs.getInt("total_qty")),
                    String.format("%.2f", rs.getDouble("revenue"))
                });
            }
        }
        return rows;
    }

    public static double getTodayRevenue() throws SQLException {
        String sql = "SELECT COALESCE(SUM(total_amount),0) FROM orders WHERE date(order_date)=date('now','localtime')";
        ResultSet rs = DatabaseManager.getConnection().createStatement().executeQuery(sql);
        return rs.next() ? rs.getDouble(1) : 0;
    }

    public static int getTodayOrderCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM orders WHERE date(order_date)=date('now','localtime')";
        ResultSet rs = DatabaseManager.getConnection().createStatement().executeQuery(sql);
        return rs.next() ? rs.getInt(1) : 0;
    }
}
