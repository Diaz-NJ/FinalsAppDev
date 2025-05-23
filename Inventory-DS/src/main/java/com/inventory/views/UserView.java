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

    @SuppressWarnings("unused")
    public UserView() {
        setLayout(new BorderLayout());

        // Table setup
        String[] columns = {"ID", "Username", "Role"};
        tableModel = new DefaultTableModel(columns, 0);
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
            UserDAO userDAO = new UserDAO();
            List<User> users = userDAO.getAllUsers(); // Add this method to UserDAO

            for (User user : users) {
                Object[] row = {user.getId(), user.getUsername(), user.getRole()};
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading users: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showRegisterDialog(ActionEvent e) {
        new RegisterDialog((JFrame)SwingUtilities.getWindowAncestor(this)).setVisible(true);
    }

    private void handleDelete(ActionEvent e) {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow >= 0) {
            int userId = (int) tableModel.getValueAt(selectedRow, 0);
            try {
                UserDAO userDAO = new UserDAO();
                userDAO.deleteUser(userId); // Add this method to UserDAO
                refreshTable();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Delete failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Select a user first!", "Error", JOptionPane.WARNING_MESSAGE);
        }
    }
}