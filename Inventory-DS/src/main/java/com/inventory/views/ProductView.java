package main.java.com.inventory.views;

import main.java.com.inventory.dao.ProductDAO;
import main.java.com.inventory.models.Product;
import main.java.com.inventory.models.User;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductView extends JPanel {
    private JTable productTable;
    private DefaultTableModel tableModel;
    private User currentUser;
    private ProductDAO productDAO;
    private JTextField searchField;
    private JButton addButton, editButton, deleteButton, refreshButton, lowStockButton;    
    private static final int LOW_STOCK_THRESHOLD = 20;

    public ProductView(User user, Connection conn) {
        this.currentUser = user;
        this.productDAO = new ProductDAO(conn);
        setLayout(new BorderLayout());
        setupKeyBindings();
        initializeUI();
        refreshTable();
    }

    private void initializeUI() {
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        searchPanel.add(new JLabel("Search:"));
        searchField = new JTextField(20);
        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                searchProducts();
            }
        });
        searchPanel.add(searchField);
        add(searchPanel, BorderLayout.NORTH);

        // Table setup
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

        addButton.addActionListener(e -> showProductDialog(null));
        editButton.addActionListener(e -> editSelectedProduct());
        deleteButton.addActionListener(e -> deleteSelectedProduct());
        refreshButton.addActionListener(e -> refreshTable());
        lowStockButton.addActionListener(e -> checkLowStock());

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(lowStockButton);

        refreshButton.setToolTipText("View all products");
        searchField.setToolTipText("Searches ID first, then name/description");
        lowStockButton.setToolTipText("Check products with low stock (Ctrl+L)");

        if (!currentUser.getRole().equals("admin")) {
            editButton.setEnabled(false);
            deleteButton.setEnabled(false);
            lowStockButton.setEnabled(false);
        }

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void setupKeyBindings() {
        InputMap inputMap = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getActionMap();

        // Ctrl+S: Focus search field
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK), "search");
        actionMap.put("search", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchField.requestFocusInWindow();
            }
        });

        // Ctrl+A: Add product
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK), "add");
        actionMap.put("add", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentUser.getRole().equals("admin")) {
                    showProductDialog(null);
                }
            }
        });

        // Ctrl+E: Edit product
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK), "edit");
        actionMap.put("edit", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentUser.getRole().equals("admin")) {
                    editSelectedProduct();
                }
            }
        });

        // Ctrl+D: Delete product
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK), "delete");
        actionMap.put("delete", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentUser.getRole().equals("admin")) {
                    deleteSelectedProduct();
                }
            }
        });

        // Ctrl+R: Refresh table
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK), "refresh");
        actionMap.put("refresh", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshTable();
            }
        });

        // Ctrl+L: Check low stock
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK), "lowStock");
        actionMap.put("lowStock", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentUser.getRole().equals("admin")) {
                    checkLowStock();
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
                    product.getId() // Store actual ID in hidden column
                });
            } checkLowStockOnRefresh();
        } catch (SQLException e) {
            System.err.println("Error in refreshTable: " + e.getMessage());
            showError("Error loading products: " + e.getMessage());
        }
    }

        private void checkLowStockOnRefresh() {
        try {
            List<Product> lowStockProducts = productDAO.getLowStockProducts(LOW_STOCK_THRESHOLD);
            if (!lowStockProducts.isEmpty()) {
                StringBuilder message = new StringBuilder("Low stock alert:\n");
                for (Product product : lowStockProducts) {
                    message.append(String.format("ID: %d, Name: %s, Stock: %d\n",
                        product.getId(), product.getName(), product.getStock()));
                }
                JOptionPane.showMessageDialog(this, message.toString(),
                    "Low Stock Alert", JOptionPane.WARNING_MESSAGE);
            }
        } catch (SQLException e) {
            showError("Error checking low stock: " + e.getMessage());
        }
    }

    private void checkLowStock() {
        try {
            List<Product> lowStockProducts = productDAO.getLowStockProducts(LOW_STOCK_THRESHOLD);
            if (lowStockProducts.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No products with low stock.",
                    "Low Stock Check", JOptionPane.INFORMATION_MESSAGE);
            } else {
                StringBuilder message = new StringBuilder("Low stock products:\n");
                for (Product product : lowStockProducts) {
                    message.append(String.format("ID: %d, Name: %s, Stock: %d\n",
                        product.getId(), product.getName(), product.getStock()));
                }
                JOptionPane.showMessageDialog(this, message.toString(),
                    "Low Stock Products", JOptionPane.WARNING_MESSAGE);
            }
        } catch (SQLException e) {
            showError("Error checking low stock: " + e.getMessage());
        }
    }

    private void searchProducts() {
        String query = searchField.getText().trim();
        try {
            tableModel.setRowCount(0);
            List<Product> products = new ArrayList<>();
            if (query.isEmpty()) {
                products = productDAO.getAllProducts();
            } else {
                products = productDAO.searchProducts(query);
            }
            for (Product product : products) {
                tableModel.addRow(new Object[]{
                    product.getDisplayId(),
                    product.getName(),
                    product.getCategoryName(),
                    product.getStock(),
                    String.format("₱%.2f", product.getPrice()),
                    product.getDescription(),
                    product.getId() // Store actual ID in hidden column
                });
            }
        } catch (SQLException e) {
            showError("Error searching products: " + e.getMessage());
        }
    }

    private void showProductDialog(Product product) {
        JDialog dialog = new JDialog();
        dialog.setTitle(product == null ? "Add Product" : "Edit Product");
        dialog.setLayout(new GridLayout(6, 2, 10, 10));

        JTextField nameField = new JTextField(product != null ? product.getName() : "");
        JTextField categoryField = new JTextField(product != null ? product.getCategoryName() : "");
        JTextField stockField = new JTextField(product != null ? String.valueOf(product.getStock()) : "");
        JTextField priceField = new JTextField(product != null ? String.valueOf(product.getPrice()) : "");
        JTextField descField = new JTextField(product != null ? product.getDescription() : "");

        dialog.add(new JLabel("Name:"));
        dialog.add(nameField);
        dialog.add(new JLabel("Category:"));
        dialog.add(categoryField);
        dialog.add(new JLabel("Stock:"));
        dialog.add(stockField);
        dialog.add(new JLabel("Price:"));
        dialog.add(priceField);
        dialog.add(new JLabel("Description:"));
        dialog.add(descField);

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to " + (product == null ? "add" : "update") + " this product?",
                "Confirm " + (product == null ? "Add" : "Update"),
                JOptionPane.YES_NO_OPTION
            );
    
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
            try {
                Product editedProduct = new Product(
                    product != null ? product.getId() : 0,
                    nameField.getText(),
                    categoryField.getText(),
                    Integer.parseInt(stockField.getText()),
                    Double.parseDouble(priceField.getText()),
                    descField.getText()
                );

                boolean success;
                if (product == null) {
                    success = productDAO.addProduct(editedProduct);
                } else {
                    success = productDAO.updateProduct(editedProduct);
                }

                if (success) {
                    refreshTable();
                    dialog.dispose();
                } else {
                    showError("Failed to save product");
                }
            } catch (NumberFormatException ex) {
                showError("Invalid number format");
            } catch (SQLException ex) {
                showError("Database error: " + ex.getMessage());
            } 
        });

        dialog.add(saveButton);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void editSelectedProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow >= 0) {
            int productId = (int) tableModel.getValueAt(selectedRow, 6); // Use hidden DB_ID column
            try {
                Product product = productDAO.getProduct(productId);
                if (product != null) {
                    showProductDialog(product);
                }
            } catch (SQLException e) {
                showError("Error loading product: " + e.getMessage());
            }
        } else {
            showError("Please select a product first");
        }
    }

    private void deleteSelectedProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow >= 0) {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this product?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                int productId = (int) tableModel.getValueAt(selectedRow, 6); // Use hidden DB_ID column
                try {
                    if (productDAO.deleteProduct(productId)) {
                        refreshTable();
                    } else {
                        showError("Failed to delete product");
                    }
                } catch (SQLException e) {
                    showError("Error deleting product: " + e.getMessage());
                }
            }
        } else {
            showError("Please select a product first");
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}