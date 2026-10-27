package com.order.dao;

import com.order.config.DatabaseConfig;
import com.order.model.Product;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    public List<Product> getAllProducts() throws SQLException {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT p.product_id, p.name, p.price, p.stock_qty, p.category_id, c.category_name " +
                     "FROM PRODUCT p " +
                     "JOIN CATEGORY c ON p.category_id = c.category_id " +
                     "ORDER BY p.product_id ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Product(
                        rs.getInt("product_id"),
                        rs.getString("name"),
                        rs.getBigDecimal("price"),
                        rs.getInt("stock_qty"),
                        rs.getInt("category_id"),
                        rs.getString("category_name")
                ));
            }
        }
        return list;
    }

    public List<Product> searchProducts(String keyword) throws SQLException {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT p.product_id, p.name, p.price, p.stock_qty, p.category_id, c.category_name " +
                     "FROM PRODUCT p " +
                     "JOIN CATEGORY c ON p.category_id = c.category_id " +
                     "WHERE p.name LIKE ? OR c.category_name LIKE ? " +
                     "ORDER BY p.product_id ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String pattern = "%" + keyword.trim() + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Product(
                            rs.getInt("product_id"),
                            rs.getString("name"),
                            rs.getBigDecimal("price"),
                            rs.getInt("stock_qty"),
                            rs.getInt("category_id"),
                            rs.getString("category_name")
                    ));
                }
            }
        }
        return list;
    }

    public Product getProductById(int productId) throws SQLException {
        String sql = "SELECT p.product_id, p.name, p.price, p.stock_qty, p.category_id, c.category_name " +
                     "FROM PRODUCT p " +
                     "JOIN CATEGORY c ON p.category_id = c.category_id " +
                     "WHERE p.product_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Product(
                            rs.getInt("product_id"),
                            rs.getString("name"),
                            rs.getBigDecimal("price"),
                            rs.getInt("stock_qty"),
                            rs.getInt("category_id"),
                            rs.getString("category_name")
                    );
                }
            }
        }
        return null;
    }

    public int addProduct(Product p) throws SQLException {
        String sql = "INSERT INTO PRODUCT (name, price, stock_qty, category_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getName().trim());
            ps.setBigDecimal(2, p.getPrice());
            ps.setInt(3, p.getStockQty());
            ps.setInt(4, p.getCategoryId());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    p.setProductId(id);
                    return id;
                }
            }
        }
        return 0;
    }

    public boolean updateProduct(Product p) throws SQLException {
        String sql = "UPDATE PRODUCT SET name = ?, price = ?, stock_qty = ?, category_id = ? WHERE product_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getName().trim());
            ps.setBigDecimal(2, p.getPrice());
            ps.setInt(3, p.getStockQty());
            ps.setInt(4, p.getCategoryId());
            ps.setInt(5, p.getProductId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteProduct(int productId) throws SQLException {
        // Checking if product exists in any order items
        String countSql = "SELECT COUNT(*) FROM ORDER_ITEMS WHERE product_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement checkPs = conn.prepareStatement(countSql)) {
            checkPs.setInt(1, productId);
            try (ResultSet rs = checkPs.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new SQLException("Cannot delete product. It is referenced in " + rs.getInt(1) + 
                            " order item(s). Removing it directly would violate foreign key integrity.");
                }
            }
        }

        String sql = "DELETE FROM PRODUCT WHERE product_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            return ps.executeUpdate() > 0;
        }
    }
}
