package main.java.com.inventory;

import main.java.com.inventory.dao.ProductDAO;
import main.java.com.inventory.dao.UserDAO;
import main.java.com.inventory.models.Product;
import main.java.com.inventory.models.User;
import java.sql.SQLException;

public class App {
    public static void main(String[] args) {
        try {
            // Test UserDAO
            UserDAO userDAO = new UserDAO();
            User admin = userDAO.validateUser("admin", "admin123", "admin");
            System.out.println("Logged in as: " + admin.getRole());

            // Test ProductDAO
            ProductDAO productDAO = new ProductDAO(null);
            Product newProduct = new Product(0, "USB Cable", "Electronics", 50, 9.99, "Type-C Cable");
            productDAO.addProduct(newProduct);
            System.out.println("Product added!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}