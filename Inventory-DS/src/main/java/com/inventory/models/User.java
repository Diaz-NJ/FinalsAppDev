package main.java.com.inventory.models;

public class User {
    private int id;
    private int displayId;
    private String username;
    private String role;

    public User(int id, String username, String role) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.displayId = 0;
    }

    // Getters
    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
    public int getDisplayId() { return displayId; }

    // Setters (optional)
    public void setRole(String role) { this.role = role; }
    public void setDisplayId(int displayId) { this.displayId = displayId; }

   

}