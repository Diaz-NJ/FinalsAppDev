package main.java.com.inventory.services;

import main.java.com.inventory.models.User;
import main.java.com.inventory.dao.AuditLogDAO;
import main.java.com.inventory.dao.DBConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.Duration;

public class SessionManager {
    private static User activeUser;
    private static LocalDateTime loginTime;
    private static AuditLogDAO auditLogDAO;

    static {
        try {
            Connection conn = DBConnection.getConnection();
            auditLogDAO = new AuditLogDAO(conn);
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to initialize AuditLogDAO in SessionManager: " + e.getMessage());
        }
    }

    public static void startSession(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        activeUser = user;
        loginTime = LocalDateTime.now();
        try {
            auditLogDAO.logAction(user.getId(), "User Login", 
                String.format("Username: %s, Role: %s", user.getUsername(), user.getRole()));
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to log login action for user " + user.getUsername() + ": " + e.getMessage());
        }
        System.out.println("Session started for: " + user.getUsername());
    }

    public static void clearSession(User user) {
        try {
            if (user == null || loginTime == null) {
                System.out.println("Session cleanup skipped - no active session");
                return;
            }
            
            Duration duration = Duration.between(loginTime, LocalDateTime.now());
            System.out.printf(
                "Session ended - User: %s, Duration: %d minutes %d seconds%n",
                user.getUsername(),
                duration.toMinutes(),
                duration.getSeconds() % 60
            );
            
            try {
                auditLogDAO.logAction(user.getId(), "User Logout", 
                    String.format("Username: %s, Session Duration: %d minutes", 
                        user.getUsername(), duration.toMinutes()));
            } catch (SQLException e) {
                System.err.println("[ERROR] Failed to log logout action for user " + user.getUsername() + ": " + e.getMessage());
            }
        } finally {
            activeUser = null;
            loginTime = null;
        }
    }

    public static User getCurrentUser() {
        return activeUser;
    }
}