package com.order.ui;

import com.order.dao.CustomerDAO;
import com.order.dao.OrderDAO;
import com.order.dao.ProductDAO;
import com.order.model.Customer;
import com.order.model.OrderItem;
import com.order.model.Product;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NewOrderDialog extends JDialog {

    private final CustomerDAO customerDAO = new CustomerDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final OrderDAO orderDAO = new OrderDAO();

    private JComboBox<CustomerItem> customerCombo;
    private JComboBox<ProductItem> productCombo;
    private JSpinner qtySpinner;
    private JLabel unitPriceLabel;
    private JLabel stockAvailLabel;

    private JTable cartTable;
    private DefaultTableModel cartModel;
    private final List<OrderItem> cartItems = new ArrayList<>();

    private JLabel totalAmountLabel;
    private JComboBox<String> paymentMethodCombo;
    private JComboBox<String> paymentStatusCombo;

    private boolean orderPlaced = false;

    public NewOrderDialog(Frame parent) {
        super(parent, "Create New Order & Payment", true);
        setSize(780, 640);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(UIStyle.COLOR_BG);

        initUI();
        loadCustomers();
        loadProducts();
    }

    private void initUI() {
        // Top Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIStyle.COLOR_HEADER_BG);
        headerPanel.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel title = new JLabel("🛒 New Order Checkout");
        title.setFont(UIStyle.FONT_TITLE);
        title.setForeground(Color.WHITE);
        headerPanel.add(title, BorderLayout.WEST);

        JLabel subtitle = new JLabel("Transactional Multi-Step Order Placement");
        subtitle.setFont(UIStyle.FONT_SUBTITLE);
        subtitle.setForeground(new Color(148, 163, 184));
        headerPanel.add(subtitle, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Center Content
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(12, 16, 12, 16));

        // 1. Customer Selection Card
        JPanel custCard = UIStyle.createCardPanel();
        custCard.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 5));

        customerCombo = new JComboBox<>();
        customerCombo.setFont(UIStyle.FONT_INPUT);
        customerCombo.setPreferredSize(new Dimension(320, 32));

        custCard.add(new JLabel("Select Customer *:"));
        custCard.add(customerCombo);

        centerPanel.add(custCard, BorderLayout.NORTH);

        // 2. Products & Cart (Middle)
        JPanel cartContainer = new JPanel(new BorderLayout(10, 10));
        cartContainer.setOpaque(false);

        // Item Add Toolbar
        JPanel itemBar = UIStyle.createCardPanel();
        itemBar.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));

        productCombo = new JComboBox<>();
        productCombo.setFont(UIStyle.FONT_INPUT);
        productCombo.setPreferredSize(new Dimension(220, 32));

        qtySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 9999, 1));
        qtySpinner.setFont(UIStyle.FONT_INPUT);
        qtySpinner.setPreferredSize(new Dimension(65, 32));

        unitPriceLabel = new JLabel("Price: ₹0.00");
        unitPriceLabel.setFont(UIStyle.FONT_LABEL);

        stockAvailLabel = new JLabel("Stock: 0");
        stockAvailLabel.setFont(UIStyle.FONT_LABEL);
        stockAvailLabel.setForeground(UIStyle.COLOR_PRIMARY);

        JButton addItemBtn = new JButton("Add Item");
        UIStyle.styleButton(addItemBtn, UIStyle.COLOR_PRIMARY);

        JButton removeItemBtn = new JButton("Remove Selected");
        UIStyle.styleButton(removeItemBtn, UIStyle.COLOR_DANGER);

        itemBar.add(new JLabel("Product:"));
        itemBar.add(productCombo);
        itemBar.add(new JLabel("Qty:"));
        itemBar.add(qtySpinner);
        itemBar.add(unitPriceLabel);
        itemBar.add(stockAvailLabel);
        itemBar.add(addItemBtn);
        itemBar.add(removeItemBtn);

        cartContainer.add(itemBar, BorderLayout.NORTH);

        // Cart Table
        String[] cols = {"Product ID", "Product Name", "Unit Price (₹)", "Quantity", "Subtotal (₹)"};
        cartModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        cartTable = new JTable(cartModel);
        UIStyle.styleTable(cartTable);
        JScrollPane cartScroll = new JScrollPane(cartTable);
        cartScroll.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        cartContainer.add(cartScroll, BorderLayout.CENTER);

        centerPanel.add(cartContainer, BorderLayout.CENTER);

        // 3. Payment & Submit Card (Bottom)
        JPanel bottomCard = UIStyle.createCardPanel();
        bottomCard.setLayout(new BorderLayout(15, 10));

        JPanel payDetails = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        payDetails.setOpaque(false);

        paymentMethodCombo = new JComboBox<>(new String[]{"UPI", "Credit Card", "Debit Card", "Net Banking", "Cash on Delivery"});
        paymentMethodCombo.setFont(UIStyle.FONT_INPUT);

        paymentStatusCombo = new JComboBox<>(new String[]{"Paid", "Pending"});
        paymentStatusCombo.setFont(UIStyle.FONT_INPUT);

        payDetails.add(new JLabel("Payment Method:"));
        payDetails.add(paymentMethodCombo);
        payDetails.add(new JLabel("Payment Status:"));
        payDetails.add(paymentStatusCombo);

        totalAmountLabel = new JLabel("Order Total: ₹0.00");
        totalAmountLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        totalAmountLabel.setForeground(UIStyle.COLOR_PRIMARY);

        bottomCard.add(payDetails, BorderLayout.WEST);
        bottomCard.add(totalAmountLabel, BorderLayout.EAST);

        // Dialog Action Buttons
        JPanel actionBtnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        actionBtnBar.setOpaque(false);

        JButton cancelBtn = new JButton("Cancel");
        UIStyle.styleButton(cancelBtn, UIStyle.COLOR_SECONDARY);

        JButton placeOrderBtn = new JButton("Confirm & Place Order");
        UIStyle.styleButton(placeOrderBtn, UIStyle.COLOR_SUCCESS);

        actionBtnBar.add(cancelBtn);
        actionBtnBar.add(placeOrderBtn);
        bottomCard.add(actionBtnBar, BorderLayout.SOUTH);

        centerPanel.add(bottomCard, BorderLayout.SOUTH);
        add(centerPanel, BorderLayout.CENTER);

        // Events
        productCombo.addActionListener(e -> updateProductDetailsDisplay());
        addItemBtn.addActionListener(e -> onAddToCart());
        removeItemBtn.addActionListener(e -> onRemoveFromCart());
        cancelBtn.addActionListener(e -> dispose());
        placeOrderBtn.addActionListener(e -> onPlaceOrder());
    }

    private void loadCustomers() {
        customerCombo.removeAllItems();
        try {
            List<Customer> list = customerDAO.getAllCustomers();
            for (Customer c : list) {
                customerCombo.addItem(new CustomerItem(c.getCustomerId(), c.getName(), c.getEmail()));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Could not load customers: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadProducts() {
        productCombo.removeAllItems();
        try {
            List<Product> list = productDAO.getAllProducts();
            for (Product p : list) {
                productCombo.addItem(new ProductItem(p.getProductId(), p.getName(), p.getPrice(), p.getStockQty()));
            }
            updateProductDetailsDisplay();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Could not load products: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateProductDetailsDisplay() {
        ProductItem item = (ProductItem) productCombo.getSelectedItem();
        if (item != null) {
            unitPriceLabel.setText("Price: ₹" + item.price.toPlainString());
            stockAvailLabel.setText("Stock: " + item.stock);
            if (item.stock > 0) {
                qtySpinner.setModel(new SpinnerNumberModel(1, 1, item.stock, 1));
            } else {
                qtySpinner.setModel(new SpinnerNumberModel(0, 0, 0, 1));
            }
        }
    }

    private void onAddToCart() {
        ProductItem prod = (ProductItem) productCombo.getSelectedItem();
        if (prod == null) return;

        if (prod.stock <= 0) {
            JOptionPane.showMessageDialog(this, "Product '" + prod.name + "' is out of stock!", "Stock Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int qty = (Integer) qtySpinner.getValue();
        if (qty <= 0) {
            JOptionPane.showMessageDialog(this, "Quantity must be greater than 0.", "Quantity Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Check if already in cart
        for (OrderItem oi : cartItems) {
            if (oi.getProductId() == prod.id) {
                int totalRequested = oi.getQuantity() + qty;
                if (totalRequested > prod.stock) {
                    JOptionPane.showMessageDialog(this, "Cannot add more. Total in cart (" + totalRequested + 
                            ") exceeds stock (" + prod.stock + ").", "Stock Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                oi.setQuantity(totalRequested);
                refreshCartTable();
                return;
            }
        }

        OrderItem newItem = new OrderItem(0, prod.id, prod.name, qty, prod.price);
        cartItems.add(newItem);
        refreshCartTable();
    }

    private void onRemoveFromCart() {
        int row = cartTable.getSelectedRow();
        if (row >= 0) {
            cartItems.remove(row);
            refreshCartTable();
        } else {
            JOptionPane.showMessageDialog(this, "Select an item to remove.", "Selection Required", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void refreshCartTable() {
        cartModel.setRowCount(0);
        BigDecimal total = BigDecimal.ZERO;

        for (OrderItem oi : cartItems) {
            BigDecimal subtotal = oi.getSubtotal();
            total = total.add(subtotal);
            cartModel.addRow(new Object[]{
                    oi.getProductId(),
                    oi.getProductName(),
                    oi.getUnitPrice().toPlainString(),
                    oi.getQuantity(),
                    subtotal.toPlainString()
            });
        }
        totalAmountLabel.setText("Order Total: ₹" + total.toPlainString());
    }

    private void onPlaceOrder() {
        CustomerItem cust = (CustomerItem) customerCombo.getSelectedItem();
        if (cust == null) {
            JOptionPane.showMessageDialog(this, "Please select a customer.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (cartItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cart is empty! Add at least one product.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String paymentMethod = (String) paymentMethodCombo.getSelectedItem();
        String paymentStatus = (String) paymentStatusCombo.getSelectedItem();
        Date today = new Date(System.currentTimeMillis());

        try {
            int newOrderId = orderDAO.createOrderWithItems(
                    cust.id,
                    today,
                    "Placed",
                    cartItems,
                    paymentMethod,
                    paymentStatus
            );

            JOptionPane.showMessageDialog(
                    this,
                    "🎉 Order #" + newOrderId + " placed successfully!\n" +
                    "Customer: " + cust.name + "\n" +
                    "Items: " + cartItems.size() + "\n" +
                    "Payment Status: " + paymentStatus + " via " + paymentMethod + "\n" +
                    "Database transaction committed.",
                    "Order Confirmed",
                    JOptionPane.INFORMATION_MESSAGE
            );

            orderPlaced = true;
            dispose();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Order placement failed! Transaction rolled back.\nReason: " + ex.getMessage(),
                    "Transaction Rolled Back",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    public boolean isOrderPlaced() {
        return orderPlaced;
    }

    // Helper items
    private static class CustomerItem {
        final int id;
        final String name;
        final String email;

        CustomerItem(int id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }

        @Override
        public String toString() {
            return name + " (" + email + ")";
        }
    }

    private static class ProductItem {
        final int id;
        final String name;
        final BigDecimal price;
        final int stock;

        ProductItem(int id, String name, BigDecimal price, int stock) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.stock = stock;
        }

        @Override
        public String toString() {
            return name + " (₹" + price + ")";
        }
    }
}
