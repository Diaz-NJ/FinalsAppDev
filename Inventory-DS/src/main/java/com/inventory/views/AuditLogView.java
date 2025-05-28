package main.java.com.inventory.views;

import main.java.com.inventory.dao.AuditLogDAO;
import main.java.com.inventory.models.User;
import main.java.com.inventory.utils.ErrorHandler;
import main.java.com.inventory.utils.ThemeManager;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class AuditLogView extends JPanel implements ThemeManager.ThemeChangeListener {
    private JTable logTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JButton refreshButton;
    private AuditLogDAO auditLogDAO;
    private User currentUser;
    private Connection conn;

    public AuditLogView(User user, Connection conn) throws SQLException {
        this.currentUser = user;
        this.conn = conn;
        if (!currentUser.getRole().equals("Owner") && !currentUser.getRole().equals("Manager") && !currentUser.getRole().equals("Admin")) {
        throw new SecurityException("Permission denied: Only Owner, Manager, or Admin can view audit logs");
    }
        if (conn == null || conn.isClosed()) {
            System.err.println("[DEBUG] AuditLogView: Connection is null or closed");
            return; // Prevent further execution if connection is invalid
        }
        this.auditLogDAO = new AuditLogDAO(conn);
        setLayout(new BorderLayout());
        initializeUI();
        refreshTable();
        ThemeManager.addThemeChangeListener(this);
        applyThemeToComponents();
    }

    private void initializeUI() {
        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search:"));
        searchField = new JTextField(20);
        searchField.setToolTipText("Search by Username, Action, or Details");
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                searchLogs();
            }
        });
        searchPanel.add(searchField);
        add(searchPanel, BorderLayout.NORTH);

        // Table setup
        String[] columns = {"ID", "Username", "Action", "Details", "Timestamp"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        logTable = new JTable(tableModel);
        logTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(logTable), BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel();
        refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(_ -> refreshTable());
        buttonPanel.add(refreshButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Apply theme
        ThemeManager.applyThemeToComponent(searchPanel);
        ThemeManager.applyThemeToComponent(searchField);
        ThemeManager.applyThemeToComponent(refreshButton);
        applyThemeToTable();
    }

    private void refreshTable() {
        try {
            tableModel.setRowCount(0);
            List<AuditLogDAO.AuditLog> logs = auditLogDAO.getAllAuditLogs();
            System.out.println("[DEBUG] AuditLogView: Loaded " + logs.size() + " audit logs from database");
            for (AuditLogDAO.AuditLog log : logs) {
                System.out.println("[DEBUG] AuditLogView: Adding log - ID: " + log.getId() + ", Username: " + log.getUsername() + 
                                   ", Action: " + log.getAction() + ", Details: " + log.getDetails() + ", Timestamp: " + log.getTimestamp());
                tableModel.addRow(new Object[]{
                    log.getId(),
                    log.getUsername(),
                    log.getAction(),
                    log.getDetails(),
                    log.getTimestamp().toString()
                });
            }
            applyThemeToTable();
            System.out.println("[DEBUG] AuditLogView: Refreshed table with " + logs.size() + " logs");
        } catch (SQLException e) {
            System.err.println("[ERROR] AuditLogView: SQLException - " + e.getMessage());
            ErrorHandler.handleError(this, "Error loading audit logs", e);
        }
    }

    private void searchLogs() {
        String query = searchField.getText().trim();
        System.out.println("[DEBUG] AuditLogView.searchLogs: Query = '" + query + "'");
        try {
            tableModel.setRowCount(0);
            List<AuditLogDAO.AuditLog> logs = query.isEmpty() ? auditLogDAO.getAllAuditLogs() : auditLogDAO.searchAuditLogs(query);
            for (AuditLogDAO.AuditLog log : logs) {
                tableModel.addRow(new Object[]{
                    log.getId(),
                    log.getUsername(),
                    log.getAction(),
                    log.getDetails(),
                    log.getTimestamp().toString()
                });
            }
            applyThemeToTable();
            System.out.println("[DEBUG] Searched audit logs with query '" + query + "', found: " + logs.size());
        } catch (SQLException e) {
            System.err.println("[ERROR] AuditLogView: SQLException during search - " + e.getMessage());
            ErrorHandler.handleError(this, "Error searching audit logs", e);
        }
    }

    private void applyThemeToTable() {
        java.util.Map<String, java.awt.Color> colors = ThemeManager.getCurrentTheme() == ThemeManager.ThemeMode.LIGHT ?
            ThemeManager.LIGHT_COLORS : ThemeManager.DARK_COLORS;
        logTable.setBackground(colors.get("Table.background"));
        logTable.setForeground(colors.get("Table.foreground"));
        logTable.setGridColor(colors.get("Table.gridColor"));
        logTable.repaint();
        System.out.println("[DEBUG] AuditLogView table updated - Background: " + logTable.getBackground());
    }

    private void applyThemeToComponents() {
        ThemeManager.applyThemeToComponent(this);
        ThemeManager.applyThemeToComponent(searchField);
        ThemeManager.applyThemeToComponent(refreshButton);
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
        System.out.println("[DEBUG] AuditLogView applied theme - Background: " + getBackground());
    }

    @Override
    public void onThemeChanged(ThemeManager.ThemeMode newTheme) {
        System.out.println("[DEBUG] AuditLogView: Theme changed to " + newTheme);
        SwingUtilities.updateComponentTreeUI(this);
        applyThemeToTable();
        applyThemeToComponents();
        repaint();
        revalidate();
        System.out.println("[DEBUG] AuditLogView background after update: " + getBackground());
    }
}