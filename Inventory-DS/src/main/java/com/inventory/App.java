package main.java.com.inventory;

import main.java.com.inventory.utils.ErrorHandler;
import main.java.com.inventory.utils.ThemeManager;
import main.java.com.inventory.views.LoginView;

import javax.swing.*;

public class App {
    public static void main(String[] args) {
        // Set LookAndFeel with fallback
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            System.out.println("[DEBUG] NimbusLookAndFeel set successfully");
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to set NimbusLookAndFeel: " + e.getMessage());
            e.printStackTrace();
            try {
                // Fallback to the system default LookAndFeel
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                System.out.println("[DEBUG] Fallback to system LookAndFeel: " + UIManager.getLookAndFeel().getName());
            } catch (Exception ex) {
                System.err.println("[ERROR] Failed to set system LookAndFeel: " + ex.getMessage());
                ex.printStackTrace();
                // Use the default MetalLookAndFeel as a last resort
                try {
                    UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                    System.out.println("[DEBUG] Fallback to cross-platform LookAndFeel: " + UIManager.getLookAndFeel().getName());
                } catch (Exception ex2) {
                    System.err.println("[FATAL] Failed to set any LookAndFeel: " + ex2.getMessage());
                    ex2.printStackTrace();
                    System.exit(1);
                }
            }
        }

        // Initialize UI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                // Apply initial theme
                ThemeManager.setTheme(ThemeManager.ThemeMode.LIGHT);
                System.out.println("[DEBUG] Initial theme set to LIGHT");

                // Start with LoginView
                LoginView loginView = new LoginView();
                loginView.setVisible(true);
            } catch (Exception e) {
                ErrorHandler.handleError(null, "Failed to initialize application", e);
                System.exit(1);
            }
        });
    }
}