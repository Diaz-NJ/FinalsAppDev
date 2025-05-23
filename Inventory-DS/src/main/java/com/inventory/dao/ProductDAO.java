package main.java.com.inventory.dao;

import main.java.com.inventory.models.Product;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {
    private Connection conn;

    public ProductDAO(Connection conn) {
        this.conn = conn;
    }

    // Create
public boolean addProduct(Product product) throws SQLException {
    // 1. Get or create category
    int categoryId;
    try {
        categoryId = getCategoryId(product.getCategoryName());
    } catch (SQLException e) {
        // Category doesn't exist - create it
        System.out.println("[DEBUG] Creating new category: " + product.getCategoryName());
        categoryId = createCategory(product.getCategoryName());
    }

    // 2. Add product with the category ID
    String sql = "INSERT INTO products (name, category_id, stock, price, description) VALUES (?, ?, ?, ?, ?)";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, product.getName());
        stmt.setInt(2, categoryId);
        stmt.setInt(3, product.getStock());
        stmt.setDouble(4, product.getPrice());
        stmt.setString(5, product.getDescription());
        
        return stmt.executeUpdate() > 0;
    }
}

private int createCategory(String categoryName) throws SQLException {
    String sql = "INSERT INTO categories (name) VALUES (?)";
    try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        stmt.setString(1, categoryName);
        stmt.executeUpdate();
        
        try (ResultSet rs = stmt.getGeneratedKeys()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        throw new SQLException("Failed to create category");
    }
}

private int getCategoryId(String categoryName) throws SQLException {
    String sql = "SELECT id FROM categories WHERE name = ?";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, categoryName);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return rs.getInt("id");
        }
        throw new SQLException("Category not found");
    }
}  

    // Read (single)
    public Product getProduct(int id) throws SQLException {
        String sql = "SELECT * FROM products WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("category_name"),
                        rs.getInt("stock"),
                        rs.getDouble("price"),
                        rs.getString("description")
                    );
                }
            }
        }
        return null;
    }

    // Read (all)
    public List<Product> getAllProducts() throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.id, p.name, c.name AS category_name, p.stock, " +
                 "p.price, p.description FROM products p " +
                 "LEFT JOIN categories c ON p.category_id = c.id";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Product product = new Product(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("category_name"),
                    rs.getInt("stock"),
                    rs.getDouble("price"),
                    rs.getString("description")
                );
            products.add(product);
            }
        }
        return products;
    }

    // Search
    public List<Product> searchProducts(String query) throws SQLException {
        List<Product> products = new ArrayList<>();
        try {
        int searchId = Integer.parseInt(query);
        // First try exact ID match
        String idSql = "SELECT * FROM products WHERE id = ?";
        try (PreparedStatement idStmt = conn.prepareStatement(idSql)) {
            idStmt.setInt(1, searchId);
            ResultSet rs = idStmt.executeQuery();
            if (rs.next()) {
                Product product = new Product(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("category_name"),
                    rs.getInt("stock"),
                    rs.getDouble("price"),
                    rs.getString("description")
                );
                products.add(product);
                return products; // Return immediately if ID match found
            }
        }
    } catch (NumberFormatException e) {
        // Not a number - proceed to text search
    }

    // If no ID match found, search text fields
    String textSql = "SELECT * FROM products WHERE name LIKE ? OR description LIKE ?";
    try (PreparedStatement textStmt = conn.prepareStatement(textSql)) {
        String searchTerm = "%" + query + "%";
        textStmt.setString(1, searchTerm);
        textStmt.setString(2, searchTerm);
        ResultSet rs = textStmt.executeQuery();
        while (rs.next()) {
            Product product = new Product(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("category_name"),
                rs.getInt("stock"),
                rs.getDouble("price"),
                rs.getString("description")
            );
            products.add(product);
        }
    }
    return products;
    }

    // Update
    public boolean updateProduct(Product product) throws SQLException {
        String sql = "UPDATE products SET name=?, category_name=?, stock=?, price=?, description=? WHERE id=?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, product.getName());
            stmt.setString(2, product.getCategoryName());
            stmt.setInt(3, product.getStock());
            stmt.setDouble(4, product.getPrice());
            stmt.setString(5, product.getDescription());
            stmt.setInt(6, product.getId());
            return stmt.executeUpdate() > 0;
        }
    }

    // Delete
public boolean deleteProduct(int productId) throws SQLException {
    String sql = "DELETE FROM products WHERE id = ?";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, productId);
        int affectedRows = stmt.executeUpdate();
        
        if (affectedRows > 0) {
            resetAutoIncrement(); // Reset counter after deletion
            return true;
        }
        return false;
    }
}

private void resetAutoIncrement() throws SQLException {
    // Get current max ID
    String maxIdSql = "SELECT MAX(id) FROM products";
    try (Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(maxIdSql)) {
        if (rs.next()) {
            int maxId = rs.getInt(1);
            // Reset auto-increment to next available number
            String resetSql = "ALTER TABLE products AUTO_INCREMENT = " + (maxId + 1);
            try (Statement resetStmt = conn.createStatement()) {
                resetStmt.executeUpdate(resetSql);
            }
        }
    }
}
}