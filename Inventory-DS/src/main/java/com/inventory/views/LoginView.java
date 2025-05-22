package main.java.com.inventory.views;

import main.java.com.inventory.dao.UserDAO;
import main.java.com.inventory.models.User;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;

public class LoginView extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleComboBox;

    public LoginView() {
        setTitle("Inventory Management - Login");
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
        roleComboBox = new JComboBox<>(new String[]{"admin", "user"});
        panel.add(roleComboBox);

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(this::handleLogin);
        panel.add(loginButton);

        add(panel);
    }

    private void handleLogin(ActionEvent e) {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        String role = (String) roleComboBox.getSelectedItem();

        try {
            UserDAO userDAO = new UserDAO();
            User user = userDAO.validateUser(username, password);

            if (user != null && user.getRole().equals(role)) {
                JOptionPane.showMessageDialog(this, "Login successful!");
                new DashboardView(user).setVisible(true);
                dispose(); // Close login window
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginView().setVisible(true));
    }
}