package com.order.ui;

import com.order.dao.CategoryDAO;
import com.order.model.Category;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class CategoryPanel extends JPanel {

    private final CategoryDAO categoryDAO = new CategoryDAO();

    private JTable table;
    private DefaultTableModel tableModel;

    private JTextField idField;
    private JTextField nameField;

    private JButton addBtn;
    private JButton updateBtn;
    private JButton deleteBtn;
    private JButton clearBtn;

    public CategoryPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(UIStyle.COLOR_BG);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        initUI();
        loadCategories();
    }

    private void initUI() {
        // Header
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel titleLbl = new JLabel("Category Management");
        titleLbl.setFont(UIStyle.FONT_TITLE);
        titleLbl.setForeground(UIStyle.COLOR_TEXT_MAIN);

        JButton refreshBtn = new JButton("Refresh");
        UIStyle.styleButton(refreshBtn, UIStyle.COLOR_SECONDARY);
        refreshBtn.addActionListener(e -> loadCategories());

        topPanel.add(titleLbl, BorderLayout.WEST);
        topPanel.add(refreshBtn, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Center SplitPane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.65);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);

        // Table Card
        JPanel tableCard = UIStyle.createCardPanel();
        tableCard.setLayout(new BorderLayout(10, 10));

        String[] cols = {"Category ID", "Category Name"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        UIStyle.styleTable(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getColumnModel().getColumn(1).setPreferredWidth(300);

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

        JLabel formTitle = UIStyle.createHeaderLabel("Category Details");
        formCard.add(formTitle, BorderLayout.NORTH);

        JPanel fieldsPanel = new JPanel(new GridLayout(5, 1, 5, 5));
        fieldsPanel.setOpaque(false);

        idField = UIStyle.createStyledTextField(15);
        idField.setEditable(false);
        idField.setBackground(new Color(241, 245, 249));

        nameField = UIStyle.createStyledTextField(15);

        fieldsPanel.add(new JLabel("Category ID (Auto):"));
        fieldsPanel.add(idField);
        fieldsPanel.add(new JLabel("Category Name *:"));
        fieldsPanel.add(nameField);

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

        // Action Handlers
        addBtn.addActionListener(e -> onAdd());
        updateBtn.addActionListener(e -> onUpdate());
        deleteBtn.addActionListener(e -> onDelete());
        clearBtn.addActionListener(e -> clearForm());
    }

    public void loadCategories() {
        tableModel.setRowCount(0);
        try {
            List<Category> list = categoryDAO.getAllCategories();
            for (Category c : list) {
                tableModel.addRow(new Object[]{c.getCategoryId(), c.getCategoryName()});
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load categories: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void populateForm() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            idField.setText(String.valueOf(tableModel.getValueAt(row, 0)));
            nameField.setText(String.valueOf(tableModel.getValueAt(row, 1)));
        }
    }

    private void clearForm() {
        table.clearSelection();
        idField.setText("");
        nameField.setText("");
    }

    private void onAdd() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Category name is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            nameField.requestFocus();
            return;
        }

        try {
            int newId = categoryDAO.addCategory(new Category(name));
            JOptionPane.showMessageDialog(this, "Category added successfully with ID #" + newId + "!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadCategories();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Could not add category: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onUpdate() {
        String idText = idField.getText().trim();
        if (idText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a category to update.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Category name is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int id = Integer.parseInt(idText);
            boolean ok = categoryDAO.updateCategory(new Category(id, name));
            if (ok) {
                JOptionPane.showMessageDialog(this, "Category updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadCategories();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Could not update category: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onDelete() {
        String idText = idField.getText().trim();
        if (idText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a category to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = Integer.parseInt(idText);
        String name = nameField.getText().trim();

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete category '" + name + "' (ID: " + id + ")?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean ok = categoryDAO.deleteCategory(id);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Category deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                    clearForm();
                    loadCategories();
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Constraint Violation", JOptionPane.WARNING_MESSAGE);
            }
        }
    }
}
