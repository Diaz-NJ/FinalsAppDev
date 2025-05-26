package main.java.com.inventory.utils;

import javax.swing.*;
import java.awt.Component;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;

public class ErrorHandler {
    public static void handleError(Component parent, String userMessage, Exception e) {
        // Log error details to console (extensible to file)
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("[ERROR] ").append(LocalDateTime.now()).append(": ").append(userMessage);
        if (e != null) {
            logMessage.append("\nCause: ").append(e.getMessage());
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            logMessage.append("\nStack Trace:\n").append(sw.toString());
        }
        System.err.println(logMessage);

        // Show user-friendly message
        JOptionPane.showMessageDialog(parent, userMessage, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void handleError(Component parent, String userMessage) {
        handleError(parent, userMessage, null);
    }
}