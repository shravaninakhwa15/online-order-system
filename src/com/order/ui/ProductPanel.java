package com.order.ui;

import com.order.dao.CategoryDAO;
import com.order.dao.ProductDAO;
import com.order.model.Category;
import com.order.model.Product;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class ProductPanel extends JPanel {

    private final ProductDAO productDAO = new ProductDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    private JTable table;
    private DefaultTableModel tableModel;

    private JTextField idField;
    private JTextField nameField;
    private JTextField priceField;
    private JTextField stockField;
    private JComboBox<CategoryItem> categoryCombo;
    private JTextField searchField;

    private JButton addBtn;
    private JButton updateBtn;
    private JButton deleteBtn;
    private JButton clearBtn;

    public ProductPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(UIStyle.COLOR_BG);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        initUI();
        loadCategories();
        loadProducts();
    }

    private void initUI() {
        // Toolbar
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setOpaque(false);

        JLabel titleLbl = new JLabel("Product Management");
        titleLbl.setFont(UIStyle.FONT_TITLE);
        titleLbl.setForeground(UIStyle.COLOR_TEXT_MAIN);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchPanel.setOpaque(false);
        searchField = UIStyle.createStyledTextField(18);
        JButton searchBtn = new JButton("Search");
        UIStyle.styleButton(searchBtn, UIStyle.COLOR_PRIMARY);
        JButton resetBtn = new JButton("Reset");
        UIStyle.styleButton(resetBtn, UIStyle.COLOR_SECONDARY);

        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);
        searchPanel.add(resetBtn);

        topPanel.add(titleLbl, BorderLayout.WEST);
        topPanel.add(searchPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Center SplitPane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.68);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);

        // Table Card
        JPanel tableCard = UIStyle.createCardPanel();
        tableCard.setLayout(new BorderLayout(10, 10));

        String[] cols = {"Product ID", "Product Name", "Category", "Price (₹)", "Stock Qty"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        UIStyle.styleTable(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(180);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(80);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                populateForm();
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        tableCard.add(scrollPane, BorderLayout.CENTER);

        // Form Card
        JPanel formCard = UIStyle.createCardPanel();
        formCard.setLayout(new BorderLayout(10, 10));
        formCard.setPreferredSize(new Dimension(320, 0));

        JLabel formTitle = UIStyle.createHeaderLabel("Product Details");
        formCard.add(formTitle, BorderLayout.NORTH);

        JPanel fieldsPanel = new JPanel(new GridLayout(10, 1, 5, 5));
        fieldsPanel.setOpaque(false);

        idField = UIStyle.createStyledTextField(15);
        idField.setEditable(false);
        idField.setBackground(new Color(241, 245, 249));

        nameField = UIStyle.createStyledTextField(15);
        priceField = UIStyle.createStyledTextField(15);
        stockField = UIStyle.createStyledTextField(15);

        categoryCombo = new JComboBox<>();
        categoryCombo.setFont(UIStyle.FONT_INPUT);
        categoryCombo.setBackground(Color.WHITE);

        fieldsPanel.add(new JLabel("Product ID (Auto):"));
        fieldsPanel.add(idField);
        fieldsPanel.add(new JLabel("Product Name *:"));
        fieldsPanel.add(nameField);
        fieldsPanel.add(new JLabel("Category *:"));
        fieldsPanel.add(categoryCombo);
        fieldsPanel.add(new JLabel("Price (₹ > 0) *:"));
        fieldsPanel.add(priceField);
        fieldsPanel.add(new JLabel("Stock Quantity (>= 0) *:"));
        fieldsPanel.add(stockField);

        formCard.add(fieldsPanel, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        btnPanel.setOpaque(false);

        addBtn = new JButton("Add");
        UIStyle.styleButton(addBtn, UIStyle.COLOR_PRIMARY);

        updateBtn = new JButton("Update");
        UIStyle.styleButton(updateBtn, UIStyle.COLOR_SUCCESS);

        deleteBtn = new JButton("Delete");
        UIStyle.styleButton(deleteBtn, UIStyle.COLOR_DANGER);

        clearBtn = new JButton("Clear Form");
        UIStyle.styleButton(clearBtn, UIStyle.COLOR_SECONDARY);

        btnPanel.add(addBtn);
        btnPanel.add(updateBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(clearBtn);

        formCard.add(btnPanel, BorderLayout.SOUTH);

        splitPane.setLeftComponent(tableCard);
        splitPane.setRightComponent(formCard);
        add(splitPane, BorderLayout.CENTER);

        // Listeners
        addBtn.addActionListener(e -> onAdd());
        updateBtn.addActionListener(e -> onUpdate());
        deleteBtn.addActionListener(e -> onDelete());
        clearBtn.addActionListener(e -> clearForm());

        searchBtn.addActionListener(e -> onSearch());
        resetBtn.addActionListener(e -> {
            searchField.setText("");
            loadProducts();
        });
        searchField.addActionListener(e -> onSearch());
    }

    public void loadCategories() {
        categoryCombo.removeAllItems();
        try {
            List<Category> list = categoryDAO.getAllCategories();
            for (Category c : list) {
                categoryCombo.addItem(new CategoryItem(c.getCategoryId(), c.getCategoryName()));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Could not load categories: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void loadProducts() {
        tableModel.setRowCount(0);
        try {
            List<Product> list = productDAO.getAllProducts();
            for (Product p : list) {
                tableModel.addRow(new Object[]{
                        p.getProductId(),
                        p.getName(),
                        p.getCategoryName(),
                        p.getPrice().toPlainString(),
                        p.getStockQty()
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load products: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onSearch() {
        String kw = searchField.getText().trim();
        if (kw.isEmpty()) {
            loadProducts();
            return;
        }
        tableModel.setRowCount(0);
        try {
            List<Product> list = productDAO.searchProducts(kw);
            for (Product p : list) {
                tableModel.addRow(new Object[]{
                        p.getProductId(),
                        p.getName(),
                        p.getCategoryName(),
                        p.getPrice().toPlainString(),
                        p.getStockQty()
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Search error: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void populateForm() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            idField.setText(String.valueOf(tableModel.getValueAt(row, 0)));
            nameField.setText(String.valueOf(tableModel.getValueAt(row, 1)));
            String categoryName = String.valueOf(tableModel.getValueAt(row, 2));
            priceField.setText(String.valueOf(tableModel.getValueAt(row, 3)));
            stockField.setText(String.valueOf(tableModel.getValueAt(row, 4)));

            for (int i = 0; i < categoryCombo.getItemCount(); i++) {
                if (categoryCombo.getItemAt(i).name.equalsIgnoreCase(categoryName)) {
                    categoryCombo.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private void clearForm() {
        table.clearSelection();
        idField.setText("");
        nameField.setText("");
        priceField.setText("");
        stockField.setText("");
        if (categoryCombo.getItemCount() > 0) {
            categoryCombo.setSelectedIndex(0);
        }
    }

    private boolean validateInputs() {
        String name = nameField.getText().trim();
        String priceText = priceField.getText().trim();
        String stockText = stockField.getText().trim();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Product name cannot be empty.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            nameField.requestFocus();
            return false;
        }

        if (categoryCombo.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please select a category.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        try {
            BigDecimal price = new BigDecimal(priceText);
            if (price.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(this, "Price must be greater than 0 (CHECK constraint price > 0).",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                priceField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Price must be a valid decimal number.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            priceField.requestFocus();
            return false;
        }

        try {
            int stock = Integer.parseInt(stockText);
            if (stock < 0) {
                JOptionPane.showMessageDialog(this, "Stock quantity cannot be negative (CHECK constraint stock_qty >= 0).",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                stockField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Stock quantity must be a valid whole number.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            stockField.requestFocus();
            return false;
        }

        return true;
    }

    private void onAdd() {
        if (!validateInputs()) return;

        CategoryItem cat = (CategoryItem) categoryCombo.getSelectedItem();
        Product p = new Product(
                nameField.getText().trim(),
                new BigDecimal(priceField.getText().trim()),
                Integer.parseInt(stockField.getText().trim()),
                cat.id
        );

        try {
            int newId = productDAO.addProduct(p);
            JOptionPane.showMessageDialog(this, "Product added successfully with ID #" + newId + "!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadProducts();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Could not add product: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onUpdate() {
        String idText = idField.getText().trim();
        if (idText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a product to update.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!validateInputs()) return;

        CategoryItem cat = (CategoryItem) categoryCombo.getSelectedItem();
        Product p = new Product(
                Integer.parseInt(idText),
                nameField.getText().trim(),
                new BigDecimal(priceField.getText().trim()),
                Integer.parseInt(stockField.getText().trim()),
                cat.id
        );

        try {
            boolean ok = productDAO.updateProduct(p);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Product #" + p.getProductId() + " updated successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                loadProducts();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Update failed: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onDelete() {
        String idText = idField.getText().trim();
        if (idText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a product to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = Integer.parseInt(idText);
        String name = nameField.getText().trim();

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete product '" + name + "' (ID: " + id + ")?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean ok = productDAO.deleteProduct(id);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Product deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                    clearForm();
                    loadProducts();
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Constraint Violation", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    // Helper item for JComboBox
    public static class CategoryItem {
        public final int id;
        public final String name;

        public CategoryItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
