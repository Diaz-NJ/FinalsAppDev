package main.java.com.inventory.dao;

import main.java.com.inventory.models.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private Connection conn;
    private AuditLogDAO auditLogDAO;

    public UserDAO(Connection conn) {
        this.conn = conn;
        this.auditLogDAO = new AuditLogDAO(conn);
    }

    public User validateUser(String username, String password, String role) throws SQLException {
    String sql = "SELECT * FROM users WHERE username = ? AND password = ? AND LOWER(role) = LOWER(?)";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, username);
        stmt.setString(2, password);
        stmt.setString(3, role);
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                String dbRole = rs.getString("role");
                String normalizedRole = dbRole != null ? 
                dbRole.substring(0, 1).toUpperCase() + dbRole.substring(1).toLowerCase() : null;
                String dbPermissions = rs.getString("permissions");
                User user = new User(rs.getInt("id"), rs.getString("username"), normalizedRole);
                user.setPermissions(dbPermissions != null ? dbPermissions : user.getDefaultPermissions(normalizedRole));
                return user;
            }
        }
    }
    return null;
}

    public boolean usernameExists(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    boolean exists = rs.getInt(1) > 0;
                    return exists;
                }
            }
        }
        return false;
    }

    public boolean addUser(User user, String password) throws SQLException {
        boolean autoCommit = conn.getAutoCommit();
        try {
            conn.setAutoCommit(false);
            if (usernameExists(user.getUsername())) {
                conn.rollback();
                return false;
            }

            String sql = "INSERT INTO users (username, password, role, permissions) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, user.getUsername());
                stmt.setString(2, password);
                stmt.setString(3, user.getRole());
                stmt.setString(4, user.getPermissions());
                int affectedRows = stmt.executeUpdate();
                if (affectedRows > 0) {
                    try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            user.setId(generatedKeys.getInt(1));
                        }
                    }
                    auditLogDAO.logAction(user.getId(), "User Added", 
                        String.format("Username: %s, Role: %s", user.getUsername(), user.getRole()));
                    conn.commit();
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }
            }
        } catch (SQLException e) {
            conn.rollback();
            System.err.println("[ERROR] Failed to add user: " + user.getUsername() + " - " + e.getMessage());
            throw e;
        } finally {
            conn.setAutoCommit(autoCommit);
        }
    }

    public boolean addUser(String username, String password, String role) throws SQLException {
        User user = new User(0, username, role); // Permissions set by User constructor
        return addUser(user, password);
    }

    public boolean deleteUser(int userId) throws SQLException {
        String sql = "SELECT username FROM users WHERE id = ?";
        String username = null;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    username = rs.getString("username");
                }
            }
        }

        boolean autoCommit = conn.getAutoCommit();
        try {
            conn.setAutoCommit(false);
            String details = String.format("User ID: %d, Username: %s", userId, username != null ? username : "Unknown");
            auditLogDAO.logAction(userId, "User Deleted", details); // Log before deletion

            sql = "DELETE FROM users WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                int affectedRows = stmt.executeUpdate();
                if (affectedRows > 0) {
                    conn.commit();
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }
            }
        } catch (SQLException e) {
            conn.rollback();
            System.err.println("[ERROR] Failed to delete user with ID: " + userId + " - " + e.getMessage());
            throw e;
        } finally {
            conn.setAutoCommit(autoCommit);
        }
    }

    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                User user = new User(rs.getInt("id"), rs.getString("username"), rs.getString("role"));
                user.setPermissions(rs.getString("permissions"));
                users.add(user);
            }
        }
        return users;
    }

    public List<User> searchUsers(String query) throws SQLException {
        List<User> users = new ArrayList<>();
        String trimmedQuery = query.trim();
        System.out.println("[DEBUG] searchUsers: Raw query = '" + query + "', Trimmed query = '" + trimmedQuery + "'");
        String sql = "SELECT id, username, role, permissions FROM users WHERE username LIKE ? OR role LIKE ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + trimmedQuery + "%");
            stmt.setString(2, "%" + trimmedQuery + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    User user = new User(rs.getInt("id"), rs.getString("username"), rs.getString("role"));
                    user.setPermissions(rs.getString("permissions"));
                    users.add(user);
                }
            }
        }
        return users;
    }
}