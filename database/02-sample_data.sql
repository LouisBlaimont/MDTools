
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

/* Insert into sub_group table */
INSERT INTO sub_group (sub_group_name, group_id) VALUES
('Plastic Scalpels', 1),
('Metal Scalpels', 1),
('Curved Scissors', 2),
('Straight Scissors', 2),
('Locking Forceps', 3),
('Non-Locking Forceps', 3);

-- Insert into users table
INSERT INTO users (username, email, password_fingerprint, role_name, job_position, workplace) VALUES 
('john_doe', 'john@example.com', '$2y$10$umwMQdPProLsXUBJWLSLaeGMt3WjM4Sp6C6Unxh38YZOsEnSzzWOm', 'ROLE_USER', 'Surgeon', 'City Hospital'),
('jane_admin', 'jane@example.com', 'hashed_password_456', 'ROLE_ADMIN', 'IT Manager', 'Head Office');

-- Insert into logs table
INSERT INTO logs (user_id, action) VALUES 
(1, 'Logged in'),
(2, 'Changed settings'),
(1, 'Added new instrument');

-- Insert into supplier table
INSERT INTO supplier (supplier_name, sold_by_md, closed) VALUES 
('Medicon', TRUE, FALSE),
('Geomed', TRUE, FALSE);

/* Add more categories linked to each sub_group */
-- Categories for "Plastic Scalpels" (sub_group_id = 1)
INSERT INTO category (sub_group_id) VALUES
(1), -- Category 1 for Plastic Scalpels
(1), -- Category 2 for Plastic Scalpels
(1); -- Category 3 for Plastic Scalpels

-- Categories for "Metal Scalpels" (sub_group_id = 2)
INSERT INTO category (sub_group_id) VALUES
(2), -- Category 1 for Metal Scalpels
(2), -- Category 2 for Metal Scalpels
(2); -- Category 3 for Metal Scalpels

-- Categories for "Curved Scissors" (sub_group_id = 3)
INSERT INTO category (sub_group_id) VALUES
(3), -- Category 1 for Curved Scissors
(3), -- Category 2 for Curved Scissors
(3); -- Category 3 for Curved Scissors

-- Categories for "Straight Scissors" (sub_group_id = 4)
INSERT INTO category (sub_group_id) VALUES
(4), -- Category 1 for Straight Scissors
(4), -- Category 2 for Straight Scissors
(4); -- Category 3 for Straight Scissors

-- Categories for "Locking Forceps" (sub_group_id = 5)
INSERT INTO category (sub_group_id) VALUES
(5), -- Category 1 for Locking Forceps
(5), -- Category 2 for Locking Forceps
(5); -- Category 3 for Locking Forceps

-- Categories for "Non-Locking Forceps" (sub_group_id = 6)
INSERT INTO category (sub_group_id) VALUES
(6), -- Category 1 for Non-Locking Forceps
(6), -- Category 2 for Non-Locking Forceps
(6); -- Category 3 for Non-Locking Forceps


-- Insert into characteristic table
-- Insert into characteristic table
INSERT INTO characteristic (characteristic_name) VALUES 
('Length'), 
('Material'), 
('Sharpness'), 
('Flexibility'), 
('Grip Type'),
('Function'), 
('Name');

-- Link characteristics to sub_group
INSERT INTO sub_group_characteristic (sub_group_id, characteristic_id, order_position) VALUES
-- Plastic Scalpels group
(1, 1, NULL), -- Length
(1, 2, 2), -- Material
(1, 3, 3), -- Sharpness
(1, 6, NULL), -- Function
(1, 7, NULL), -- Name

-- Metal Scalpels group
(2, 1, NULL), -- Length
(2, 2, 2), -- Material
(2, 3, 3), -- Sharpness
(2, 6, NULL), -- Function
(2, 7, NULL), -- Name

-- straight Scissors group
(3, 1, NULL), -- Length
(3, 4, 2), -- Flexibility
(3, 6, NULL), -- Function
(3, 7, NULL), -- Name

-- Cureved Scissors group
(4, 1, NULL), -- Length
(4, 4, 2), -- Flexibility
(4, 6, NULL), -- Function
(4, 7, NULL), -- Name

-- locking Forceps group
(5, 1, NULL), -- Length
(5, 5, 2), -- Grip Type
(5, 6, NULL), -- Function
(5, 7, NULL), -- Name

-- non-locking Forceps group
(6, 1, NULL), -- Length
(6, 5, 2), -- Grip Type
(6, 6, NULL), -- Function
(6, 7, NULL); -- Name

-- Add characteristic values for Plastic Scalpels (sub_group_id = 1)
INSERT INTO category_characteristic (category_id, characteristic_id, characteristic_value, value_abreviation) VALUES
(1, 1, '10cm', '10CM'), -- Length
(1, 2, 'Plastic', 'PL'), -- Material
(1, 3, 'Sharp', 'SH'), -- Sharpness
(1, 6, 'Cutting', 'CT'), -- Function
(1, 7, 'Kelly', 'kel'), -- Name
(2, 1, '15cm', '15CM'), -- Length
(2, 2, 'Plastic', 'PL'), -- Material
(2, 3, 'Very Sharp', 'VSH'), -- Sharpness
(2, 6, 'Cutting', 'CT'), -- Function
(2, 7, 'Kelly', 'kel'), -- Name
(3, 1, '20cm', '20CM'), -- Length
(3, 2, 'Plastic', 'PL'), -- Material
(3, 6, 'Cutting', 'CT'), -- Function
(3, 7, 'Kelly', 'kel'); -- Name

-- Add characteristic values for Metal Scalpels (sub_group_id = 2)
INSERT INTO category_characteristic (category_id, characteristic_id, characteristic_value, value_abreviation) VALUES
(4, 1, '12cm', '12CM'), -- Length
(4, 2, 'Steel', 'ST'), -- Material
(4, 3, 'Sharp', 'SH'), -- Sharpness
(4, 6, 'Cutting', 'CT'), -- Function
(4, 7, 'Kelly', 'kel'), -- Name
(5, 1, '18cm', '18CM'), -- Length
(5, 2, 'Stainless Steel', 'SS'), -- Material
(5, 3, 'Very Sharp', 'VSH'), -- Sharpness
(5, 6, 'Cutting', 'CT'), -- Function
(5, 7, 'Kelly', 'kel'), -- Name
(6, 1, '22cm', '22CM'), -- Length
(6, 2, 'Steel', 'ST'), -- Material
(6, 6, 'Cutting', 'CT'), -- Function
(6, 7, 'Kelly', 'kel'); -- Name

-- Add characteristic values for Curved Scissors (sub_group_id = 3)
INSERT INTO category_characteristic (category_id, characteristic_id, characteristic_value, value_abreviation) VALUES
(7, 1, '14cm', '14CM'), -- Length
(7, 4, 'Flexible', 'FLX'), -- Flexibility
(7, 6, 'Cutting', 'CT'), -- Function
(7, 7, 'Kelly', 'kel'), -- Name
(8, 1, '16cm', '16CM'), -- Length
(8, 4, 'Semi-Flexible', 'SFLX'), -- Flexibility
(8, 6, 'Cutting', 'CT'), -- Function
(8, 7, 'Kelly', 'kel'), -- Name
(9, 1, '20cm', '20CM'), -- Length
(9, 6, 'Cutting', 'CT'), -- Function
(9, 7, 'Kelly', 'kel'); -- Name

-- Add characteristic values for Straight Scissors (sub_group_id = 4)
INSERT INTO category_characteristic (category_id, characteristic_id, characteristic_value, value_abreviation) VALUES
(10, 1, '12cm', '12CM'), -- Length
(10, 4, 'Rigid', 'RGD'), -- Flexibility
(10, 6, 'Cutting', 'CT'), -- Function
(10, 7, 'Kelly', 'kel'), -- Name
(11, 1, '18cm', '18CM'), -- Length
(11, 4, 'Semi-Rigid', 'SRGD'), -- Flexibility
(11, 6, 'Cutting', 'CT'), -- Function
(11, 7, 'Kelly', 'kel'), -- Name
(12, 1, '20cm', '20CM'), -- Length
(12, 6, 'Cutting', 'CT'), -- Function
(12, 7, 'Kelly', 'kel'); -- Name

-- Add characteristic values for Locking Forceps (sub_group_id = 5)
INSERT INTO category_characteristic (category_id, characteristic_id, characteristic_value, value_abreviation) VALUES
(13, 1, '10cm', '10CM'), -- Length
(13, 5, 'Ergonomic', 'ERG'), -- Grip Type
(13, 6, 'Cutting', 'CT'), -- Function
(13, 7, 'Kelly', 'kel'), -- Name
(14, 1, '15cm', '15CM'), -- Length
(14, 5, 'Comfortable', 'COMF'), -- Grip Type
(14, 6, 'Cutting', 'CT'), -- Function
(14, 7, 'Kelly', 'kel'), -- Name
(15, 1, '20cm', '20CM'), -- Length
(15, 6, 'Cutting', 'CT'), -- Function
(15, 7, 'Kelly', 'kel'); -- Name

-- Add characteristic values for Non-Locking Forceps (sub_group_id = 6)
INSERT INTO category_characteristic (category_id, characteristic_id, characteristic_value, value_abreviation) VALUES
(16, 1, '11cm', '11CM'), -- Length
(16, 5, 'Textured', 'TXT'), -- Grip Type
(16, 6, 'Cutting', 'CT'), -- Function
(16, 7, 'Kelly', 'kel'), -- Name
(17, 1, '17cm', '17CM'), -- Length
(17, 5, 'Smooth', 'SMTH'), -- Grip Type
(17, 6, 'Cutting', 'CT'), -- Function
(17, 7, 'Kelly', 'kel'), -- Name
(18, 1, '21cm', '21CM'), -- Length
(18, 6, 'Cutting', 'CT'), -- Function
(18, 7, 'Kelly', 'kel'); -- Name


-- Insert into instruments table
-- Add two instruments for each category, alternating suppliers
-- For Plastic Scalpels (categories linked to sub_group_id = 1)
INSERT INTO instruments (supplier_id, category_id, reference, supplier_description, price, obsolete) VALUES
(1, 1, 'PLS-1001', 'Plastic Scalpel Type A', 9.99, FALSE),
(2, 1, 'PLS-1002', 'Plastic Scalpel Type B', 10.49, FALSE),
(1, 2, 'PLS-2001', 'Plastic Scalpel Type C', 9.99, FALSE),
(2, 2, 'PLS-2002', 'Plastic Scalpel Type D', 10.49, FALSE),
(1, 3, 'PLS-3001', 'Plastic Scalpel Type E', 9.99, FALSE),
(2, 3, 'PLS-3002', 'Plastic Scalpel Type F', 10.49, FALSE);

-- For Metal Scalpels (categories linked to sub_group_id = 2)
INSERT INTO instruments (supplier_id, category_id, reference, supplier_description, price, obsolete) VALUES
(1, 4, 'MTS-1001', 'Metal Scalpel Type A', 19.99, FALSE),
(2, 4, 'MTS-1002', 'Metal Scalpel Type B', 20.49, FALSE),
(1, 5, 'MTS-2001', 'Metal Scalpel Type C', 19.99, FALSE),
(2, 5, 'MTS-2002', 'Metal Scalpel Type D', 20.49, FALSE),
(1, 6, 'MTS-3001', 'Metal Scalpel Type E', 19.99, FALSE),
(2, 6, 'MTS-3002', 'Metal Scalpel Type F', 20.49, FALSE);

-- For Curved Scissors (categories linked to sub_group_id = 3)
INSERT INTO instruments (supplier_id, category_id, reference, supplier_description, price, obsolete) VALUES
(1, 7, 'CRS-1001', 'Curved Scissors Type A', 15.99, FALSE),
(2, 7, 'CRS-1002', 'Curved Scissors Type B', 16.49, FALSE),
(1, 8, 'CRS-2001', 'Curved Scissors Type C', 15.99, FALSE),
(2, 8, 'CRS-2002', 'Curved Scissors Type D', 16.49, FALSE),
(1, 9, 'CRS-3001', 'Curved Scissors Type E', 15.99, FALSE),
(2, 9, 'CRS-3002', 'Curved Scissors Type F', 16.49, FALSE);

-- For Straight Scissors (categories linked to sub_group_id = 4)
INSERT INTO instruments (supplier_id, category_id, reference, supplier_description, price, obsolete) VALUES
(1, 10, 'STS-1001', 'Straight Scissors Type A', 13.99, FALSE),
(2, 10, 'STS-1002', 'Straight Scissors Type B', 14.49, FALSE),
(1, 11, 'STS-2001', 'Straight Scissors Type C', 13.99, FALSE),
(2, 11, 'STS-2002', 'Straight Scissors Type D', 14.49, FALSE),
(1, 12, 'STS-3001', 'Straight Scissors Type E', 13.99, FALSE),
(2, 12, 'STS-3002', 'Straight Scissors Type F', 14.49, FALSE);

-- For Locking Forceps (categories linked to sub_group_id = 5)
INSERT INTO instruments (supplier_id, category_id, reference, supplier_description, price, obsolete) VALUES
(1, 13, 'LCK-1001', 'Locking Forceps Type A', 25.99, FALSE),
(2, 13, 'LCK-1002', 'Locking Forceps Type B', 26.49, FALSE),
(1, 14, 'LCK-2001', 'Locking Forceps Type C', 25.99, FALSE),
(2, 14, 'LCK-2002', 'Locking Forceps Type D', 26.49, FALSE),
(1, 15, 'LCK-3001', 'Locking Forceps Type E', 25.99, FALSE),
(2, 15, 'LCK-3002', 'Locking Forceps Type F', 26.49, FALSE);

-- For Non-Locking Forceps (categories linked to sub_group_id = 6)
INSERT INTO instruments (supplier_id, category_id, reference, supplier_description, price, obsolete) VALUES
(1, 16, 'NLK-1001', 'Non-Locking Forceps Type A', 22.99, FALSE),
(2, 16, 'NLK-1002', 'Non-Locking Forceps Type B', 23.49, FALSE),
(1, 17, 'NLK-2001', 'Non-Locking Forceps Type C', 22.99, FALSE),
(2, 17, 'NLK-2002', 'Non-Locking Forceps Type D', 23.49, FALSE),
(1, 18, 'NLK-3001', 'Non-Locking Forceps Type E', 22.99, FALSE),
(2, 18, 'NLK-3002', 'Non-Locking Forceps Type F', 23.49, FALSE);

-- Insert into alternatives table
INSERT INTO alternatives (instruments_id_1, instruments_id_2) VALUES 
(1, 2);

-- Insert into orders table
INSERT INTO orders (user_id, order_name) VALUES 
(1, 'Base Maxillo'), 
(2, 'Complementaire Maxillo');

-- Insert into order_items table
INSERT INTO order_items (order_id, instrument_id, quantity) VALUES 
(1, 1, 5), 
(2, 2, 3), 
(2, 3, 2);
