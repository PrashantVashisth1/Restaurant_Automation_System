package ras.view.panels;

import ras.db.IngredientDAO;
import ras.db.PurchaseOrderDAO;
import ras.model.Ingredient;
import ras.model.PurchaseOrder;
import ras.model.User;
import ras.view.util.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PurchaseOrderPanel extends JPanel {

    private final User currentUser;
    private DefaultTableModel tableModel;
    private JTable poTable;
    private List<PurchaseOrder> poList;

    public PurchaseOrderPanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        buildUI();
        loadData();
    }

    private void buildUI() {
        // Header
        JLabel title = UITheme.createLabel("📋  Purchase Orders", UITheme.FONT_TITLE, UITheme.TEXT_PRIMARY);
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.BG_DARK);
        header.add(title, BorderLayout.WEST);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setBackground(UITheme.BG_DARK);
        JButton autoGenBtn = UITheme.createButton("⚡ Auto-Generate POs (FR-016/017)", UITheme.ACCENT_PRIMARY);
        autoGenBtn.addActionListener(e -> autoGeneratePOs());
        JButton refreshBtn = UITheme.createButton("🔄 Refresh", UITheme.ACCENT_INFO);
        refreshBtn.addActionListener(e -> loadData());
        btnPanel.add(autoGenBtn);
        btnPanel.add(refreshBtn);
        header.add(btnPanel, BorderLayout.EAST);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        add(header, BorderLayout.NORTH);

        // Table
        JPanel tableCard = UITheme.createCard("All Purchase Orders");
        tableCard.setLayout(new BorderLayout());

        String[] cols = {"ID", "Ingredient", "Code", "Current Stock", "Threshold", "Qty Ordered", "Date", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        poTable = new JTable(tableModel);
        UITheme.styleTable(poTable);
        tableCard.add(UITheme.scrollPane(poTable), BorderLayout.CENTER);

        // Action buttons
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        actions.setBackground(UITheme.BG_CARD);

        JButton finalizeBtn = UITheme.createButton("✅ Finalize Selected (FR-019)", UITheme.ACCENT_SUCCESS);
        finalizeBtn.addActionListener(e -> updateStatus("FINALIZED"));

        JButton cancelBtn = UITheme.createButton("❌ Cancel Selected", UITheme.ACCENT_DANGER);
        cancelBtn.addActionListener(e -> updateStatus("CANCELLED"));

        JButton editQtyBtn = UITheme.createButton("✏ Edit Quantity (FR-019)", UITheme.ACCENT_WARNING);
        editQtyBtn.addActionListener(e -> editQuantity());

        JButton viewBtn = UITheme.createButton("📄 View PO Details", UITheme.ACCENT_INFO);
        viewBtn.addActionListener(e -> viewPODetails());

        actions.add(finalizeBtn);
        actions.add(cancelBtn);
        actions.add(editQtyBtn);
        actions.add(viewBtn);
        tableCard.add(actions, BorderLayout.SOUTH);

        add(tableCard, BorderLayout.CENTER);
    }

    private void loadData() {
        try {
            poList = PurchaseOrderDAO.getAll();
            tableModel.setRowCount(0);
            for (PurchaseOrder po : poList) {
                tableModel.addRow(new Object[]{
                    po.getId(), po.getIngredientName(), po.getIngredientCode(),
                    String.format("%.2f", po.getCurrentStock()),
                    String.format("%.2f", po.getThresholdValue()),
                    String.format("%.2f", po.getQuantityOrdered()),
                    po.getOrderDate(), po.getStatus()
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    // FR-016, FR-017: Auto-generate POs for below-threshold ingredients
    private void autoGeneratePOs() {
        try {
            // First recalculate thresholds
            IngredientDAO.updateAllThresholds();
            List<Ingredient> lowStock = IngredientDAO.getLowStockIngredients();
            if (lowStock.isEmpty()) {
                JOptionPane.showMessageDialog(this, "✅ All ingredients are above threshold. No POs needed.",
                    "No Action Required", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            int generated = 0;
            String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            StringBuilder report = new StringBuilder("Purchase Orders Generated:\n\n");
            for (Ingredient ing : lowStock) {
                double needed = Math.max(ing.getThresholdValue() - ing.getCurrentStock(), ing.getThresholdValue());
                PurchaseOrder po = new PurchaseOrder(0, ing.getId(), ing.getCode(), ing.getName(),
                    ing.getCurrentStock(), ing.getThresholdValue(), needed, today, "PENDING");
                PurchaseOrderDAO.add(po);
                report.append(String.format("• %s [%s]: need %.2f %s\n",
                    ing.getName(), ing.getCode(), needed, ing.getUnit()));
                generated++;
            }
            loadData();
            JOptionPane.showMessageDialog(this,
                String.format("Generated %d Purchase Order(s)!\n\n%s", generated, report.toString()),
                "POs Generated", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void updateStatus(String newStatus) {
        int row = poTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a PO first."); return; }
        try {
            PurchaseOrder po = poList.get(row);
            PurchaseOrderDAO.updateStatus(po.getId(), newStatus);
            loadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void editQuantity() {
        int row = poTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a PO first."); return; }
        PurchaseOrder po = poList.get(row);
        String input = JOptionPane.showInputDialog(this,
            "Current qty: " + po.getQuantityOrdered() + "\nEnter new quantity:",
            "Edit PO Quantity", JOptionPane.PLAIN_MESSAGE);
        if (input == null) return;
        try {
            double newQty = Double.parseDouble(input.trim());
            PurchaseOrderDAO.updateQuantity(po.getId(), newQty);
            loadData();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid quantity.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void viewPODetails() {
        int row = poTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a PO to view."); return; }
        PurchaseOrder po = poList.get(row);
        String details = String.format(
            "╔══════════════════════════════════════╗\n" +
            "║         PURCHASE ORDER #%-5d         ║\n" +
            "╠══════════════════════════════════════╣\n" +
            "  Date         : %s\n" +
            "  Ingredient   : %s\n" +
            "  Code         : %s\n" +
            "──────────────────────────────────────\n" +
            "  Current Stock : %.2f\n" +
            "  Threshold     : %.2f\n" +
            "  Order Qty     : %.2f\n" +
            "──────────────────────────────────────\n" +
            "  Status        : %s\n" +
            "╚══════════════════════════════════════╝",
            po.getId(), po.getOrderDate(), po.getIngredientName(), po.getIngredientCode(),
            po.getCurrentStock(), po.getThresholdValue(), po.getQuantityOrdered(), po.getStatus()
        );
        JTextArea ta = new JTextArea(details);
        ta.setFont(UITheme.FONT_MONO);
        ta.setBackground(UITheme.BG_CARD);
        ta.setForeground(UITheme.TEXT_PRIMARY);
        ta.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(ta), "PO #" + po.getId(), JOptionPane.PLAIN_MESSAGE);
    }
}
