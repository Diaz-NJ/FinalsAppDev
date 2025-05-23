package main.java.com.inventory.views;

import main.java.com.inventory.models.User;
import main.java.com.inventory.services.SessionManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DashboardView extends JFrame {
    private User currentUser;
    private Connection conn;
    private JButton logoutButton;

    public DashboardView(User user) {
        this.currentUser = user;
        try {
            // Initialize database connection
            this.conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/inventory_ds", "username", "password");
            initializeUI();
            setupWindowListener();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database connection failed: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void initializeUI() {
        setTitle("Dashboard - Welcome, " + currentUser.getUsername());
        setSize(1000,700);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Changed for proper cleanup
        setLocationRelativeTo(null);

        // Create toolbar with logout button
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        logoutButton = new JButton("Logout", new ImageIcon("src/assets/logout.png"));
        styleLogoutButton();
        logoutButton.addActionListener(this::performLogout);
        
        toolBar.add(logoutButton);
        add(toolBar, BorderLayout.NORTH);

        // Create tabbed interface
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Products", new ProductView(currentUser, conn));
        
        if (currentUser.getRole().equals("admin")) {
            tabbedPane.addTab("Users", new UserView()); // Assuming UserView exists
        }
        
        add(tabbedPane, BorderLayout.CENTER);
    }

    private void styleLogoutButton() {
        logoutButton.setBackground(new Color(235, 64, 52)); // Red color
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFocusPainted(false);
        logoutButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
    }

    private void setupWindowListener() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmAndExit();
            }
        });
    }

    private void performLogout(ActionEvent e) {
        confirmAndExit();
    }

    private void confirmAndExit() {
    try {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Logout from " + currentUser.getUsername() + "?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            // Ensure we have a valid user reference
            User userToLogout = currentUser;
            dispose(); // Close window first to prevent race conditions
            
            // Clear session after UI cleanup
            if (userToLogout != null) {
                SessionManager.clearSession(userToLogout);
            }

            // Restart login view
            EventQueue.invokeLater(() -> {
                try {
                    new LoginView().setVisible(true);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null,
                        "Application restart failed. Please relaunch.\nError: " + ex.getMessage(),
                        "Fatal Error",
                        JOptionPane.ERROR_MESSAGE);
                    System.exit(1);
                }
            });
        }
    } catch (Exception ex) {
        JOptionPane.showMessageDialog(this,
            "Logout process failed: " + ex.getMessage(),
            "Error",
            JOptionPane.ERROR_MESSAGE);
    }
}
}