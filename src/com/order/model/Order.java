package com.order.model;

import java.math.BigDecimal;
import java.sql.Date;

public class Order {
    private int orderId;
    private Date orderDate;
    private String status;
    private BigDecimal totalAmount;
    private int customerId;
    private String customerName; // Helper for joins & UI tables

    public Order() {}

    public Order(int orderId, Date orderDate, String status, BigDecimal totalAmount, int customerId) {
        this.orderId = orderId;
        this.orderDate = orderDate;
        this.status = status;
        this.totalAmount = totalAmount;
        this.customerId = customerId;
    }

    public Order(int orderId, Date orderDate, String status, BigDecimal totalAmount, int customerId, String customerName) {
        this(orderId, orderDate, status, totalAmount, customerId);
        this.customerName = customerName;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    @Override
    public String toString() {
        return "Order #" + orderId + " - " + (customerName != null ? customerName : "Cust #" + customerId) + " (₹" + totalAmount + ")";
    }
}
