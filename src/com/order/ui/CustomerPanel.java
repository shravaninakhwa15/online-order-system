package com.order.ui;

import com.order.dao.CustomerDAO;
import com.order.model.Customer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class CustomerPanel extends JPanel {

    private final CustomerDAO customerDAO = new CustomerDAO();

    private JTable table;
    private DefaultTableModel tableModel;

    private JTextField idField;
    private JTextField nameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextField addressField;
    private JTextField searchField;

    private JButton addBtn;
    private JButton updateBtn;
    private JButton deleteBtn;
    private JButton clearBtn;

    public CustomerPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(UIStyle.COLOR_BG);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        initUI();
        loadCustomers();
    }

    private void initUI() {
        // Top Toolbar: Title & Search
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setOpaque(false);

        JLabel titleLbl = new JLabel("Customer Management");
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

        // Center: SplitPane with Table on Left/Center and Form on Right
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.68);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);

        // Table Card
        JPanel tableCard = UIStyle.createCardPanel();
        tableCard.setLayout(new BorderLayout(10, 10));

        String[] cols = {"ID", "Name", "Email", "Phone", "Address"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        UIStyle.styleTable(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(140);
        table.getColumnModel().getColumn(2).setPreferredWidth(170);
        table.getColumnModel().getColumn(3).setPreferredWidth(110);
        table.getColumnModel().getColumn(4).setPreferredWidth(180);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                populateFormFromSelectedRow();
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        tableCard.add(scrollPane, BorderLayout.CENTER);

        // Form Card (Right Side)
        JPanel formCard = UIStyle.createCardPanel();
        formCard.setLayout(new BorderLayout(10, 10));
        formCard.setPreferredSize(new Dimension(320, 0));

        JLabel formTitle = UIStyle.createHeaderLabel("Customer Details");
        formCard.add(formTitle, BorderLayout.NORTH);

        JPanel fieldsPanel = new JPanel(new GridLayout(10, 1, 5, 5));
        fieldsPanel.setOpaque(false);

        idField = UIStyle.createStyledTextField(15);
        idField.setEditable(false);
        idField.setBackground(new Color(241, 245, 249));

        nameField = UIStyle.createStyledTextField(15);
        emailField = UIStyle.createStyledTextField(15);
        phoneField = UIStyle.createStyledTextField(15);
        addressField = UIStyle.createStyledTextField(15);

        fieldsPanel.add(new JLabel("Customer ID (Auto):"));
        fieldsPanel.add(idField);
        fieldsPanel.add(new JLabel("Full Name *:"));
        fieldsPanel.add(nameField);
        fieldsPanel.add(new JLabel("Email Address *:"));
        fieldsPanel.add(emailField);
        fieldsPanel.add(new JLabel("Phone Number:"));
        fieldsPanel.add(phoneField);
        fieldsPanel.add(new JLabel("Address:"));
        fieldsPanel.add(addressField);

        formCard.add(fieldsPanel, BorderLayout.CENTER);

        // Action Buttons
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

        // Event Listeners
        addBtn.addActionListener(e -> onAddCustomer());
        updateBtn.addActionListener(e -> onUpdateCustomer());
        deleteBtn.addActionListener(e -> onDeleteCustomer());
        clearBtn.addActionListener(e -> clearForm());

        searchBtn.addActionListener(e -> onSearch());
        resetBtn.addActionListener(e -> {
            searchField.setText("");
            loadCustomers();
        });
        searchField.addActionListener(e -> onSearch());
    }

    public void loadCustomers() {
        tableModel.setRowCount(0);
        try {
            List<Customer> list = customerDAO.getAllCustomers();
            for (Customer c : list) {
                tableModel.addRow(new Object[]{
                        c.getCustomerId(),
                        c.getName(),
                        c.getEmail(),
                        c.getPhone() != null ? c.getPhone() : "",
                        c.getAddress() != null ? c.getAddress() : ""
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load customers: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onSearch() {
        String kw = searchField.getText().trim();
        if (kw.isEmpty()) {
            loadCustomers();
            return;
        }
        tableModel.setRowCount(0);
        try {
            List<Customer> list = customerDAO.searchCustomers(kw);
            for (Customer c : list) {
                tableModel.addRow(new Object[]{
                        c.getCustomerId(),
                        c.getName(),
                        c.getEmail(),
                        c.getPhone() != null ? c.getPhone() : "",
                        c.getAddress() != null ? c.getAddress() : ""
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Search error: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void populateFormFromSelectedRow() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            idField.setText(String.valueOf(tableModel.getValueAt(row, 0)));
            nameField.setText(String.valueOf(tableModel.getValueAt(row, 1)));
            emailField.setText(String.valueOf(tableModel.getValueAt(row, 2)));
            phoneField.setText(String.valueOf(tableModel.getValueAt(row, 3)));
            addressField.setText(String.valueOf(tableModel.getValueAt(row, 4)));
        }
    }

    private void clearForm() {
        table.clearSelection();
        idField.setText("");
        nameField.setText("");
        emailField.setText("");
        phoneField.setText("");
        addressField.setText("");
    }

    private boolean validateInputs() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Customer name is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            nameField.requestFocus();
            return false;
        }
        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Customer email is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            emailField.requestFocus();
            return false;
        }
        if (!email.contains("@") || !email.contains(".")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            emailField.requestFocus();
            return false;
        }
        return true;
    }

    private void onAddCustomer() {
        if (!validateInputs()) return;

        Customer c = new Customer(
                nameField.getText().trim(),
                emailField.getText().trim(),
                phoneField.getText().trim(),
                addressField.getText().trim()
        );

        try {
            int newId = customerDAO.addCustomer(c);
            JOptionPane.showMessageDialog(this, "Customer added successfully with ID #" + newId + "!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadCustomers();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Could not add customer: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onUpdateCustomer() {
        String idText = idField.getText().trim();
        if (idText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a customer from the table to update.",
                    "No Customer Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!validateInputs()) return;

        Customer c = new Customer(
                Integer.parseInt(idText),
                nameField.getText().trim(),
                emailField.getText().trim(),
                phoneField.getText().trim(),
                addressField.getText().trim()
        );

        try {
            boolean ok = customerDAO.updateCustomer(c);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Customer #" + c.getCustomerId() + " updated successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                loadCustomers();
            } else {
                JOptionPane.showMessageDialog(this, "Customer not found.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Update failed: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onDeleteCustomer() {
        String idText = idField.getText().trim();
        if (idText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a customer from the table to delete.",
                    "No Customer Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int customerId = Integer.parseInt(idText);
        String name = nameField.getText().trim();

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete customer '" + name + "' (ID: " + customerId + ")?\n" +
                "Note: Any associated orders, order items, and payment records will also be securely removed.",
                "Confirm Cascade Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean ok = customerDAO.deleteCustomer(customerId);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Customer #" + customerId + " and associated records deleted.",
                            "Deleted", JOptionPane.INFORMATION_MESSAGE);
                    clearForm();
                    loadCustomers();
                } else {
                    JOptionPane.showMessageDialog(this, "Customer not found.", "Warning", JOptionPane.WARNING_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Delete failed: " + ex.getMessage(),
                        "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
