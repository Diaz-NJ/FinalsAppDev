package main.java.com.inventory.dao;

import main.java.com.inventory.models.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    // Login validation
    public User validateUser(String username, String password) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
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
        String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, password); // Store hashed password in real apps
            stmt.setString(3, user.getRole());
            stmt.executeUpdate();
        }
    }
    // Add to UserDAO.java
public List<User> getAllUsers() throws SQLException {
    List<User> users = new ArrayList<>();
    String sql = "SELECT * FROM users";
    try (Connection conn = DBConnection.getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {
        while (rs.next()) {
            users.add(new User(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("role")
            ));
        }
    }
    return users;
}

public void deleteUser(int userId) throws SQLException {
    String sql = "DELETE FROM users WHERE id=?";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, userId);
        stmt.executeUpdate();
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