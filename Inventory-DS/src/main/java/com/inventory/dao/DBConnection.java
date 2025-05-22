package main.java.com.inventory.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/inventory_ds";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // Default XAMPP password (empty)

    // Private constructor to prevent instantiation
    private DBConnection() {}

    // Get a connection to the database
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // Load MySQL JDBC driver
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found!", e);
        }
    }

    // Test the connection (optional)
    public static void main(String[] args) {
        try (Connection conn = DBConnection.getConnection()) {
            System.out.println("✅ Database connected successfully!");
        } catch (SQLException e) {
            System.err.println("❌ Connection failed: " + e.getMessage());
        }
    }
}
