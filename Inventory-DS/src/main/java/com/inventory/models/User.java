package main.java.com.inventory.models;

public class User {
    private int id;
    private int displayId;
    private String username;
    private String permissions;
    private String role;

    public User(int id, String username, String role) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.displayId = 0;
        // Default permissions based on role
        this.permissions = getDefaultPermissions(role);
    }

    public String getDefaultPermissions(String role) {
        switch (role.toLowerCase()) {
            case "owner":
                return "add:1,edit:1,delete:1,addUser:1,deleteUser:1,lowStock:1,view:1";
            case "manager":
                return "add:1,edit:1,delete:1,lowStock:1,view:1"; // Product and audit log access
            case "admin":
                return "addUser:1,deleteUser:1,viewUsers:1,viewAudit:1,addOwner:0,view:1,lowStock:1"; // Added view and lowStock
            case "staff":
                return "add:1,delete:1,edit:1,view:1,lowStock:1,import:1,export:1"; // Full Product dashboard access
            default:
                return "add:0,edit:0,delete:0,addUser:0,deleteUser:0,lowStock:0,view:0";
        }
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getPermissions() { return permissions; }
    public void setPermissions(String permissions) { this.permissions = permissions; }
    public int getDisplayId() { return displayId; }
    public void setDisplayId(int displayId) { this.displayId = displayId; }
}