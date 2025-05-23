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
        String sql = "INSERT INTO products (name, category_name, stock, price, description) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, product.getName());
            stmt.setString(2, product.getCategoryName());
            stmt.setInt(3, product.getStock());
            stmt.setDouble(4, product.getPrice());
            stmt.setString(5, product.getDescription());
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        product.setId(rs.getInt(1));
                        return true;
                    }
                }
            }
            return false;
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
        String sql = "SELECT p.id, p.name, c.name AS category_name, p.stock, p.price, p.description " +
             "FROM products p JOIN categories c ON p.category_name = c.id";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                products.add(new Product(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("category_name"),
                    rs.getInt("stock"),
                    rs.getDouble("price"),
                    rs.getString("description")
                ));
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
                products.add(new Product(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("category_name"),
                    rs.getInt("stock"),
                    rs.getDouble("price"),
                    rs.getString("description")
                ));
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
            products.add(new Product(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("category_name"),
                rs.getInt("stock"),
                rs.getDouble("price"),
                rs.getString("description")
            ));
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
        String sql = "DELETE FROM products WHERE id=?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            return stmt.executeUpdate() > 0;
        }
    }
}