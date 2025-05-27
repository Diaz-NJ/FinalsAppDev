package main.java.com.inventory.views;

import main.java.com.inventory.models.User;
import main.java.com.inventory.services.SessionManager;
import main.java.com.inventory.utils.ThemeManager;
import main.java.com.inventory.utils.ThemeTogglePanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DashboardView extends JFrame implements ThemeManager.ThemeChangeListener {
    private User currentUser;
    private Connection conn;
    private JButton logoutButton;
    private JToolBar toolBar;
    private JTabbedPane tabbedPane;

    public DashboardView(User user) {
        this.currentUser = user;
        try {
            // Initialize database connection
            this.conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/inventory_ds", "username", "password");
            ThemeManager.setTheme(ThemeManager.ThemeMode.DARK); // Set initial theme
            initializeUI();
            setupWindowListener();
            ThemeManager.addThemeChangeListener(this);
            applyThemeToComponents();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database connection failed: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void initializeUI() {
        setTitle("Dashboard - Welcome, " + currentUser.getUsername());
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);

        toolBar = new JToolBar();
        toolBar.setFloatable(false);

        logoutButton = new JButton("Logout", new ImageIcon("src/assets/logout.png"));
        logoutButton.setFocusPainted(false);
        logoutButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        logoutButton.addActionListener(this::performLogout);
        toolBar.add(logoutButton);

        ThemeTogglePanel themePanel = new ThemeTogglePanel();
        toolBar.add(themePanel);

        add(toolBar, BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Products", new ProductView(currentUser, conn));
        
        if (currentUser.getRole().equals("admin")) {
            tabbedPane.addTab("Users", new UserView()); 
        }
        
        add(tabbedPane, BorderLayout.CENTER);
    }

    private void setupWindowListener() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmAndExit();
            }
        });
    }

    private void performLogout(ActionEvent e) {
        confirmAndExit();
    }

    private void confirmAndExit() {
        try {
            ThemeManager.setTheme(ThemeManager.getCurrentTheme()); // Reapply theme
            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Logout from " + currentUser.getUsername() + "?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );

            if (confirm == JOptionPane.YES_OPTION) {
                User userToLogout = currentUser;
                dispose();
                
                if (userToLogout != null) {
                    SessionManager.clearSession(userToLogout);
                }

                EventQueue.invokeLater(() -> {
                    try {
                        new LoginView().setVisible(true);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null,
                            "Application restart failed. Please relaunch.\nError: " + ex.getMessage(),
                            "Fatal Error",
                            JOptionPane.ERROR_MESSAGE);
                        System.exit(1);
                    }
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Logout process failed: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void applyThemeToComponents() {
        getContentPane().setBackground(ThemeManager.getCurrentTheme() == ThemeManager.ThemeMode.LIGHT ?
            ThemeManager.LIGHT_COLORS.get("Panel.background") : ThemeManager.DARK_COLORS.get("Panel.background"));
        
        toolBar.setBackground(ThemeManager.getCurrentTheme() == ThemeManager.ThemeMode.LIGHT ?
            ThemeManager.LIGHT_COLORS.get("ToolBar.background") : ThemeManager.DARK_COLORS.get("ToolBar.background"));
        ThemeManager.applyThemeToComponent(toolBar);
        ThemeManager.applyThemeToComponent(logoutButton);
        
        tabbedPane.setBackground(ThemeManager.getCurrentTheme() == ThemeManager.ThemeMode.LIGHT ?
            ThemeManager.LIGHT_COLORS.get("TabbedPane.background") : ThemeManager.DARK_COLORS.get("TabbedPane.background"));
        tabbedPane.setForeground(ThemeManager.getCurrentTheme() == ThemeManager.ThemeMode.LIGHT ?
            ThemeManager.LIGHT_COLORS.get("TabbedPane.foreground") : ThemeManager.DARK_COLORS.get("TabbedPane.foreground"));
        ThemeManager.applyThemeToComponent(tabbedPane);
        for (Component tab : tabbedPane.getComponents()) {
            ThemeManager.applyThemeToComponent(tab);
        }
        
        System.out.println("[DEBUG] DashboardView applied theme - ContentPane Background: " + getContentPane().getBackground());
        System.out.println("[DEBUG] DashboardView toolbar background: " + toolBar.getBackground());
        System.out.println("[DEBUG] DashboardView tabbedPane background: " + tabbedPane.getBackground());
    }

    @Override
    public void onThemeChanged(ThemeManager.ThemeMode newTheme) {
        System.out.println("[DEBUG] DashboardView: Theme changed to " + newTheme);
        SwingUtilities.updateComponentTreeUI(this);
        applyThemeToComponents();
        repaint();
        revalidate();
        System.out.println("[DEBUG] DashboardView contentPane background after update: " + getContentPane().getBackground());
    }
}