-- =====================================================================
-- College SLA Microproject: Online Order System
-- Authors: Sonali Makwana (Roll No: 66) & Shravani Nakhwa (Roll No: 69)
-- Class: SYIT1
-- College: Shah & Anchor Kutchchi Engineering College
-- Database: MySQL 8.0
-- =====================================================================

-- Step 1: Create Database
CREATE DATABASE IF NOT EXISTS online_order_system
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE online_order_system;

-- Step 2: Drop Tables / Views in Child-First Order to Prevent FK Violations
DROP VIEW IF EXISTS PAYMENT;
DROP VIEW IF EXISTS ORDER_ITEM;
DROP TABLE IF EXISTS PAYMENTS;
DROP TABLE IF EXISTS ORDER_ITEMS;
DROP TABLE IF EXISTS ORDERS;
DROP TABLE IF EXISTS PRODUCT;
DROP TABLE IF EXISTS CATEGORY;
DROP TABLE IF EXISTS CUSTOMER;

-- =====================================================================
-- Table 1: CUSTOMER
-- =====================================================================
CREATE TABLE CUSTOMER (
    customer_id INT PRIMARY KEY AUTO_INCREMENT,
    name        VARCHAR(50) NOT NULL,
    email       VARCHAR(60) NOT NULL UNIQUE,
    phone       VARCHAR(15),
    address     VARCHAR(100)
);

-- =====================================================================
-- Table 2: CATEGORY
-- =====================================================================
CREATE TABLE CATEGORY (
    category_id   INT PRIMARY KEY AUTO_INCREMENT,
    category_name VARCHAR(40) NOT NULL UNIQUE
);

-- =====================================================================
-- Table 3: PRODUCT
-- =====================================================================
CREATE TABLE PRODUCT (
    product_id  INT PRIMARY KEY AUTO_INCREMENT,
    name        VARCHAR(60) NOT NULL,
    price       DECIMAL(10,2) NOT NULL CHECK (price > 0),
    stock_qty   INT NOT NULL DEFAULT 0 CHECK (stock_qty >= 0),
    category_id INT NOT NULL,
    CONSTRAINT fk_product_category 
        FOREIGN KEY (category_id) REFERENCES CATEGORY(category_id) 
        ON UPDATE CASCADE ON DELETE RESTRICT
);

-- =====================================================================
-- Table 4: ORDERS
-- =====================================================================
CREATE TABLE ORDERS (
    order_id     INT PRIMARY KEY AUTO_INCREMENT,
    order_date   DATE NOT NULL,
    status       VARCHAR(15) NOT NULL DEFAULT 'Placed' 
                 CHECK (status IN ('Placed', 'Processing', 'Shipped', 'Delivered', 'Cancelled')),
    total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 CHECK (total_amount >= 0),
    customer_id  INT NOT NULL,
    CONSTRAINT fk_orders_customer 
        FOREIGN KEY (customer_id) REFERENCES CUSTOMER(customer_id) 
        ON UPDATE CASCADE ON DELETE RESTRICT
);

-- =====================================================================
-- Table 5: ORDER_ITEMS (Composite Primary Key: order_id + product_id)
-- =====================================================================
CREATE TABLE ORDER_ITEMS (
    order_id   INT NOT NULL,
    product_id INT NOT NULL,
    quantity   INT NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(10,2) NOT NULL CHECK (unit_price > 0),
    PRIMARY KEY (order_id, product_id),
    CONSTRAINT fk_order_items_orders 
        FOREIGN KEY (order_id) REFERENCES ORDERS(order_id) 
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_order_items_product 
        FOREIGN KEY (product_id) REFERENCES PRODUCT(product_id) 
        ON UPDATE CASCADE ON DELETE RESTRICT
);

-- =====================================================================
-- Table 6: PAYMENTS
-- =====================================================================
CREATE TABLE PAYMENTS (
    payment_id   INT PRIMARY KEY AUTO_INCREMENT,
    order_id     INT NOT NULL UNIQUE,
    payment_date DATE NOT NULL,
    amount       DECIMAL(10,2) NOT NULL CHECK (amount > 0),
    method       VARCHAR(20) NOT NULL 
                 CHECK (method IN ('UPI', 'Credit Card', 'Debit Card', 'Net Banking', 'Cash on Delivery', 'Cash')),
    status       VARCHAR(15) NOT NULL 
                 CHECK (status IN ('Paid', 'Pending', 'Failed', 'Refunded')),
    CONSTRAINT fk_payments_orders 
        FOREIGN KEY (order_id) REFERENCES ORDERS(order_id) 
        ON UPDATE CASCADE ON DELETE CASCADE
);

-- Compatibility views for Stage 2 naming (ORDER_ITEM and PAYMENT)
CREATE OR REPLACE VIEW ORDER_ITEM AS SELECT * FROM ORDER_ITEMS;
CREATE OR REPLACE VIEW PAYMENT AS SELECT * FROM PAYMENTS;

-- =====================================================================
-- Sample Data Insertion (Parent tables first, then child tables)
-- =====================================================================

-- 1. CUSTOMER (8 records)
INSERT INTO CUSTOMER (customer_id, name, email, phone, address) VALUES
(1, 'Aarav Sharma',  'aarav@mail.com',  '9820011001', 'Ambarnath, Maharashtra'),
(2, 'Priya Patil',   'priya@mail.com',  '9820011002', 'Kalyan, Maharashtra'),
(3, 'Rohan Desai',   'rohan@mail.com',  '9820011003', 'Thane, Maharashtra'),
(4, 'Sneha Joshi',   'sneha@mail.com',  '9820011004', 'Dombivli, Maharashtra'),
(5, 'Vikram Singh',  'vikram@mail.com', '9820011005', 'Badlapur, Maharashtra'),
(6, 'Neha Kulkarni', 'neha@mail.com',   '9820011006', 'Pune, Maharashtra'),
(7, 'Aditya Rao',    'aditya@mail.com', '9820011007', 'Mumbai, Maharashtra'),
(8, 'Isha Mehta',    'isha@mail.com',   '9820011008', 'Navi Mumbai, Maharashtra');

-- 2. CATEGORY (5 records)
INSERT INTO CATEGORY (category_id, category_name) VALUES
(1, 'Electronics'),
(2, 'Books'),
(3, 'Clothing'),
(4, 'Home & Kitchen'),
(5, 'Sports');

-- 3. PRODUCT (10 records)
INSERT INTO PRODUCT (product_id, name, price, stock_qty, category_id) VALUES
(101, 'Wireless Mouse',           599.00,  50, 1),
(102, 'Bluetooth Headphones',    1999.00,  30, 1),
(103, 'USB-C Charger',            799.00,  40, 1),
(104, 'SQL Made Easy (Book)',     450.00,  25, 2),
(105, 'Data Structures Textbook', 650.00,  20, 2),
(106, 'Cotton T-Shirt',           399.00, 100, 3),
(107, 'Denim Jeans',             1299.00,  60, 3),
(108, 'Steel Water Bottle',       350.00,  80, 4),
(109, 'Non-stick Pan',            899.00,  35, 4),
(110, 'Cricket Bat',             1499.00,  15, 5);

-- 4. ORDERS (8 records)
INSERT INTO ORDERS (order_id, order_date, status, total_amount, customer_id) VALUES
(1001, '2026-10-01', 'Delivered', 1198.00, 1),
(1002, '2026-10-03', 'Delivered', 1999.00, 2),
(1003, '2026-10-05', 'Shipped',   1249.00, 3),
(1004, '2026-10-08', 'Delivered', 2198.00, 4),
(1005, '2026-10-10', 'Placed',    1499.00, 5),
(1006, '2026-10-12', 'Shipped',   1348.00, 1),
(1007, '2026-10-15', 'Cancelled',  399.00, 6),
(1008, '2026-10-18', 'Placed',    2649.00, 7);

-- 5. ORDER_ITEMS (13 records)
INSERT INTO ORDER_ITEMS (order_id, product_id, quantity, unit_price) VALUES
(1001, 101, 2,  599.00),
(1002, 102, 1, 1999.00),
(1003, 104, 1,  450.00),
(1003, 103, 1,  799.00),
(1004, 107, 1, 1299.00),
(1004, 109, 1,  899.00),
(1005, 110, 1, 1499.00),
(1006, 108, 1,  350.00),
(1006, 101, 1,  599.00),
(1006, 106, 1,  399.00),
(1007, 106, 1,  399.00),
(1008, 105, 1,  650.00),
(1008, 102, 1, 1999.00);

-- 6. PAYMENTS (8 records)
INSERT INTO PAYMENTS (payment_id, order_id, payment_date, amount, method, status) VALUES
(1, 1001, '2026-10-01', 1198.00, 'UPI',              'Paid'),
(2, 1002, '2026-10-03', 1999.00, 'Credit Card',      'Paid'),
(3, 1003, '2026-10-05', 1249.00, 'Net Banking',      'Paid'),
(4, 1004, '2026-10-08', 2198.00, 'UPI',              'Paid'),
(5, 1005, '2026-10-10', 1499.00, 'Cash on Delivery', 'Pending'),
(6, 1006, '2026-10-12', 1348.00, 'Debit Card',       'Paid'),
(7, 1007, '2026-10-15',  399.00, 'UPI',              'Refunded'),
(8, 1008, '2026-10-18', 2649.00, 'Credit Card',      'Paid');

-- Adjust auto_increment sequence counters
ALTER TABLE CUSTOMER AUTO_INCREMENT = 9;
ALTER TABLE CATEGORY AUTO_INCREMENT = 6;
ALTER TABLE PRODUCT AUTO_INCREMENT = 111;
ALTER TABLE ORDERS AUTO_INCREMENT = 1009;
ALTER TABLE PAYMENTS AUTO_INCREMENT = 9;
