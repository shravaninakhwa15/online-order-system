package com.order.dao;

import com.order.config.DatabaseConfig;
import com.order.model.OrderItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderItemDAO {

    public List<OrderItem> getItemsByOrderId(int orderId) throws SQLException {
        List<OrderItem> list = new ArrayList<>();
        String sql = "SELECT oi.order_id, oi.product_id, p.name AS product_name, oi.quantity, oi.unit_price " +
                     "FROM ORDER_ITEMS oi " +
                     "JOIN PRODUCT p ON oi.product_id = p.product_id " +
                     "WHERE oi.order_id = ? " +
                     "ORDER BY oi.product_id ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new OrderItem(
                            rs.getInt("order_id"),
                            rs.getInt("product_id"),
                            rs.getString("product_name"),
                            rs.getInt("quantity"),
                            rs.getBigDecimal("unit_price")
                    ));
                }
            }
        }
        return list;
    }

    public List<OrderItem> getAllOrderItems() throws SQLException {
        List<OrderItem> list = new ArrayList<>();
        String sql = "SELECT oi.order_id, oi.product_id, p.name AS product_name, oi.quantity, oi.unit_price " +
                     "FROM ORDER_ITEMS oi " +
                     "JOIN PRODUCT p ON oi.product_id = p.product_id " +
                     "ORDER BY oi.order_id DESC, oi.product_id ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new OrderItem(
                        rs.getInt("order_id"),
                        rs.getInt("product_id"),
                        rs.getString("product_name"),
                        rs.getInt("quantity"),
                        rs.getBigDecimal("unit_price")
                ));
            }
        }
        return list;
    }

    public List<OrderItem> searchOrderItems(String keyword) throws SQLException {
        List<OrderItem> list = new ArrayList<>();
        String sql = "SELECT oi.order_id, oi.product_id, p.name AS product_name, oi.quantity, oi.unit_price " +
                     "FROM ORDER_ITEMS oi " +
                     "JOIN PRODUCT p ON oi.product_id = p.product_id " +
                     "WHERE CAST(oi.order_id AS CHAR) LIKE ? OR p.name LIKE ? " +
                     "ORDER BY oi.order_id DESC, oi.product_id ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String pattern = "%" + keyword.trim() + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new OrderItem(
                            rs.getInt("order_id"),
                            rs.getInt("product_id"),
                            rs.getString("product_name"),
                            rs.getInt("quantity"),
                            rs.getBigDecimal("unit_price")
                    ));
                }
            }
        }
        return list;
    }
}
