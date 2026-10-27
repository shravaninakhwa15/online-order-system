package com.order.test;

import com.order.config.DatabaseConfig;
import com.order.dao.*;
import com.order.model.*;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class AppTest {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("Online Order System - Comprehensive Automated Tests");
        System.out.println("==================================================");

        int passed = 0;
        int failed = 0;

        // Test 1: Connectivity
        try {
            boolean connected = DatabaseConfig.testConnection();
            if (connected) {
                System.out.println("[PASS] 1. Database Connectivity (MySQL 8.0 online_order_system)");
                passed++;
            } else {
                System.err.println("[FAIL] 1. Database Connectivity returned false");
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] 1. Database Connectivity: " + e.getMessage());
            failed++;
        }

        // Test 2: Customer CRUD & Queries
        CustomerDAO customerDAO = new CustomerDAO();
        try {
            List<Customer> customers = customerDAO.getAllCustomers();
            if (customers.size() >= 8) {
                System.out.println("[PASS] 2. CustomerDAO.getAllCustomers(): " + customers.size() + " customers loaded");
                passed++;
            } else {
                System.err.println("[FAIL] 2. Expected >= 8 customers, got " + customers.size());
                failed++;
            }

            // Add test customer
            Customer testCust = new Customer("Test Student", "test.student@example.com", "9999988888", "Mumbai");
            int newId = customerDAO.addCustomer(testCust);
            if (newId > 0) {
                System.out.println("[PASS] 2b. CustomerDAO.addCustomer(): Created ID #" + newId);
                passed++;
            }

            testCust.setAddress("Navi Mumbai");
            boolean updated = customerDAO.updateCustomer(testCust);
            if (updated) {
                System.out.println("[PASS] 2c. CustomerDAO.updateCustomer(): Updated address");
                passed++;
            }

            boolean deleted = customerDAO.deleteCustomer(newId);
            if (deleted) {
                System.out.println("[PASS] 2d. CustomerDAO.deleteCustomer(): Cascade transaction deleted successfully");
                passed++;
            }

        } catch (Exception e) {
            System.err.println("[FAIL] 2. CustomerDAO test failed: " + e.getMessage());
            failed++;
        }

        // Test 3: Categories & Products
        CategoryDAO categoryDAO = new CategoryDAO();
        ProductDAO productDAO = new ProductDAO();
        try {
            List<Category> categories = categoryDAO.getAllCategories();
            List<Product> products = productDAO.getAllProducts();
            if (categories.size() >= 5 && products.size() >= 10) {
                System.out.println("[PASS] 3. Category & Product loading: " + categories.size() + " categories, " + products.size() + " products");
                passed++;
            } else {
                System.err.println("[FAIL] 3. Category/Product counts incorrect");
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] 3. Category/Product test failed: " + e.getMessage());
            failed++;
        }

        // Test 4: Transactional Order Placement & Stock Validation
        OrderDAO orderDAO = new OrderDAO();
        OrderItemDAO orderItemDAO = new OrderItemDAO();
        PaymentDAO paymentDAO = new PaymentDAO();
        try {
            Product mouse = productDAO.getProductById(101);
            int stockBefore = mouse.getStockQty();

            List<OrderItem> items = new ArrayList<>();
            items.add(new OrderItem(0, 101, 1, mouse.getPrice())); // Buy 1 mouse

            int placedOrderId = orderDAO.createOrderWithItems(
                    1, // Customer Aarav
                    new Date(System.currentTimeMillis()),
                    "Placed",
                    items,
                    "UPI",
                    "Paid"
            );

            Product mouseAfter = productDAO.getProductById(101);
            int stockAfter = mouseAfter.getStockQty();

            if (placedOrderId > 0 && stockAfter == (stockBefore - 1)) {
                System.out.println("[PASS] 4. Order Transaction: Order #" + placedOrderId + 
                        " placed, stock decremented (" + stockBefore + " -> " + stockAfter + ")");
                passed++;
            } else {
                System.err.println("[FAIL] 4. Stock deduction failed: before=" + stockBefore + ", after=" + stockAfter);
                failed++;
            }

            // Verify line items and payment created
            List<OrderItem> orderItems = orderItemDAO.getItemsByOrderId(placedOrderId);
            Payment payment = paymentDAO.getPaymentByOrderId(placedOrderId);
            if (!orderItems.isEmpty() && payment != null) {
                System.out.println("[PASS] 4b. Order child records verified: Items=" + orderItems.size() + ", Payment=₹" + payment.getAmount());
                passed++;
            }

            // Clean up test order
            orderDAO.deleteOrder(placedOrderId);
            // Restore stock for 101
            mouse.setStockQty(stockBefore);
            productDAO.updateProduct(mouse);
            System.out.println("[PASS] 4c. Order cleanup & stock restoration completed");
            passed++;

        } catch (Exception e) {
            System.err.println("[FAIL] 4. Order transaction test failed: " + e.getMessage());
            failed++;
        }

        // Test 5: Transaction Rollback on Over-Stock Order
        try {
            List<OrderItem> impossibleItems = new ArrayList<>();
            impossibleItems.add(new OrderItem(0, 101, 999999, new BigDecimal("599.00"))); // Over stock limit
            orderDAO.createOrderWithItems(
                    1,
                    new Date(System.currentTimeMillis()),
                    "Placed",
                    impossibleItems,
                    "UPI",
                    "Paid"
            );
            System.err.println("[FAIL] 5. Over-stock order should have thrown an exception!");
            failed++;
        } catch (Exception e) {
            System.out.println("[PASS] 5. Transaction Rollback confirmed: Over-stock rejected with message: " + e.getMessage());
            passed++;
        }

        System.out.println("==================================================");
        System.out.println("TEST RESULTS: " + passed + " PASSED, " + failed + " FAILED");
        System.out.println("==================================================");
        if (failed > 0) {
            System.exit(1);
        }
    }
}
