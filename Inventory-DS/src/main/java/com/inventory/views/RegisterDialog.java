package main.java.com.inventory.views;

import main.java.com.inventory.dao.UserDAO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;

public class RegisterDialog extends JDialog {
    public RegisterDialog(JFrame parent) {
        super(parent, "Register New User", true);
        setLayout(new GridLayout(4, 2, 10, 10));
        setSize(300, 200);

        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JComboBox<String> roleComboBox = new JComboBox<>(new String[]{"admin", "user"});

        add(new JLabel("Username:"));
        add(usernameField);
        add(new JLabel("Password:"));
        add(passwordField);
        add(new JLabel("Role:"));
        add(roleComboBox);

        JButton registerButton = new JButton("Register");
        registerButton.addActionListener(e -> {
            try {
                UserDAO userDAO = new UserDAO();
                userDAO.addUser(
                    usernameField.getText(),
                    new String(passwordField.getPassword()), // In real apps, hash this!
                    (String) roleComboBox.getSelectedItem()
                );
                JOptionPane.showMessageDialog(this, "User registered successfully!");
                dispose();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        add(registerButton);
    }

    public RegisterDialog(UserView userView) {
    }
}