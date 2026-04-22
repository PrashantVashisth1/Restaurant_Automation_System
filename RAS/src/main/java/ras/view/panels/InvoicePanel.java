package ras.view.panels;

import ras.db.IngredientDAO;
import ras.db.InvoiceDAO;
import ras.db.PurchaseOrderDAO;
import ras.model.Ingredient;
import ras.model.Invoice;
import ras.model.PurchaseOrder;
import ras.model.User;
import ras.view.util.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class InvoicePanel extends JPanel {

    private final User currentUser;
    private DefaultTableModel tableModel;
    private JTable invoiceTable;
    private List<Invoice> invoices;
    private JTextField ingCodeField, supplierField, qtyField, unitPriceField, dateField, poIdField;
    private JLabel ingNameLabel, totalAmtLabel, cashBalLabel;

    public InvoicePanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        buildUI();
        loadData();
    }

    private void buildUI() {
        JLabel title = UITheme.createLabel("🧾  Invoice Entry & Cheque Printing", UITheme.FONT_TITLE, UITheme.TEXT_PRIMARY);
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.BG_DARK);
        header.add(title, BorderLayout.WEST);
        header.add(UITheme.createLabel("FR-020 to FR-026", UITheme.FONT_SMALL, UITheme.TEXT_MUTED), BorderLayout.EAST);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        add(header, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildEntryForm(), buildInvoiceTable());
        split.setDividerLocation(400);
        split.setBorder(null);
        add(split, BorderLayout.CENTER);
    }

    private JPanel buildEntryForm() {
        JPanel form = UITheme.createCard("Enter Invoice (Goods Received)");
        form.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(5, 4, 5, 4);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        // PO ID
        gc.gridx = 0; gc.gridy = 0;
        form.add(UITheme.createLabel("Purchase Order ID (optional):", UITheme.FONT_SMALL, UITheme.TEXT_SECONDARY), gc);
        gc.gridy = 1; poIdField = UITheme.createTextField(10); form.add(poIdField, gc);

        // Ingredient Code
        gc.gridy = 2;
        form.add(UITheme.createLabel("Ingredient Code:", UITheme.FONT_SMALL, UITheme.TEXT_SECONDARY), gc);
        gc.gridy = 3; ingCodeField = UITheme.createTextField(14); form.add(ingCodeField, gc);
        gc.gridy = 4;
        JButton lookupBtn = UITheme.createButton("🔍 Lookup", UITheme.ACCENT_INFO);
        lookupBtn.addActionListener(e -> lookupIngredient());
        ingCodeField.addActionListener(e -> lookupIngredient());
        form.add(lookupBtn, gc);
        gc.gridy = 5; ingNameLabel = UITheme.createLabel("—", UITheme.FONT_BODY, UITheme.ACCENT_SUCCESS);
        form.add(ingNameLabel, gc);

        // Supplier
        gc.gridy = 6;
        form.add(UITheme.createLabel("Supplier Name:", UITheme.FONT_SMALL, UITheme.TEXT_SECONDARY), gc);
        gc.gridy = 7; supplierField = UITheme.createTextField(14); form.add(supplierField, gc);

        // Quantity received
        gc.gridy = 8;
        form.add(UITheme.createLabel("Quantity Received:", UITheme.FONT_SMALL, UITheme.TEXT_SECONDARY), gc);
        gc.gridy = 9; qtyField = UITheme.createTextField(10);
        qtyField.addActionListener(e -> calcTotal());
        form.add(qtyField, gc);

        // Unit Price
        gc.gridy = 10;
        form.add(UITheme.createLabel("Unit Price (₹):", UITheme.FONT_SMALL, UITheme.TEXT_SECONDARY), gc);
        gc.gridy = 11; unitPriceField = UITheme.createTextField(10);
        unitPriceField.addActionListener(e -> calcTotal());
        form.add(unitPriceField, gc);

        // Total amount display
        gc.gridy = 12;
        form.add(UITheme.createLabel("Invoice Total:", UITheme.FONT_SMALL, UITheme.TEXT_SECONDARY), gc);
        gc.gridy = 13;
        totalAmtLabel = UITheme.createLabel("₹—", new Font("Segoe UI", Font.BOLD, 18), UITheme.ACCENT_WARNING);
        form.add(totalAmtLabel, gc);
        gc.gridy = 14;
        JButton calcBtn = UITheme.createButton("Calculate Total", UITheme.TEXT_MUTED);
        calcBtn.addActionListener(e -> calcTotal());
        form.add(calcBtn, gc);

        // Date
        gc.gridy = 15;
        form.add(UITheme.createLabel("Invoice Date:", UITheme.FONT_SMALL, UITheme.TEXT_SECONDARY), gc);
        gc.gridy = 16; dateField = UITheme.createTextField(12);
        dateField.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        form.add(dateField, gc);

        // Cash balance
        gc.gridy = 17;
        form.add(UITheme.createLabel("Current Cash Balance:", UITheme.FONT_SMALL, UITheme.TEXT_SECONDARY), gc);
        gc.gridy = 18;
        cashBalLabel = UITheme.createLabel("₹—", UITheme.FONT_SUBTITLE, UITheme.ACCENT_INFO);
        form.add(cashBalLabel, gc);
        refreshCashBalance();

        // Submit
        gc.gridy = 19; gc.insets = new Insets(16, 4, 4, 4);
        JButton submitBtn = UITheme.createButton("📥  Record Invoice & Process Cheque", UITheme.ACCENT_SUCCESS);
        submitBtn.addActionListener(e -> recordInvoice());
        form.add(submitBtn, gc);

        return form;
    }

    private JPanel buildInvoiceTable() {
        JPanel tableCard = UITheme.createCard("All Invoices");
        tableCard.setLayout(new BorderLayout());

        String[] cols = {"ID", "Ingredient", "Supplier", "Qty", "Unit Price", "Total", "Date", "Cheque", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        invoiceTable = new JTable(tableModel);
        UITheme.styleTable(invoiceTable);
        tableCard.add(UITheme.scrollPane(invoiceTable), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        actions.setBackground(UITheme.BG_CARD);
        JButton printChequeBtn = UITheme.createButton("🖨 Print Cheque (Pending)", UITheme.ACCENT_PRIMARY);
        printChequeBtn.addActionListener(e -> printCheque());
        JButton refreshBtn = UITheme.createButton("🔄 Refresh", UITheme.ACCENT_INFO);
        refreshBtn.addActionListener(e -> loadData());
        actions.add(printChequeBtn);
        actions.add(refreshBtn);
        tableCard.add(actions, BorderLayout.SOUTH);

        return tableCard;
    }

    private void lookupIngredient() {
        try {
            Ingredient ing = IngredientDAO.findByCode(ingCodeField.getText().trim().toUpperCase());
            if (ing == null) {
                ingNameLabel.setText("❌ Not found"); ingNameLabel.setForeground(UITheme.ACCENT_DANGER);
            } else {
                ingNameLabel.setText(ing.getName()); ingNameLabel.setForeground(UITheme.ACCENT_SUCCESS);
                ingCodeField.putClientProperty("ingredient", ing);
            }
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
    }

    private void calcTotal() {
        try {
            double qty = Double.parseDouble(qtyField.getText().trim());
            double up  = Double.parseDouble(unitPriceField.getText().trim());
            totalAmtLabel.setText("₹" + String.format("%.2f", qty * up));
        } catch (NumberFormatException ignored) { totalAmtLabel.setText("₹—"); }
    }

    private void recordInvoice() {
        Object ingObj = ingCodeField.getClientProperty("ingredient");
        if (ingObj == null || !(ingObj instanceof Ingredient)) {
            JOptionPane.showMessageDialog(this, "Lookup a valid ingredient first."); return;
        }
        Ingredient ing = (Ingredient) ingObj;
        double qty, unitPrice;
        try {
            qty = Double.parseDouble(qtyField.getText().trim());
            unitPrice = Double.parseDouble(unitPriceField.getText().trim());
            if (qty <= 0 || unitPrice <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter valid quantity and price."); return;
        }
        double total = qty * unitPrice;
        int poId = 0;
        try { poId = Integer.parseInt(poIdField.getText().trim()); } catch (Exception ignored) {}

        String date = dateField.getText().trim();
        String supplier = supplierField.getText().trim();
        if (supplier.isEmpty()) supplier = "Unknown Supplier";

        try {
            double cashBalance = InvoiceDAO.getCashBalance();
            boolean canPay = cashBalance >= total;

            Invoice inv = new Invoice(0, poId, ing.getId(), ing.getName(), supplier,
                qty, unitPrice, date, false, null, canPay ? "PAID" : "PENDING");
            int invId = InvoiceDAO.add(inv);

            // FR-021: Update stock
            IngredientDAO.addStock(ing.getId(), qty);

            if (canPay) {
                // FR-024: Print cheque
                String chequeNo = "CHQ-" + System.currentTimeMillis();
                InvoiceDAO.markChequePrinted(invId, chequeNo);
                InvoiceDAO.deductCashBalance(total);
                if (poId > 0) PurchaseOrderDAO.updateStatus(poId, "FINALIZED");
                printChequeDialog(supplier, total, chequeNo, date);
            } else {
                // FR-025: Notify manager if insufficient
                JOptionPane.showMessageDialog(this,
                    String.format("⚠ Insufficient cash balance!\n" +
                        "Invoice Total: ₹%.2f\nAvailable Cash: ₹%.2f\n\nInvoice saved as PENDING. Manager notified.",
                        total, cashBalance),
                    "Insufficient Balance — Invoice Pending", JOptionPane.WARNING_MESSAGE);
            }
            loadData();
            refreshCashBalance();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void printCheque() {
        int row = invoiceTable.getSelectedRow();
        if (row < 0 || invoices == null || row >= invoices.size()) {
            JOptionPane.showMessageDialog(this, "Select a pending invoice."); return;
        }
        Invoice inv = invoices.get(row);
        if (!"PENDING".equals(inv.getStatus())) {
            JOptionPane.showMessageDialog(this, "Only PENDING invoices can be processed."); return;
        }
        try {
            double cashBalance = InvoiceDAO.getCashBalance();
            if (cashBalance < inv.getTotalAmount()) {
                JOptionPane.showMessageDialog(this,
                    String.format("Still insufficient balance.\nNeed: ₹%.2f  Available: ₹%.2f",
                        inv.getTotalAmount(), cashBalance), "Cannot Print Cheque", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String chequeNo = "CHQ-" + System.currentTimeMillis();
            InvoiceDAO.markChequePrinted(inv.getId(), chequeNo);
            InvoiceDAO.deductCashBalance(inv.getTotalAmount());
            printChequeDialog(inv.getSupplierName(), inv.getTotalAmount(), chequeNo, inv.getInvoiceDate());
            loadData();
            refreshCashBalance();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void printChequeDialog(String payee, double amount, String chequeNo, String date) {
        String amountWords = convertToWords(amount);
        String cheque = String.format(
            "┌─────────────────────────────────────────────┐\n" +
            "│           RESTAURANT AUTOMATION SYSTEM       │\n" +
            "│                    CHEQUE                    │\n" +
            "├─────────────────────────────────────────────┤\n" +
            "│  Cheque No : %-30s│\n" +
            "│  Date      : %-30s│\n" +
            "├─────────────────────────────────────────────┤\n" +
            "│  Pay To    : %-30s│\n" +
            "│  Amount    : ₹%-29.2f│\n" +
            "│  In Words  : %-30s│\n" +
            "└─────────────────────────────────────────────┘\n" +
            "\n  Authorized Signature: ____________________\n",
            chequeNo, date, payee, amount, amountWords
        );
        JTextArea ta = new JTextArea(cheque);
        ta.setFont(UITheme.FONT_MONO);
        ta.setBackground(new Color(250, 248, 230));
        ta.setForeground(Color.BLACK);
        ta.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(ta),
            "🖨 Cheque Printed — " + chequeNo, JOptionPane.PLAIN_MESSAGE);
    }

    private String convertToWords(double amount) {
        long rupees = (long) amount;
        long paise = Math.round((amount - rupees) * 100);
        String[] ones = {"", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine",
            "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen"};
        String[] tens = {"", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"};
        if (rupees == 0) return "Zero Rupees Only";
        String result = "";
        if (rupees >= 1000) result += ones[(int) (rupees / 1000)] + " Thousand ";
        rupees %= 1000;
        if (rupees >= 100) result += ones[(int) (rupees / 100)] + " Hundred ";
        rupees %= 100;
        if (rupees >= 20) result += tens[(int) (rupees / 10)] + " " + ones[(int) (rupees % 10)] + " ";
        else result += ones[(int) rupees] + " ";
        result += "Rupees";
        if (paise > 0) result += " and " + paise + " Paise";
        return result.trim() + " Only";
    }

    private void loadData() {
        try {
            invoices = InvoiceDAO.getAll();
            tableModel.setRowCount(0);
            for (Invoice inv : invoices) {
                tableModel.addRow(new Object[]{
                    inv.getId(), inv.getIngredientName(), inv.getSupplierName(),
                    String.format("%.2f", inv.getQuantityReceived()),
                    String.format("%.2f", inv.getUnitPrice()),
                    "₹" + String.format("%.2f", inv.getTotalAmount()),
                    inv.getInvoiceDate(),
                    inv.isChequePrinted() ? inv.getChequeNumber() : "Pending",
                    inv.getStatus()
                });
            }
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Error: " + e.getMessage()); }
    }

    private void refreshCashBalance() {
        try { cashBalLabel.setText("₹" + String.format("%.2f", InvoiceDAO.getCashBalance())); }
        catch (Exception ignored) {}
    }
}
