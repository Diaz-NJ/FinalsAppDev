package main.java.com.inventory.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/inventory_ds";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // Default XAMPP password (empty)
    private static Connection connection = null;

    private DBConnection() {}

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                initializeDatabase(connection);
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL JDBC Driver not found!", e);
            }
        }
        return connection;
    }

    public static void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            connection = null;
        }
    }

    private static void initializeDatabase(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            String createAuditLogsTable = "CREATE TABLE IF NOT EXISTS audit_logs (" +
                                         "id INT AUTO_INCREMENT PRIMARY KEY, " +
                                         "user_id INT, " +
                                         "action VARCHAR(100) NOT NULL, " +
                                         "details TEXT, " +
                                         "timestamp DATETIME NOT NULL, " +
                                         "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL" +
                                         ")";
            stmt.executeUpdate(createAuditLogsTable);
        }
    }
}