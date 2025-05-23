package main.java.com.inventory.dao;

import main.java.com.inventory.models.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private Connection conn;
     public UserDAO() throws SQLException {
        this.conn = DBConnection.getConnection();
    }
    // Login validation
    public User validateUser(String username, String password, String role) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ? AND role = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, role);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // In a real app, use BCrypt.checkpw(password, storedHash)
                String storedHash = rs.getString("password");
                if (password.equals(storedHash)) { // Replace with proper hashing
                    return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("role")
                    );
                }
            }
            return null; // Invalid credentials
        }
    }

    // Add a new user (for admin)
    public void addUser(User user, String password) throws SQLException {
        if (user == null || user.getUsername() == null || user.getUsername().trim().isEmpty()) {
        throw new SQLException("Username cannot be empty");
    }
        String checkSql = "SELECT COUNT(*) FROM users WHERE username = ?";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
        checkStmt.setString(1, user.getUsername());
        ResultSet rs = checkStmt.executeQuery();

        if (rs.next() && rs.getInt(1) > 0) {
            throw new SQLException("Username already exists");
        }
    }
        String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getUsername().trim());
            stmt.setString(2, password); // Store hashed password in real apps
            stmt.setString(3, user.getRole());
            stmt.executeUpdate();
        }
    }
    // Add to UserDAO.java
public List<User> getAllUsers() throws SQLException {
    List<User> users = new ArrayList<>();
    String sql = "SELECT id, username, role FROM users ORDER BY id";

   try (Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {

        int sequenceNumber = 1;
        while (rs.next()) {
            User user = new User(
                rs.getInt("id"), // Display this instead of id
                rs.getString("username"),
                rs.getString("role")
            );
            user.setDisplayId(sequenceNumber++);
            users.add(user);
        }
    }
    return users;
}

public boolean deleteUser(int userId) throws SQLException {
    // First verify user exists
    String checkSql = "SELECT id FROM users WHERE id = ?";
    String deleteSql = "DELETE FROM users WHERE id = ?";
    
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement checkStmt = conn.prepareStatement(checkSql);
         PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
        
        // Check existence
        checkStmt.setInt(1, userId);
        ResultSet rs = checkStmt.executeQuery();
        if (!rs.next()) {
            return false; // User doesn't exist
        }
        
        // Delete user
        deleteStmt.setInt(1, userId);
        return deleteStmt.executeUpdate() > 0;
    }
}

public void addUser(String username, String password, String role) throws SQLException {
    String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, username);
        stmt.setString(2, password); // Hash this in production!
        stmt.setString(3, role);
        stmt.executeUpdate();
    }
}
}