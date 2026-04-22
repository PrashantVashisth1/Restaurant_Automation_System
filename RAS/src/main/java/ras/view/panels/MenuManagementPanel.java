package ras.view.panels;

import ras.db.MenuItemDAO;
import ras.model.MenuItem;
import ras.model.User;
import ras.view.util.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MenuManagementPanel extends JPanel {

    private final User currentUser;
    private DefaultTableModel tableModel;
    private JTable menuTable;
    private JTextField codeField, nameField, categoryField, priceField;
    private List<MenuItem> menuItems;

    public MenuManagementPanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        buildUI();
        loadData();
    }

    private void buildUI() {
        JLabel title = UITheme.createLabel("🍕  Menu Management & Price Control", UITheme.FONT_TITLE, UITheme.TEXT_PRIMARY);
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.BG_DARK);
        header.add(title, BorderLayout.WEST);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        add(header, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildForm(), buildTable());
        split.setDividerLocation(360);
        split.setBorder(null);
        split.setBackground(UITheme.BG_DARK);
        add(split, BorderLayout.CENTER);
    }

    private JPanel buildForm() {
        JPanel form = UITheme.createCard("Add / Edit Item");
        form.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 4, 6, 4);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        String[][] fields = {{"Item Code (e.g. B011)", "code"}, {"Item Name", "name"},
            {"Category", "category"}, {"Price (₹)", "price"}};

        codeField     = UITheme.createTextField(16);
        nameField     = UITheme.createTextField(16);
        categoryField = UITheme.createTextField(16);
        priceField    = UITheme.createTextField(10);

        JTextField[] tfs = {codeField, nameField, categoryField, priceField};
        for (int i = 0; i < fields.length; i++) {
            gc.gridx = 0; gc.gridy = i * 2;
            form.add(UITheme.createLabel(fields[i][0], UITheme.FONT_SMALL, UITheme.TEXT_SECONDARY), gc);
            gc.gridy = i * 2 + 1;
            form.add(tfs[i], gc);
        }

        gc.gridy = 8; gc.insets = new Insets(14, 4, 4, 4);
        JButton addBtn = UITheme.createButton("➕  Add Item", UITheme.ACCENT_SUCCESS);
        addBtn.addActionListener(e -> addItem());
        form.add(addBtn, gc);

        gc.gridy = 9; gc.insets = new Insets(4, 4, 4, 4);
        JButton updatePriceBtn = UITheme.createButton("💲  Update Price", UITheme.ACCENT_WARNING);
        updatePriceBtn.addActionListener(e -> updatePrice());
        form.add(updatePriceBtn, gc);

        gc.gridy = 10;
        JButton updateBtn = UITheme.createButton("✏  Update Item", UITheme.ACCENT_PRIMARY);
        updateBtn.addActionListener(e -> updateItem());
        form.add(updateBtn, gc);

        gc.gridy = 11;
        JButton deleteBtn = UITheme.createButton("🗑  Delete Item", UITheme.ACCENT_DANGER);
        deleteBtn.addActionListener(e -> deleteItem());
        form.add(deleteBtn, gc);

        gc.gridy = 12;
        JButton clearBtn = UITheme.createButton("Clear Fields", UITheme.TEXT_MUTED);
        clearBtn.addActionListener(e -> clearFields());
        form.add(clearBtn, gc);

        // Price log section
        gc.gridy = 13; gc.insets = new Insets(20, 4, 4, 4);
        form.add(UITheme.createLabel("── Price Change Log ──", UITheme.FONT_SMALL, UITheme.TEXT_MUTED), gc);
        gc.gridy = 14; gc.weighty = 1; gc.fill = GridBagConstraints.BOTH;
        JTextArea logArea = new JTextArea(6, 20);
        logArea.setFont(UITheme.FONT_MONO);
        logArea.setBackground(UITheme.BG_INPUT);
        logArea.setForeground(UITheme.TEXT_SECONDARY);
        logArea.setEditable(false);
        JScrollPane logSp = new JScrollPane(logArea);
        form.add(logSp, gc);

        // Load log
        SwingUtilities.invokeLater(() -> {
            try {
                List<String[]> log = MenuItemDAO.getPriceChangeLog();
                StringBuilder sb = new StringBuilder();
                for (String[] row : log)
                    sb.append(String.format("[%s] %s (code:%s): ₹%s → ₹%s by %s\n",
                        row[0], row[1], row[2], row[3], row[4], row[5]));
                logArea.setText(sb.length() > 0 ? sb.toString() : "(No price changes logged yet)");
            } catch (Exception ex) { logArea.setText("Error loading log."); }
        });

        return form;
    }

    private JPanel buildTable() {
        JPanel tableCard = UITheme.createCard("All Menu Items");
        tableCard.setLayout(new BorderLayout());

        String[] cols = {"ID", "Code", "Name", "Category", "Price (₹)", "Available"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        menuTable = new JTable(tableModel);
        UITheme.styleTable(menuTable);
        menuTable.getSelectionModel().addListSelectionListener(e -> fillFormFromTable());
        tableCard.add(UITheme.scrollPane(menuTable), BorderLayout.CENTER);
        return tableCard;
    }

    private void loadData() {
        try {
            menuItems = MenuItemDAO.getAll();
            tableModel.setRowCount(0);
            for (MenuItem m : menuItems) {
                tableModel.addRow(new Object[]{
                    m.getId(), m.getCode(), m.getName(), m.getCategory(),
                    String.format("%.2f", m.getPrice()), m.isAvailable() ? "Yes" : "No"
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading menu: " + e.getMessage());
        }
    }

    private void fillFormFromTable() {
        int row = menuTable.getSelectedRow();
        if (row < 0 || menuItems == null || row >= menuItems.size()) return;
        MenuItem m = menuItems.get(row);
        codeField.setText(m.getCode());
        nameField.setText(m.getName());
        categoryField.setText(m.getCategory());
        priceField.setText(String.format("%.2f", m.getPrice()));
    }

    private void addItem() {
        try {
            String code = codeField.getText().trim().toUpperCase();
            String name = nameField.getText().trim();
            String cat  = categoryField.getText().trim();
            double price = Double.parseDouble(priceField.getText().trim());
            if (code.isEmpty() || name.isEmpty()) { JOptionPane.showMessageDialog(this, "Code and Name required."); return; }
            MenuItem m = new MenuItem(0, code, name, cat, price, true);
            MenuItemDAO.add(m);
            loadData();
            clearFields();
            JOptionPane.showMessageDialog(this, "Item added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter a valid price.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updatePrice() {
        int row = menuTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select an item first."); return; }
        MenuItem m = menuItems.get(row);
        try {
            double newPrice = Double.parseDouble(priceField.getText().trim());
            double oldPrice = m.getPrice();
            MenuItemDAO.updatePrice(m.getId(), newPrice, currentUser.getId(), oldPrice);
            loadData();
            JOptionPane.showMessageDialog(this,
                String.format("Price updated: %s\n₹%.2f → ₹%.2f", m.getName(), oldPrice, newPrice),
                "Price Updated", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter a valid price.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateItem() {
        int row = menuTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select an item to update."); return; }
        MenuItem m = menuItems.get(row);
        try {
            m.setCode(codeField.getText().trim().toUpperCase());
            m.setName(nameField.getText().trim());
            m.setCategory(categoryField.getText().trim());
            m.setPrice(Double.parseDouble(priceField.getText().trim()));
            MenuItemDAO.update(m);
            loadData();
            JOptionPane.showMessageDialog(this, "Item updated!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteItem() {
        int row = menuTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select an item to delete."); return; }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this item?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            MenuItemDAO.delete(menuItems.get(row).getId());
            loadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFields() {
        codeField.setText(""); nameField.setText("");
        categoryField.setText(""); priceField.setText("");
        menuTable.clearSelection();
    }
}
