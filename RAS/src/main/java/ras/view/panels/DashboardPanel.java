package ras.view.panels;

import ras.db.*;
import ras.model.User;
import ras.view.util.UITheme;

import javax.swing.*;
import java.awt.*;

public class DashboardPanel extends JPanel {

    private final User currentUser;

    public DashboardPanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        buildUI();
    }

    private void buildUI() {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.BG_DARK);
        JLabel title = UITheme.createLabel("Dashboard", UITheme.FONT_TITLE, UITheme.TEXT_PRIMARY);
        JLabel greeting = UITheme.createLabel("Welcome back, " + currentUser.getUsername() + "  •  " + new java.util.Date().toString().substring(0, 24),
            UITheme.FONT_SMALL, UITheme.TEXT_SECONDARY);
        header.add(title, BorderLayout.NORTH);
        header.add(greeting, BorderLayout.SOUTH);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        add(header, BorderLayout.NORTH);

        // Stats row
        JPanel stats = new JPanel(new GridLayout(1, 4, 16, 0));
        stats.setBackground(UITheme.BG_DARK);

        try {
            double todayRevenue = OrderDAO.getTodayRevenue();
            int todayOrders = OrderDAO.getTodayOrderCount();
            int pendingInvoices = InvoiceDAO.getPendingInvoiceCount();
            int lowStock = IngredientDAO.getLowStockIngredients().size();
            double cashBalance = InvoiceDAO.getCashBalance();

            stats.setLayout(new GridLayout(1, 5, 14, 0));
            stats.add(UITheme.statCard("Today's Revenue", "₹" + String.format("%.0f", todayRevenue), UITheme.ACCENT_SUCCESS));
            stats.add(UITheme.statCard("Orders Today", String.valueOf(todayOrders), UITheme.ACCENT_PRIMARY));
            stats.add(UITheme.statCard("Cash Balance", "₹" + String.format("%.0f", cashBalance), UITheme.ACCENT_INFO));
            stats.add(UITheme.statCard("Pending Invoices", String.valueOf(pendingInvoices),
                pendingInvoices > 0 ? UITheme.ACCENT_WARNING : UITheme.ACCENT_SUCCESS));
            stats.add(UITheme.statCard("Low Stock Alerts", String.valueOf(lowStock),
                lowStock > 0 ? UITheme.ACCENT_DANGER : UITheme.ACCENT_SUCCESS));
        } catch (Exception e) {
            stats.add(new JLabel("Error loading stats: " + e.getMessage()));
        }

        JPanel statsWrap = new JPanel(new BorderLayout());
        statsWrap.setBackground(UITheme.BG_DARK);
        statsWrap.add(stats, BorderLayout.CENTER);
        statsWrap.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        // Main content area — two columns
        JPanel content = new JPanel(new GridLayout(1, 2, 16, 0));
        content.setBackground(UITheme.BG_DARK);

        // Low Stock Alerts
        JPanel lowStockCard = UITheme.createCard("⚠ Low Stock Ingredients");
        lowStockCard.setLayout(new BorderLayout());
        try {
            java.util.List<ras.model.Ingredient> lowList = IngredientDAO.getLowStockIngredients();
            String[] cols = {"Code", "Ingredient", "Stock", "Threshold", "Unit"};
            Object[][] data = new Object[lowList.size()][5];
            for (int i = 0; i < lowList.size(); i++) {
                ras.model.Ingredient ing = lowList.get(i);
                data[i] = new Object[]{ing.getCode(), ing.getName(),
                    String.format("%.2f", ing.getCurrentStock()),
                    String.format("%.2f", ing.getThresholdValue()), ing.getUnit()};
            }
            JTable table = new JTable(data, cols);
            UITheme.styleTable(table);
            table.setEnabled(false);
            lowStockCard.add(UITheme.scrollPane(table), BorderLayout.CENTER);
            if (lowList.isEmpty()) {
                JLabel ok = UITheme.createLabel("✓ All ingredients above threshold", UITheme.FONT_BODY, UITheme.ACCENT_SUCCESS);
                ok.setHorizontalAlignment(JLabel.CENTER);
                lowStockCard.add(ok, BorderLayout.SOUTH);
            }
        } catch (Exception e) {
            lowStockCard.add(new JLabel("Error: " + e.getMessage()), BorderLayout.CENTER);
        }

        // Recent Orders
        JPanel recentOrdersCard = UITheme.createCard("🧾 Recent Orders (Today)");
        recentOrdersCard.setLayout(new BorderLayout());
        try {
            java.util.List<ras.model.Order> orders = OrderDAO.getAllOrders();
            String today = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
            java.util.List<ras.model.Order> todayOrders = new java.util.ArrayList<>();
            for (ras.model.Order o : orders) {
                if (o.getOrderDate().startsWith(today)) todayOrders.add(o);
                if (todayOrders.size() >= 10) break;
            }
            String[] cols = {"Bill No", "Time", "Amount", "Clerk"};
            Object[][] data = new Object[todayOrders.size()][4];
            for (int i = 0; i < todayOrders.size(); i++) {
                ras.model.Order o = todayOrders.get(i);
                String time = o.getOrderDate().length() > 16 ? o.getOrderDate().substring(11, 16) : o.getOrderDate();
                data[i] = new Object[]{o.getBillNo(), time,
                    "₹" + String.format("%.2f", o.getTotalAmount()), "Clerk #" + o.getClerkId()};
            }
            JTable table = new JTable(data, cols);
            UITheme.styleTable(table);
            table.setEnabled(false);
            recentOrdersCard.add(UITheme.scrollPane(table), BorderLayout.CENTER);
        } catch (Exception e) {
            recentOrdersCard.add(new JLabel("Error: " + e.getMessage()), BorderLayout.CENTER);
        }

        content.add(lowStockCard);
        content.add(recentOrdersCard);

        JPanel center = new JPanel(new BorderLayout(0, 0));
        center.setBackground(UITheme.BG_DARK);
        center.add(statsWrap, BorderLayout.NORTH);
        center.add(content, BorderLayout.CENTER);

        add(center, BorderLayout.CENTER);
    }
}
