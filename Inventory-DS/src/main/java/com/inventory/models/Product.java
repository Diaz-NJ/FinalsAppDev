package main.java.com.inventory.models;

public class Product {
    private int id;
    private int displayId; // New field for sequential display
    private String name;
    private String categoryName;
    private Integer stock;
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
        this.displayId = 0; // Initialize to 0
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getDisplayId() { return displayId; }
    public void setDisplayId(int displayId) { this.displayId = displayId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}