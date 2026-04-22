package ras.db;

import java.sql.*;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:ras_data.db";
    private static Connection conn;

    public static void init() throws SQLException {
        conn = DriverManager.getConnection(DB_URL);
        conn.createStatement().execute("PRAGMA foreign_keys = ON");
        createTables();
        seedDefaultData();
    }

    private static void createTables() throws SQLException {
        Statement stmt = conn.createStatement();

        // Users
        stmt.executeUpdate(
            "CREATE TABLE IF NOT EXISTS users (" +
            "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  username TEXT UNIQUE NOT NULL," +
            "  password_hash TEXT NOT NULL," +
            "  role TEXT NOT NULL," +          // MANAGER / CLERK / STOREKEEPER
            "  locked INTEGER DEFAULT 0," +
            "  failed_attempts INTEGER DEFAULT 0" +
            ")"
        );

        // Menu Items (FR-007)
        stmt.executeUpdate(
            "CREATE TABLE IF NOT EXISTS menu_items (" +
            "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  code TEXT UNIQUE NOT NULL," +
            "  name TEXT NOT NULL," +
            "  category TEXT," +
            "  price REAL NOT NULL," +
            "  is_available INTEGER DEFAULT 1" +
            ")"
        );

        // Price change log (FR-010)
        stmt.executeUpdate(
            "CREATE TABLE IF NOT EXISTS price_change_log (" +
            "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  menu_item_id INTEGER NOT NULL," +
            "  old_price REAL," +
            "  new_price REAL," +
            "  changed_at TEXT," +
            "  manager_id INTEGER," +
            "  FOREIGN KEY(menu_item_id) REFERENCES menu_items(id)," +
            "  FOREIGN KEY(manager_id) REFERENCES users(id)" +
            ")"
        );

        // Orders (FR-001 to FR-006)
        stmt.executeUpdate(
            "CREATE TABLE IF NOT EXISTS orders (" +
            "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  bill_no TEXT UNIQUE NOT NULL," +
            "  order_date TEXT NOT NULL," +
            "  total_amount REAL DEFAULT 0," +
            "  status TEXT DEFAULT 'BILLED'," +
            "  clerk_id INTEGER," +
            "  FOREIGN KEY(clerk_id) REFERENCES users(id)" +
            ")"
        );

        // Order Items
        stmt.executeUpdate(
            "CREATE TABLE IF NOT EXISTS order_items (" +
            "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  order_id INTEGER NOT NULL," +
            "  menu_item_id INTEGER NOT NULL," +
            "  menu_item_code TEXT," +
            "  menu_item_name TEXT," +
            "  quantity INTEGER NOT NULL," +
            "  unit_price REAL NOT NULL," +
            "  FOREIGN KEY(order_id) REFERENCES orders(id)," +
            "  FOREIGN KEY(menu_item_id) REFERENCES menu_items(id)" +
            ")"
        );

        // Ingredients (FR-011)
        stmt.executeUpdate(
            "CREATE TABLE IF NOT EXISTS ingredients (" +
            "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  code TEXT UNIQUE NOT NULL," +
            "  name TEXT NOT NULL," +
            "  unit TEXT," +
            "  current_stock REAL DEFAULT 0," +
            "  threshold_value REAL DEFAULT 0" +
            ")"
        );

        // Ingredient Usage (FR-012 to FR-014)
        stmt.executeUpdate(
            "CREATE TABLE IF NOT EXISTS ingredient_usage (" +
            "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  ingredient_id INTEGER NOT NULL," +
            "  menu_item_id INTEGER," +
            "  quantity_used REAL NOT NULL," +
            "  usage_date TEXT NOT NULL," +
            "  FOREIGN KEY(ingredient_id) REFERENCES ingredients(id)" +
            ")"
        );

        // Purchase Orders (FR-015 to FR-019)
        stmt.executeUpdate(
            "CREATE TABLE IF NOT EXISTS purchase_orders (" +
            "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  ingredient_id INTEGER NOT NULL," +
            "  ingredient_code TEXT," +
            "  ingredient_name TEXT," +
            "  current_stock REAL," +
            "  threshold_value REAL," +
            "  quantity_ordered REAL NOT NULL," +
            "  order_date TEXT NOT NULL," +
            "  status TEXT DEFAULT 'PENDING'," +  // PENDING / FINALIZED / CANCELLED
            "  FOREIGN KEY(ingredient_id) REFERENCES ingredients(id)" +
            ")"
        );

        // Invoices (FR-020 to FR-026)
        stmt.executeUpdate(
            "CREATE TABLE IF NOT EXISTS invoices (" +
            "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  purchase_order_id INTEGER," +
            "  ingredient_id INTEGER NOT NULL," +
            "  ingredient_name TEXT," +
            "  supplier_name TEXT," +
            "  quantity_received REAL NOT NULL," +
            "  unit_price REAL NOT NULL," +
            "  total_amount REAL NOT NULL," +
            "  invoice_date TEXT NOT NULL," +
            "  cheque_printed INTEGER DEFAULT 0," +
            "  cheque_number TEXT," +
            "  status TEXT DEFAULT 'PENDING'," +  // PENDING / PAID
            "  FOREIGN KEY(ingredient_id) REFERENCES ingredients(id)" +
            ")"
        );

        // Cash account
        stmt.executeUpdate(
            "CREATE TABLE IF NOT EXISTS cash_account (" +
            "  id INTEGER PRIMARY KEY," +
            "  balance REAL DEFAULT 0" +
            ")"
        );

        stmt.close();
    }

    private static void seedDefaultData() throws SQLException {
        // Always ensure all 3 default users exist (INSERT OR IGNORE is safe on existing DBs)
        insertUser("manager", hashPassword("manager123"), "MANAGER");
        insertUser("clerk", hashPassword("clerk123"), "CLERK");
        insertUser("storekeeper", hashPassword("store123"), "STOREKEEPER");

        // Only seed menu/inventory if they are empty
        ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM menu_items");
        if (rs.getInt(1) > 0) return;

        // Default menu items
        insertMenuItem("B001", "Butter Chicken", "Main Course", 220.00);
        insertMenuItem("B002", "Paneer Tikka", "Starter", 180.00);
        insertMenuItem("B003", "Dal Makhani", "Main Course", 150.00);
        insertMenuItem("B004", "Naan", "Bread", 30.00);
        insertMenuItem("B005", "Biryani", "Main Course", 260.00);
        insertMenuItem("B006", "Mango Lassi", "Beverage", 80.00);
        insertMenuItem("B007", "Gulab Jamun", "Dessert", 60.00);
        insertMenuItem("B008", "Tandoori Chicken", "Starter", 300.00);
        insertMenuItem("B009", "Veg Pulao", "Main Course", 140.00);
        insertMenuItem("B010", "Masala Chai", "Beverage", 40.00);

        // Default ingredients
        insertIngredient("I001", "Chicken", "kg", 50.0);
        insertIngredient("I002", "Paneer", "kg", 20.0);
        insertIngredient("I003", "Rice", "kg", 100.0);
        insertIngredient("I004", "Flour (Maida)", "kg", 60.0);
        insertIngredient("I005", "Tomatoes", "kg", 30.0);
        insertIngredient("I006", "Onions", "kg", 40.0);
        insertIngredient("I007", "Milk", "litre", 25.0);
        insertIngredient("I008", "Butter", "kg", 10.0);
        insertIngredient("I009", "Cooking Oil", "litre", 15.0);
        insertIngredient("I010", "Sugar", "kg", 20.0);

        // Seed initial cash balance
        conn.createStatement().executeUpdate("INSERT OR IGNORE INTO cash_account (id, balance) VALUES (1, 50000.00)");
    }

    private static void insertUser(String username, String hash, String role) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
            "INSERT OR IGNORE INTO users (username, password_hash, role) VALUES (?,?,?)");
        ps.setString(1, username);
        ps.setString(2, hash);
        ps.setString(3, role);
        ps.executeUpdate();
        ps.close();
    }

    private static void insertMenuItem(String code, String name, String category, double price) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
            "INSERT OR IGNORE INTO menu_items (code, name, category, price) VALUES (?,?,?,?)");
        ps.setString(1, code);
        ps.setString(2, name);
        ps.setString(3, category);
        ps.setDouble(4, price);
        ps.executeUpdate();
        ps.close();
    }

    private static void insertIngredient(String code, String name, String unit, double stock) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
            "INSERT OR IGNORE INTO ingredients (code, name, unit, current_stock, threshold_value) VALUES (?,?,?,?,?)");
        ps.setString(1, code);
        ps.setString(2, name);
        ps.setString(3, unit);
        ps.setDouble(4, stock);
        ps.setDouble(5, 5.0); // default threshold
        ps.executeUpdate();
        ps.close();
    }

    // Simple SHA-256 based password hashing
    public static String hashPassword(String password) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes("UTF-8"));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (Exception e) {
            return password; // fallback
        }
    }

    public static boolean checkPassword(String plain, String hash) {
        return hashPassword(plain).equals(hash);
    }

    public static Connection getConnection() { return conn; }
}
