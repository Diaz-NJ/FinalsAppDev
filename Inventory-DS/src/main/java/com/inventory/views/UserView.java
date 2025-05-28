package main.java.com.inventory.views;

import main.java.com.inventory.dao.UserDAO;
import main.java.com.inventory.models.User;
import main.java.com.inventory.services.SessionManager;
import main.java.com.inventory.utils.ErrorHandler;
import main.java.com.inventory.utils.ThemeManager;
import main.java.com.inventory.dao.DBConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserView extends JPanel implements ThemeManager.ThemeChangeListener {
    private JTable userTable;
    private DefaultTableModel tableModel;
    private JButton addButton, deleteButton, refreshButton, auditLogButton;
    private JTextField searchField;
    private Connection conn;

    public interface RegisterCallback {
        void onRegister(String username, String password, String role);
    }
    
    public UserView(Connection conn) { // Updated constructor
        this.conn = conn;
        setLayout(new BorderLayout());
        initializeUI();
        setupKeyBindings();
        refreshTable();
        ThemeManager.addThemeChangeListener(this);
        applyThemeToComponents();
    }

    private void initializeUI() {
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search:"));
        searchField = new JTextField(20);
        searchField.setToolTipText("Search by Display ID, username, or role (Ctrl+S)");
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                searchUsers();
            }
        });
        searchPanel.add(searchField);
        add(searchPanel, BorderLayout.NORTH);

        String[] columns = {"ID", "Username", "Role", "DB_ID"};
        tableModel = new DefaultTableModel(columns, 0) { 
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        userTable = new JTable(tableModel);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(userTable), BorderLayout.CENTER);
        userTable.removeColumn(userTable.getColumnModel().getColumn(3));

        JPanel buttonPanel = new JPanel();
        addButton = new JButton("Add User");
        deleteButton = new JButton("Delete");
        refreshButton = new JButton("View All");
        auditLogButton = new JButton("View Audit Logs");

        addButton.addActionListener(e -> {
            if (hasPermission("addUser")) showRegisterDialog(e);
            else ErrorHandler.handleError(this, "Permission denied: Add User not allowed");
        });
        deleteButton.addActionListener(e -> {
            if (hasPermission("deleteUser")) handleDelete(e);
            else ErrorHandler.handleError(this, "Permission denied: Delete User not allowed");
        });
        refreshButton.addActionListener(_ -> refreshTable());
        auditLogButton.addActionListener(_ -> showAuditLogView());

        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(auditLogButton);
        add(buttonPanel, BorderLayout.SOUTH);

        ThemeManager.applyThemeToComponent(searchPanel);
        ThemeManager.applyThemeToComponent(searchField);
        ThemeManager.applyThemeToComponent(addButton);
        ThemeManager.applyThemeToComponent(deleteButton);
        ThemeManager.applyThemeToComponent(refreshButton);
        ThemeManager.applyThemeToComponent(auditLogButton);
    }

    private void setupKeyBindings() {
        InputMap inputMap = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK), "search");
        actionMap.put("search", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchField.requestFocusInWindow();
            }
        });
    }

    private void searchUsers() {
        String query = searchField.getText().trim();
        System.out.println("[DEBUG] UserView.searchUsers: Query = '" + query + "'");
        try {
            tableModel.setRowCount(0);
            UserDAO userDAO = new UserDAO(conn);
            List<User> users;

            if (query.matches("\\d+")) {
                try {
                    int displayId = Integer.parseInt(query);
                    System.out.println("[DEBUG] UserView.searchUsers: Searching for Display ID = " + displayId);
                    users = userDAO.getAllUsers();
                    List<User> filteredUsers = new ArrayList<>();
                    int currentDisplayId = 1;
                    for (User user : users) {
                        user.setDisplayId(currentDisplayId);
                        System.out.println("[DEBUG] UserView.searchUsers: Checking user - displayId=" + currentDisplayId + ", DB_ID=" + user.getId());
                        if (currentDisplayId == displayId) {
                            filteredUsers.add(user);
                            System.out.println("[DEBUG] UserView.searchUsers: Found match for Display ID " + displayId + " with DB_ID " + user.getId());
                            break;
                        }
                        currentDisplayId++;
                    }
                    users = filteredUsers;
                } catch (NumberFormatException e) {
                    System.out.println("[DEBUG] UserView.searchUsers: Invalid numeric query");
                    users = userDAO.searchUsers(query);
                }
            } else {
                System.out.println("[DEBUG] UserView.searchUsers: Searching for username or role");
                users = query.isEmpty() ? userDAO.getAllUsers() : userDAO.searchUsers(query);
                for (int i = 0; i < users.size(); i++) {
                    users.get(i).setDisplayId(i + 1);
                }
            }

            for (User user : users) {
                System.out.println("[DEBUG] UserView.searchUsers: Adding to table - displayId=" + user.getDisplayId() + ", DB_ID=" + user.getId());
                tableModel.addRow(new Object[] {
                    user.getDisplayId(),
                    user.getUsername(),
                    user.getRole(),
                    user.getId()
                });
            }
            System.out.println("[DEBUG] Searched users with query '" + query + "', found: " + users.size());
        } catch (SQLException e) {
            System.err.println("[ERROR] UserView: Error searching users - " + e.getMessage());
            ErrorHandler.handleError(this, "Error searching users", e);
        }
    }

    private void refreshTable() {
        try {
            tableModel.setRowCount(0);
            tableModel.setColumnCount(0);

            String[] columns = {"ID", "Username", "Role", "DB_ID"}; 
            tableModel.setColumnIdentifiers(columns);

            UserDAO userDAO = new UserDAO(conn);
            List<User> users = userDAO.getAllUsers();

            for (int i = 0; i < users.size(); i++) {
                User user = users.get(i);
                user.setDisplayId(i + 1);
                System.out.println("[DEBUG] refreshTable: Adding user - displayId=" + user.getDisplayId() + ", DB_ID=" + user.getId());
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
            System.err.println("[ERROR] UserView: Error refreshing table - " + e.getMessage());
            ErrorHandler.handleError(this, "Error loading users", e);
        }
    }

    private void handleDelete(ActionEvent e) {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow >= 0) {
            int userIdToDelete = (int) tableModel.getValueAt(selectedRow, 3);
            String username = (String) tableModel.getValueAt(selectedRow, 1);
            
            ThemeManager.setTheme(ThemeManager.getCurrentTheme());
            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Permanently delete user '" + username + "'?",
                "Confirm User Deletion",
                JOptionPane.YES_NO_OPTION
            );
            
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    UserDAO userDAO = new UserDAO(conn);
                    boolean success = userDAO.deleteUser(userIdToDelete);
                    
                    if (success) {
                        refreshTable();
                        ThemeManager.setTheme(ThemeManager.getCurrentTheme());
                        JOptionPane.showMessageDialog(this, "User deleted successfully",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        ErrorHandler.handleError(this, "No user found with ID: " + userIdToDelete);
                    }
                } catch (SQLException ex) {
                    String message = ex.getMessage().contains("foreign key constraint") ?
                        "Cannot delete user: They are referenced by another record (e.g., in a related table)" :
                        "Database error while deleting user";
                    System.err.println("[ERROR] UserView: " + message + " - " + ex.getMessage());
                    ErrorHandler.handleError(this, message, ex);
                }
            }
        } else {
            ErrorHandler.handleError(this, "Please select a user first", null);
        }
    }

    private void showRegisterDialog(ActionEvent e) {
        RegisterDialog dialog = new RegisterDialog((JFrame)SwingUtilities.getWindowAncestor(this));
        dialog.setRegisterCallback((username, password, role) -> {
            ThemeManager.setTheme(ThemeManager.getCurrentTheme());
            int confirm = JOptionPane.showConfirmDialog(
                dialog,
                "Create new user '" + username + "' with role '" + role + "'?",
                "Confirm User Creation",
                JOptionPane.YES_NO_OPTION
            );
            
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    UserDAO userDAO = new UserDAO(conn);
                    System.out.println("[DEBUG] UserView: Attempting to add user - Username: " + username + ", Role: " + role);
                    boolean addSuccess = userDAO.addUser(username, password, role);
                    if (addSuccess) {
                        refreshTable();
                        ThemeManager.setTheme(ThemeManager.getCurrentTheme());
                        JOptionPane.showMessageDialog(this, "User registered successfully!",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        System.err.println("[ERROR] UserView: Failed to add user: " + username);
                        ErrorHandler.handleError(dialog, "Failed to add user: " + username);
                    }
                } catch (SQLException ex) {
                    String message = ex.getMessage().contains("Duplicate entry") || 
                                     ex.getMessage().contains("already exists") ?
                                     "Username '" + username + "' is already taken" :
                                     "Database error while registering user: " + ex.getMessage();
                    System.err.println("[ERROR] UserView: " + message);
                    ErrorHandler.handleError(this, message, ex);
                }
            }
        });
        dialog.setVisible(true);
    }

    private void showAuditLogView() {
        if (!SessionManager.getCurrentUser().getRole().equals("admin")) {
            ErrorHandler.handleError(this, "Permission denied: Only admins can view audit logs");
            return;
        }
        try {
            JDialog dialog = new JDialog((JFrame)SwingUtilities.getWindowAncestor(this), "Audit Logs", true);
            dialog.setSize(800, 600);
            dialog.setLocationRelativeTo(this);
            dialog.add(new AuditLogView(SessionManager.getCurrentUser(), conn));
            dialog.setVisible(true);
        } catch (SQLException e) {
            System.err.println("[ERROR] UserView: Error opening audit log view - " + e.getMessage());
            ErrorHandler.handleError(this, "Error opening audit log view", e);
        }
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
                            case "View Audit Logs":
                                button.setEnabled(SessionManager.getCurrentUser().getRole().equals("admin"));
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

    private void applyThemeToComponents() {
        ThemeManager.applyThemeToComponent(this);
        ThemeManager.applyThemeToComponent(userTable);
        ThemeManager.applyThemeToComponent(addButton);
        ThemeManager.applyThemeToComponent(deleteButton);
        ThemeManager.applyThemeToComponent(refreshButton);
        ThemeManager.applyThemeToComponent(auditLogButton);
        ThemeManager.applyThemeToComponent(searchField);
        for (Component comp : getComponents()) {
            if (comp instanceof JPanel) {
                JPanel panel = (JPanel) comp;
                panel.setBackground(ThemeManager.getCurrentTheme() == ThemeManager.ThemeMode.LIGHT ?
                    ThemeManager.LIGHT_COLORS.get("Panel.background") : ThemeManager.DARK_COLORS.get("Panel.background"));
                for (Component subComp : panel.getComponents()) {
                    ThemeManager.applyThemeToComponent(subComp);
                }
            }
        }
        System.out.println("[DEBUG] UserView applied theme - Background: " + getBackground());
    }

    @Override
    public void onThemeChanged(ThemeManager.ThemeMode newTheme) {
        System.out.println("[DEBUG] UserView: Theme changed to " + newTheme);
        SwingUtilities.updateComponentTreeUI(this);
        userTable.setBackground(newTheme == ThemeManager.ThemeMode.LIGHT ?
            ThemeManager.LIGHT_COLORS.get("Table.background") : ThemeManager.DARK_COLORS.get("Table.background"));
        userTable.setForeground(newTheme == ThemeManager.ThemeMode.LIGHT ?
            ThemeManager.LIGHT_COLORS.get("Table.foreground") : ThemeManager.DARK_COLORS.get("Table.foreground"));
        applyThemeToComponents();
        repaint();
        revalidate();
        System.out.println("[DEBUG] UserView background after update: " + getBackground());
    }

    private class RegisterDialog extends JDialog implements ThemeManager.ThemeChangeListener {
        private JTextField usernameField;
        private JPasswordField passwordField;
        private JComboBox<String> roleComboBox;
        private JButton registerButton;
        private RegisterCallback callback;

        public RegisterDialog(JFrame parent) {
            super(parent, "Register New User", true);
            setLayout(new GridLayout(4, 2, 10, 10));

            usernameField = new JTextField(15);
            passwordField = new JPasswordField(15);
            roleComboBox = new JComboBox<>(new String[]{"user", "admin"});
            registerButton = new JButton("Register");

            add(new JLabel("Username:"));
            add(usernameField);
            add(new JLabel("Password:"));
            add(passwordField);
            add(new JLabel("Role:"));
            add(roleComboBox);
            add(new JLabel(""));
            add(registerButton);

            registerButton.addActionListener(_ -> {
                String username = usernameField.getText().trim();
                String password = new String(passwordField.getPassword()).trim();
                String role = (String) roleComboBox.getSelectedItem();

                if (username.isEmpty() || password.isEmpty()) {
                    ErrorHandler.handleError(this, "Username and password cannot be empty");
                    return;
                }

                if (password.length() < 8) {
                    ErrorHandler.handleError(this, "Password must be at least 8 characters long");
                    return;
                }

                if (callback != null) {
                    callback.onRegister(username, password, role);
                    dispose();
                }
            });

            ThemeManager.addThemeChangeListener(this);
            applyThemeToComponents();
            pack();
            setLocationRelativeTo(parent);
        }

        public void setRegisterCallback(RegisterCallback callback) {
            this.callback = callback;
        }

        private void applyThemeToComponents() {
            ThemeManager.applyThemeToComponent(this);
            ThemeManager.applyThemeToComponent(usernameField);
            ThemeManager.applyThemeToComponent(passwordField);
            ThemeManager.applyThemeToComponent(roleComboBox);
            ThemeManager.applyThemeToComponent(registerButton);
            for (Component comp : getContentPane().getComponents()) {
                if (comp instanceof JLabel) {
                    ThemeManager.applyThemeToComponent(comp);
                }
            }
            System.out.println("[DEBUG] RegisterDialog applied theme - Background: " + getBackground());
        }

        @Override
        public void onThemeChanged(ThemeManager.ThemeMode newTheme) {
            System.out.println("[DEBUG] RegisterDialog: Theme changed to " + newTheme);
            SwingUtilities.updateComponentTreeUI(this);
            applyThemeToComponents();
            repaint();
            revalidate();
            System.out.println("[DEBUG] RegisterDialog updated - Background: " + getBackground());
        }
    }
}