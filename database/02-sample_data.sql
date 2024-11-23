
/* if problem with sequence you can add this:
ALTER SEQUENCE "group_group_id_seq" RESTART WITH 1;
ALTER SEQUENCE users_user_id_seq RESTART WITH 1;
ALTER SEQUENCE category_category_id_seq RESTART WITH 1;
ALTER SEQUENCE characteristic_characteristic_id_seq RESTART WITH 1;
ALTER SEQUENCE supplier_supplier_id_seq RESTART WITH 1;
ALTER SEQUENCE instruments_instrument_id_seq RESTART WITH 1;
*/


-- Insert into "group" table
INSERT INTO "group" (group_name) VALUES 
('Scalpels'), 
('Scissors'), 
('Forceps');

-- Insert into users table
INSERT INTO users (username, email, password, role_name, job_position, workplace) VALUES 
('john_doe', 'john@example.com', 'hashed_password_123', 'user', 'Surgeon', 'City Hospital'),
('jane_admin', 'jane@example.com', 'hashed_password_456', 'admin', 'IT Manager', 'Head Office');

-- Insert into logs table
INSERT INTO logs (user_id, action) VALUES 
(1, 'Logged in'),
(2, 'Changed settings'),
(1, 'Added new instrument');

-- Insert into supplier table
INSERT INTO supplier (supplier_name, sold_by_md, closed) VALUES 
('Medicon', TRUE, FALSE),
('Geomed', TRUE, FALSE);

-- Insert into category table
INSERT INTO category (group_id, shape) VALUES 
(1, NULL), 
(1, NULL), 
(3, NULL);

-- Insert into characteristic table
INSERT INTO characteristic (characteristic_name) VALUES 
('Length'), 
('Material'), 
('Sharpness');

-- Insert into category_characteristic table
INSERT INTO category_characteristic (category_id, characteristic_id, value, value_abreviation) 
VALUES
(1, 1, 'Large', 'L'),      -- volume, avec "L" comme abrégé
(1, 2, 'Curved', 'C'),      -- curve, avec "C" comme abrégé
(2, 1, '10cm', '10CM');

-- Insert into instruments table
INSERT INTO instruments (supplier_id, category_id, reference, supplier_description, price, obsolete) VALUES 
(1, 1, 'SC-1234', '15cm Scalpel', 15.99, FALSE),
(2, 2, 'SC-5678', '15cm Scalpel de geomed', 10.50, FALSE),
(2, 3, 'FR-0001', 'Forceps with lock', 20.75, TRUE);

-- Insert into group_characteristic table
INSERT INTO group_characteristic (group_id, characteristic_id, order_position) VALUES 
(1, 1, 1), 
(1, 2, 2), 
(2, 1, 1);

-- Insert into alternatives table
INSERT INTO alternatives (instruments_id_1, instruments_id_2) VALUES 
(1, 2);

-- Insert into orders table
INSERT INTO orders (user_id) VALUES 
(1), 
(2);

-- Insert into order_items table
INSERT INTO order_items (order_id, instrument_id, quantity) VALUES 
(1, 1, 5), 
(2, 2, 3), 
(2, 3, 2);
