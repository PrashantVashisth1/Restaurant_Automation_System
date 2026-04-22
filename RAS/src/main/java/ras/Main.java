package ras;

import ras.db.DatabaseManager;
import ras.view.LoginFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Set look and feel
        try {
            // Try FlatLaf dark theme for premium look
            try {
                Class.forName("com.formdev.flatlaf.FlatDarkLaf");
                UIManager.setLookAndFeel("com.formdev.flatlaf.FlatDarkLaf");
            } catch (ClassNotFoundException e) {
                // Fallback to Nimbus if FlatLaf not available
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            }
        } catch (Exception e) {
            // Use default look and feel
        }

        // Apply custom theme overrides
        ras.view.util.UITheme.applyGlobalDefaults();

        SwingUtilities.invokeLater(() -> {
            try {
                DatabaseManager.init();
                LoginFrame login = new LoginFrame();
                login.setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                    "Failed to initialize database:\n" + e.getMessage(),
                    "Startup Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}
