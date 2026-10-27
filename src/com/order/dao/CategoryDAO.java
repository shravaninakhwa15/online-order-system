package com.order.dao;

import com.order.config.DatabaseConfig;
import com.order.model.Category;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {

    public List<Category> getAllCategories() throws SQLException {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT category_id, category_name FROM CATEGORY ORDER BY category_id ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Category(
                        rs.getInt("category_id"),
                        rs.getString("category_name")
                ));
            }
        }
        return list;
    }

    public Category getCategoryById(int categoryId) throws SQLException {
        String sql = "SELECT category_id, category_name FROM CATEGORY WHERE category_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Category(
                            rs.getInt("category_id"),
                            rs.getString("category_name")
                    );
                }
            }
        }
        return null;
    }

    public int addCategory(Category c) throws SQLException {
        String sql = "INSERT INTO CATEGORY (category_name) VALUES (?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getCategoryName().trim());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    c.setCategoryId(id);
                    return id;
                }
            }
        }
        return 0;
    }

    public boolean updateCategory(Category c) throws SQLException {
        String sql = "UPDATE CATEGORY SET category_name = ? WHERE category_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getCategoryName().trim());
            ps.setInt(2, c.getCategoryId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteCategory(int categoryId) throws SQLException {
        // Checking if products exist under this category
        String countSql = "SELECT COUNT(*) FROM PRODUCT WHERE category_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement checkPs = conn.prepareStatement(countSql)) {
            checkPs.setInt(1, categoryId);
            try (ResultSet rs = checkPs.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new SQLException("Cannot delete category. There are " + rs.getInt(1) + 
                            " product(s) linked to this category. Delete or reassign those products first.");
                }
            }
        }

        String sql = "DELETE FROM CATEGORY WHERE category_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            return ps.executeUpdate() > 0;
        }
    }
}
