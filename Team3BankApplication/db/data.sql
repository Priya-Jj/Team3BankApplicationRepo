INSERT INTO customers (customer_number, full_name, email, created_date)
VALUES ('487-978494', 'Carla Romero', 'carla.romero@example.com', SYSDATE);
INSERT INTO accounts (account_number, customer_id, account_type, account_status, balance, opened_date)
SELECT '6', c.customer_id, 'CHECKING', 'ACTIVE', 9000.50, SYSDATE
FROM customers c WHERE c.customer_number = '500-100200';
COMMIT ;