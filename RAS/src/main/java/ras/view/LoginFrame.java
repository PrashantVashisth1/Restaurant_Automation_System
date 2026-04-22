package ras.view;

import ras.db.DatabaseManager;
import ras.model.User;
import ras.view.util.UITheme;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel statusLabel;
    private int failedAttempts = 0;

    public LoginFrame() {
        setTitle("RAS — Restaurant Automation System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(440, 560);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(UITheme.BG_DARK);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(UITheme.BG_DARK);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(UITheme.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(36, 40, 36, 40)
        ));
        card.setPreferredSize(new Dimension(360, 420));

        // Logo / Icon text
        JLabel icon = new JLabel("🍽", JLabel.CENTER);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 52));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("Restaurant Automation", JLabel.CENTER);
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Sign in to continue", JLabel.CENTER);
        subtitle.setFont(UITheme.FONT_BODY);
        subtitle.setForeground(UITheme.TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Form fields
        JLabel userLbl = UITheme.createLabel("Username", UITheme.FONT_SMALL, UITheme.TEXT_SECONDARY);
        userLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        usernameField = UITheme.createTextField(22);
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        usernameField.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel passLbl = UITheme.createLabel("Password", UITheme.FONT_SMALL, UITheme.TEXT_SECONDARY);
        passLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        passwordField = UITheme.createPasswordField(22);
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        passwordField.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton loginBtn = UITheme.createButton("Sign In", UITheme.ACCENT_PRIMARY);
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginBtn.addActionListener(e -> handleLogin());

        // Allow Enter key
        passwordField.addActionListener(e -> handleLogin());
        usernameField.addActionListener(e -> passwordField.requestFocus());

        statusLabel = UITheme.createLabel("", UITheme.FONT_SMALL, UITheme.ACCENT_DANGER);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusLabel.setHorizontalAlignment(JLabel.CENTER);

        JLabel hint = UITheme.createLabel("Default: manager/manager123 | clerk/clerk123", UITheme.FONT_SMALL, UITheme.TEXT_MUTED);
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(icon);
        card.add(Box.createVerticalStrut(8));
        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(28));
        card.add(userLbl);
        card.add(Box.createVerticalStrut(4));
        card.add(usernameField);
        card.add(Box.createVerticalStrut(14));
        card.add(passLbl);
        card.add(Box.createVerticalStrut(4));
        card.add(passwordField);
        card.add(Box.createVerticalStrut(20));
        card.add(loginBtn);
        card.add(Box.createVerticalStrut(10));
        card.add(statusLabel);
        card.add(Box.createVerticalStrut(18));
        card.add(hint);

        root.add(card);
        setContentPane(root);
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter username and password.");
            return;
        }

        try {
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE username=?");
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                statusLabel.setText("Invalid username or password.");
                failedAttempts++;
                return;
            }

            boolean isLocked = rs.getInt("locked") == 1;
            if (isLocked) {
                statusLabel.setText("Account locked. Contact manager to reset.");
                return;
            }

            String hash = rs.getString("password_hash");
            boolean match = DatabaseManager.checkPassword(password, hash);

            if (match) {
                // Reset failed attempts
                conn.createStatement().executeUpdate("UPDATE users SET failed_attempts=0 WHERE username='" + username + "'");
                User user = new User(rs.getInt("id"), rs.getString("username"),
                    hash, rs.getString("role"), false, 0);
                openMainFrame(user);
            } else {
                failedAttempts++;
                int newFails = rs.getInt("failed_attempts") + 1;
                if (newFails >= 5) {
                    conn.createStatement().executeUpdate(
                        "UPDATE users SET locked=1, failed_attempts=" + newFails + " WHERE username='" + username + "'");
                    statusLabel.setText("Account locked after 5 failed attempts.");
                } else {
                    conn.createStatement().executeUpdate(
                        "UPDATE users SET failed_attempts=" + newFails + " WHERE username='" + username + "'");
                    statusLabel.setText("Invalid password. " + (5 - newFails) + " attempts left.");
                }
            }
        } catch (SQLException e) {
            statusLabel.setText("Database error: " + e.getMessage());
        }
    }

    private void openMainFrame(User user) {
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame(user);
            frame.setVisible(true);
            dispose();
        });
    }
}
