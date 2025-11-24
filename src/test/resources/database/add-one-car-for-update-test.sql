DELETE FROM rentals WHERE car_id = 1;

DELETE FROM cars WHERE id = 1;

INSERT INTO cars (id, model, brand, type, inventory, daily_fee, is_deleted)
VALUES (1, 'Tesla Model S', 'Tesla', 'SEDAN', 5, 150.00, 0);

