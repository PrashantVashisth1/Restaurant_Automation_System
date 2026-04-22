package ras.view.panels;

import ras.db.InvoiceDAO;
import ras.db.OrderDAO;
import ras.model.User;
import ras.view.util.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ReportPanel extends JPanel {

    private final User currentUser;
    private JComboBox<String> monthYearBox;
    private JTextField fromDateField, toDateField;
    private JPanel reportDisplayArea;

    public ReportPanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        buildUI();
    }

    private void buildUI() {
        JLabel title = UITheme.createLabel("📈  Reports & Analytics", UITheme.FONT_TITLE, UITheme.TEXT_PRIMARY);
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.BG_DARK);
        header.add(title, BorderLayout.WEST);
        header.add(UITheme.createLabel("FR-027, FR-028, FR-029, FR-030", UITheme.FONT_SMALL, UITheme.TEXT_MUTED), BorderLayout.EAST);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        add(header, BorderLayout.NORTH);

        // Controls
        JPanel controls = UITheme.createCard("Report Options");
        controls.setLayout(new FlowLayout(FlowLayout.LEFT, 16, 8));

        // Month/Year for monthly reports
        String[] months = generateMonthOptions();
        monthYearBox = new JComboBox<>(months);
        monthYearBox.setFont(UITheme.FONT_BODY);
        controls.add(UITheme.createLabel("Month:", UITheme.FONT_BODY, UITheme.TEXT_SECONDARY));
        controls.add(monthYearBox);

        JButton salesBtn = UITheme.createButton("📊 Monthly Sales Report (FR-027)", UITheme.ACCENT_PRIMARY);
        salesBtn.addActionListener(e -> showMonthlySalesReport());
        JButton expBtn = UITheme.createButton("💸 Monthly Expenses Report (FR-028)", UITheme.ACCENT_DANGER);
        expBtn.addActionListener(e -> showMonthlyExpensesReport());

        controls.add(salesBtn);
        controls.add(expBtn);

        // Separator
        controls.add(new JSeparator(JSeparator.VERTICAL));
        controls.add(UITheme.createLabel("  From:", UITheme.FONT_BODY, UITheme.TEXT_SECONDARY));
        fromDateField = UITheme.createTextField(10);
        fromDateField.setText(new SimpleDateFormat("yyyy-MM-dd").format(getMonthStart()));
        controls.add(fromDateField);
        controls.add(UITheme.createLabel("To:", UITheme.FONT_BODY, UITheme.TEXT_SECONDARY));
        toDateField = UITheme.createTextField(10);
        toDateField.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        controls.add(toDateField);
        JButton statBtn = UITheme.createButton("📉 Statistical Sales Report (FR-029)", UITheme.ACCENT_WARNING);
        statBtn.addActionListener(e -> showStatisticalSalesReport());
        controls.add(statBtn);

        add(controls, BorderLayout.NORTH);

        // Report display area
        reportDisplayArea = new JPanel(new BorderLayout());
        reportDisplayArea.setBackground(UITheme.BG_DARK);
        // Default placeholder
        JLabel placeholder = UITheme.createLabel(
            "Select a report type above to generate and view data here. Reports are printable (FR-030).",
            UITheme.FONT_BODY, UITheme.TEXT_MUTED);
        placeholder.setHorizontalAlignment(JLabel.CENTER);
        reportDisplayArea.add(placeholder, BorderLayout.CENTER);
        add(reportDisplayArea, BorderLayout.CENTER);
    }

    private void showMonthlySalesReport() {
        String yearMonth = (String) monthYearBox.getSelectedItem();
        try {
            List<String[]> rows = OrderDAO.getMonthlySalesReport(yearMonth);
            double grandTotal = rows.stream().mapToDouble(r -> Double.parseDouble(r[4])).sum();
            int totalItems = rows.stream().mapToInt(r -> Integer.parseInt(r[2])).sum();

            String[] cols = {"Item Code", "Item Name", "Qty Sold", "Unit Price", "Revenue"};
            DefaultTableModel model = new DefaultTableModel(cols, 0) {
                public boolean isCellEditable(int r, int c) { return false; }
            };
            for (String[] row : rows) model.addRow(row);

            JTable table = new JTable(model);
            UITheme.styleTable(table);

            JPanel panel = new JPanel(new BorderLayout(0, 8));
            panel.setBackground(UITheme.BG_DARK);

            // Stats header
            JPanel statsBar = new JPanel(new GridLayout(1, 3, 14, 0));
            statsBar.setBackground(UITheme.BG_DARK);
            statsBar.add(UITheme.statCard("Month", yearMonth, UITheme.ACCENT_PRIMARY));
            statsBar.add(UITheme.statCard("Total Items Sold", String.valueOf(totalItems), UITheme.ACCENT_INFO));
            statsBar.add(UITheme.statCard("Total Revenue", "₹" + String.format("%.2f", grandTotal), UITheme.ACCENT_SUCCESS));
            statsBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
            panel.add(statsBar, BorderLayout.NORTH);

            JPanel tableCard = UITheme.createCard("📊 Monthly Sales — " + yearMonth);
            tableCard.setLayout(new BorderLayout());
            tableCard.add(UITheme.scrollPane(table), BorderLayout.CENTER);

            JButton printBtn = UITheme.createButton("🖨 Print Report (FR-030)", UITheme.ACCENT_PRIMARY);
            printBtn.addActionListener(e -> printReport("Monthly Sales Report — " + yearMonth, table));
            JPanel printWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            printWrap.setBackground(UITheme.BG_CARD);
            printWrap.add(printBtn);
            tableCard.add(printWrap, BorderLayout.SOUTH);
            panel.add(tableCard, BorderLayout.CENTER);

            updateReportArea(panel);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void showMonthlyExpensesReport() {
        String yearMonth = (String) monthYearBox.getSelectedItem();
        try {
            List<String[]> rows = InvoiceDAO.getMonthlyExpensesReport(yearMonth);
            double grandTotal = rows.stream().mapToDouble(r -> Double.parseDouble(r[4])).sum();

            String[] cols = {"Ingredient", "Supplier", "Qty", "Unit Price", "Total", "Date", "Status"};
            DefaultTableModel model = new DefaultTableModel(cols, 0) {
                public boolean isCellEditable(int r, int c) { return false; }
            };
            for (String[] row : rows) model.addRow(row);

            JTable table = new JTable(model);
            UITheme.styleTable(table);

            JPanel panel = new JPanel(new BorderLayout(0, 8));
            panel.setBackground(UITheme.BG_DARK);

            JPanel statsBar = new JPanel(new GridLayout(1, 3, 14, 0));
            statsBar.setBackground(UITheme.BG_DARK);
            statsBar.add(UITheme.statCard("Month", yearMonth, UITheme.ACCENT_PRIMARY));
            statsBar.add(UITheme.statCard("Invoices", String.valueOf(rows.size()), UITheme.ACCENT_WARNING));
            statsBar.add(UITheme.statCard("Total Expenses", "₹" + String.format("%.2f", grandTotal), UITheme.ACCENT_DANGER));
            statsBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
            panel.add(statsBar, BorderLayout.NORTH);

            JPanel tableCard = UITheme.createCard("💸 Monthly Expenses — " + yearMonth);
            tableCard.setLayout(new BorderLayout());
            tableCard.add(UITheme.scrollPane(table), BorderLayout.CENTER);

            JButton printBtn = UITheme.createButton("🖨 Print Report (FR-030)", UITheme.ACCENT_DANGER);
            printBtn.addActionListener(e -> printReport("Monthly Expenses Report — " + yearMonth, table));
            JPanel printWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            printWrap.setBackground(UITheme.BG_CARD);
            printWrap.add(printBtn);
            tableCard.add(printWrap, BorderLayout.SOUTH);
            panel.add(tableCard, BorderLayout.CENTER);

            updateReportArea(panel);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void showStatisticalSalesReport() {
        String from = fromDateField.getText().trim();
        String to = toDateField.getText().trim();
        try {
            List<String[]> rows = OrderDAO.getStatisticalSalesReport(from, to);

            String[] cols = {"Item Code", "Item Name", "Total Qty Sold", "Total Revenue"};
            DefaultTableModel model = new DefaultTableModel(cols, 0) {
                public boolean isCellEditable(int r, int c) { return false; }
            };
            for (String[] row : rows) model.addRow(row);

            JTable table = new JTable(model);
            UITheme.styleTable(table);

            double totalRevenue = rows.stream().mapToDouble(r -> Double.parseDouble(r[3])).sum();
            int totalQty = rows.stream().mapToInt(r -> Integer.parseInt(r[2])).sum();

            JPanel panel = new JPanel(new BorderLayout(0, 8));
            panel.setBackground(UITheme.BG_DARK);

            JPanel statsBar = new JPanel(new GridLayout(1, 3, 14, 0));
            statsBar.setBackground(UITheme.BG_DARK);
            statsBar.add(UITheme.statCard("Period", from + " to " + to, UITheme.ACCENT_PRIMARY));
            statsBar.add(UITheme.statCard("Total Qty Sold", String.valueOf(totalQty), UITheme.ACCENT_INFO));
            statsBar.add(UITheme.statCard("Total Revenue", "₹" + String.format("%.2f", totalRevenue), UITheme.ACCENT_SUCCESS));
            statsBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
            panel.add(statsBar, BorderLayout.NORTH);

            JPanel tableCard = UITheme.createCard("📉 Statistical Sales Report: " + from + " → " + to);
            tableCard.setLayout(new BorderLayout());
            tableCard.add(UITheme.scrollPane(table), BorderLayout.CENTER);

            JButton printBtn = UITheme.createButton("🖨 Print Report (FR-030)", UITheme.ACCENT_WARNING);
            printBtn.addActionListener(e -> printReport("Statistical Sales Report", table));
            JPanel printWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            printWrap.setBackground(UITheme.BG_CARD);
            printWrap.add(printBtn);
            tableCard.add(printWrap, BorderLayout.SOUTH);
            panel.add(tableCard, BorderLayout.CENTER);

            updateReportArea(panel);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void printReport(String title, JTable table) {
        try {
            boolean done = table.print(JTable.PrintMode.NORMAL,
                new java.text.MessageFormat(title),
                new java.text.MessageFormat("Page {0}"));
            if (done)
                JOptionPane.showMessageDialog(this, "Report sent to printer!", "Print Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (java.awt.print.PrinterException e) {
            JOptionPane.showMessageDialog(this, "Printing error: " + e.getMessage() +
                "\n(Tip: This would print on a physical printer if connected.)", "Print", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void updateReportArea(JPanel panel) {
        reportDisplayArea.removeAll();
        reportDisplayArea.add(panel, BorderLayout.CENTER);
        reportDisplayArea.revalidate();
        reportDisplayArea.repaint();
    }

    private String[] generateMonthOptions() {
        Calendar cal = Calendar.getInstance();
        String[] options = new String[12];
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        for (int i = 0; i < 12; i++) {
            options[i] = sdf.format(cal.getTime());
            cal.add(Calendar.MONTH, -1);
        }
        return options;
    }

    private Date getMonthStart() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        return cal.getTime();
    }
}
