package main.java.com.inventory.dao;

import main.java.com.inventory.models.Product;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ProductDAO {
    private Connection conn;
    private AuditLogDAO auditLogDAO;
    private int userId;

    public ProductDAO(Connection conn, int userId) {
        this.conn = conn;
        this.userId = userId;
        this.auditLogDAO = new AuditLogDAO(conn);
    }

    public boolean addProduct(Product product) throws SQLException {
        int categoryId;
        try {
            categoryId = getCategoryId(product.getCategoryName());
        } catch (SQLException e) {
            categoryId = createCategory(product.getCategoryName());
        }

        String sql = "INSERT INTO products (name, category_id, stock, price, description) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, product.getName());
            stmt.setInt(2, categoryId);
            stmt.setInt(3, product.getStock());
            stmt.setDouble(4, product.getPrice());
            stmt.setString(5, product.getDescription());
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        product.setId(rs.getInt(1));
                    }
                }
                auditLogDAO.logAction(userId, "Product Added", 
                    String.format("Product ID: %d, Name: %s", product.getId(), product.getName()));
                return true;
            }
            return false;
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
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                auditLogDAO.logAction(userId, "Product Edited", 
                    String.format("Product ID: %d, Name: %s", product.getId(), product.getName()));
                return true;
            }
            return false;
        }
    }

    public boolean deleteProduct(int productId) throws SQLException {
        String sql = "SELECT name FROM products WHERE id = ?";
        String productName = null;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    productName = rs.getString("name");
                }
            }
        }

        sql = "DELETE FROM products WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                auditLogDAO.logAction(userId, "Product Deleted", 
                    String.format("Product ID: %d, Name: %s", productId, productName != null ? productName : "Unknown"));
                return true;
            }
            return false;
        }
    }

    // New method to import products from a CSV file
    public void importProductsFromCSV(String filePath) throws SQLException, IOException, CsvValidationException {
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            // Read header row
            String[] headers = reader.readNext();
            if (headers == null || headers.length != 5 || 
                !headers[0].equals("name") || !headers[1].equals("category_name") || 
                !headers[2].equals("stock") || !headers[3].equals("price") || 
                !headers[4].equals("description")) {
                throw new IOException("Invalid CSV format. Expected headers: name,category_name,stock,price,description");
            }

            String[] row;
            while ((row = reader.readNext()) != null) {
                // Parse CSV row
                String name = row[0].trim();
                String categoryName = row[1].trim();
                String stockStr = row[2].trim();
                String priceStr = row[3].trim();
                String description = row[4].trim();

                // Validate required fields
                if (name.isEmpty() || categoryName.isEmpty() || stockStr.isEmpty() || priceStr.isEmpty()) {
                    System.out.println("[WARN] Skipping row due to missing required fields: " + String.join(",", row));
                    continue;
                }

                // Parse numeric fields
                int stock;
                double price;
                try {
                    stock = Integer.parseInt(stockStr);
                    price = Double.parseDouble(priceStr);
                } catch (NumberFormatException e) {
                    System.out.println("[WARN] Skipping row due to invalid stock or price format: " + String.join(",", row));
                    continue;
                }

                // Validate numeric values
                if (stock < 0 || price < 0) {
                    System.out.println("[WARN] Skipping row due to negative stock or price: " + String.join(",", row));
                    continue;
                }

                // Create product
                Product product = new Product(0, name, categoryName, stock, price, description);

                // Insert into database
                try {
                    addProduct(product);
                } catch (SQLException e) {
                    System.out.println("[ERROR] Failed to import product: " + name + " - " + e.getMessage());
                    throw e; // Re-throw to handle in UI
                }
            }

            auditLogDAO.logAction(userId, "Imported Products", "Imported products from CSV file: " + filePath);
        }
    }

    // New method to export products to a CSV file
    public void exportProductsToCSV(String filePath) throws SQLException, IOException {
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
            // Write header
            writer.writeNext(new String[]{"name", "category_name", "stock", "price", "description"});

            // Get all products
            List<Product> products = getAllProducts();

            // Write each product to CSV
            for (Product product : products) {
                writer.writeNext(new String[]{
                    product.getName(),
                    product.getCategoryName() != null ? product.getCategoryName() : "",
                    String.valueOf(product.getStock()),
                    String.valueOf(product.getPrice()),
                    product.getDescription() != null ? product.getDescription() : ""
                });
            }

            auditLogDAO.logAction(userId, "Exported Products", "Exported products to CSV file: " + filePath);
        }
    }
}