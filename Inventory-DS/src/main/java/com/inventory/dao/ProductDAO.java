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

    public boolean addProduct(Product product) throws SQLException {
        int categoryId;
        try {
            categoryId = getCategoryId(product.getCategoryName());
        } catch (SQLException e) {
            categoryId = createCategory(product.getCategoryName());
        }

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

    public Product getProduct(int id) throws SQLException {
        String sql = "SELECT p.id, p.name, c.name AS category_name, p.stock, p.price, p.description " +
                    "FROM products p LEFT JOIN categories c ON p.category_id = c.id WHERE p.id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Product product = new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("category_name"),
                        rs.getInt("stock"),
                        rs.getDouble("price"),
                        rs.getString("description")
                    );
                    product.setDisplayId(0); // Not used in single product fetch
                    return product;
                }
            }
        }
        return null;
    }

    public List<Product> getAllProducts() throws SQLException {
        List<Product> products = new ArrayList<>();

        String sql = "SELECT p.id, p.name, c.name AS category_name, p.stock, " +
                     "p.price, p.description FROM products p " +
                     "LEFT JOIN categories c ON p.category_id = c.id ORDER BY p.id";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            int sequenceNumber = 1;
            while (rs.next()) {
                Product product = new Product(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("category_name"),
                    rs.getInt("stock"),
                    rs.getDouble("price"),
                    rs.getString("description")
                );
                product.setDisplayId(sequenceNumber++);
                products.add(product);
            }
        }
        return products;
    }

    public List<Product> getLowStockProducts(int threshold) throws SQLException {
        List<Product> lowStockProducts = new ArrayList<>();
        String sql = "SELECT p.id, p.name, c.name AS category_name, p.stock, p.price, p.description " +
                     "FROM products p LEFT JOIN categories c ON p.category_id = c.id " +
                     "WHERE p.stock < ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, threshold);
            try (ResultSet rs = stmt.executeQuery()) {
                int sequenceNumber = 1;
                while (rs.next()) {
                    Product product = new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("category_name"),
                        rs.getInt("stock"),
                        rs.getDouble("price"),
                        rs.getString("description")
                    );
                    product.setDisplayId(sequenceNumber++);
                    lowStockProducts.add(product);
                }
            }
        }
        return lowStockProducts;
    }

    public List<Product> searchProducts(String query) throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.id, p.name, c.name AS category_name, p.stock, p.price, p.description " +
                     "FROM products p LEFT JOIN categories c ON p.category_id = c.id " +
                     "WHERE p.name LIKE ? OR p.description LIKE ? OR c.name LIKE ? OR p.id = ? " +
                     "ORDER BY p.id";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            String searchTerm = "%" + query + "%";
            stmt.setString(1, searchTerm);
            stmt.setString(2, searchTerm);
            stmt.setString(3, searchTerm);
            try {
                int id = Integer.parseInt(query);
                stmt.setInt(4, id);
            } catch (NumberFormatException e) {
                stmt.setInt(4, -1); // Invalid ID, no match
            }
            try (ResultSet rs = stmt.executeQuery()) {
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
        }
        return products;
    }

    public boolean updateProduct(Product product) throws SQLException {
        int categoryId;
        try {
            categoryId = getCategoryId(product.getCategoryName());
        } catch (SQLException e) {
            categoryId = createCategory(product.getCategoryName());
        }

        String sql = "UPDATE products SET name=?, category_id=?, stock=?, price=?, description=? WHERE id=?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, product.getName());
            stmt.setInt(2, categoryId);
            stmt.setInt(3, product.getStock());
            stmt.setDouble(4, product.getPrice());
            stmt.setString(5, product.getDescription());
            stmt.setInt(6, product.getId());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deleteProduct(int productId) throws SQLException {
        String sql = "DELETE FROM products WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                return true;
            }
            return false;
        }
    }
}