Task 4: Order Management using Joins

Question: Create an order management system using a relational database. Design and create tables for Customers, Products, and Orders. Insert appropriate sample data into each table. Write SQL queries using JOIN operations to display the customer-wise order history. Use subqueries to identify the highest value order and the most active customer. Apply ORDER BY to sort the results appropriately.

Real-Time Application: This system is commonly used in e-commerce and retail applications to track customer purchases, analyze sales, and manage orders.

-- ===================================================== -- Task 4: Order Management using Joins -- =====================================================

-- 1. Create Database CREATE DATABASE order_management; USE order_management;

-- ===================================================== -- 2. Create Tables -- =====================================================

-- Customers table CREATE TABLE customers ( customer_id INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(50) );

-- Products table CREATE TABLE products ( product_id INT PRIMARY KEY AUTO_INCREMENT, product_name VARCHAR(50), price DECIMAL(10,2) );

-- Orders table CREATE TABLE orders ( order_id INT PRIMARY KEY AUTO_INCREMENT, customer_id INT, product_id INT, quantity INT, order_date DATE, FOREIGN KEY (customer_id) REFERENCES customers(customer_id), FOREIGN KEY (product_id) REFERENCES products(product_id) );

-- ===================================================== -- 3. Insert Sample Data -- =====================================================

-- Insert customers INSERT INTO customers (name) VALUES ('Ram'), ('Amit'), ('Sara');

-- Insert products INSERT INTO products (product_name, price) VALUES ('Laptop', 50000), ('Phone', 20000), ('Headphones', 3000);

-- Insert orders INSERT INTO orders (customer_id, product_id, quantity, order_date) VALUES (1, 1, 1, '2024-01-10'), (1, 3, 2, '2024-01-12'), (2, 2, 1, '2024-01-15'), (3, 2, 2, '2024-01-18'), (3, 3, 1, '2024-01-20');

-- ===================================================== -- 4. Display Customer Order History (JOIN) -- =====================================================

SELECT c.name AS customer_name, p.product_name, o.quantity, p.price, o.order_date FROM orders o JOIN customers c ON o.customer_id = c.customer_id JOIN products p ON o.product_id = p.product_id ORDER BY o.order_date;

-- ===================================================== -- 5. Highest Value Order (SUBQUERY) -- =====================================================

SELECT c.name AS customer_name, p.product_name, (o.quantity * p.price) AS total_value FROM orders o JOIN customers c ON o.customer_id = c.customer_id JOIN products p ON o.product_id = p.product_id WHERE (o.quantity * p.price) = ( SELECT MAX(o2.quantity * p2.price) FROM orders o2 JOIN products p2 ON o2.product_id = p2.product_id );

-- ===================================================== -- 6. Most Active Customer (SUBQUERY) -- =====================================================

SELECT name FROM customers WHERE customer_id = ( SELECT customer_id FROM orders GROUP BY customer_id ORDER BY COUNT(order_id) DESC LIMIT 1 );

-- ===================================================== -- END OF TASK 4 -- =====================================================
