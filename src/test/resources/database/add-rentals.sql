DELETE FROM rentals;
DELETE FROM cars;
DELETE FROM users;

ALTER TABLE rentals AUTO_INCREMENT = 1;
ALTER TABLE cars AUTO_INCREMENT = 1;
ALTER TABLE users AUTO_INCREMENT = 1;

INSERT INTO users (id, email, first_name, last_name, password, role, is_deleted)
VALUES
(1, 'customer@example.com', 'John', 'Doe', 'password', 'ROLE_CUSTOMER', 0),
(2, 'manager@example.com', 'Jane', 'Smith', 'password', 'ROLE_MANAGER', 0);

INSERT INTO cars (id, model, brand, type, inventory, daily_fee, is_deleted)
VALUES
(1, 'Model S', 'Tesla', 'SEDAN', 5, 150.00, 0),
(2, 'Mustang', 'Ford', 'SUV', 3, 120.00, 0);

INSERT INTO rentals (id, car_id, user_id, rental_date, return_date, active)
VALUES
(1, 1, 1, '2025-11-12', '2025-11-15', 1);
