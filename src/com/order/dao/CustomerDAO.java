package com.order.dao;

import com.order.config.DatabaseConfig;
import com.order.model.Customer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {

    public List<Customer> getAllCustomers() throws SQLException {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT customer_id, name, email, phone, address FROM CUSTOMER ORDER BY customer_id ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Customer(
                        rs.getInt("customer_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("address")
                ));
            }
        }
        return list;
    }

    public List<Customer> searchCustomers(String keyword) throws SQLException {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT customer_id, name, email, phone, address FROM CUSTOMER " +
                     "WHERE name LIKE ? OR email LIKE ? OR phone LIKE ? OR address LIKE ? " +
                     "ORDER BY customer_id ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String pattern = "%" + keyword.trim() + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ps.setString(4, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Customer(
                            rs.getInt("customer_id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("phone"),
                            rs.getString("address")
                    ));
                }
            }
        }
        return list;
    }

    public Customer getCustomerById(int customerId) throws SQLException {
        String sql = "SELECT customer_id, name, email, phone, address FROM CUSTOMER WHERE customer_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Customer(
                            rs.getInt("customer_id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("phone"),
                            rs.getString("address")
                    );
                }
            }
        }
        return null;
    }

    public int addCustomer(Customer c) throws SQLException {
        String sql = "INSERT INTO CUSTOMER (name, email, phone, address) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getName().trim());
            ps.setString(2, c.getEmail().trim());
            ps.setString(3, c.getPhone() != null ? c.getPhone().trim() : null);
            ps.setString(4, c.getAddress() != null ? c.getAddress().trim() : null);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int generatedId = rs.getInt(1);
                    c.setCustomerId(generatedId);
                    return generatedId;
                }
            }
        }
        return 0;
    }

    public boolean updateCustomer(Customer c) throws SQLException {
        String sql = "UPDATE CUSTOMER SET name = ?, email = ?, phone = ?, address = ? WHERE customer_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getName().trim());
            ps.setString(2, c.getEmail().trim());
            ps.setString(3, c.getPhone() != null ? c.getPhone().trim() : null);
            ps.setString(4, c.getAddress() != null ? c.getAddress().trim() : null);
            ps.setInt(5, c.getCustomerId());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Delete customer and cascade child orders, items, and payments safely using a single transaction.
     * Implements the exact child-first deletion requirement from the SLA Stage 3 report.
     */
    public boolean deleteCustomer(int customerId) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false); // Begin transaction

            // 1. Delete payments belonging to the customer's orders
            String delPayments = "DELETE FROM PAYMENTS WHERE order_id IN (SELECT order_id FROM ORDERS WHERE customer_id = ?)";
            try (PreparedStatement ps = conn.prepareStatement(delPayments)) {
                ps.setInt(1, customerId);
                ps.executeUpdate();
            }

            // 2. Delete order items belonging to the customer's orders
            String delItems = "DELETE FROM ORDER_ITEMS WHERE order_id IN (SELECT order_id FROM ORDERS WHERE customer_id = ?)";
            try (PreparedStatement ps = conn.prepareStatement(delItems)) {
                ps.setInt(1, customerId);
                ps.executeUpdate();
            }

            // 3. Delete customer orders
            String delOrders = "DELETE FROM ORDERS WHERE customer_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(delOrders)) {
                ps.setInt(1, customerId);
                ps.executeUpdate();
            }

            // 4. Delete the customer
            String delCust = "DELETE FROM CUSTOMER WHERE customer_id = ?";
            int rowsDeleted;
            try (PreparedStatement ps = conn.prepareStatement(delCust)) {
                ps.setInt(1, customerId);
                rowsDeleted = ps.executeUpdate();
            }

            conn.commit(); // Commit transaction
            return rowsDeleted > 0;

        } catch (SQLException ex) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback on any failure
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
