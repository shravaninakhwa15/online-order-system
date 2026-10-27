package com.order;

import com.order.config.DatabaseConfig;
import com.order.ui.MainFrame;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        // Set modern look and feel
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // Fallback to default
            }
        }

        // Test database connectivity
        boolean dbOk = DatabaseConfig.testConnection();
        if (!dbOk) {
            System.err.println("Database connection warning: Could not connect to MySQL.");
        }

        // Launch Desktop GUI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            MainFrame app = new MainFrame();
            app.setVisible(true);

            if (!dbOk) {
                JOptionPane.showMessageDialog(
                        app,
                        "Could not connect to MySQL database at:\n" + DatabaseConfig.getDbUrl() + "\n\n" +
                        "Please verify:\n" +
                        "1. MySQL Server is running (Service: MySQL80).\n" +
                        "2. Database 'online_order_system' was created using sql/schema.sql.\n" +
                        "3. Your credentials in 'config/db.properties' are correct.",
                        "Database Connection Warning",
                        JOptionPane.WARNING_MESSAGE
                );
            }
        });
    }
}
