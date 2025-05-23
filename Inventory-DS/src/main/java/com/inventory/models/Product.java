package main.java.com.inventory.models;

public class Product {
    private int id;
    private String name;
    private String categoryName;
    private int stock;
    private double price;
    private String description;

    public Product(int id, String name, String categoryName, int stock, 
                 double price, String description) {
        this.id = id;
        this.name = name;
        this.categoryName = categoryName;
        this.stock = stock;
        this.price = price;
        this.description = description;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}