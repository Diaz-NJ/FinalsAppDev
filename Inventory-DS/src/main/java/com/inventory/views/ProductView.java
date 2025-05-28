package main.java.com.inventory.views;

import main.java.com.inventory.dao.ProductDAO;
import main.java.com.inventory.models.Product;
import main.java.com.inventory.models.User;
import main.java.com.inventory.utils.ErrorHandler;
import main.java.com.inventory.utils.ThemeManager;
import main.java.com.inventory.dao.AuditLogDAO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProductView extends JPanel implements ThemeManager.ThemeChangeListener {
    private JTable productTable;
    private DefaultTableModel tableModel;
    private User currentUser;
    private ProductDAO productDAO;
    private Connection conn;
    private JTextField searchField;
    private JButton addButton, editButton, deleteButton, refreshButton, lowStockButton;    
    private static final int LOW_STOCK_THRESHOLD = 20;

    public ProductView(User user, Connection conn) {
        this.currentUser = user;
        this.conn = conn;
        this.productDAO = new ProductDAO(conn, currentUser.getId());
        setLayout(new BorderLayout());
        setupKeyBindings();
        initializeUI();
        applyThemeToTable();
        refreshTable();
        ThemeManager.addThemeChangeListener(this);
    }

    private void initializeUI() {
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        searchPanel.add(new JLabel("Search:"));
        searchField = new JTextField(20);
        searchField.setToolTipText("Search by Display ID, Name, Category, or Description (Ctrl+S)");
        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                searchProducts();
            }
        });
        searchPanel.add(searchField);
        add(searchPanel, BorderLayout.NORTH);

        String[] columns = {"ID", "Name", "Category", "Stock", "Price", "Description", "DB_ID"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        productTable = new JTable(tableModel);
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(productTable);
        add(scrollPane, BorderLayout.CENTER);

        productTable.removeColumn(productTable.getColumnModel().getColumn(6));

        JPanel buttonPanel = new JPanel();
        addButton = new JButton("Add");
        editButton = new JButton("Edit");
        deleteButton = new JButton("Delete");
        refreshButton = new JButton("View All");
        lowStockButton = new JButton("Check Low Stock");

        addButton.addActionListener(_ -> {
            if (hasPermission("add")) showProductDialog(null);
            else ErrorHandler.handleError(this, "Permission denied: Add not allowed");
        });
        editButton.addActionListener(_ -> {
            if (hasPermission("edit")) editSelectedProduct();
            else ErrorHandler.handleError(this, "Permission denied: Edit not allowed");
        });
        deleteButton.addActionListener(_ -> {
            if (hasPermission("delete")) deleteSelectedProduct();
            else ErrorHandler.handleError(this, "Permission denied: Delete not allowed");
        });
        refreshButton.addActionListener(_ -> refreshTable());
        lowStockButton.addActionListener(_ -> {
            if (hasPermission("lowStock")) checkLowStock();
            else ErrorHandler.handleError(this, "Permission denied: Low Stock check not allowed");
        });

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(lowStockButton);

        refreshButton.setToolTipText("View all products (Ctrl+R)");
        searchField.setToolTipText("Search by Display ID, Name, Category, or Description (Ctrl+S)");
        addButton.setToolTipText("Add new product (Ctrl+A)");
        editButton.setToolTipText("Edit selected product (Ctrl+E)");
        deleteButton.setToolTipText("Delete selected product (Ctrl+D)");
        lowStockButton.setToolTipText("Check products with low stock (Ctrl+L)");

        updateButtonStates();
        add(buttonPanel, BorderLayout.SOUTH);
        setFocusable(true);
        requestFocusInWindow();
    }

    private void setupKeyBindings() {
        InputMap inputMap = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK), "search");
        actionMap.put("search", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchField.requestFocusInWindow();
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK), "add");
        actionMap.put("add", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentUser.getRole().equals("admin") || hasPermission("add")) {
                    showProductDialog(null);
                } else {
                    ErrorHandler.handleError(ProductView.this, "Permission denied: Add not allowed");
                }
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK), "edit");
        actionMap.put("edit", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentUser.getRole().equals("admin") || hasPermission("edit")) {
                    editSelectedProduct();
                } else {
                    ErrorHandler.handleError(ProductView.this, "Permission denied: Edit not allowed");
                }
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK), "delete");
        actionMap.put("delete", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentUser.getRole().equals("admin") || hasPermission("delete")) {
                    deleteSelectedProduct();
                } else {
                    ErrorHandler.handleError(ProductView.this, "Permission denied: Delete not allowed");
                }
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK), "refresh");
        actionMap.put("refresh", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshTable();
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK), "lowStock");
        actionMap.put("lowStock", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentUser.getRole().equals("admin") || hasPermission("lowStock")) {
                    checkLowStock();
                } else {
                    ErrorHandler.handleError(ProductView.this, "Permission denied: Low Stock check not allowed");
                }
            }
        });
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        try {
            List<Product> products = productDAO.getAllProducts();
            for (Product product : products) {
                tableModel.addRow(new Object[]{
                    product.getDisplayId(),
                    product.getName(),
                    product.getCategoryName(),
                    product.getStock() == null ? 0 : product.getStock(),
                    String.format("₱%.2f", product.getPrice()),
                    product.getDescription(),
                    product.getId()
                });
            }
            // Log the "Viewed all products" action
            AuditLogDAO auditLogDAO = new AuditLogDAO(conn);
            auditLogDAO.logAction(currentUser.getId(), "Viewed all products", 
                "Viewed by " + currentUser.getUsername());
            System.out.println("[DEBUG] ProductView: Logged 'Viewed all products' action for user: " + currentUser.getUsername());

            applyThemeToTable();
            checkLowStockOnRefresh();
            System.out.println("[DEBUG] Refreshed table with " + products.size() + " products");
        } catch (SQLException e) {
            System.err.println("[ERROR] ProductView: Error loading products - " + e.getMessage());
            ErrorHandler.handleError(this, "Error loading products", e);
        }
    }

    private void applyThemeToTable() {
        Map<String, Color> colors = (ThemeManager.getCurrentTheme() == ThemeManager.ThemeMode.LIGHT) ? 
            ThemeManager.LIGHT_COLORS : ThemeManager.DARK_COLORS;
        productTable.setBackground(colors.get("Table.background"));
        productTable.setForeground(colors.get("Table.foreground"));
        productTable.setGridColor(colors.get("Table.gridColor"));
        productTable.repaint();
        System.out.println("[DEBUG] ProductView table updated - Background: " + productTable.getBackground() + 
                           ", Foreground: " + productTable.getForeground());
    }

    private void checkLowStockOnRefresh() {
        try {
            List<Product> lowStockProducts = productDAO.getLowStockProducts(LOW_STOCK_THRESHOLD);
            if (!lowStockProducts.isEmpty()) {
                StringBuilder message = new StringBuilder("Low stock alert:\n");
                for (Product product : lowStockProducts) {
                    message.append(String.format("ID: %d, Name: %s, Stock: %d\n",
                        product.getDisplayId(), product.getName(), product.getStock()));
                }
                JOptionPane optionPane = new JOptionPane(message.toString(), JOptionPane.WARNING_MESSAGE);
                ThemeManager.applyThemeToOptionPane(optionPane);
                JDialog dialog = optionPane.createDialog(this, "Low Stock Alert");
                dialog.setVisible(true);
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] ProductView: Error checking low stock - " + e.getMessage());
            ErrorHandler.handleError(this, "Error checking low stock", e);
        }
    }

    private void checkLowStock() {
        try {
            List<Product> lowStockProducts = productDAO.getLowStockProducts(LOW_STOCK_THRESHOLD);
            if (lowStockProducts.isEmpty()) {
                JOptionPane optionPane = new JOptionPane("No products with low stock.", JOptionPane.INFORMATION_MESSAGE);
                ThemeManager.applyThemeToOptionPane(optionPane);
                JDialog dialog = optionPane.createDialog(this, "Low Stock Check");
                dialog.setVisible(true);
            } else {
                StringBuilder message = new StringBuilder("Low stock products:\n");
                for (Product product : lowStockProducts) {
                    message.append(String.format("ID: %d, Name: %s, Stock: %d\n",
                        product.getDisplayId(), product.getName(), product.getStock()));
                }
                JOptionPane optionPane = new JOptionPane(message.toString(), JOptionPane.WARNING_MESSAGE);
                ThemeManager.applyThemeToOptionPane(optionPane);
                JDialog dialog = optionPane.createDialog(this, "Low Stock Products");
                dialog.setVisible(true);
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] ProductView: Error checking low stock - " + e.getMessage());
            ErrorHandler.handleError(this, "Error checking low stock", e);
        }
    }

    private void searchProducts() {
        String query = searchField.getText().trim();
        System.out.println("[DEBUG] ProductView.searchProducts: Query = '" + query + "'");
        try {
            tableModel.setRowCount(0);
            List<Product> products;

            // Check if query is a numeric Display ID
            if (query.matches("\\d+")) {
                try {
                    int displayId = Integer.parseInt(query);
                    System.out.println("[DEBUG] ProductView.searchProducts: Searching for Display ID = " + displayId);
                    products = productDAO.getAllProducts();
                    List<Product> filteredProducts = new ArrayList<>();
                    for (Product product : products) {
                        if (product.getDisplayId() == displayId) {
                            filteredProducts.add(product);
                            System.out.println("[DEBUG] ProductView.searchProducts: Found match for Display ID " + displayId + " with DB_ID " + product.getId());
                            break; // Only want the exact match
                        }
                    }
                    products = filteredProducts; // Use filtered list, even if empty
                    System.out.println("[DEBUG] ProductView.searchProducts: Filtered products count = " + products.size());
                } catch (NumberFormatException e) {
                    System.out.println("[DEBUG] ProductView.searchProducts: Invalid numeric query");
                    products = productDAO.searchProducts(query);
                }
            } else {
                System.out.println("[DEBUG] ProductView.searchProducts: Searching for name, description, category, or DB ID");
                products = query.isEmpty() ? productDAO.getAllProducts() : productDAO.searchProducts(query);
                // Assign displayId for non-numeric search results
                int sequenceNumber = 1;
                for (Product product : products) {
                    product.setDisplayId(sequenceNumber++);
                }
            }

            // Populate table
            for (Product product : products) {
                System.out.println("[DEBUG] ProductView.searchProducts: Adding to table - DisplayID=" + product.getDisplayId() + ", DB_ID=" + product.getId() +
                                   ", Name=" + product.getName());
                tableModel.addRow(new Object[]{
                    product.getDisplayId(),
                    product.getName(),
                    product.getCategoryName(),
                    product.getStock(),
                    String.format("₱%.2f", product.getPrice()),
                    product.getDescription(),
                    product.getId()
                });
            }
            productTable.revalidate();
            productTable.repaint();
            applyThemeToTable();
            System.out.println("[DEBUG] Searched products with query '" + query + "', found: " + products.size() + ", table rows: " + tableModel.getRowCount());
        } catch (SQLException e) {
            System.err.println("[ERROR] ProductView: Error searching products - " + e.getMessage());
            ErrorHandler.handleError(this, "Error searching products", e);
        }
    }

    private class ProductDialog extends JDialog implements ThemeManager.ThemeChangeListener {
        private JTextField nameField, categoryField, stockField, priceField, descField;
        private JButton saveButton;

        public ProductDialog(Product product) {
            super(SwingUtilities.getWindowAncestor(ProductView.this), 
                  product == null ? "Add Product" : "Edit Product", 
                  Dialog.ModalityType.APPLICATION_MODAL);
            setLayout(new GridLayout(6, 2, 10, 10));

            nameField = new JTextField(product != null ? product.getName() : "");
            categoryField = new JTextField(product != null ? product.getCategoryName() : "");
            stockField = new JTextField(product != null ? String.valueOf(product.getStock()) : "");
            priceField = new JTextField(product != null ? String.valueOf(product.getPrice()) : "");
            descField = new JTextField(product != null ? product.getDescription() : "");
            saveButton = new JButton("Save");

            add(new JLabel("Name:"));
            add(nameField);
            add(new JLabel("Category:"));
            add(categoryField);
            add(new JLabel("Stock:"));
            add(stockField);
            add(new JLabel("Price:"));
            add(priceField);
            add(new JLabel("Description:"));
            add(descField);
            add(saveButton);

            saveButton.addActionListener(_ -> saveProduct(product));

            ThemeManager.applyThemeToComponent(this);
            applyThemeToComponents();
            ThemeManager.addThemeChangeListener(this);

            pack();
            setLocationRelativeTo(ProductView.this);
        }

        private void applyThemeToComponents() {
            ThemeManager.applyThemeToComponent(nameField);
            ThemeManager.applyThemeToComponent(categoryField);
            ThemeManager.applyThemeToComponent(stockField);
            ThemeManager.applyThemeToComponent(priceField);
            ThemeManager.applyThemeToComponent(descField);
            ThemeManager.applyThemeToComponent(saveButton);
            for (Component comp : getContentPane().getComponents()) {
                if (comp instanceof JLabel) {
                    ThemeManager.applyThemeToComponent(comp);
                }
            }
            System.out.println("[DEBUG] ProductDialog applied theme - Background: " + getBackground());
        }

        private void saveProduct(Product product) {
            JOptionPane optionPane = new JOptionPane(
                "Are you sure you want to " + (product == null ? "add" : "update") + " this product?",
                JOptionPane.QUESTION_MESSAGE,
                JOptionPane.YES_NO_OPTION
            );
            ThemeManager.applyThemeToOptionPane(optionPane);
            JDialog dialog = optionPane.createDialog(this, "Confirm " + (product == null ? "Add" : "Update"));
            dialog.setVisible(true);
            Object selectedValue = optionPane.getValue();
            if (selectedValue == null || !selectedValue.equals(JOptionPane.YES_OPTION)) {
                return;
            }

            try {
                String name = nameField.getText().trim();
                String category = categoryField.getText().trim();
                String stockText = stockField.getText().trim();
                String priceText = priceField.getText().trim();
                String description = descField.getText().trim();

                if (name.isEmpty() || category.isEmpty() || stockText.isEmpty() || priceText.isEmpty()) {
                    throw new IllegalArgumentException("All fields except description must be filled");
                }

                int stock = Integer.parseInt(stockText);
                double price = Double.parseDouble(priceText);

                if (stock < 0) {
                    throw new IllegalArgumentException("Stock cannot be negative");
                }
                if (price < 0) {
                    throw new IllegalArgumentException("Price cannot be negative");
                }

                Product editedProduct = new Product(
                    product != null ? product.getId() : 0,
                    name,
                    category,
                    stock,
                    price,
                    description
                );

                boolean success;
                AuditLogDAO auditLogDAO = new AuditLogDAO(conn);
                if (product == null) {
                    success = productDAO.addProduct(editedProduct);
                    if (success) {
                        auditLogDAO.logAction(currentUser.getId(), "Product Added", 
                            String.format("Product: %s, Category: %s", name, category));
                        System.out.println("[DEBUG] ProductView: Logged 'Product Added' action for product: " + name);
                    }
                } else {
                    success = productDAO.updateProduct(editedProduct);
                    if (success) {
                        auditLogDAO.logAction(currentUser.getId(), "Product Edited", 
                            String.format("Product ID: %d, Name: %s", editedProduct.getId(), name));
                        System.out.println("[DEBUG] ProductView: Logged 'Product Edited' action for product: " + name);
                    }
                }

                if (success) {
                    refreshTable();
                    dispose();
                } else {
                    throw new SQLException("Failed to save product");
                }
            } catch (NumberFormatException ex) {
                ErrorHandler.handleError(this, "Invalid number format for stock or price", ex);
            } catch (IllegalArgumentException ex) {
                ErrorHandler.handleError(this, ex.getMessage(), ex);
            } catch (SQLException ex) {
                System.err.println("[ERROR] ProductDialog: Database error while saving product - " + ex.getMessage());
                ErrorHandler.handleError(this, "Database error while saving product", ex);
            }
        }

        @Override
        public void onThemeChanged(ThemeManager.ThemeMode newTheme) {
            System.out.println("[DEBUG] ProductDialog: Theme changed to " + newTheme);
            SwingUtilities.updateComponentTreeUI(this);
            applyThemeToComponents();
            repaint();
            revalidate();
            System.out.println("[DEBUG] ProductDialog updated - Background: " + getBackground());
        }
    }

    private void showProductDialog(Product product) {
        ProductDialog dialog = new ProductDialog(product);
        dialog.setVisible(true);
    }

    private void editSelectedProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow >= 0) {
            int productId = (int) tableModel.getValueAt(selectedRow, 6);
            try {
                Product product = productDAO.getProduct(productId);
                if (product != null) {
                    showProductDialog(product);
                }
            } catch (SQLException e) {
                System.err.println("[ERROR] ProductView: Error loading product - " + e.getMessage());
                ErrorHandler.handleError(this, "Error loading product", e); 
            }
        } else {
            ErrorHandler.handleError(this, "Please select a product first", null);
        }
    }

    private void deleteSelectedProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow >= 0) {
            int productId = (int) tableModel.getValueAt(selectedRow, 6);
            String productName = (String) tableModel.getValueAt(selectedRow, 1);

            JOptionPane optionPane = new JOptionPane(
                "Are you sure you want to delete this product?",
                JOptionPane.QUESTION_MESSAGE,
                JOptionPane.YES_NO_OPTION
            );
            ThemeManager.applyThemeToOptionPane(optionPane);
            JDialog dialog = optionPane.createDialog(this, "Confirm Delete");
            dialog.setVisible(true);
            Object selectedValue = optionPane.getValue();
            if (selectedValue == null || !selectedValue.equals(JOptionPane.YES_OPTION)) {
                return;
            }

            try {
                // Log the "Product Deleted" action
                AuditLogDAO auditLogDAO = new AuditLogDAO(conn);
                auditLogDAO.logAction(currentUser.getId(), "Product Deleted", 
                    String.format("Product ID: %d, Name: %s", productId, productName));
                System.out.println("[DEBUG] ProductView: Logged 'Product Deleted' action for product: " + productName);

                if (productDAO.deleteProduct(productId)) {
                    refreshTable();
                } else {
                    ErrorHandler.handleError(this, "Failed to delete product", null);
                }
            } catch (SQLException e) {
                System.err.println("[ERROR] ProductView: Error deleting product - " + e.getMessage());
                ErrorHandler.handleError(this, "Error deleting product", e);
            }
        } else {
            ErrorHandler.handleError(this, "Please select a product first", null);
        }
    }

    private void updateButtonStates() {
        addButton.setEnabled(currentUser.getRole().equals("admin") || hasPermission("add"));
        editButton.setEnabled(currentUser.getRole().equals("admin") || hasPermission("edit"));
        deleteButton.setEnabled(currentUser.getRole().equals("admin") || hasPermission("delete"));
        lowStockButton.setEnabled(currentUser.getRole().equals("admin") || hasPermission("lowStock"));
    }

    private boolean hasPermission(String permission) {
        String perms = currentUser.getPermissions();
        if (perms == null || perms.isEmpty()) return false;
        String[] permArray = perms.split(",");
        for (String perm : permArray) {
            String[] keyValue = perm.split(":");
            if (keyValue.length == 2 && keyValue[0].trim().equals(permission) && keyValue[1].trim().equals("1")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onThemeChanged(ThemeManager.ThemeMode newTheme) {
        System.out.println("[DEBUG] ProductView: Theme changed to " + newTheme);
        SwingUtilities.updateComponentTreeUI(this);
        applyThemeToTable();
        applyThemeToComponents();
        repaint();
        revalidate();
        System.out.println("[DEBUG] ProductView background after update: " + getBackground());
    }

    private void applyThemeToComponents() {
        ThemeManager.applyThemeToComponent(this);
        ThemeManager.applyThemeToComponent(searchField);
        for (Component comp : getComponents()) {
            if (comp instanceof JPanel) {
                JPanel panel = (JPanel) comp;
                panel.setBackground(ThemeManager.getCurrentTheme() == ThemeManager.ThemeMode.LIGHT ?
                    ThemeManager.LIGHT_COLORS.get("Panel.background") : ThemeManager.DARK_COLORS.get("Panel.background"));
                for (Component subComp : panel.getComponents()) {
                    ThemeManager.applyThemeToComponent(subComp);
                }
            }
        }
        System.out.println("[DEBUG] ProductView applied theme - Background: " + getBackground());
    }
}