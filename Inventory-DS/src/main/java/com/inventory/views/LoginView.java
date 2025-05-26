package main.java.com.inventory.views;

import main.java.com.inventory.models.User;
import main.java.com.inventory.dao.UserDAO;
import main.java.com.inventory.services.SessionManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;

public class LoginView extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleComboBox;

    public LoginView() {
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Inventory System - Login");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Username
        panel.add(new JLabel("Username:"));
        usernameField = new JTextField();
        panel.add(usernameField);

        // Password
        panel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        panel.add(passwordField);

        // Role
        panel.add(new JLabel("Role:"));
        roleComboBox = new JComboBox<>(new String[]{"User", "Admin"});
        panel.add(roleComboBox);

        // Login Button
        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(this::handleLogin);
        panel.add(loginButton);

        add(panel);
    }

    private void handleLogin(ActionEvent e) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String role = (String) roleComboBox.getSelectedItem();

        try {
            // 1. Validate credentials
            UserDAO userDAO = new UserDAO();
            User authenticatedUser = userDAO.validateUser(username, password, role);

            if (authenticatedUser != null) {
                // 2. Start new session
                SessionManager.startSession(authenticatedUser);
                
                // 3. Close login window
                this.dispose();
                
                // 4. Open dashboard
                SwingUtilities.invokeLater(() -> {
                    DashboardView dashboard = new DashboardView(SessionManager.getCurrentUser());
                    dashboard.setVisible(true);
                });
            } else {
                JOptionPane.showMessageDialog(this,
                    "Invalid credentials or role mismatch",
                    "Login Failed",
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Database error: " + ex.getMessage(),
                "Login Error",
                JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Unexpected error: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        // Ensure UI runs on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            LoginView loginView = new LoginView();
            loginView.setVisible(true);
        });
    }
}