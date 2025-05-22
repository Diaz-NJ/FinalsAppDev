package main.java.com.inventory.services;

import main.java.com.inventory.models.User;
import java.time.LocalDateTime;
import java.time.Duration;

public class SessionManager {
    private static User activeUser;
    private static LocalDateTime loginTime;
    
    public static void startSession(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        activeUser = user;
        loginTime = LocalDateTime.now(); // Initialize login time
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
            
        } finally {
            // Always clear session even if logging fails
            activeUser = null;
            loginTime = null;
        }
    }

    public static User getCurrentUser() {
        return activeUser;
    }
}