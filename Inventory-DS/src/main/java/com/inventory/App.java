package main.java.com.inventory;

import main.java.com.inventory.dao.DBConnection;
import main.java.com.inventory.dao.UserDAO;
import main.java.com.inventory.models.User;
import main.java.com.inventory.utils.ErrorHandler;
import main.java.com.inventory.utils.ThemeManager;
import main.java.com.inventory.views.LoginView;

import javax.swing.*;
import java.sql.Connection;
import java.sql.SQLException;

public class App {
    public static void main(String[] args) {
        // Set Nimbus LookAndFeel before any UI components are created
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            System.out.println("[DEBUG] NimbusLookAndFeel set successfully");
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to set NimbusLookAndFeel: " + e.getMessage());
            e.printStackTrace();
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

                // Run DAO tests in background (for debugging)
                runDAOTests();
            } catch (Exception e) {
                ErrorHandler.handleError(null, "Failed to initialize application", e);
                System.exit(1);
            }
        });
    }

    // DAO tests for debugging
    private static void runDAOTests() {
        Connection conn = null;
        try {
            System.out.println("[DEBUG] Attempting to establish database connection...");
            conn = DBConnection.getConnection();
            System.out.println("[DEBUG] Database connection established successfully");

            // Test UserDAO
            UserDAO userDAO = new UserDAO(conn);
            User admin = userDAO.validateUser("admin", "admin123", "admin");
            System.out.println("[DEBUG] UserDAO test: Logged in as: " + (admin != null ? admin.getRole() : "null"));

        } catch (SQLException e) {
            System.err.println("[ERROR] Database connection or query failed: " + e.getMessage());
            ErrorHandler.handleError(null, "DAO test failed due to database error. Check MySQL server and credentials.", e);
        } catch (Exception e) {
            System.err.println("[ERROR] Unexpected error in DAO tests: " + e.getMessage());
            ErrorHandler.handleError(null, "Unexpected error during DAO tests", e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                    System.out.println("[DEBUG] Database connection closed");
                } catch (SQLException e) {
                    System.err.println("[ERROR] Failed to close database connection: " + e.getMessage());
                }
            }
        }
    }
}