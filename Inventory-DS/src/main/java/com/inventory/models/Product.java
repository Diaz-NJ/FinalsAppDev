package main.java.com.inventory.models;

public class Product {
    private int id;
    private String name;
    private String categoryName; // Changed from categoryId
    private int stock;
    private double price;
    private String description;

    // Constructor
    public Product(int id, String name, String categoryName, int stock, 
                 double price, String description) {
        this.id = id;
        this.name = name;
        this.categoryName = categoryName;
        this.stock = stock;
        this.price = price;
        this.description = description;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getCategoryName() { return categoryName; }
    public int getStock() { return stock; }
    public double getPrice() { return price; }
    public String getDescription() { return description; }

    // Setters (optional)
    public void setStock(int stock) { this.stock = stock; }
}