package com.order.model;

import java.math.BigDecimal;

public class Product {
    private int productId;
    private String name;
    private BigDecimal price;
    private int stockQty;
    private int categoryId;
    private String categoryName; // Helper for joins & UI tables

    public Product() {}

    public Product(int productId, String name, BigDecimal price, int stockQty, int categoryId) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.stockQty = stockQty;
        this.categoryId = categoryId;
    }

    public Product(int productId, String name, BigDecimal price, int stockQty, int categoryId, String categoryName) {
        this(productId, name, price, stockQty, categoryId);
        this.categoryName = categoryName;
    }

    public Product(String name, BigDecimal price, int stockQty, int categoryId) {
        this.name = name;
        this.price = price;
        this.stockQty = stockQty;
        this.categoryId = categoryId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getStockQty() {
        return stockQty;
    }

    public void setStockQty(int stockQty) {
        this.stockQty = stockQty;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    @Override
    public String toString() {
        return name + " (₹" + price + ", Stock: " + stockQty + ")";
    }
}
