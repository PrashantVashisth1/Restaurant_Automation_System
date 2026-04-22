package ras.view.panels;

import ras.db.IngredientDAO;
import ras.db.MenuItemDAO;
import ras.model.Ingredient;
import ras.model.IngredientUsage;
import ras.model.MenuItem;
import ras.model.User;
import ras.view.util.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class IngredientUsagePanel extends JPanel {

    private final User currentUser;
    private JTextField ingCodeField, menuItemCodeField, qtyField, dateField;
    private JLabel ingNameLabel, menuItemNameLabel;
    private DefaultTableModel tableModel;

    public IngredientUsagePanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        buildUI();
        loadHistory();
    }

    private void buildUI() {
        JLabel title = UITheme.createLabel("🌿  Ingredient Issuance & Usage Entry", UITheme.FONT_TITLE, UITheme.TEXT_PRIMARY);
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.BG_DARK);
        header.add(title, BorderLayout.WEST);
        header.add(UITheme.createLabel("Requirement: FR-012, FR-013, FR-014", UITheme.FONT_SMALL, UITheme.TEXT_MUTED), BorderLayout.EAST);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        add(header, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildEntryForm(), buildHistoryTable());
        split.setDividerLocation(400);
        split.setBorder(null);
        add(split, BorderLayout.CENTER);
    }

    private JPanel buildEntryForm() {
        JPanel form = UITheme.createCard("Record Ingredient Issuance");
        form.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(7, 4, 7, 4);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        // Ingredient Code
        gc.gridx = 0; gc.gridy = 0;
        form.add(UITheme.createLabel("Ingredient Code:", UITheme.FONT_SMALL, UITheme.TEXT_SECONDARY), gc);
        gc.gridy = 1;
        ingCodeField = UITheme.createTextField(14);
        form.add(ingCodeField, gc);
        gc.gridy = 2;
        JButton ingLookup = UITheme.createButton("🔍 Lookup", UITheme.ACCENT_INFO);
        ingLookup.addActionListener(e -> lookupIngredient());
        ingCodeField.addActionListener(e -> lookupIngredient());
        form.add(ingLookup, gc);
        gc.gridy = 3;
        ingNameLabel = UITheme.createLabel("—", UITheme.FONT_BODY, UITheme.ACCENT_SUCCESS);
        form.add(ingNameLabel, gc);

        // Menu Item Code
        gc.gridy = 4;
        form.add(UITheme.createLabel("For Food Item Code (optional):", UITheme.FONT_SMALL, UITheme.TEXT_SECONDARY), gc);
        gc.gridy = 5;
        menuItemCodeField = UITheme.createTextField(14);
        form.add(menuItemCodeField, gc);
        gc.gridy = 6;
        JButton menuLookup = UITheme.createButton("🔍 Lookup Item", UITheme.ACCENT_INFO);
        menuLookup.addActionListener(e -> lookupMenuItem());
        menuItemCodeField.addActionListener(e -> lookupMenuItem());
        form.add(menuLookup, gc);
        gc.gridy = 7;
        menuItemNameLabel = UITheme.createLabel("—", UITheme.FONT_BODY, UITheme.ACCENT_WARNING);
        form.add(menuItemNameLabel, gc);

        // Quantity
        gc.gridy = 8;
        form.add(UITheme.createLabel("Quantity Issued:", UITheme.FONT_SMALL, UITheme.TEXT_SECONDARY), gc);
        gc.gridy = 9;
        qtyField = UITheme.createTextField(10);
        form.add(qtyField, gc);

        // Date
        gc.gridy = 10;
        form.add(UITheme.createLabel("Usage Date (yyyy-MM-dd):", UITheme.FONT_SMALL, UITheme.TEXT_SECONDARY), gc);
        gc.gridy = 11;
        dateField = UITheme.createTextField(12);
        dateField.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        form.add(dateField, gc);

        gc.gridy = 12; gc.insets = new Insets(16, 4, 4, 4);
        JButton submitBtn = UITheme.createButton("✅  Record Issuance (Deduct Stock)", UITheme.ACCENT_SUCCESS);
        submitBtn.addActionListener(e -> recordUsage());
        form.add(submitBtn, gc);

        // Ingredients quick reference
        gc.gridy = 13; gc.weighty = 1; gc.fill = GridBagConstraints.BOTH;
        gc.insets = new Insets(14, 4, 4, 4);
        try {
            List<Ingredient> ings = IngredientDAO.getAll();
            StringBuilder sb = new StringBuilder("Quick Reference:\n");
            for (Ingredient ing : ings)
                sb.append(String.format("  [%s] %s — Stock: %.2f %s\n",
                    ing.getCode(), ing.getName(), ing.getCurrentStock(), ing.getUnit()));
            JTextArea ref = new JTextArea(sb.toString());
            ref.setFont(UITheme.FONT_MONO);
            ref.setBackground(UITheme.BG_INPUT);
            ref.setForeground(UITheme.TEXT_SECONDARY);
            ref.setEditable(false);
            form.add(UITheme.scrollPane(ref), gc);
        } catch (Exception ignored) {}

        return form;
    }

    private JPanel buildHistoryTable() {
        JPanel tableCard = UITheme.createCard("📋 Usage History (Last 30 Days)");
        tableCard.setLayout(new BorderLayout());

        String[] cols = {"Date", "Ingredient", "For Food Item", "Qty Used"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(tableModel);
        UITheme.styleTable(table);
        tableCard.add(UITheme.scrollPane(table), BorderLayout.CENTER);
        return tableCard;
    }

    private void lookupIngredient() {
        try {
            Ingredient ing = IngredientDAO.findByCode(ingCodeField.getText().trim().toUpperCase());
            if (ing == null) {
                ingNameLabel.setText("❌ Not found");
                ingNameLabel.setForeground(UITheme.ACCENT_DANGER);
            } else {
                ingNameLabel.setText(ing.getName() + " [Stock: " + String.format("%.2f", ing.getCurrentStock()) + " " + ing.getUnit() + "]");
                ingNameLabel.setForeground(UITheme.ACCENT_SUCCESS);
                ingCodeField.putClientProperty("ingredient", ing);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void lookupMenuItem() {
        try {
            String code = menuItemCodeField.getText().trim().toUpperCase();
            if (code.isEmpty()) { menuItemNameLabel.setText("(General use)"); return; }
            MenuItem m = MenuItemDAO.findByCode(code);
            if (m == null) {
                menuItemNameLabel.setText("❌ Not found");
                menuItemNameLabel.setForeground(UITheme.ACCENT_DANGER);
            } else {
                menuItemNameLabel.setText(m.getName());
                menuItemNameLabel.setForeground(UITheme.ACCENT_SUCCESS);
                menuItemCodeField.putClientProperty("menuItem", m);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void recordUsage() {
        Object ingObj = ingCodeField.getClientProperty("ingredient");
        if (ingObj == null || !(ingObj instanceof Ingredient)) {
            JOptionPane.showMessageDialog(this, "Lookup a valid ingredient first."); return;
        }
        Ingredient ing = (Ingredient) ingObj;
        double qty;
        try {
            qty = Double.parseDouble(qtyField.getText().trim());
            if (qty <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter a valid positive quantity."); return;
        }
        String date = dateField.getText().trim();
        Object menuObj = menuItemCodeField.getClientProperty("menuItem");
        int menuItemId = (menuObj instanceof MenuItem) ? ((MenuItem) menuObj).getId() : 0;
        String menuItemName = (menuObj instanceof MenuItem) ? ((MenuItem) menuObj).getName() : "General";

        try {
            IngredientUsage usage = new IngredientUsage(0, ing.getId(), ing.getName(),
                menuItemId, menuItemName, qty, date);
            IngredientDAO.recordUsage(usage);    // FR-012: record
            IngredientDAO.deductStock(ing.getId(), qty);  // FR-013: deduct
            JOptionPane.showMessageDialog(this,
                String.format("Recorded: %.2f %s of %s deducted from stock.", qty, ing.getUnit(), ing.getName()),
                "Success", JOptionPane.INFORMATION_MESSAGE);
            loadHistory();
            // Clear
            ingCodeField.setText(""); menuItemCodeField.setText("");
            qtyField.setText(""); ingNameLabel.setText("—"); menuItemNameLabel.setText("—");
            ingCodeField.putClientProperty("ingredient", null);
            menuItemCodeField.putClientProperty("menuItem", null);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void loadHistory() {
        try {
            List<Ingredient> ings = IngredientDAO.getAll();
            tableModel.setRowCount(0);
            for (Ingredient ing : ings) {
                List<IngredientUsage> history = IngredientDAO.getRecentUsage(ing.getId(), 30);
                for (IngredientUsage u : history) {
                    tableModel.addRow(new Object[]{u.getUsageDate(), u.getIngredientName(),
                        u.getMenuItemName(), String.format("%.2f", u.getQuantityUsed())});
                }
            }
        } catch (Exception ex) { /* ignore */ }
    }
}
