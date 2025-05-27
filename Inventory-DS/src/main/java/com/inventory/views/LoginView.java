package main.java.com.inventory.views;

import main.java.com.inventory.models.User;
import main.java.com.inventory.dao.UserDAO;
import main.java.com.inventory.services.SessionManager;
import main.java.com.inventory.utils.ThemeManager;
import main.java.com.inventory.utils.ErrorHandler;
import main.java.com.inventory.dao.DBConnection;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.SQLException;

public class LoginView extends JFrame implements ThemeManager.ThemeChangeListener {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleComboBox;

    public LoginView() {
        initializeUI();
        ThemeManager.addThemeChangeListener(this);
    }

    private void initializeUI() {
        setTitle("Inventory System - Login");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(new JLabel("Username:"));
        usernameField = new JTextField();
        panel.add(usernameField);

        panel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        panel.add(passwordField);

        panel.add(new JLabel("Role:"));
        roleComboBox = new JComboBox<>(new String[]{"User", "Admin"});
        panel.add(roleComboBox);

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(this::handleLogin);
        panel.add(loginButton);

        add(panel);

        // Apply theme to all components
        ThemeManager.applyThemeToComponent(this);
        ThemeManager.applyThemeToComponent(panel);
        ThemeManager.applyThemeToComponent(usernameField);
        ThemeManager.applyThemeToComponent(passwordField);
        ThemeManager.applyThemeToComponent(roleComboBox);
        ThemeManager.applyThemeToComponent(loginButton);
        SwingUtilities.updateComponentTreeUI(this);
    }

    private void handleLogin(ActionEvent e) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String role = (String) roleComboBox.getSelectedItem();

        try {
            if (username.isEmpty() || password.isEmpty()) {
                throw new IllegalArgumentException("Username and password cannot be empty");
            }

            Connection conn = DBConnection.getConnection();
            UserDAO userDAO = new UserDAO(conn);
            User authenticatedUser = userDAO.validateUser(username, password, role);

            if (authenticatedUser != null) {
                SessionManager.startSession(authenticatedUser);
                this.dispose();
                SwingUtilities.invokeLater(() -> {
                    DashboardView dashboard = new DashboardView(SessionManager.getCurrentUser());
                    dashboard.setVisible(true);
                });
            } else {
                throw new SecurityException("Invalid credentials or role mismatch");
            }
        } catch (SQLException ex) {
            ErrorHandler.handleError(this, "Database error during login", ex);
        } catch (IllegalArgumentException | SecurityException ex) {
            ErrorHandler.handleError(this, ex.getMessage(), ex);
        } catch (Exception ex) {
            ErrorHandler.handleError(this, "Unexpected error during login", ex);
        }
    }

    @Override
    public void onThemeChanged(ThemeManager.ThemeMode newTheme) {
        System.out.println("[DEBUG] LoginView: Theme changed to " + newTheme);
        SwingUtilities.updateComponentTreeUI(this);
        ThemeManager.applyThemeToComponent(this);
    }
}