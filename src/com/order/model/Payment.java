package com.order.model;

import java.math.BigDecimal;
import java.sql.Date;

public class Payment {
    private int paymentId;
    private int orderId;
    private Date paymentDate;
    private BigDecimal amount;
    private String method;
    private String status;

    public Payment() {}

    public Payment(int paymentId, int orderId, Date paymentDate, BigDecimal amount, String method, String status) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.paymentDate = paymentDate;
        this.amount = amount;
        this.method = method;
        this.status = status;
    }

    public Payment(int orderId, Date paymentDate, BigDecimal amount, String method, String status) {
        this.orderId = orderId;
        this.paymentDate = paymentDate;
        this.amount = amount;
        this.method = method;
        this.status = status;
    }

    public int getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public Date getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(Date paymentDate) {
        this.paymentDate = paymentDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Payment #" + paymentId + " (Order #" + orderId + ", ₹" + amount + ", " + status + ")";
    }
}
