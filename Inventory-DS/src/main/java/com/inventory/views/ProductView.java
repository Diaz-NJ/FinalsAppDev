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
import java.io.IOException;
import com.opencsv.exceptions.CsvValidationException;

public class ProductView extends JPanel implements ThemeManager.ThemeChangeListener {
    private JTable productTable;
    private DefaultTableModel tableModel;
    private User currentUser;
    private ProductDAO productDAO;
    private Connection conn;
    private JTextField searchField;
    private JButton addButton, editButton, deleteButton, refreshButton, lowStockButton;
    private JButton importButton, exportButton;
    private JButton backupButton, restoreButton;
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
        importButton = new JButton("Import CSV");
        exportButton = new JButton("Export CSV");
        backupButton = new JButton("Backup");
        restoreButton = new JButton("Restore");

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
        importButton.addActionListener(_ -> {
            if (hasPermission("add")) importCSV();
            else ErrorHandler.handleError(this, "Permission denied: Import not allowed");
        });
        exportButton.addActionListener(_ -> {
            if (hasPermission("view")) exportCSV();
            else ErrorHandler.handleError(this, "Permission denied: Export not allowed");
        });
        backupButton.addActionListener(_ -> {
            if (hasPermission("view")) backupDatabase();
            else ErrorHandler.handleError(this, "Permission denied: Backup not allowed");
        });
        restoreButton.addActionListener(_ -> {
            if (hasPermission("view")) restoreDatabase();
            else ErrorHandler.handleError(this, "Permission denied: Restore not allowed");
        });

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(lowStockButton);
        buttonPanel.add(importButton);
        buttonPanel.add(exportButton);
        buttonPanel.add(backupButton);
        buttonPanel.add(restoreButton);

        refreshButton.setToolTipText("View all products (Ctrl+R)");
        searchField.setToolTipText("Search by Display ID, Name, Category, or Description (Ctrl+S)");
        addButton.setToolTipText("Add new product (Ctrl+A)");
        editButton.setToolTipText("Edit selected product (Ctrl+E)");
        deleteButton.setToolTipText("Delete selected product (Ctrl+D)");
        lowStockButton.setToolTipText("Check products with low stock (Ctrl+L)");
        importButton.setToolTipText("Import products from CSV file (Ctrl+I)");
        exportButton.setToolTipText("Export products to CSV file (Ctrl+E)");
        backupButton.setToolTipText("Backup database (Ctrl+B)");
        restoreButton.setToolTipText("Restore database (Ctrl+Shift+R)");

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
                if (hasPermission("add")) showProductDialog(null);
                else ErrorHandler.handleError(ProductView.this, "Permission denied: Add not allowed");
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK), "edit");
        actionMap.put("edit", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (hasPermission("edit")) editSelectedProduct();
                else ErrorHandler.handleError(ProductView.this, "Permission denied: Edit not allowed");
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK), "delete");
        actionMap.put("delete", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (hasPermission("delete")) deleteSelectedProduct();
                else ErrorHandler.handleError(ProductView.this, "Permission denied: Delete not allowed");
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
                if (hasPermission("lowStock")) checkLowStock();
                else ErrorHandler.handleError(ProductView.this, "Permission denied: Low Stock check not allowed");
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK), "import");
        actionMap.put("import", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (hasPermission("add")) importCSV();
                else ErrorHandler.handleError(ProductView.this, "Permission denied: Import not allowed");
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK), "export");
        actionMap.put("export", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (hasPermission("view")) exportCSV();
                else ErrorHandler.handleError(ProductView.this, "Permission denied: Export not allowed");
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_DOWN_MASK), "backup");
        actionMap.put("backup", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (hasPermission("view")) backupDatabase();
                else ErrorHandler.handleError(ProductView.this, "Permission denied: Backup not allowed");
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK), "restore");
        actionMap.put("restore", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (hasPermission("view")) restoreDatabase();
                else ErrorHandler.handleError(ProductView.this, "Permission denied: Restore not allowed");
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
            AuditLogDAO auditLogDAO = new AuditLogDAO(conn);
            auditLogDAO.logAction(currentUser.getId(), "Viewed all products", 
                "Viewed by " + currentUser.getUsername());

            applyThemeToTable();
            checkLowStockOnRefresh();
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
        try {
            tableModel.setRowCount(0);
            List<Product> products;

            if (query.matches("\\d+")) {
                try {
                    int displayId = Integer.parseInt(query);
                    products = productDAO.getAllProducts();
                    List<Product> filteredProducts = new ArrayList<>();
                    for (Product product : products) {
                        if (product.getDisplayId() == displayId) {
                            filteredProducts.add(product);
                            break;
                        }
                    }
                    products = filteredProducts;
                } catch (NumberFormatException e) {
                    products = productDAO.searchProducts(query);
                }
            } else {
                products = query.isEmpty() ? productDAO.getAllProducts() : productDAO.searchProducts(query);
                int sequenceNumber = 1;
                for (Product product : products) {
                    product.setDisplayId(sequenceNumber++);
                }
            }

            for (Product product : products) {
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
                if (product == null) {
                    success = productDAO.addProduct(editedProduct);
                } else {
                    success = productDAO.updateProduct(editedProduct);
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
            SwingUtilities.updateComponentTreeUI(this);
            applyThemeToComponents();
            repaint();
            revalidate();
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
                } else {
                    ErrorHandler.handleError(this, "Product not found", null);
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
        String role = currentUser.getRole() != null ? currentUser.getRole().toLowerCase() : "";
        boolean isOwner = role.equals("owner");
        boolean isManager = role.equals("manager");
        boolean isAdmin = role.equals("admin");
        boolean isStaff = role.equals("staff");

        // Owner has full access
        if (isOwner) {
            addButton.setEnabled(true);
            editButton.setEnabled(true);
            deleteButton.setEnabled(true);
            refreshButton.setEnabled(true);
            lowStockButton.setEnabled(true);
            importButton.setEnabled(true);
            exportButton.setEnabled(true);
            backupButton.setEnabled(true);
            restoreButton.setEnabled(true);
            return;
        }

        // Manager has access to all product features and audit log
        if (isManager) {
            addButton.setEnabled(hasPermission("add"));
            editButton.setEnabled(hasPermission("edit"));
            deleteButton.setEnabled(hasPermission("delete"));
            refreshButton.setEnabled(hasPermission("view"));
            lowStockButton.setEnabled(hasPermission("lowStock"));
            importButton.setEnabled(hasPermission("add"));
            exportButton.setEnabled(hasPermission("view"));
            backupButton.setEnabled(hasPermission("view"));
            restoreButton.setEnabled(hasPermission("view"));
            return;
        }

        // Admin can view all and check low stock
        if (isAdmin) {
            addButton.setEnabled(false);
            editButton.setEnabled(false);
            deleteButton.setEnabled(false);
            refreshButton.setEnabled(hasPermission("view"));
            lowStockButton.setEnabled(hasPermission("lowStock"));
            importButton.setEnabled(false);
            exportButton.setEnabled(false);
            backupButton.setEnabled(false);
            restoreButton.setEnabled(false);
            return;
        }

        // Staff has full product access
        if (isStaff) {
            addButton.setEnabled(hasPermission("add"));
            editButton.setEnabled(hasPermission("edit"));
            deleteButton.setEnabled(hasPermission("delete"));
            refreshButton.setEnabled(hasPermission("view"));
            lowStockButton.setEnabled(hasPermission("lowStock"));
            importButton.setEnabled(hasPermission("add"));
            exportButton.setEnabled(hasPermission("view"));
            backupButton.setEnabled(false);
            restoreButton.setEnabled(false);
            return;
        }

        // Default: Disable all buttons for other roles
        addButton.setEnabled(false);
        editButton.setEnabled(false);
        deleteButton.setEnabled(false);
        refreshButton.setEnabled(false);
        lowStockButton.setEnabled(false);
        importButton.setEnabled(false);
        exportButton.setEnabled(false);
        backupButton.setEnabled(false);
        restoreButton.setEnabled(false);
    }

    private boolean hasPermission(String permission) {
        if (currentUser.getRole() != null && (currentUser.getRole().toLowerCase().equals("owner") || currentUser.getRole().toLowerCase().equals("manager"))) return true;
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
        SwingUtilities.updateComponentTreeUI(this);
        applyThemeToTable();
        applyThemeToComponents();
        repaint();
        revalidate();
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
    }

    private void applyThemeToFileChooser(JFileChooser fileChooser) {
        if (ThemeManager.getCurrentTheme() == ThemeManager.ThemeMode.DARK) {
            Map<String, Color> darkColors = ThemeManager.DARK_COLORS;
            fileChooser.setBackground(darkColors.get("Panel.background"));
            fileChooser.setForeground(darkColors.get("Panel.foreground"));
            
            applyThemeToComponentsRecursive(fileChooser, darkColors);
        }
    }

    private void applyThemeToComponentsRecursive(Component component, Map<String, Color> darkColors) {
        if (component == null) return;

        component.setBackground(darkColors.get("Panel.background"));
        component.setForeground(darkColors.get("Panel.foreground"));

        if (component instanceof JLabel) {
            ((JLabel) component).setForeground(darkColors.get("Panel.foreground"));
        } else if (component instanceof JTextField) {
            ((JTextField) component).setBackground(darkColors.get("Panel.background"));
            ((JTextField) component).setForeground(darkColors.get("Panel.foreground"));
            ((JTextField) component).setCaretColor(darkColors.get("Panel.foreground"));
        } else if (component instanceof JList) {
            ((JList<?>) component).setBackground(darkColors.get("Panel.background"));
            ((JList<?>) component).setForeground(darkColors.get("Panel.foreground"));
            ((JList<?>) component).setSelectionBackground(darkColors.get("Table.selectionBackground"));
            ((JList<?>) component).setSelectionForeground(darkColors.get("Table.selectionForeground"));
        } else if (component instanceof JButton) {
            ((JButton) component).setBackground(darkColors.get("Button.background"));
            ((JButton) component).setForeground(darkColors.get("Button.foreground"));
        } else if (component instanceof JComboBox) {
            ((JComboBox<?>) component).setBackground(darkColors.get("Panel.background"));
            ((JComboBox<?>) component).setForeground(darkColors.get("Panel.foreground"));
        }

        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                applyThemeToComponentsRecursive(child, darkColors);
            }
        }
    }

    private void importCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select CSV File to Import");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(java.io.File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".csv");
            }

            @Override
            public String getDescription() {
                return "CSV Files (*.csv)";
            }
        });

        applyThemeToFileChooser(fileChooser);

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                String filePath = fileChooser.getSelectedFile().getAbsolutePath();
                productDAO.importProductsFromCSV(filePath);
                refreshTable();
                JOptionPane optionPane = new JOptionPane("Products imported successfully!", JOptionPane.INFORMATION_MESSAGE);
                ThemeManager.applyThemeToOptionPane(optionPane);
                JDialog dialog = optionPane.createDialog(this, "Import Success");
                dialog.setVisible(true);
            } catch (SQLException e) {
                System.err.println("[ERROR] ProductView: Error importing CSV - " + e.getMessage());
                ErrorHandler.handleError(this, "Error importing CSV: " + e.getMessage(), e);
            } catch (IOException e) {
                System.err.println("[ERROR] ProductView: Error reading CSV file - " + e.getMessage());
                ErrorHandler.handleError(this, "Error reading CSV file: " + e.getMessage(), e);
            } catch (CsvValidationException e) {
                System.err.println("[ERROR] ProductView: Error validating CSV file - " + e.getMessage());
                ErrorHandler.handleError(this, "Error validating CSV file: " + e.getMessage(), e);
            }
        }
    }

    private void exportCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save CSV File");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(java.io.File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".csv");
            }

            @Override
            public String getDescription() {
                return "CSV Files (*.csv)";
            }
        });

        applyThemeToFileChooser(fileChooser);

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                String filePath = fileChooser.getSelectedFile().getAbsolutePath();
                if (!filePath.toLowerCase().endsWith(".csv")) {
                    filePath += ".csv";
                }
                productDAO.exportProductsToCSV(filePath);
                JOptionPane optionPane = new JOptionPane("Products exported successfully to " + filePath, JOptionPane.INFORMATION_MESSAGE);
                ThemeManager.applyThemeToOptionPane(optionPane);
                JDialog dialog = optionPane.createDialog(this, "Export Success");
                dialog.setVisible(true);
            } catch (SQLException e) {
                System.err.println("[ERROR] ProductView: Error exporting CSV - " + e.getMessage());
                ErrorHandler.handleError(this, "Error exporting CSV: " + e.getMessage(), e);
            } catch (IOException e) {
                System.err.println("[ERROR] ProductView: Error writing CSV file - " + e.getMessage());
                ErrorHandler.handleError(this, "Error writing CSV file: " + e.getMessage(), e);
            }
        }
    }

    private void backupDatabase() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Backup File");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(java.io.File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".sql");
            }

            @Override
            public String getDescription() {
                return "SQL Files (*.sql)";
            }
        });

        applyThemeToFileChooser(fileChooser);

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                String filePath = fileChooser.getSelectedFile().getAbsolutePath();
                if (!filePath.toLowerCase().endsWith(".sql")) {
                    filePath += ".sql";
                }

                String dbUsername = "root";
                String dbPassword = "";
                String dbName = "inventory_ds";
                String host = "localhost";

                ProcessBuilder pb = new ProcessBuilder(
                    "mysqldump", "-u" + dbUsername, "-p" + dbPassword, "-h" + host, dbName, "-r", filePath
                );
                Process process = pb.start();
                int exitCode = process.waitFor();

                if (exitCode == 0) {
                    JOptionPane optionPane = new JOptionPane("Database backed up successfully to " + filePath, JOptionPane.INFORMATION_MESSAGE);
                    ThemeManager.applyThemeToOptionPane(optionPane);
                    JDialog dialog = optionPane.createDialog(this, "Backup Success");
                    dialog.setVisible(true);
                    AuditLogDAO auditLogDAO = new AuditLogDAO(conn);
                    try {
                        auditLogDAO.logAction(currentUser.getId(), "Database Backup", "Backed up to " + filePath);
                    } catch (SQLException e) {
                        System.err.println("[ERROR] ProductView: Error logging backup action - " + e.getMessage());
                        ErrorHandler.handleError(this, "Error logging backup action: " + e.getMessage(), e);
                    }
                } else {
                    throw new IOException("Backup failed with exit code: " + exitCode);
                }
            } catch (IOException | InterruptedException e) {
                System.err.println("[ERROR] ProductView: Error during backup - " + e.getMessage());
                ErrorHandler.handleError(this, "Error creating backup: " + e.getMessage(), e);
            }
        }
    }

    private void restoreDatabase() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Backup File to Restore");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(java.io.File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".sql");
            }

            @Override
            public String getDescription() {
                return "SQL Files (*.sql)";
            }
        });

        applyThemeToFileChooser(fileChooser);

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                String filePath = fileChooser.getSelectedFile().getAbsolutePath();

                JOptionPane optionPane = new JOptionPane(
                    "Are you sure you want to restore the database? This will overwrite existing data!",
                    JOptionPane.WARNING_MESSAGE,
                    JOptionPane.YES_NO_OPTION
                );
                ThemeManager.applyThemeToOptionPane(optionPane);
                JDialog dialog = optionPane.createDialog(this, "Confirm Restore");
                dialog.setVisible(true);
                Object selectedValue = optionPane.getValue();
                if (selectedValue == null || !selectedValue.equals(JOptionPane.YES_OPTION)) {
                    return;
                }

                String dbUsername = "root";
                String dbPassword = "";
                String dbName = "inventory_ds";
                String host = "localhost";

                ProcessBuilder pb = new ProcessBuilder(
                    "mysql", "-u" + dbUsername, "-p" + dbPassword, "-h" + host, dbName, "<", filePath
                );
                pb.redirectErrorStream(true);
                Process process = pb.start();
                int exitCode = process.waitFor();

                if (exitCode == 0) {
                    JOptionPane optionPaneSuccess = new JOptionPane("Database restored successfully from " + filePath, JOptionPane.INFORMATION_MESSAGE);
                    ThemeManager.applyThemeToOptionPane(optionPaneSuccess);
                    JDialog dialogSuccess = optionPaneSuccess.createDialog(this, "Restore Success");
                    dialogSuccess.setVisible(true);
                    refreshTable();
                    AuditLogDAO auditLogDAO = new AuditLogDAO(conn);
                    try {
                        auditLogDAO.logAction(currentUser.getId(), "Database Restore", "Restored from " + filePath);
                    } catch (SQLException e) {
                        System.err.println("[ERROR] ProductView: Error logging restore action - " + e.getMessage());
                        ErrorHandler.handleError(this, "Error logging restore action: " + e.getMessage(), e);
                    }
                } else {
                    throw new IOException("Restore failed with exit code: " + exitCode);
                }
            } catch (IOException | InterruptedException e) {
                System.err.println("[ERROR] ProductView: Error during restore - " + e.getMessage());
                ErrorHandler.handleError(this, "Error restoring database: " + e.getMessage(), e);
            }
        }
    }
}