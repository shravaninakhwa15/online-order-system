package com.order.dao;

import com.order.config.DatabaseConfig;
import com.order.model.Order;
import com.order.model.OrderItem;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    public List<Order> getAllOrders() throws SQLException {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT o.order_id, o.order_date, o.status, o.total_amount, o.customer_id, c.name AS customer_name " +
                     "FROM ORDERS o " +
                     "JOIN CUSTOMER c ON o.customer_id = c.customer_id " +
                     "ORDER BY o.order_id DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Order(
                        rs.getInt("order_id"),
                        rs.getDate("order_date"),
                        rs.getString("status"),
                        rs.getBigDecimal("total_amount"),
                        rs.getInt("customer_id"),
                        rs.getString("customer_name")
                ));
            }
        }
        return list;
    }

    public List<Order> searchOrders(String keyword) throws SQLException {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT o.order_id, o.order_date, o.status, o.total_amount, o.customer_id, c.name AS customer_name " +
                     "FROM ORDERS o " +
                     "JOIN CUSTOMER c ON o.customer_id = c.customer_id " +
                     "WHERE CAST(o.order_id AS CHAR) LIKE ? OR c.name LIKE ? OR o.status LIKE ? " +
                     "ORDER BY o.order_id DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String pattern = "%" + keyword.trim() + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Order(
                            rs.getInt("order_id"),
                            rs.getDate("order_date"),
                            rs.getString("status"),
                            rs.getBigDecimal("total_amount"),
                            rs.getInt("customer_id"),
                            rs.getString("customer_name")
                    ));
                }
            }
        }
        return list;
    }

    public Order getOrderById(int orderId) throws SQLException {
        String sql = "SELECT o.order_id, o.order_date, o.status, o.total_amount, o.customer_id, c.name AS customer_name " +
                     "FROM ORDERS o " +
                     "JOIN CUSTOMER c ON o.customer_id = c.customer_id " +
                     "WHERE o.order_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Order(
                            rs.getInt("order_id"),
                            rs.getDate("order_date"),
                            rs.getString("status"),
                            rs.getBigDecimal("total_amount"),
                            rs.getInt("customer_id"),
                            rs.getString("customer_name")
                    );
                }
            }
        }
        return null;
    }

    public boolean updateOrderStatus(int orderId, String newStatus) throws SQLException {
        String sql = "UPDATE ORDERS SET status = ? WHERE order_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, orderId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Multi-step transaction:
     * 1. Validates stock availability.
     * 2. Calculates total amount (quantity * unit_price).
     * 3. Inserts order header.
     * 4. Inserts order line items and deducts stock.
     * 5. Inserts corresponding payment.
     * 6. Commits atomically or rolls back on any error.
     */
    public int createOrderWithItems(int customerId, Date orderDate, String orderStatus,
                                    List<OrderItem> items, String paymentMethod, String paymentStatus) throws SQLException {
        if (items == null || items.isEmpty()) {
            throw new SQLException("Cannot place an order without items.");
        }

        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false); // Begin transaction

            // 1. Calculate total and verify stock
            BigDecimal calculatedTotal = BigDecimal.ZERO;
            for (OrderItem item : items) {
                String checkStockSql = "SELECT name, stock_qty, price FROM PRODUCT WHERE product_id = ? FOR UPDATE";
                try (PreparedStatement checkPs = conn.prepareStatement(checkStockSql)) {
                    checkPs.setInt(1, item.getProductId());
                    try (ResultSet rs = checkPs.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Product ID " + item.getProductId() + " not found.");
                        }
                        String prodName = rs.getString("name");
                        int currentStock = rs.getInt("stock_qty");
                        if (currentStock < item.getQuantity()) {
                            throw new SQLException("Insufficient stock for '" + prodName + 
                                    "'. Requested: " + item.getQuantity() + ", Available: " + currentStock);
                        }
                    }
                }
                BigDecimal lineSubtotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                calculatedTotal = calculatedTotal.add(lineSubtotal);
            }

            // 2. Insert into ORDERS
            int newOrderId;
            String insertOrderSql = "INSERT INTO ORDERS (order_date, status, total_amount, customer_id) VALUES (?, ?, ?, ?)";
            try (PreparedStatement psOrder = conn.prepareStatement(insertOrderSql, Statement.RETURN_GENERATED_KEYS)) {
                psOrder.setDate(1, orderDate != null ? orderDate : new Date(System.currentTimeMillis()));
                psOrder.setString(2, orderStatus != null ? orderStatus : "Placed");
                psOrder.setBigDecimal(3, calculatedTotal);
                psOrder.setInt(4, customerId);
                psOrder.executeUpdate();
                try (ResultSet rs = psOrder.getGeneratedKeys()) {
                    if (rs.next()) {
                        newOrderId = rs.getInt(1);
                    } else {
                        throw new SQLException("Failed to obtain generated order ID.");
                    }
                }
            }

            // 3. Insert ORDER_ITEMS and deduct stock
            String insertItemSql = "INSERT INTO ORDER_ITEMS (order_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?)";
            String deductStockSql = "UPDATE PRODUCT SET stock_qty = stock_qty - ? WHERE product_id = ?";
            try (PreparedStatement psItem = conn.prepareStatement(insertItemSql);
                 PreparedStatement psStock = conn.prepareStatement(deductStockSql)) {
                for (OrderItem item : items) {
                    psItem.setInt(1, newOrderId);
                    psItem.setInt(2, item.getProductId());
                    psItem.setInt(3, item.getQuantity());
                    psItem.setBigDecimal(4, item.getUnitPrice());
                    psItem.executeUpdate();

                    psStock.setInt(1, item.getQuantity());
                    psStock.setInt(2, item.getProductId());
                    psStock.executeUpdate();
                }
            }

            // 4. Insert PAYMENT record
            String insertPaymentSql = "INSERT INTO PAYMENTS (order_id, payment_date, amount, method, status) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement psPay = conn.prepareStatement(insertPaymentSql)) {
                psPay.setInt(1, newOrderId);
                psPay.setDate(2, orderDate != null ? orderDate : new Date(System.currentTimeMillis()));
                psPay.setBigDecimal(3, calculatedTotal);
                psPay.setString(4, paymentMethod != null ? paymentMethod : "UPI");
                psPay.setString(5, paymentStatus != null ? paymentStatus : "Paid");
                psPay.executeUpdate();
            }

            conn.commit(); // Transaction success!
            return newOrderId;

        } catch (SQLException ex) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback all changes
                } catch (SQLException rbEx) {
                    rbEx.printStackTrace();
                }
            }
            throw ex;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {
                    closeEx.printStackTrace();
                }
            }
        }
    }

    /**
     * Delete order and its child rows (PAYMENTS, ORDER_ITEMS) in safe sequence with transaction rollback.
     */
    public boolean deleteOrder(int orderId) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false); // Begin transaction

            // 1. Delete payment
            String delPay = "DELETE FROM PAYMENTS WHERE order_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(delPay)) {
                ps.setInt(1, orderId);
                ps.executeUpdate();
            }

            // 2. Delete order items
            String delItems = "DELETE FROM ORDER_ITEMS WHERE order_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(delItems)) {
                ps.setInt(1, orderId);
                ps.executeUpdate();
            }

            // 3. Delete order
            String delOrd = "DELETE FROM ORDERS WHERE order_id = ?";
            int rowsDeleted;
            try (PreparedStatement ps = conn.prepareStatement(delOrd)) {
                ps.setInt(1, orderId);
                rowsDeleted = ps.executeUpdate();
            }

            conn.commit();
            return rowsDeleted > 0;

        } catch (SQLException ex) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rbEx) {
                    rbEx.printStackTrace();
                }
            }
            throw ex;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {
                    closeEx.printStackTrace();
                }
            }
        }
    }
}
