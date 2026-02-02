-- ===================================================== -- Task 5: Transaction-Based Payment Simulation -- =====================================================

-- Description: -- This task simulates an online payment system using -- database transactions. The payment amount is deducted -- from the user account and added to the merchant account. -- COMMIT is used on success and ROLLBACK on failure.

-- ===================================================== -- 1. Create Database -- =====================================================

CREATE DATABASE payment_simulation; USE payment_simulation;

-- ===================================================== -- 2. Create Tables -- =====================================================

-- Users table (payer) CREATE TABLE users ( user_id INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(50), balance DECIMAL(10,2) );

-- Merchants table (receiver) CREATE TABLE merchants ( merchant_id INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(50), balance DECIMAL(10,2) );

-- ===================================================== -- 3. Insert Sample Data -- =====================================================

-- Insert users INSERT INTO users (name, balance) VALUES ('Ram', 5000), ('Amit', 3000);

-- Insert merchants INSERT INTO merchants (name, balance) VALUES ('Amazon', 10000), ('Flipkart', 8000);

-- ===================================================== -- 4. Successful Transaction (COMMIT) -- =====================================================

-- Ram pays 1000 to Amazon

START TRANSACTION;

-- Deduct amount from user UPDATE users SET balance = balance - 1000 WHERE user_id = 1 AND balance >= 1000;

-- Add amount to merchant UPDATE merchants SET balance = balance + 1000 WHERE merchant_id = 1;

-- Save changes COMMIT;

-- ===================================================== -- 5. Check Balances After COMMIT -- =====================================================

SELECT * FROM users; SELECT * FROM merchants;

-- ===================================================== -- 6. Failed Transaction (ROLLBACK) -- =====================================================

-- Amit tries to pay more than available balance

START TRANSACTION;

-- Deduct amount (will fail due to insufficient balance) UPDATE users SET balance = balance - 10000 WHERE user_id = 2 AND balance >= 10000;

-- Add amount to merchant UPDATE merchants SET balance = balance + 10000 WHERE merchant_id = 2;

-- Revert changes ROLLBACK;

-- ===================================================== -- 7. Check Balances After ROLLBACK -- =====================================================

SELECT * FROM users; SELECT * FROM merchants;

-- ===================================================== -- END OF TASK 5 -- =====================================================
