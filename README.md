# Online Order System

> **College SLA Microproject (Database System Concepts)**  
> **Institute:** Shah & Anchor Kutchchi Engineering College (Autonomous Institute Affiliated to University of Mumbai)  
> **Class:** SYIT1  
> **Project Team:**
> - **Sonali Makwana** (Roll No: 66)
> - **Shravani Nakhwa** (Roll No: 69)

---

## 📖 Project Overview

The **Online Order System** is a full-featured desktop application built using **Java Swing** for the frontend graphical user interface (GUI) and **MySQL 8.0** for the relational database management system. 

It implements the end-to-end lifecycle of an e-commerce order management system:
- Customer registration and management
- Category classification
- Product catalog with live stock tracking
- Multi-item order creation with transactional checkout and atomic commit/rollback
- Order item breakdown and line calculations
- Payment processing and status audits
- Analytics dashboard with real-time KPI metrics and low-stock alerts

---

## 🗄️ Relational Database Architecture

The system utilizes **6 interconnected relational tables** ensuring strict data integrity via Primary Keys, Foreign Keys, `NOT NULL`, `UNIQUE`, and `CHECK` constraints:

```
[CUSTOMER] 1 ───────────< ∞ [ORDERS] 1 ─────────── 1 [PAYMENTS]
                                │
                                │ 1
                                │
                                ∨ ∞
                          [ORDER_ITEMS]
                                ∧ ∞
                                │
                                │ 1
[CATEGORY] 1 ───────────< ∞ [PRODUCT]
```

### Table Specifications & Constraints:

1. **`CUSTOMER`**
   - `customer_id` (INT, PRIMARY KEY, AUTO_INCREMENT)
   - `name` (VARCHAR(50), NOT NULL)
   - `email` (VARCHAR(60), NOT NULL, UNIQUE)
   - `phone` (VARCHAR(15))
   - `address` (VARCHAR(100))

2. **`CATEGORY`**
   - `category_id` (INT, PRIMARY KEY, AUTO_INCREMENT)
   - `category_name` (VARCHAR(40), NOT NULL, UNIQUE)

3. **`PRODUCT`**
   - `product_id` (INT, PRIMARY KEY, AUTO_INCREMENT)
   - `name` (VARCHAR(60), NOT NULL)
   - `price` (DECIMAL(10,2), NOT NULL, `CHECK (price > 0)`)
   - `stock_qty` (INT, NOT NULL, DEFAULT 0, `CHECK (stock_qty >= 0)`)
   - `category_id` (INT, NOT NULL, FOREIGN KEY -> `CATEGORY(category_id)`)

4. **`ORDERS`**
   - `order_id` (INT, PRIMARY KEY, AUTO_INCREMENT)
   - `order_date` (DATE, NOT NULL)
   - `status` (VARCHAR(15), NOT NULL, DEFAULT 'Placed', `CHECK (status IN ('Placed', 'Processing', 'Shipped', 'Delivered', 'Cancelled'))`)
   - `total_amount` (DECIMAL(10,2), NOT NULL, DEFAULT 0.00, `CHECK (total_amount >= 0)`)
   - `customer_id` (INT, NOT NULL, FOREIGN KEY -> `CUSTOMER(customer_id)`)

5. **`ORDER_ITEMS`**
   - `(order_id, product_id)` (Composite PRIMARY KEY)
   - `quantity` (INT, NOT NULL, `CHECK (quantity > 0)`)
   - `unit_price` (DECIMAL(10,2), NOT NULL, `CHECK (unit_price > 0)`)
   - Foreign Keys: `order_id` -> `ORDERS(order_id)` (ON DELETE CASCADE), `product_id` -> `PRODUCT(product_id)`

6. **`PAYMENTS`**
   - `payment_id` (INT, PRIMARY KEY, AUTO_INCREMENT)
   - `order_id` (INT, NOT NULL, UNIQUE, FOREIGN KEY -> `ORDERS(order_id)`)
   - `payment_date` (DATE, NOT NULL)
   - `amount` (DECIMAL(10,2), NOT NULL, `CHECK (amount > 0)`)
   - `method` (VARCHAR(20), NOT NULL, `CHECK (method IN ('UPI', 'Credit Card', 'Debit Card', 'Net Banking', 'Cash on Delivery', 'Cash'))`)
   - `status` (VARCHAR(15), NOT NULL, `CHECK (status IN ('Paid', 'Pending', 'Failed', 'Refunded'))`)

---

## 🛡️ Data Integrity & Transactions

- **Prepared Statements**: All SQL queries utilize parameterized `PreparedStatement` to prevent SQL Injection and ensure type safety.
- **ACID Transactions**:
  - Creating an order runs within an atomic transaction (`conn.setAutoCommit(false)`): checks stock, decrements inventory, inserts order, inserts line items, inserts payment record, and commits.
  - If any item fails (e.g., insufficient stock), the transaction **automatically rolls back**, ensuring no corrupt data is left behind.
- **Cascade & Child-First Deletion**: Child rows are handled cleanly to prevent foreign key constraint violations.
- **Secure Credentials**: Sensitive database passwords are kept in `config/db.properties` and excluded from source control via `.gitignore`.

---

## 🚀 How to Run the Project

### Prerequisites
1. **Java JDK 17 or higher** installed (`java -version`, `javac -version`).
2. **MySQL Server 8.0** installed and running on port 3306.

---

### Step 1: Set Up Database

#### Option A: One-Click Setup
Double-click `setup_database.bat` and enter your MySQL root credentials.

#### Option B: MySQL Workbench
1. Open MySQL Workbench.
2. Open `sql/schema.sql` (`File` -> `Open SQL Script...`).
3. Click the lightning bolt icon ⚡ to execute the script.

---

### Step 2: Configure Database Password
Ensure `config/db.properties` matches your local MySQL password:
```properties
db.url=jdbc:mysql://localhost:3306/online_order_system?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.user=root
db.password=YOUR_PASSWORD
```

---

### Step 3: Run the Application

#### Option A: Quick Launch (JAR)
Double-click `run.bat` or run in terminal:
```bash
java -jar online-order-system.jar
```

#### Option B: Rebuild & Package
Double-click `build.bat` to recompile all sources and regenerate `online-order-system.jar`.

---

## 💻 Running on Another Computer

To run this project on your lab computer or college laptop:
1. Copy or clone the project folder (`online-order-system`).
2. Run `sql/schema.sql` on the MySQL Server of that computer.
3. Edit `config/db.properties` with that computer's MySQL root password.
4. Double-click `run.bat`!
