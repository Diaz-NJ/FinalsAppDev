package main.java.com.inventory.views;

import main.java.com.inventory.dao.UserDAO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;

public class RegisterDialog extends JDialog {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleComboBox;

    public RegisterDialog(JFrame parent) {
        super(parent, "Register New User", true);
        setLayout(new GridLayout(4, 2, 10, 10));
        setSize(400, 200);  // Increased size
        setLocationRelativeTo(parent);

        // Components
        usernameField = new JTextField(15);
        passwordField = new JPasswordField(15);
        roleComboBox = new JComboBox<>(new String[]{"user", "admin"});

        // Layout
        add(new JLabel("Username:"));
        add(usernameField);
        add(new JLabel("Password:"));
        add(passwordField);
        add(new JLabel("Role:"));
        add(roleComboBox);

        // Register Button
        JButton registerButton = new JButton("Register");
        registerButton.addActionListener(this::registerUser);
        add(registerButton);

        // Cancel Button
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        add(cancelButton);

        pack();  // Adjusts window to fit components
    }

    private void registerUser(ActionEvent e) {
        try {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String role = (String) roleComboBox.getSelectedItem();

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Username and password are required!", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            UserDAO userDAO = new UserDAO();
            userDAO.addUser(username, password, role);
            JOptionPane.showMessageDialog(this, 
                "User registered successfully!");
            dispose();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, 
                "Error: " + ex.getMessage(), 
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}