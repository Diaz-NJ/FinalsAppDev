package main.java.com.inventory.dao;

import main.java.com.inventory.models.Product;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {
    // Create
    public void addProduct(Product product) throws SQLException {
        String sql = "INSERT INTO products (name, category_name, stock, price, description) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, product.getName());
            stmt.setString(2, product.getCategoryName());
            stmt.setInt(3, product.getStock());
            stmt.setDouble(4, product.getPrice());
            stmt.setString(5, product.getDescription());
            stmt.executeUpdate();
        }
    }

    // Read (all products)
    public List<Product> getAllProducts() throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.id, p.name, c.name AS category_name, p.stock, p.price, p.description " +
             "FROM products p JOIN categories c ON p.category_id = c.id";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
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

    // Update
    public void updateProduct(Product product) throws SQLException {
        String sql = "UPDATE products SET name=?, category_name=?, stock=?, price=?, description=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, product.getName());
            stmt.setString(2, product.getCategoryName());
            stmt.setInt(3, product.getStock());
            stmt.setDouble(4, product.getPrice());
            stmt.setString(5, product.getDescription());
            stmt.setInt(6, product.getId());
            stmt.executeUpdate();
        }
    }

    // Delete
    public void deleteProduct(int productId) throws SQLException {
        String sql = "DELETE FROM products WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            stmt.executeUpdate();
        }
    }
}
