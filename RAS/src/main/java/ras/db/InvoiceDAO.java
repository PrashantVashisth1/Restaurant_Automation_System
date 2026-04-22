package ras.db;

import ras.model.Invoice;
import java.sql.*;
import java.util.*;

public class InvoiceDAO {

    public static int add(Invoice invoice) throws SQLException {
        String sql = "INSERT INTO invoices (purchase_order_id, ingredient_id, ingredient_name, supplier_name, " +
                     "quantity_received, unit_price, total_amount, invoice_date, cheque_printed, cheque_number, status) " +
                     "VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, invoice.getPurchaseOrderId());
            ps.setInt(2, invoice.getIngredientId());
            ps.setString(3, invoice.getIngredientName());
            ps.setString(4, invoice.getSupplierName());
            ps.setDouble(5, invoice.getQuantityReceived());
            ps.setDouble(6, invoice.getUnitPrice());
            ps.setDouble(7, invoice.getTotalAmount());
            ps.setString(8, invoice.getInvoiceDate());
            ps.setInt(9, invoice.isChequePrinted() ? 1 : 0);
            ps.setString(10, invoice.getChequeNumber());
            ps.setString(11, invoice.getStatus());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            return keys.next() ? keys.getInt(1) : -1;
        }
    }

    public static List<Invoice> getAll() throws SQLException {
        List<Invoice> list = new ArrayList<>();
        String sql = "SELECT * FROM invoices ORDER BY invoice_date DESC";
        try (Statement st = DatabaseManager.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public static void markChequePrinted(int invoiceId, String chequeNumber) throws SQLException {
        String sql = "UPDATE invoices SET cheque_printed=1, cheque_number=?, status='PAID' WHERE id=?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, chequeNumber);
            ps.setInt(2, invoiceId);
            ps.executeUpdate();
        }
    }

    // Cash balance operations
    public static double getCashBalance() throws SQLException {
        ResultSet rs = DatabaseManager.getConnection().createStatement()
            .executeQuery("SELECT balance FROM cash_account WHERE id=1");
        return rs.next() ? rs.getDouble("balance") : 0;
    }

    public static void deductCashBalance(double amount) throws SQLException {
        PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(
            "UPDATE cash_account SET balance = balance - ? WHERE id=1");
        ps.setDouble(1, amount);
        ps.executeUpdate();
        ps.close();
    }

    public static void addCashBalance(double amount) throws SQLException {
        PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(
            "UPDATE cash_account SET balance = balance + ? WHERE id=1");
        ps.setDouble(1, amount);
        ps.executeUpdate();
        ps.close();
    }

    // FR-028: Monthly expenses report
    public static List<String[]> getMonthlyExpensesReport(String yearMonth) throws SQLException {
        List<String[]> rows = new ArrayList<>();
        String sql = "SELECT ingredient_name, supplier_name, quantity_received, unit_price, total_amount, invoice_date, status " +
                     "FROM invoices WHERE strftime('%Y-%m', invoice_date) = ? ORDER BY invoice_date DESC";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, yearMonth);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                rows.add(new String[]{
                    rs.getString("ingredient_name"), rs.getString("supplier_name"),
                    String.format("%.2f", rs.getDouble("quantity_received")),
                    String.format("%.2f", rs.getDouble("unit_price")),
                    String.format("%.2f", rs.getDouble("total_amount")),
                    rs.getString("invoice_date"), rs.getString("status")
                });
            }
        }
        return rows;
    }

    public static int getPendingInvoiceCount() throws SQLException {
        ResultSet rs = DatabaseManager.getConnection().createStatement()
            .executeQuery("SELECT COUNT(*) FROM invoices WHERE status='PENDING'");
        return rs.next() ? rs.getInt(1) : 0;
    }

    private static Invoice map(ResultSet rs) throws SQLException {
        return new Invoice(
            rs.getInt("id"), rs.getInt("purchase_order_id"), rs.getInt("ingredient_id"),
            rs.getString("ingredient_name"), rs.getString("supplier_name"),
            rs.getDouble("quantity_received"), rs.getDouble("unit_price"),
            rs.getString("invoice_date"), rs.getInt("cheque_printed") == 1,
            rs.getString("cheque_number"), rs.getString("status")
        );
    }
}
