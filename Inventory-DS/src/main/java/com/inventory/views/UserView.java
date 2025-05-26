package main.java.com.inventory.views;

import main.java.com.inventory.dao.UserDAO;
import main.java.com.inventory.models.User;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.util.List;

public class UserView extends JPanel {
    private JTable userTable;
    private DefaultTableModel tableModel;

    // ADD THIS INTERFACE:
public interface RegisterCallback {
    void onRegister(String username, String password, String role);
}
    @SuppressWarnings("unused")
    public UserView() {
        setLayout(new BorderLayout());

        // Table setup
        String[] columns = {"ID", "Username", "Role","DB_ID"};
        tableModel = new DefaultTableModel(columns, 0){ 
            @Override
        public boolean isCellEditable(int row, int column) {
            return false; // Make all cells non-editable
        }};
        userTable = new JTable(tableModel);
        add(new JScrollPane(userTable), BorderLayout.CENTER);

        // Buttons panel
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Add User");
        JButton deleteButton = new JButton("Delete");
        JButton refreshButton = new JButton("View All");

        addButton.addActionListener(this::showRegisterDialog);
        deleteButton.addActionListener(this::handleDelete);
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

              String[] columns = {"ID", "Username", "Role", "DB_ID"}; // 4 columns
        tableModel.setColumnIdentifiers(columns);

            UserDAO userDAO = new UserDAO();
            List<User> users = userDAO.getAllUsers(); // Add this method to UserDAO

        for (User user : users) {
            tableModel.addRow(new Object[] {
                user.getDisplayId(), // Display position
                user.getUsername(), 
                user.getRole(),
                user.getId() // Hidden actual ID
            });
        }
         userTable.removeColumn(userTable.getColumnModel().getColumn(3));
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, 
            "Error loading users: " + e.getMessage(), 
            "Error", 
            JOptionPane.ERROR_MESSAGE);
    }
}

private void handleDelete(ActionEvent e) {
    int selectedRow = userTable.getSelectedRow();
    if (selectedRow >= 0) {
        // Get the actual ID from the hidden column (column 3 in this case)
        int userIdToDelete = (int) tableModel.getValueAt(selectedRow, 3); // Renamed from actualUserId
        String username = (String) tableModel.getValueAt(selectedRow, 1);
        
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Permanently delete user '" + username + "'?",
            "Confirm User Deletion",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                UserDAO userDAO = new UserDAO();
                boolean success = userDAO.deleteUser(userIdToDelete); // Use actual ID
                
                if (success) {
                    refreshTable();
                    JOptionPane.showMessageDialog(this, "User deleted successfully");
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Failed to delete user", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                    "Database error: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }
}

    private void showRegisterDialog(ActionEvent e) {
    RegisterDialog dialog = new RegisterDialog((JFrame)SwingUtilities.getWindowAncestor(this));
    dialog.setRegisterCallback((username, password, role) -> {
        int confirm = JOptionPane.showConfirmDialog(
            dialog,
            "Create new user '" + username + "' with role '" + role + "'?",
            "Confirm User Creation",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                UserDAO userDAO = new UserDAO();
                userDAO.addUser(username, password, role);
                refreshTable();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, 
                    "Error: " + ex.getMessage(), 
                    "Registration Failed", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    });
    dialog.setVisible(true);
}
}