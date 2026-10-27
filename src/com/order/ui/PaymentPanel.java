package com.order.ui;

import com.order.dao.PaymentDAO;
import com.order.model.Payment;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class PaymentPanel extends JPanel {

    private final PaymentDAO paymentDAO = new PaymentDAO();

    private JTable table;
    private DefaultTableModel tableModel;

    private JTextField searchField;
    private JComboBox<String> statusFilterCombo;
    private JComboBox<String> updateStatusCombo;
    private JLabel summaryLabel;

    public PaymentPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(UIStyle.COLOR_BG);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        initUI();
        loadPayments();
    }

    private void initUI() {
        // Top Toolbar
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setOpaque(false);

        JLabel titleLbl = new JLabel("Payment Management");
        titleLbl.setFont(UIStyle.FONT_TITLE);
        titleLbl.setForeground(UIStyle.COLOR_TEXT_MAIN);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controls.setOpaque(false);

        statusFilterCombo = new JComboBox<>(new String[]{"All Statuses", "Paid", "Pending", "Failed", "Refunded"});
        statusFilterCombo.setFont(UIStyle.FONT_INPUT);

        searchField = UIStyle.createStyledTextField(14);
        JButton searchBtn = new JButton("Search");
        UIStyle.styleButton(searchBtn, UIStyle.COLOR_PRIMARY);

        JButton resetBtn = new JButton("Reset");
        UIStyle.styleButton(resetBtn, UIStyle.COLOR_SECONDARY);

        controls.add(new JLabel("Status:"));
        controls.add(statusFilterCombo);
        controls.add(new JLabel("Search:"));
        controls.add(searchField);
        controls.add(searchBtn);
        controls.add(resetBtn);

        topPanel.add(titleLbl, BorderLayout.WEST);
        topPanel.add(controls, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Center Table Card
        JPanel tableCard = UIStyle.createCardPanel();
        tableCard.setLayout(new BorderLayout(10, 10));

        String[] cols = {"Payment ID", "Order ID", "Payment Date", "Amount (₹)", "Payment Method", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        table = new JTable(tableModel);
        UIStyle.styleTable(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(90);
        table.getColumnModel().getColumn(1).setPreferredWidth(90);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(120);
        table.getColumnModel().getColumn(4).setPreferredWidth(160);
        table.getColumnModel().getColumn(5).setPreferredWidth(120);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        tableCard.add(scrollPane, BorderLayout.CENTER);

        // Bottom Action Bar
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setOpaque(false);

        JPanel updatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        updatePanel.setOpaque(false);

        updateStatusCombo = new JComboBox<>(new String[]{"Paid", "Pending", "Refunded", "Failed"});
        updateStatusCombo.setFont(UIStyle.FONT_INPUT);

        JButton updateBtn = new JButton("Update Status");
        UIStyle.styleButton(updateBtn, UIStyle.COLOR_SUCCESS);

        updatePanel.add(new JLabel("Update Selected Payment Status:"));
        updatePanel.add(updateStatusCombo);
        updatePanel.add(updateBtn);

        summaryLabel = new JLabel("Loading payments summary...");
        summaryLabel.setFont(UIStyle.FONT_LABEL);
        summaryLabel.setForeground(UIStyle.COLOR_PRIMARY);

        bottomBar.add(updatePanel, BorderLayout.WEST);
        bottomBar.add(summaryLabel, BorderLayout.EAST);
        tableCard.add(bottomBar, BorderLayout.SOUTH);

        add(tableCard, BorderLayout.CENTER);

        // Listeners
        searchBtn.addActionListener(e -> onSearch());
        searchField.addActionListener(e -> onSearch());
        resetBtn.addActionListener(e -> {
            searchField.setText("");
            statusFilterCombo.setSelectedIndex(0);
            loadPayments();
        });
        statusFilterCombo.addActionListener(e -> filterByStatus());
        updateBtn.addActionListener(e -> onUpdateStatus());
    }

    public void loadPayments() {
        tableModel.setRowCount(0);
        try {
            List<Payment> list = paymentDAO.getAllPayments();
            BigDecimal totalPaid = BigDecimal.ZERO;
            int countPaid = 0;

            for (Payment p : list) {
                if ("Paid".equalsIgnoreCase(p.getStatus())) {
                    totalPaid = totalPaid.add(p.getAmount());
                    countPaid++;
                }
                tableModel.addRow(new Object[]{
                        p.getPaymentId(),
                        p.getOrderId(),
                        p.getPaymentDate(),
                        p.getAmount().toPlainString(),
                        p.getMethod(),
                        p.getStatus()
                });
            }
            summaryLabel.setText("Total Records: " + list.size() + "  |  Paid Volume: ₹" + totalPaid.toPlainString() + " (" + countPaid + " transactions)");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load payments: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onSearch() {
        String kw = searchField.getText().trim();
        if (kw.isEmpty()) {
            loadPayments();
            return;
        }
        tableModel.setRowCount(0);
        try {
            List<Payment> list = paymentDAO.searchPayments(kw);
            for (Payment p : list) {
                tableModel.addRow(new Object[]{
                        p.getPaymentId(),
                        p.getOrderId(),
                        p.getPaymentDate(),
                        p.getAmount().toPlainString(),
                        p.getMethod(),
                        p.getStatus()
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Search error: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filterByStatus() {
        String selected = (String) statusFilterCombo.getSelectedItem();
        if (selected == null || selected.equals("All Statuses")) {
            loadPayments();
            return;
        }
        tableModel.setRowCount(0);
        try {
            List<Payment> list = paymentDAO.getAllPayments();
            for (Payment p : list) {
                if (p.getStatus().equalsIgnoreCase(selected)) {
                    tableModel.addRow(new Object[]{
                            p.getPaymentId(),
                            p.getOrderId(),
                            p.getPaymentDate(),
                            p.getAmount().toPlainString(),
                            p.getMethod(),
                            p.getStatus()
                    });
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Filter error: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onUpdateStatus() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a payment to update.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int payId = (Integer) tableModel.getValueAt(row, 0);
        String newStatus = (String) updateStatusCombo.getSelectedItem();

        try {
            boolean ok = paymentDAO.updatePaymentStatus(payId, newStatus);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Payment #" + payId + " updated to status '" + newStatus + "'!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                loadPayments();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Update failed: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
