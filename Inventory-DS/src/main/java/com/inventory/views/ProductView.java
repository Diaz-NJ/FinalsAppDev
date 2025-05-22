package main.java.com.inventory.views;

import main.java.com.inventory.dao.ProductDAO;
import main.java.com.inventory.models.Product;
import main.java.com.inventory.models.User;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.util.List;

public class ProductView extends JPanel {
    private JTable productTable;
    private DefaultTableModel tableModel;
    @SuppressWarnings("unused")
    private User currentUser;

    public ProductView(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout());

        // Table setup
        String[] columns = {"ID", "Name", "Category", "Stock", "Price", "Description"};
        tableModel = new DefaultTableModel(columns, 0);
        productTable = new JTable(tableModel);
        add(new JScrollPane(productTable), BorderLayout.CENTER);

        // Buttons panel
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Add Product");
        JButton refreshButton = new JButton("Refresh");
        buttonPanel.add(addButton);
        buttonPanel.add(refreshButton);

        // Admin-only buttons
        if (user.getRole().equals("admin")) {
            JButton deleteButton = new JButton("Delete");
            buttonPanel.add(deleteButton);
            deleteButton.addActionListener(this::handleDelete);
        }

        add(buttonPanel, BorderLayout.SOUTH);

        // Button actions
        addButton.addActionListener(this::showAddProductDialog);
        refreshButton.addActionListener(e -> refreshTable());

        // Load data initially
        refreshTable();
    }

    private void refreshTable() {
        try {
            tableModel.setRowCount(0); // Clear table
            ProductDAO productDAO = new ProductDAO();
            List<Product> products = productDAO.getAllProducts();

            for (Product product : products) {
                Object[] row = {
                    product.getId(),
                    product.getName(),
                    product.getCategoryName(),
                    product.getStock(),
                    product.getPrice(),
                    product.getDescription()
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading products: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAddProductDialog(ActionEvent e) {
        JDialog dialog = new JDialog();
        dialog.setTitle("Add New Product");
        dialog.setLayout(new GridLayout(6, 2, 10, 10));

        JTextField nameField = new JTextField();
        JTextField categoryField = new JTextField();
        JTextField stockField = new JTextField();
        JTextField priceField = new JTextField();
        JTextField descField = new JTextField();

        dialog.add(new JLabel("Name:"));
        dialog.add(nameField);
        dialog.add(new JLabel("Category ID:"));
        dialog.add(categoryField);
        dialog.add(new JLabel("Stock:"));
        dialog.add(stockField);
        dialog.add(new JLabel("Price:"));
        dialog.add(priceField);
        dialog.add(new JLabel("Description:"));
        dialog.add(descField);

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(event -> {
            try {
                Product product = new Product(
                    0, // ID is auto-incremented
                    nameField.getText(),
                    categoryField.getText(),
                    Integer.parseInt(stockField.getText()),
                    Double.parseDouble(priceField.getText()),
                    descField.getText()
                );
                ProductDAO productDAO = new ProductDAO();
                productDAO.addProduct(product);
                refreshTable();
                dialog.dispose();
            } catch (NumberFormatException | SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid input: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(saveButton);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void handleDelete(ActionEvent e) {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow >= 0) {
            int productId = (int) tableModel.getValueAt(selectedRow, 0);
            try {
                ProductDAO productDAO = new ProductDAO();
                productDAO.deleteProduct(productId);
                refreshTable();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Delete failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Select a product first!", "Error", JOptionPane.WARNING_MESSAGE);
        }
    }
}