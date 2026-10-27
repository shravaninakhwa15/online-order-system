package com.order.ui;

import com.order.dao.OrderDAO;
import com.order.dao.OrderItemDAO;
import com.order.dao.PaymentDAO;
import com.order.model.Order;
import com.order.model.OrderItem;
import com.order.model.Payment;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class OrderPanel extends JPanel {

    private final OrderDAO orderDAO = new OrderDAO();
    private final OrderItemDAO orderItemDAO = new OrderItemDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();

    private JTable table;
    private DefaultTableModel tableModel;

    private JTextField searchField;
    private JComboBox<String> statusFilterCombo;
    private JComboBox<String> updateStatusCombo;

    public OrderPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(UIStyle.COLOR_BG);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        initUI();
        loadOrders();
    }

    private void initUI() {
        // Top Toolbar
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setOpaque(false);

        JLabel titleLbl = new JLabel("Order Management");
        titleLbl.setFont(UIStyle.FONT_TITLE);
        titleLbl.setForeground(UIStyle.COLOR_TEXT_MAIN);

        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controlsPanel.setOpaque(false);

        statusFilterCombo = new JComboBox<>(new String[]{"All Statuses", "Placed", "Processing", "Shipped", "Delivered", "Cancelled"});
        statusFilterCombo.setFont(UIStyle.FONT_INPUT);

        searchField = UIStyle.createStyledTextField(14);

        JButton searchBtn = new JButton("Search");
        UIStyle.styleButton(searchBtn, UIStyle.COLOR_PRIMARY);

        JButton newOrderBtn = new JButton("+ New Order");
        UIStyle.styleButton(newOrderBtn, UIStyle.COLOR_PURPLE);

        JButton refreshBtn = new JButton("Refresh");
        UIStyle.styleButton(refreshBtn, UIStyle.COLOR_SECONDARY);

        controlsPanel.add(new JLabel("Status:"));
        controlsPanel.add(statusFilterCombo);
        controlsPanel.add(new JLabel("Search:"));
        controlsPanel.add(searchField);
        controlsPanel.add(searchBtn);
        controlsPanel.add(newOrderBtn);
        controlsPanel.add(refreshBtn);

        topPanel.add(titleLbl, BorderLayout.WEST);
        topPanel.add(controlsPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Center Table Card
        JPanel tableCard = UIStyle.createCardPanel();
        tableCard.setLayout(new BorderLayout(10, 10));

        String[] cols = {"Order ID", "Order Date", "Customer Name", "Order Status", "Total Amount (₹)"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        table = new JTable(tableModel);
        UIStyle.styleTable(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(110);
        table.getColumnModel().getColumn(2).setPreferredWidth(180);
        table.getColumnModel().getColumn(3).setPreferredWidth(110);
        table.getColumnModel().getColumn(4).setPreferredWidth(120);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        tableCard.add(scrollPane, BorderLayout.CENTER);

        // Bottom Action Bar
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setOpaque(false);

        JPanel statusChangePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        statusChangePanel.setOpaque(false);

        updateStatusCombo = new JComboBox<>(new String[]{"Placed", "Processing", "Shipped", "Delivered", "Cancelled"});
        updateStatusCombo.setFont(UIStyle.FONT_INPUT);

        JButton updateStatusBtn = new JButton("Change Status");
        UIStyle.styleButton(updateStatusBtn, UIStyle.COLOR_SUCCESS);

        JButton viewItemsBtn = new JButton("View Order Items & Payment");
        UIStyle.styleButton(viewItemsBtn, UIStyle.COLOR_PRIMARY);

        JButton deleteOrderBtn = new JButton("Delete Order");
        UIStyle.styleButton(deleteOrderBtn, UIStyle.COLOR_DANGER);

        statusChangePanel.add(new JLabel("Update Selected Order Status:"));
        statusChangePanel.add(updateStatusCombo);
        statusChangePanel.add(updateStatusBtn);
        statusChangePanel.add(viewItemsBtn);
        statusChangePanel.add(deleteOrderBtn);

        bottomBar.add(statusChangePanel, BorderLayout.WEST);
        tableCard.add(bottomBar, BorderLayout.SOUTH);

        add(tableCard, BorderLayout.CENTER);

        // Listeners
        searchBtn.addActionListener(e -> onSearch());
        searchField.addActionListener(e -> onSearch());
        refreshBtn.addActionListener(e -> {
            searchField.setText("");
            statusFilterCombo.setSelectedIndex(0);
            loadOrders();
        });
        statusFilterCombo.addActionListener(e -> filterByStatus());
        newOrderBtn.addActionListener(e -> {
            Frame topFrame = (Frame) SwingUtilities.getWindowAncestor(this);
            NewOrderDialog dlg = new NewOrderDialog(topFrame);
            dlg.setVisible(true);
            if (dlg.isOrderPlaced()) {
                loadOrders();
            }
        });
        updateStatusBtn.addActionListener(e -> onUpdateStatus());
        viewItemsBtn.addActionListener(e -> onViewItems());
        deleteOrderBtn.addActionListener(e -> onDeleteOrder());
    }

    public void loadOrders() {
        tableModel.setRowCount(0);
        try {
            List<Order> list = orderDAO.getAllOrders();
            for (Order o : list) {
                tableModel.addRow(new Object[]{
                        o.getOrderId(),
                        o.getOrderDate(),
                        o.getCustomerName() != null ? o.getCustomerName() : "Customer #" + o.getCustomerId(),
                        o.getStatus(),
                        o.getTotalAmount().toPlainString()
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load orders: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onSearch() {
        String kw = searchField.getText().trim();
        if (kw.isEmpty()) {
            loadOrders();
            return;
        }
        tableModel.setRowCount(0);
        try {
            List<Order> list = orderDAO.searchOrders(kw);
            for (Order o : list) {
                tableModel.addRow(new Object[]{
                        o.getOrderId(),
                        o.getOrderDate(),
                        o.getCustomerName() != null ? o.getCustomerName() : "Customer #" + o.getCustomerId(),
                        o.getStatus(),
                        o.getTotalAmount().toPlainString()
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
            loadOrders();
            return;
        }
        tableModel.setRowCount(0);
        try {
            List<Order> list = orderDAO.getAllOrders();
            for (Order o : list) {
                if (o.getStatus().equalsIgnoreCase(selected)) {
                    tableModel.addRow(new Object[]{
                            o.getOrderId(),
                            o.getOrderDate(),
                            o.getCustomerName() != null ? o.getCustomerName() : "Customer #" + o.getCustomerId(),
                            o.getStatus(),
                            o.getTotalAmount().toPlainString()
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
            JOptionPane.showMessageDialog(this, "Select an order to update status.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int orderId = (Integer) tableModel.getValueAt(row, 0);
        String newStatus = (String) updateStatusCombo.getSelectedItem();

        try {
            boolean ok = orderDAO.updateOrderStatus(orderId, newStatus);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Order #" + orderId + " status updated to '" + newStatus + "'!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                loadOrders();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Could not update status: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onViewItems() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an order to view items.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int orderId = (Integer) tableModel.getValueAt(row, 0);
        String custName = String.valueOf(tableModel.getValueAt(row, 2));
        String status = String.valueOf(tableModel.getValueAt(row, 3));
        String total = String.valueOf(tableModel.getValueAt(row, 4));

        try {
            List<OrderItem> items = orderItemDAO.getItemsByOrderId(orderId);
            Payment payment = paymentDAO.getPaymentByOrderId(orderId);

            JDialog detailDlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Order #" + orderId + " Details", true);
            detailDlg.setSize(650, 480);
            detailDlg.setLocationRelativeTo(this);
            detailDlg.setLayout(new BorderLayout(10, 10));

            JPanel top = new JPanel(new GridLayout(2, 2, 8, 4));
            top.setBorder(new EmptyBorder(12, 16, 12, 16));
            top.setBackground(new Color(241, 245, 249));
            top.add(new JLabel("Customer: " + custName));
            top.add(new JLabel("Order Status: " + status));
            top.add(new JLabel("Total Amount: ₹" + total));
            if (payment != null) {
                top.add(new JLabel("Payment: ₹" + payment.getAmount() + " (" + payment.getMethod() + " - " + payment.getStatus() + ")"));
            } else {
                top.add(new JLabel("Payment: No record found"));
            }
            detailDlg.add(top, BorderLayout.NORTH);

            String[] cols = {"Product ID", "Product Name", "Quantity", "Unit Price (₹)", "Subtotal (₹)"};
            DefaultTableModel dm = new DefaultTableModel(cols, 0);
            JTable itemTbl = new JTable(dm);
            UIStyle.styleTable(itemTbl);

            for (OrderItem oi : items) {
                dm.addRow(new Object[]{
                        oi.getProductId(),
                        oi.getProductName(),
                        oi.getQuantity(),
                        oi.getUnitPrice().toPlainString(),
                        oi.getSubtotal().toPlainString()
                });
            }
            detailDlg.add(new JScrollPane(itemTbl), BorderLayout.CENTER);

            JButton closeBtn = new JButton("Close");
            UIStyle.styleButton(closeBtn, UIStyle.COLOR_SECONDARY);
            closeBtn.addActionListener(e -> detailDlg.dispose());
            JPanel btm = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            btm.add(closeBtn);
            detailDlg.add(btm, BorderLayout.SOUTH);

            detailDlg.setVisible(true);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Could not fetch items: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onDeleteOrder() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an order to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int orderId = (Integer) tableModel.getValueAt(row, 0);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete Order #" + orderId + "?\n" +
                "Associated payments and order items will also be removed safely in a single transaction.",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean ok = orderDAO.deleteOrder(orderId);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Order #" + orderId + " deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                    loadOrders();
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Delete failed: " + ex.getMessage(),
                        "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
