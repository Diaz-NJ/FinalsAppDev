package main.java.com.inventory.views;

import main.java.com.inventory.models.User;
import javax.swing.*;
import java.awt.*;

public class DashboardView extends JFrame {
    public DashboardView(User user) {
        setTitle("Dashboard - Welcome, " + user.getUsername() + " (" + user.getRole() + ")");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();

        // Add tabs based on user role
        tabbedPane.addTab("Products", new ProductView(user));
        if (user.getRole().equals("admin")) {
            tabbedPane.addTab("Users", new JPanel()); // Placeholder for UserView
        }

        add(tabbedPane);
    }
}