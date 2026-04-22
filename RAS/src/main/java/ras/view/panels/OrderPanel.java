package ras.view.panels;

import ras.db.InvoiceDAO;
import ras.db.MenuItemDAO;
import ras.db.OrderDAO;
import ras.model.MenuItem;
import ras.model.Order;
import ras.model.OrderItem;
import ras.model.User;
import ras.view.util.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OrderPanel extends JPanel {

    private final User currentUser;
    private JTextField itemCodeField;
    private JTextField qtyField;
    private JLabel itemNameLabel;
    private JLabel unitPriceLabel;
    private DefaultTableModel cartModel;
    private JTable cartTable;
    private JLabel totalLabel;
    private JLabel cashBalanceLabel;
    private List<OrderItem> cartItems = new ArrayList<>();
    private double grandTotal = 0;

    public OrderPanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout(16, 0));
        setBackground(UITheme.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        buildUI();
    }

    private void buildUI() {
        // Header
        JLabel title = UITheme.createLabel("🛒  New Order / Billing (POS)", UITheme.FONT_TITLE, UITheme.TEXT_PRIMARY);
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.BG_DARK);
        header.add(title, BorderLayout.WEST);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0));
        add(header, BorderLayout.NORTH);

        // Left — item entry form
        JPanel leftPanel = buildEntryForm();

        // Right — cart
        JPanel rightPanel = buildCart();

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        split.setDividerLocation(420);
        split.setBorder(null);
        split.setBackground(UITheme.BG_DARK);
        add(split, BorderLayout.CENTER);
    }

    private JPanel buildEntryForm() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(UITheme.BG_DARK);

        JPanel card = UITheme.createCard("Enter Item");
        card.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 4, 6, 4);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        // Item Code
        gc.gridx = 0; gc.gridy = 0;
        card.add(UITheme.createLabel("Item Code:", UITheme.FONT_BODY, UITheme.TEXT_SECONDARY), gc);
        gc.gridy = 1;
        itemCodeField = UITheme.createTextField(16);
        card.add(itemCodeField, gc);

        // Lookup button
        gc.gridy = 2;
        JButton lookupBtn = UITheme.createButton("🔍 Lookup Item", UITheme.ACCENT_INFO);
        card.add(lookupBtn, gc);
        lookupBtn.addActionListener(e -> lookupItem());
        itemCodeField.addActionListener(e -> lookupItem());

        // Item details
        gc.gridy = 3;
        card.add(UITheme.createLabel("Item Name:", UITheme.FONT_BODY, UITheme.TEXT_SECONDARY), gc);
        gc.gridy = 4;
        itemNameLabel = UITheme.createLabel("—", UITheme.FONT_SUBTITLE, UITheme.ACCENT_SUCCESS);
        card.add(itemNameLabel, gc);

        gc.gridy = 5;
        card.add(UITheme.createLabel("Unit Price:", UITheme.FONT_BODY, UITheme.TEXT_SECONDARY), gc);
        gc.gridy = 6;
        unitPriceLabel = UITheme.createLabel("—", UITheme.FONT_SUBTITLE, UITheme.ACCENT_WARNING);
        card.add(unitPriceLabel, gc);

        // Quantity
        gc.gridy = 7;
        card.add(UITheme.createLabel("Quantity:", UITheme.FONT_BODY, UITheme.TEXT_SECONDARY), gc);
        gc.gridy = 8;
        qtyField = UITheme.createTextField(8);
        qtyField.setText("1");
        card.add(qtyField, gc);

        // Add to cart
        gc.gridy = 9;
        gc.insets = new Insets(14, 4, 4, 4);
        JButton addBtn = UITheme.createButton("➕  Add to Order", UITheme.ACCENT_PRIMARY);
        card.add(addBtn, gc);
        addBtn.addActionListener(e -> addToCart());
        qtyField.addActionListener(e -> addToCart());

        panel.add(card);

        // Menu quick reference
        JPanel menuCard = UITheme.createCard("📋 Menu Reference");
        menuCard.setLayout(new BorderLayout());
        try {
            List<MenuItem> items = MenuItemDAO.getAll();
            String[] cols = {"Code", "Name", "Category", "Price"};
            Object[][] data = new Object[items.size()][4];
            for (int i = 0; i < items.size(); i++) {
                MenuItem m = items.get(i);
                data[i] = new Object[]{m.getCode(), m.getName(), m.getCategory(),
                    "₹" + String.format("%.2f", m.getPrice())};
            }
            JTable menuTable = new JTable(data, cols);
            UITheme.styleTable(menuTable);
            menuTable.setEnabled(false);
            menuCard.add(UITheme.scrollPane(menuTable), BorderLayout.CENTER);
            // Click to fill code
            menuTable.setEnabled(true);
            menuTable.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    int row = menuTable.getSelectedRow();
                    if (row >= 0) {
                        itemCodeField.setText((String) menuTable.getValueAt(row, 0));
                        lookupItem();
                        qtyField.requestFocus();
                    }
                }
            });
        } catch (Exception ex) {
            menuCard.add(new JLabel("Error: " + ex.getMessage()), BorderLayout.CENTER);
        }
        panel.add(Box.createVerticalStrut(14));
        panel.add(menuCard);

        return panel;
    }

    private JPanel buildCart() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(UITheme.BG_DARK);

        JPanel cartCard = UITheme.createCard("🧾 Order Cart");
        cartCard.setLayout(new BorderLayout());

        String[] cols = {"Code", "Item Name", "Qty", "Unit Price", "Line Total"};
        cartModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        cartTable = new JTable(cartModel);
        UITheme.styleTable(cartTable);
        cartCard.add(UITheme.scrollPane(cartTable), BorderLayout.CENTER);

        // Remove item
        JButton removeBtn = UITheme.createButton("❌ Remove Selected", UITheme.ACCENT_DANGER);
        removeBtn.addActionListener(e -> removeFromCart());
        JPanel removeWrap = new JPanel(new FlowLayout(FlowLayout.LEFT));
        removeWrap.setBackground(UITheme.BG_CARD);
        removeWrap.add(removeBtn);
        cartCard.add(removeWrap, BorderLayout.SOUTH);

        panel.add(cartCard, BorderLayout.CENTER);

        // Bottom — total & generate bill
        JPanel bottomCard = UITheme.createCard(null);
        bottomCard.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        gc.insets = new Insets(4, 6, 4, 6);

        gc.gridx = 0; gc.gridy = 0;
        bottomCard.add(UITheme.createLabel("Cash Balance:", UITheme.FONT_BODY, UITheme.TEXT_SECONDARY), gc);
        gc.gridx = 1;
        cashBalanceLabel = UITheme.createLabel("₹—", UITheme.FONT_SUBTITLE, UITheme.ACCENT_INFO);
        cashBalanceLabel.setHorizontalAlignment(JLabel.RIGHT);
        bottomCard.add(cashBalanceLabel, gc);
        refreshCashBalance();

        gc.gridx = 0; gc.gridy = 1;
        bottomCard.add(UITheme.createLabel("Grand Total:", UITheme.FONT_SUBTITLE, UITheme.TEXT_PRIMARY), gc);
        gc.gridx = 1;
        totalLabel = UITheme.createLabel("₹0.00", new Font("Segoe UI", Font.BOLD, 22), UITheme.ACCENT_SUCCESS);
        totalLabel.setHorizontalAlignment(JLabel.RIGHT);
        bottomCard.add(totalLabel, gc);

        gc.gridx = 0; gc.gridy = 2; gc.gridwidth = 2;
        gc.insets = new Insets(12, 6, 4, 6);
        JButton billBtn = UITheme.createButton("🖨  Generate & Print Bill", UITheme.ACCENT_SUCCESS);
        billBtn.setPreferredSize(new Dimension(Integer.MAX_VALUE, 44));
        billBtn.addActionListener(e -> generateBill());
        bottomCard.add(billBtn, gc);

        gc.gridy = 3;
        JButton clearBtn = UITheme.createButton("🗑  Clear Cart", UITheme.ACCENT_DANGER);
        clearBtn.addActionListener(e -> clearCart());
        bottomCard.add(clearBtn, gc);

        panel.add(bottomCard, BorderLayout.SOUTH);
        return panel;
    }

    private void lookupItem() {
        String code = itemCodeField.getText().trim().toUpperCase();
        if (code.isEmpty()) return;
        try {
            MenuItem item = MenuItemDAO.findByCode(code);
            if (item == null) {
                itemNameLabel.setText("❌ Item not found");
                itemNameLabel.setForeground(UITheme.ACCENT_DANGER);
                unitPriceLabel.setText("—");
            } else {
                itemNameLabel.setText(item.getName());
                itemNameLabel.setForeground(UITheme.ACCENT_SUCCESS);
                unitPriceLabel.setText("₹" + String.format("%.2f", item.getPrice()));
                itemCodeField.putClientProperty("menuItem", item);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addToCart() {
        Object stored = itemCodeField.getClientProperty("menuItem");
        if (stored == null || !(stored instanceof MenuItem)) {
            JOptionPane.showMessageDialog(this, "Please lookup a valid item code first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        MenuItem item = (MenuItem) stored;
        int qty;
        try {
            qty = Integer.parseInt(qtyField.getText().trim());
            if (qty <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter a valid positive quantity.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Check if item already in cart — merge
        for (OrderItem oi : cartItems) {
            if (oi.getMenuItemId() == item.getId()) {
                oi.setQuantity(oi.getQuantity() + qty);
                refreshCartTable();
                return;
            }
        }

        OrderItem oi = new OrderItem(0, 0, item.getId(), item.getCode(), item.getName(), qty, item.getPrice());
        cartItems.add(oi);
        refreshCartTable();

        // Reset fields
        itemCodeField.setText("");
        qtyField.setText("1");
        itemNameLabel.setText("—");
        unitPriceLabel.setText("—");
        itemCodeField.putClientProperty("menuItem", null);
        itemCodeField.requestFocus();
    }

    private void removeFromCart() {
        int row = cartTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select an item to remove."); return; }
        cartItems.remove(row);
        refreshCartTable();
    }

    private void refreshCartTable() {
        cartModel.setRowCount(0);
        grandTotal = 0;
        for (OrderItem oi : cartItems) {
            cartModel.addRow(new Object[]{
                oi.getMenuItemCode(), oi.getMenuItemName(),
                oi.getQuantity(), "₹" + String.format("%.2f", oi.getUnitPrice()),
                "₹" + String.format("%.2f", oi.getLineTotal())
            });
            grandTotal += oi.getLineTotal();
        }
        totalLabel.setText("₹" + String.format("%.2f", grandTotal));
    }

    private void generateBill() {
        if (cartItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cart is empty. Add items before generating bill.", "Empty Cart", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            String billNo = "BILL-" + System.currentTimeMillis();
            String orderDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            Order order = new Order(0, billNo, orderDate, grandTotal, "BILLED", currentUser.getId());
            int orderId = OrderDAO.createOrder(order);
            for (OrderItem oi : cartItems) {
                oi.setOrderId(orderId);
                OrderDAO.addOrderItem(oi);
            }
            // Add to cash balance (revenue)
            InvoiceDAO.addCashBalance(grandTotal);
            showBillDialog(billNo, orderDate);
            clearCart();
            refreshCashBalance();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error generating bill: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showBillDialog(String billNo, String orderDate) {
        StringBuilder sb = new StringBuilder();
        sb.append("╔══════════════════════════════════════╗\n");
        sb.append("║        RESTAURANT AUTOMATION          ║\n");
        sb.append("║              SYSTEM (RAS)             ║\n");
        sb.append("╠══════════════════════════════════════╣\n");
        sb.append(String.format("  Bill No : %-26s\n", billNo));
        sb.append(String.format("  Date    : %-26s\n", orderDate));
        sb.append(String.format("  Clerk   : %-26s\n", currentUser.getUsername()));
        sb.append("──────────────────────────────────────\n");
        sb.append(String.format("  %-12s %4s %8s %10s\n", "Item", "Qty", "Price", "Total"));
        sb.append("──────────────────────────────────────\n");
        for (OrderItem oi : cartItems) {
            sb.append(String.format("  %-12s %4d %8.2f %10.2f\n",
                oi.getMenuItemName().length() > 12 ? oi.getMenuItemName().substring(0, 12) : oi.getMenuItemName(),
                oi.getQuantity(), oi.getUnitPrice(), oi.getLineTotal()));
        }
        sb.append("══════════════════════════════════════\n");
        sb.append(String.format("  GRAND TOTAL :             ₹%8.2f\n", grandTotal));
        sb.append("╚══════════════════════════════════════╝\n");
        sb.append("\n    *** Thank you! Visit again! ***\n");

        JTextArea ta = new JTextArea(sb.toString());
        ta.setFont(UITheme.FONT_MONO);
        ta.setBackground(UITheme.BG_CARD);
        ta.setForeground(UITheme.TEXT_PRIMARY);
        ta.setEditable(false);
        JScrollPane sp = new JScrollPane(ta);
        sp.setPreferredSize(new Dimension(420, 380));

        JOptionPane.showMessageDialog(this, sp, "Bill Generated — " + billNo, JOptionPane.PLAIN_MESSAGE);
    }

    private void clearCart() {
        cartItems.clear();
        refreshCartTable();
    }

    private void refreshCashBalance() {
        try {
            double bal = InvoiceDAO.getCashBalance();
            cashBalanceLabel.setText("₹" + String.format("%.2f", bal));
        } catch (Exception ignored) {}
    }
}
