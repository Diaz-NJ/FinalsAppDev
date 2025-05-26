package main.java.com.inventory.views;

import main.java.com.inventory.dao.UserDAO;
import main.java.com.inventory.models.User;
import main.java.com.inventory.services.SessionManager;
import main.java.com.inventory.utils.ErrorHandler;
import main.java.com.inventory.dao.DBConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class UserView extends JPanel {
    private JTable userTable;
    private DefaultTableModel tableModel;

    public interface RegisterCallback {
        void onRegister(String username, String password, String role);
    }
    
    public UserView() {
        setLayout(new BorderLayout());

        String[] columns = {"ID", "Username", "Role","DB_ID"};
        tableModel = new DefaultTableModel(columns, 0){ 
            @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }};
        userTable = new JTable(tableModel);
        add(new JScrollPane(userTable), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Add User");
        JButton deleteButton = new JButton("Delete");
        JButton refreshButton = new JButton("View All");

        addButton.addActionListener(e -> {
            if (hasPermission("addUser")) showRegisterDialog(e);
            else ErrorHandler.handleError(this, "Permission denied: Add User not allowed");
        });
        deleteButton.addActionListener(e -> {
            if (hasPermission("deleteUser")) handleDelete(e);
            else ErrorHandler.handleError(this, "Permission denied: Delete User not allowed");
        });
        refreshButton.addActionListener(e -> refreshTable());

        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        add(buttonPanel, BorderLayout.SOUTH);

        refreshTable();
    }

    private void refreshTable() {
        try {
            tableModel.setRowCount(0);
            tableModel.setColumnCount(0);

            String[] columns = {"ID", "Username", "Role", "DB_ID"}; 
            tableModel.setColumnIdentifiers(columns);

            Connection conn = DBConnection.getConnection();
            UserDAO userDAO = new UserDAO(conn);
            List<User> users = userDAO.getAllUsers();

        for (int i = 0; i < users.size(); i++) {
                User user = users.get(i);
                user.setDisplayId(i + 1);
                tableModel.addRow(new Object[] {
                    user.getDisplayId(),
                    user.getUsername(), 
                    user.getRole(),
                    user.getId()
                });
            }
            userTable.removeColumn(userTable.getColumnModel().getColumn(3));
            System.out.println("[DEBUG] Refreshed table with " + users.size() + " users");
        } catch (SQLException e) {
            ErrorHandler.handleError(this, "Error loading users", e);
        }
    }

private void handleDelete(ActionEvent e) {
    int selectedRow = userTable.getSelectedRow();
    if (selectedRow >= 0) {
        int userIdToDelete = (int) tableModel.getValueAt(selectedRow, 3);
        String username = (String) tableModel.getValueAt(selectedRow, 1);
        
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Permanently delete user '" + username + "'?",
            "Confirm User Deletion",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                 Connection conn = DBConnection.getConnection();
                    UserDAO userDAO = new UserDAO(conn);
                    boolean success = userDAO.deleteUser(userIdToDelete);
                
                if (success) {
                    refreshTable();
                    JOptionPane.showMessageDialog(this, "User deleted successfully",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    ErrorHandler.handleError(this, "No user found with ID: " + userIdToDelete);
                }
            } catch (SQLException ex) {
                    String message = ex.getMessage().contains("foreign key constraint") ?
                        "Cannot delete user: They are referenced by another record (e.g., in a related table)" :
                        "Database error while deleting user";
                    ErrorHandler.handleError(this, message, ex);
                }
            }
        } else {
            ErrorHandler.handleError(this, "Please select a user first", null);
        }
    }

    private void showRegisterDialog(ActionEvent e) {
    RegisterDialog dialog = new RegisterDialog((JFrame)SwingUtilities.getWindowAncestor(this));
    dialog.setRegisterCallback((username, password, role, success) -> {
        if (!success) {
                return;
            }

        int confirm = JOptionPane.showConfirmDialog(
            dialog,
            "Create new user '" + username + "' with role '" + role + "'?",
            "Confirm User Creation",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Connection conn = DBConnection.getConnection();
                    UserDAO userDAO = new UserDAO(conn);
                    boolean addSuccess = userDAO.addUser(username, password, role);
                    if (addSuccess) {
                        refreshTable();
                        JOptionPane.showMessageDialog(this, "User registered successfully!",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        ErrorHandler.handleError(dialog, "Failed to add user: " + username);
                    }
                } catch (SQLException ex) {
                    String message = ex.getMessage().contains("Duplicate entry") || 
                                     ex.getMessage().contains("already exists") ?
                                     "Username '" + username + "' is already taken" :
                                     "Database error while registering user";
                    ErrorHandler.handleError(this, message, ex);
                }
            }
        });
        dialog.setVisible(true);
    }

     private void updateButtonStates() {
        Component[] components = getComponents();
        for (Component c : components) {
            if (c instanceof JPanel) {
                for (Component btn : ((JPanel)c).getComponents()) {
                    if (btn instanceof JButton) {
                        JButton button = (JButton) btn;
                        switch (button.getText()) {
                            case "Add User":
                                button.setEnabled(hasPermission("addUser"));
                                break;
                            case "Delete":
                                button.setEnabled(hasPermission("deleteUser"));
                                break;
                        }
                    }
                }
            }
        }
    }

    private boolean hasPermission(String permission) {
        if (SessionManager.getCurrentUser() == null || SessionManager.getCurrentUser().getRole() == null) {
            System.out.println("[DEBUG] hasPermission: currentUser or role is null");
            return false;
        }
        if (SessionManager.getCurrentUser().getRole().equals("admin")) {
            System.out.println("[DEBUG] hasPermission: Admin role detected, granting access for " + permission);
            return true;
        }
        String perms = SessionManager.getCurrentUser().getPermissions();
        if (perms == null || perms.isEmpty()) {
            System.out.println("[DEBUG] hasPermission: Permissions string is null or empty");
            return false;
        }
        String[] permArray = perms.split(",");
        for (String perm : permArray) {
            String[] keyValue = perm.split(":");
            if (keyValue.length == 2 && keyValue[0].trim().equals(permission)) {
                boolean hasAccess = keyValue[1].trim().equals("1");
                System.out.println("[DEBUG] hasPermission: Checking " + permission + " -> " + hasAccess);
                return hasAccess;
            }
        }
        System.out.println("[DEBUG] hasPermission: Permission " + permission + " not found in permissions string");
        return false;
    }
}