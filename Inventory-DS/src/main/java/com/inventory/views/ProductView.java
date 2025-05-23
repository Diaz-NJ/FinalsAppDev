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
    private JButton addButton, editButton, deleteButton, refreshButton;

    public ProductView(User user, Connection conn) {
        this.currentUser = user;
        this.productDAO = new ProductDAO(conn);
        setLayout(new BorderLayout());
        initializeUI();
        refreshTable();
    }

    private void initializeUI() {
        // Search Panel
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
        String[] columns = {"ID", "Name", "Category", "Stock", "Price", "Description"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        productTable = new JTable(tableModel);
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(productTable), BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel();
        addButton = new JButton("Add");
        editButton = new JButton("Edit");
        deleteButton = new JButton("Delete");
        refreshButton = new JButton("View All");

        addButton.addActionListener(e -> showProductDialog(null));
        editButton.addActionListener(e -> editSelectedProduct());
        deleteButton.addActionListener(e -> deleteSelectedProduct());
        refreshButton.addActionListener(e -> refreshTable());

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);

        refreshButton.setToolTipText("View all products");
        searchField.setToolTipText("Searches ID first, then name/description");

        // Disable edit/delete for non-admin users
        if (!currentUser.getRole().equals("admin")) {
            editButton.setEnabled(false);
            deleteButton.setEnabled(false);
        }

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void refreshTable() {
        try {
            tableModel.setRowCount(0);
            System.out.println("[DEBUG] Fetching products...");
            List<Product> products = productDAO.getAllProducts();
            System.out.println("[DEBUG] Found " + products.size() + " products");
            for (Product product : products) {
                tableModel.addRow(new Object[]{
                    product.getId(),
                    product.getName(),
                    product.getCategoryName(),
                    product.getStock(),
                    String.format("₱%.2f", product.getPrice()),
                    product.getDescription()
                });
            }
        } catch (SQLException e) {
            showError("Error loading products: " + e.getMessage());
        }
    }

    private void searchProducts() {
        String query = searchField.getText().trim();
        System.out.println("[DEBUG] Searching for: '" + query + "'");
        try {
            tableModel.setRowCount(0);
            List<Product> products = new ArrayList<>();
            System.out.println("[DEBUG] Search results: " + products.size() + " items");
            if (query.isEmpty()) {
                products = productDAO.getAllProducts();
            } else {
                products = productDAO.searchProducts(query);
            }
            for (Product product : products) {
                tableModel.addRow(new Object[]{
                    product.getId(),
                    product.getName(),
                    product.getCategoryName(),
                    product.getStock(),
                    String.format("₱%.2f", product.getPrice()),
                    product.getDescription()
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
            int productId = (int) tableModel.getValueAt(selectedRow, 0);
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
                int productId = (int) tableModel.getValueAt(selectedRow, 0);
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