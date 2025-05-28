package main.java.com.inventory.views;

import main.java.com.inventory.dao.UserDAO;
import main.java.com.inventory.utils.ErrorHandler;
import main.java.com.inventory.utils.ThemeManager;
import main.java.com.inventory.dao.DBConnection;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.SQLException;

public class RegisterDialog extends JDialog implements ThemeManager.ThemeChangeListener {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleComboBox;
    private RegisterCallback callback;

    public RegisterDialog(JFrame parent) {
        super(parent, "Register New User", true);
        setLayout(new GridLayout(4, 2, 10, 10));
        setSize(400, 200);  // Increased size
        setLocationRelativeTo(parent);

        usernameField = new JTextField(15);
        passwordField = new JPasswordField(15);
        roleComboBox = new JComboBox<>(new String[]{"user", "admin"});

        add(new JLabel("Username:"));
        add(usernameField);
        add(new JLabel("Password:"));
        add(passwordField);
        add(new JLabel("Role:"));
        add(roleComboBox);

        JButton registerButton = new JButton("Register");
        registerButton.addActionListener(this::registerUser);
        add(registerButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(_ -> dispose());
        add(cancelButton);

        pack();
        ThemeManager.addThemeChangeListener(this);
    }

 public interface RegisterCallback {
    void onRegister(String username, String password, String role, boolean success  );
}

public void setRegisterCallback(RegisterCallback callback) {
    this.callback = callback;
}

    private void registerUser(ActionEvent e) {
        try {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String role = (String) roleComboBox.getSelectedItem();

               if (username.isEmpty() || password.isEmpty()) {
            throw new IllegalArgumentException("Username and password cannot be empty");
        }

        if (username.length() < 3) {
                throw new IllegalArgumentException("Username must be at least 3 characters long");
            }

            if (password.length() < 6) {
                throw new IllegalArgumentException("Password must be at least 6 characters long");
            }

             Connection conn = DBConnection.getConnection();
            UserDAO userDAO = new UserDAO(conn);
            
            if (userDAO.usernameExists(username)) {
                throw new IllegalArgumentException("Username '" + username + "' is already taken");
            }

            dispose();
            if (callback != null) {
                callback.onRegister(username, password, role, true);
            } else {
                throw new SQLException("Failed to add user: " + username);
            }
        
        } catch (SQLException ex) {
            String message = ex.getMessage().contains("Duplicate entry") || 
                             ex.getMessage().contains("already exists") ?
                             "Username '" + usernameField.getText().trim() + "' is already taken" :
                             "Database error while registering user";
            ErrorHandler.handleError(this, message, ex);
        if (callback != null) {
                callback.onRegister(usernameField.getText().trim(), new String(passwordField.getPassword()),
                    (String) roleComboBox.getSelectedItem(), false);
            }
        } catch (IllegalArgumentException ex) {
            ErrorHandler.handleError(this, ex.getMessage(), ex);
            if (callback != null) {
                callback.onRegister(usernameField.getText().trim(), new String(passwordField.getPassword()),
                    (String) roleComboBox.getSelectedItem(), false);
            }
        } catch (Exception ex) {
            ErrorHandler.handleError(this, "Unexpected error while registering user", ex);
            if (callback != null) {
                callback.onRegister(usernameField.getText().trim(), new String(passwordField.getPassword()),
                    (String) roleComboBox.getSelectedItem(), false);
            }
        }
    }
        @Override
    public void onThemeChanged(ThemeManager.ThemeMode newTheme) {
        SwingUtilities.updateComponentTreeUI(this);
    }
}
    