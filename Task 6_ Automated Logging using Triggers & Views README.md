-- ===================================================== -- Task 6: Automated Logging using Triggers & Views -- =====================================================

-- Description: -- This task demonstrates automated audit logging using -- database TRIGGERS and reporting using VIEWS. -- Every INSERT and UPDATE operation is logged automatically, -- and a daily activity report is generated using a VIEW.

-- ===================================================== -- 1. Create Database -- =====================================================

CREATE DATABASE audit_logging; USE audit_logging;

-- ===================================================== -- 2. Create Tables -- =====================================================

-- Main table where operations occur CREATE TABLE main_table ( id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(50), amount DECIMAL(10,2), created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP );

-- Audit log table to store logs CREATE TABLE audit_log ( log_id INT AUTO_INCREMENT PRIMARY KEY, action_type VARCHAR(10), record_id INT, action_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP );

-- ===================================================== -- 3. Create Triggers -- =====================================================

DELIMITER $$

-- Trigger to log INSERT operations CREATE TRIGGER log_after_insert AFTER INSERT ON main_table FOR EACH ROW BEGIN INSERT INTO audit_log (action_type, record_id) VALUES ('INSERT', NEW.id); END$$

-- Trigger to log UPDATE operations CREATE TRIGGER log_after_update AFTER UPDATE ON main_table FOR EACH ROW BEGIN INSERT INTO audit_log (action_type, record_id) VALUES ('UPDATE', NEW.id); END$$

DELIMITER ;

-- ===================================================== -- 4. Test Trigger Functionality -- =====================================================

-- Insert data into main table INSERT INTO main_table (name, amount) VALUES ('Order1', 1500);

-- Update data in main table UPDATE main_table SET amount = 2000 WHERE id = 1;

-- View audit log entries SELECT * FROM audit_log;

-- ===================================================== -- 5. Create View for Daily Activity Report -- =====================================================

CREATE VIEW daily_activity_report AS SELECT DATE(action_time) AS activity_date, action_type, COUNT(*) AS total_actions FROM audit_log GROUP BY DATE(action_time), action_type;

-- ===================================================== -- 6. View Daily Activity Report -- =====================================================

SELECT * FROM daily_activity_report;

-- ===================================================== -- END OF TASK 6 -- =====================================================

