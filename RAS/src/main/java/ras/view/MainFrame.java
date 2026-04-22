package ras.view;

import ras.model.User;
import ras.view.panels.*;
import ras.view.util.UITheme;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private final User currentUser;
    private JPanel contentArea;
    private JButton activeNavBtn;

    public MainFrame(User user) {
        this.currentUser = user;
        setTitle("RAS — Restaurant Automation System | " + user.getUsername() + " (" + user.getRole() + ")");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 780);
        setMinimumSize(new Dimension(1100, 680));
        setLocationRelativeTo(null);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG_DARK);

        // Sidebar
        JPanel sidebar = buildSidebar();
        root.add(sidebar, BorderLayout.WEST);

        // Content
        contentArea = new JPanel(new BorderLayout());
        contentArea.setBackground(UITheme.BG_DARK);
        root.add(contentArea, BorderLayout.CENTER);

        setContentPane(root);

        // Show dashboard by default
        showPanel(new DashboardPanel(currentUser));
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UITheme.BG_PANEL);
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UITheme.BORDER_COLOR));

        // App name
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.BG_PANEL);
        header.setBorder(BorderFactory.createEmptyBorder(20, 18, 20, 18));

        JLabel appName = new JLabel("<html><b>🍽 RAS</b></html>");
        appName.setFont(new Font("Segoe UI", Font.BOLD, 18));
        appName.setForeground(UITheme.ACCENT_PRIMARY);
        JLabel appSub = new JLabel("Restaurant System");
        appSub.setFont(UITheme.FONT_SMALL);
        appSub.setForeground(UITheme.TEXT_SECONDARY);
        JPanel nameStack = new JPanel(new GridLayout(2, 1));
        nameStack.setOpaque(false);
        nameStack.add(appName);
        nameStack.add(appSub);
        header.add(nameStack);
        sidebar.add(header);

        // Divider
        sidebar.add(makeDivider());

        // User info
        JPanel userInfo = new JPanel(new GridLayout(2, 1));
        userInfo.setBackground(UITheme.BG_PANEL);
        userInfo.setBorder(BorderFactory.createEmptyBorder(6, 18, 10, 18));
        JLabel uName = new JLabel("👤 " + currentUser.getUsername());
        uName.setFont(UITheme.FONT_BODY);
        uName.setForeground(UITheme.TEXT_PRIMARY);
        JLabel uRole = new JLabel(currentUser.getRole());
        uRole.setFont(UITheme.FONT_SMALL);
        uRole.setForeground(UITheme.ACCENT_INFO);
        userInfo.add(uName);
        userInfo.add(uRole);
        sidebar.add(userInfo);
        sidebar.add(makeDivider());

        // Navigation items — role-based, matching DFD Level 0 & Level 1
        String role = currentUser.getRole();

        // Dashboard — all roles
        addNavItem(sidebar, "📊  Dashboard", () -> showPanel(new DashboardPanel(currentUser)));

        // SALES CLERK: New Order / Billing (P1 in DFD)
        if (role.equals("CLERK") || role.equals("MANAGER")) {
            addNavItem(sidebar, "🛒  New Order / Billing", () -> showPanel(new OrderPanel(currentUser)));
        }

        // STOREKEEPER: Inventory, Ingredient Issuance, Purchase Orders, Invoices (P3, P4, P5 in DFD)
        if (role.equals("STOREKEEPER") || role.equals("MANAGER")) {
            addNavItem(sidebar, "🌿  Ingredient Usage", () -> showPanel(new IngredientUsagePanel(currentUser)));
            addNavItem(sidebar, "📦  Inventory", () -> showPanel(new InventoryPanel(currentUser)));
            addNavItem(sidebar, "📋  Purchase Orders", () -> showPanel(new PurchaseOrderPanel(currentUser)));
            addNavItem(sidebar, "🧾  Invoices", () -> showPanel(new InvoicePanel(currentUser)));
        }

        // MANAGER ONLY: Menu Management (P2 in DFD), Reports (P6 in DFD)
        if (role.equals("MANAGER")) {
            addNavItem(sidebar, "🍕  Menu Management", () -> showPanel(new MenuManagementPanel(currentUser)));
            addNavItem(sidebar, "📈  Reports", () -> showPanel(new ReportPanel(currentUser)));
        }

        sidebar.add(Box.createVerticalGlue());

        // Logout button
        JButton logoutBtn = UITheme.createButton("⬅  Logout", UITheme.ACCENT_DANGER);
        logoutBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoutBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        logoutBtn.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        logoutBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Logout?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                new LoginFrame().setVisible(true);
                dispose();
            }
        });
        JPanel logoutWrap = new JPanel(new BorderLayout());
        logoutWrap.setBackground(UITheme.BG_PANEL);
        logoutWrap.setBorder(BorderFactory.createEmptyBorder(10, 10, 16, 10));
        logoutWrap.add(logoutBtn);
        sidebar.add(logoutWrap);

        return sidebar;
    }

    private void addNavItem(JPanel sidebar, String label, Runnable action) {
        JButton btn = new JButton(label);
        btn.setFont(UITheme.FONT_BODY);
        btn.setForeground(UITheme.TEXT_SECONDARY);
        btn.setBackground(UITheme.BG_PANEL);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (activeNavBtn != btn) {
                    btn.setBackground(UITheme.BG_CARD);
                    btn.setForeground(UITheme.TEXT_PRIMARY);
                }
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (activeNavBtn != btn) {
                    btn.setBackground(UITheme.BG_PANEL);
                    btn.setForeground(UITheme.TEXT_SECONDARY);
                }
            }
        });

        btn.addActionListener(e -> {
            if (activeNavBtn != null) {
                activeNavBtn.setBackground(UITheme.BG_PANEL);
                if (activeNavBtn instanceof JButton) ((JButton) activeNavBtn).setForeground(UITheme.TEXT_SECONDARY);
            }
            btn.setBackground(UITheme.ACCENT_PRIMARY);
            btn.setForeground(Color.WHITE);
            activeNavBtn = btn;
            action.run();
        });

        sidebar.add(btn);
    }

    public void showPanel(JPanel panel) {
        contentArea.removeAll();
        contentArea.add(panel, BorderLayout.CENTER);
        contentArea.revalidate();
        contentArea.repaint();
    }

    private JSeparator makeDivider() {
        JSeparator sep = new JSeparator();
        sep.setForeground(UITheme.BORDER_COLOR);
        sep.setBackground(UITheme.BORDER_COLOR);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return sep;
    }
}
