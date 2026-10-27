package com.order.ui;

import com.order.dao.OrderItemDAO;
import com.order.model.OrderItem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class OrderItemPanel extends JPanel {

    private final OrderItemDAO orderItemDAO = new OrderItemDAO();

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JLabel summaryLabel;

    public OrderItemPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(UIStyle.COLOR_BG);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        initUI();
        loadItems();
    }

    private void initUI() {
        // Top Toolbar
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setOpaque(false);

        JLabel titleLbl = new JLabel("Order Items Breakdown");
        titleLbl.setFont(UIStyle.FONT_TITLE);
        titleLbl.setForeground(UIStyle.COLOR_TEXT_MAIN);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchPanel.setOpaque(false);

        searchField = UIStyle.createStyledTextField(18);
        JButton searchBtn = new JButton("Search");
        UIStyle.styleButton(searchBtn, UIStyle.COLOR_PRIMARY);

        JButton resetBtn = new JButton("Reset");
        UIStyle.styleButton(resetBtn, UIStyle.COLOR_SECONDARY);

        searchPanel.add(new JLabel("Search (Order ID or Product):"));
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);
        searchPanel.add(resetBtn);

        topPanel.add(titleLbl, BorderLayout.WEST);
        topPanel.add(searchPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Center Table Card
        JPanel tableCard = UIStyle.createCardPanel();
        tableCard.setLayout(new BorderLayout(10, 10));

        String[] cols = {"Order ID", "Product ID", "Product Name", "Quantity", "Unit Price (₹)", "Line Subtotal (₹)"};
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
        table.getColumnModel().getColumn(2).setPreferredWidth(220);
        table.getColumnModel().getColumn(3).setPreferredWidth(80);
        table.getColumnModel().getColumn(4).setPreferredWidth(110);
        table.getColumnModel().getColumn(5).setPreferredWidth(120);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        tableCard.add(scrollPane, BorderLayout.CENTER);

        // Bottom Summary
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        summaryLabel = new JLabel("Loading items...");
        summaryLabel.setFont(UIStyle.FONT_HEADER);
        summaryLabel.setForeground(UIStyle.COLOR_PRIMARY);
        bottomPanel.add(summaryLabel, BorderLayout.WEST);
        tableCard.add(bottomPanel, BorderLayout.SOUTH);

        add(tableCard, BorderLayout.CENTER);

        // Listeners
        searchBtn.addActionListener(e -> onSearch());
        searchField.addActionListener(e -> onSearch());
        resetBtn.addActionListener(e -> {
            searchField.setText("");
            loadItems();
        });
    }

    public void loadItems() {
        tableModel.setRowCount(0);
        try {
            List<OrderItem> list = orderItemDAO.getAllOrderItems();
            int totalQty = 0;
            BigDecimal grandTotal = BigDecimal.ZERO;

            for (OrderItem oi : list) {
                totalQty += oi.getQuantity();
                grandTotal = grandTotal.add(oi.getSubtotal());
                tableModel.addRow(new Object[]{
                        oi.getOrderId(),
                        oi.getProductId(),
                        oi.getProductName(),
                        oi.getQuantity(),
                        oi.getUnitPrice().toPlainString(),
                        oi.getSubtotal().toPlainString()
                });
            }
            summaryLabel.setText("Total Records: " + list.size() + "  |  Total Units Ordered: " + totalQty + "  |  Gross Value: ₹" + grandTotal.toPlainString());
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load order items: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onSearch() {
        String kw = searchField.getText().trim();
        if (kw.isEmpty()) {
            loadItems();
            return;
        }
        tableModel.setRowCount(0);
        try {
            List<OrderItem> list = orderItemDAO.searchOrderItems(kw);
            int totalQty = 0;
            BigDecimal grandTotal = BigDecimal.ZERO;

            for (OrderItem oi : list) {
                totalQty += oi.getQuantity();
                grandTotal = grandTotal.add(oi.getSubtotal());
                tableModel.addRow(new Object[]{
                        oi.getOrderId(),
                        oi.getProductId(),
                        oi.getProductName(),
                        oi.getQuantity(),
                        oi.getUnitPrice().toPlainString(),
                        oi.getSubtotal().toPlainString()
                });
            }
            summaryLabel.setText("Found: " + list.size() + " record(s)  |  Units: " + totalQty + "  |  Total: ₹" + grandTotal.toPlainString());
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Search error: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
