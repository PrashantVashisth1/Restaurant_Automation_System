package ras.view.panels;

import ras.db.IngredientDAO;
import ras.model.Ingredient;
import ras.model.User;
import ras.view.util.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class InventoryPanel extends JPanel {

    private final User currentUser;
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField codeField, nameField, unitField, stockField, threshField;
    private List<Ingredient> ingredients;

    public InventoryPanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        buildUI();
        loadData();
    }

    private void buildUI() {
        JLabel title = UITheme.createLabel("📦  Ingredient Inventory", UITheme.FONT_TITLE, UITheme.TEXT_PRIMARY);
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.BG_DARK);
        header.add(title, BorderLayout.WEST);

        JButton refreshThreshBtn = UITheme.createButton("🔄 Recalculate All Thresholds (FR-015)", UITheme.ACCENT_INFO);
        refreshThreshBtn.addActionListener(e -> recalcThresholds());
        header.add(refreshThreshBtn, BorderLayout.EAST);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        add(header, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildForm(), buildTable());
        split.setDividerLocation(340);
        split.setBorder(null);
        add(split, BorderLayout.CENTER);
    }

    private JPanel buildForm() {
        JPanel form = UITheme.createCard("Add / Edit Ingredient");
        form.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 4, 6, 4);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        codeField  = UITheme.createTextField(14); nameField = UITheme.createTextField(14);
        unitField  = UITheme.createTextField(10); stockField = UITheme.createTextField(10);
        threshField = UITheme.createTextField(10);

        String[] labels = {"Ingredient Code", "Ingredient Name", "Unit (kg/litre/pcs)", "Current Stock", "Manual Threshold"};
        JTextField[] fields = {codeField, nameField, unitField, stockField, threshField};
        for (int i = 0; i < labels.length; i++) {
            gc.gridx = 0; gc.gridy = i * 2;
            form.add(UITheme.createLabel(labels[i], UITheme.FONT_SMALL, UITheme.TEXT_SECONDARY), gc);
            gc.gridy = i * 2 + 1;
            form.add(fields[i], gc);
        }

        gc.gridy = 10; gc.insets = new Insets(14, 4, 4, 4);
        JButton addBtn = UITheme.createButton("➕  Add Ingredient", UITheme.ACCENT_SUCCESS);
        addBtn.addActionListener(e -> addIngredient());
        form.add(addBtn, gc);

        gc.gridy = 11; gc.insets = new Insets(4, 4, 4, 4);
        JButton updateBtn = UITheme.createButton("✏  Update", UITheme.ACCENT_PRIMARY);
        updateBtn.addActionListener(e -> updateIngredient());
        form.add(updateBtn, gc);

        gc.gridy = 12;
        JButton deleteBtn = UITheme.createButton("🗑  Delete", UITheme.ACCENT_DANGER);
        deleteBtn.addActionListener(e -> deleteIngredient());
        form.add(deleteBtn, gc);

        return form;
    }

    private JPanel buildTable() {
        JPanel tableCard = UITheme.createCard("All Ingredients");
        tableCard.setLayout(new BorderLayout());
        String[] cols = {"Code", "Name", "Unit", "Stock", "Threshold", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UITheme.styleTable(table);
        // Color low-stock rows red
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                String status = (String) tableModel.getValueAt(row, 5);
                if ("LOW STOCK".equals(status)) {
                    setBackground(sel ? UITheme.TABLE_SELECT : new Color(80, 30, 30));
                    setForeground(UITheme.ACCENT_DANGER);
                } else {
                    setBackground(sel ? UITheme.TABLE_SELECT : (row % 2 == 0 ? UITheme.BG_CARD : UITheme.TABLE_ROW_ALT));
                    setForeground(UITheme.TEXT_PRIMARY);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return this;
            }
        });
        table.getSelectionModel().addListSelectionListener(e -> fillFormFromTable());
        tableCard.add(UITheme.scrollPane(table), BorderLayout.CENTER);
        return tableCard;
    }

    private void loadData() {
        try {
            ingredients = IngredientDAO.getAll();
            tableModel.setRowCount(0);
            for (Ingredient ing : ingredients) {
                tableModel.addRow(new Object[]{
                    ing.getCode(), ing.getName(), ing.getUnit(),
                    String.format("%.2f", ing.getCurrentStock()),
                    String.format("%.2f", ing.getThresholdValue()),
                    ing.isBelowThreshold() ? "LOW STOCK" : "OK"
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void fillFormFromTable() {
        int row = table.getSelectedRow();
        if (row < 0 || ingredients == null || row >= ingredients.size()) return;
        Ingredient ing = ingredients.get(row);
        codeField.setText(ing.getCode());
        nameField.setText(ing.getName());
        unitField.setText(ing.getUnit());
        stockField.setText(String.format("%.2f", ing.getCurrentStock()));
        threshField.setText(String.format("%.2f", ing.getThresholdValue()));
    }

    private void addIngredient() {
        try {
            Ingredient ing = new Ingredient(0,
                codeField.getText().trim().toUpperCase(),
                nameField.getText().trim(),
                unitField.getText().trim(),
                Double.parseDouble(stockField.getText().trim()),
                Double.parseDouble(threshField.getText().trim()));
            IngredientDAO.add(ing);
            loadData();
            clearFields();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void updateIngredient() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select an ingredient."); return; }
        try {
            Ingredient ing = ingredients.get(row);
            ing.setCode(codeField.getText().trim().toUpperCase());
            ing.setName(nameField.getText().trim());
            ing.setUnit(unitField.getText().trim());
            ing.setCurrentStock(Double.parseDouble(stockField.getText().trim()));
            ing.setThresholdValue(Double.parseDouble(threshField.getText().trim()));
            IngredientDAO.update(ing);
            loadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void deleteIngredient() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this ingredient?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            IngredientDAO.delete(ingredients.get(row).getId());
            loadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void recalcThresholds() {
        try {
            IngredientDAO.updateAllThresholds();
            loadData();
            JOptionPane.showMessageDialog(this, "Thresholds recalculated based on 3-day average consumption × 2 (FR-015).",
                "Done", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void clearFields() {
        codeField.setText(""); nameField.setText(""); unitField.setText("");
        stockField.setText(""); threshField.setText(""); table.clearSelection();
    }
}
