package com.order.dao;

import com.order.config.DatabaseConfig;
import com.order.model.Payment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaymentDAO {

    public List<Payment> getAllPayments() throws SQLException {
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT payment_id, order_id, payment_date, amount, method, status " +
                     "FROM PAYMENTS ORDER BY payment_id ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Payment(
                        rs.getInt("payment_id"),
                        rs.getInt("order_id"),
                        rs.getDate("payment_date"),
                        rs.getBigDecimal("amount"),
                        rs.getString("method"),
                        rs.getString("status")
                ));
            }
        }
        return list;
    }

    public List<Payment> searchPayments(String keyword) throws SQLException {
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT payment_id, order_id, payment_date, amount, method, status " +
                     "FROM PAYMENTS " +
                     "WHERE CAST(payment_id AS CHAR) LIKE ? OR CAST(order_id AS CHAR) LIKE ? " +
                     "OR method LIKE ? OR status LIKE ? " +
                     "ORDER BY payment_id ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String pattern = "%" + keyword.trim() + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ps.setString(4, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Payment(
                            rs.getInt("payment_id"),
                            rs.getInt("order_id"),
                            rs.getDate("payment_date"),
                            rs.getBigDecimal("amount"),
                            rs.getString("method"),
                            rs.getString("status")
                    ));
                }
            }
        }
        return list;
    }

    public Payment getPaymentByOrderId(int orderId) throws SQLException {
        String sql = "SELECT payment_id, order_id, payment_date, amount, method, status " +
                     "FROM PAYMENTS WHERE order_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Payment(
                            rs.getInt("payment_id"),
                            rs.getInt("order_id"),
                            rs.getDate("payment_date"),
                            rs.getBigDecimal("amount"),
                            rs.getString("method"),
                            rs.getString("status")
                    );
                }
            }
        }
        return null;
    }

    public boolean updatePaymentStatus(int paymentId, String newStatus) throws SQLException {
        String sql = "UPDATE PAYMENTS SET status = ? WHERE payment_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, paymentId);
            return ps.executeUpdate() > 0;
        }
    }
}
