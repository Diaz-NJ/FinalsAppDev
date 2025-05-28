package main.java.com.inventory.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AuditLogDAO {
    private Connection conn;

    public AuditLogDAO(Connection conn) {
        this.conn = conn;
        if (conn != null) {
            try {
                if (!conn.getAutoCommit()) {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException e) {}
        }
    }

    public void logAction(int userId, String action, String details) throws SQLException {
        if (conn == null || conn.isClosed()) {
            throw new SQLException("Database connection is invalid");
        }
        String sql = "INSERT INTO audit_logs (user_id, action, details, timestamp) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (userId == 0) {
                stmt.setNull(1, java.sql.Types.INTEGER); // Allow null user_id for system actions
            } else {
                stmt.setInt(1, userId);
            }
            stmt.setString(2, action);
            stmt.setString(3, details);
            stmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[ERROR] AuditLogDAO: Failed to log action - " + e.getMessage());
            throw e; // Re-throw to ensure the caller handles it
        }
    }

    public List<AuditLog> getAllAuditLogs() throws SQLException {
        List<AuditLog> logs = new ArrayList<>();
        if (conn == null || conn.isClosed()) {
            throw new SQLException("Database connection is invalid");
        }
        String sql = "SELECT al.id, al.user_id, u.username, al.action, al.details, al.timestamp " +
                     "FROM audit_logs al LEFT JOIN users u ON al.user_id = u.id ORDER BY al.timestamp DESC";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                AuditLog log = new AuditLog(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getString("username") != null ? rs.getString("username") : "System",
                    rs.getString("action"),
                    rs.getString("details"),
                    rs.getTimestamp("timestamp").toLocalDateTime()
                );
                logs.add(log);
            }
        }
        return logs;
    }

    public List<AuditLog> searchAuditLogs(String query) throws SQLException {
        List<AuditLog> logs = new ArrayList<>();
        if (conn == null || conn.isClosed()) {
            throw new SQLException("Database connection is invalid");
        }
        String sql = "SELECT al.id, al.user_id, u.username, al.action, al.details, al.timestamp " +
                     "FROM audit_logs al LEFT JOIN users u ON al.user_id = u.id " +
                     "WHERE u.username LIKE ? OR al.action LIKE ? OR al.details LIKE ? " +
                     "ORDER BY al.timestamp DESC";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            String searchTerm = "%" + query.trim() + "%";
            stmt.setString(1, searchTerm);
            stmt.setString(2, searchTerm);
            stmt.setString(3, searchTerm);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    AuditLog log = new AuditLog(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("username") != null ? rs.getString("username") : "System",
                        rs.getString("action"),
                        rs.getString("details"),
                        rs.getTimestamp("timestamp").toLocalDateTime()
                    );
                    logs.add(log);
                }
            }
        }
        return logs;
    }

    public static class AuditLog {
        private int id;
        private int userId;
        private String username;
        private String action;
        private String details;
        private LocalDateTime timestamp;

        public AuditLog(int id, int userId, String username, String action, String details, LocalDateTime timestamp) {
            this.id = id;
            this.userId = userId;
            this.username = username;
            this.action = action;
            this.details = details;
            this.timestamp = timestamp;
        }

        public int getId() { return id; }
        public int getUserId() { return userId; }
        public String getUsername() { return username; }
        public String getAction() { return action; }
        public String getDetails() { return details; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }
}