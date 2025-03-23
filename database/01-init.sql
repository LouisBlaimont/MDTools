-- Table pictures
CREATE TABLE pictures (
    id SERIAL PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    reference_type VARCHAR(255) NOT NULL,
    reference_id INTEGER NOT NULL,
    upload_date TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE pictures IS 'Stores image files with polymorphic associations to other entities';

-- Table groups
CREATE TABLE groups (
    group_id SERIAL PRIMARY KEY,
    group_name VARCHAR(100) UNIQUE NOT NULL,
    picture_id INTEGER REFERENCES pictures(id) ON DELETE SET NULL
);

--Table sub_groups
CREATE TABLE sub_groups (
    sub_group_id SERIAL PRIMARY KEY,
    sub_group_name VARCHAR(100) NOT NULL,
    group_id INTEGER REFERENCES groups(group_id) ON DELETE SET NULL,
    picture_id INTEGER REFERENCES pictures(id) ON DELETE SET NULL
);
CREATE INDEX idx_sub_groups_group_id ON sub_groups(group_id);

-- Table users
CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    username TEXT UNIQUE,
    email TEXT UNIQUE,
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    role_name TEXT,
    job_position TEXT,
    workplace TEXT,
    CONSTRAINT unique_username UNIQUE (username),
    CONSTRAINT unique_email UNIQUE (email),
    CONSTRAINT valid_email CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$')
);

-- Add a trigger for updating the updated_at timestamp
CREATE OR REPLACE FUNCTION update_modified_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_users_timestamp
BEFORE UPDATE ON users
FOR EACH ROW
EXECUTE FUNCTION update_modified_column();

CREATE TABLE authorities (
    authority TEXT PRIMARY KEY
);

CREATE TABLE user_authorities (
    user_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    authority TEXT NOT NULL REFERENCES authorities(authority) ON DELETE CASCADE,
    PRIMARY KEY (user_id, authority)
);

-- Table logs
CREATE TABLE logs (
    log_id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(user_id) ON DELETE SET NULL,
    action TEXT NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_logs_user_id ON logs(user_id);
CREATE INDEX idx_logs_timestamp ON logs(timestamp); -- For time-based queries

-- Table supplier
CREATE TABLE supplier (
    supplier_id SERIAL PRIMARY KEY,
    supplier_name VARCHAR(255) NOT NULL,
    sold_by_md BOOLEAN NOT NULL,
    closed BOOLEAN DEFAULT FALSE,
    CONSTRAINT unique_supplier_name UNIQUE (supplier_name)
);

-- Table category
CREATE TABLE category (
    category_id SERIAL PRIMARY KEY,
    sub_group_id INTEGER NOT NULL REFERENCES sub_groups(sub_group_id) ON DELETE CASCADE,
    shape VARCHAR(255),
    picture_id INTEGER REFERENCES pictures(id) ON DELETE SET NULL
);
CREATE INDEX idx_categories_sub_group_id ON category(sub_group_id);

-- Table characteristic
CREATE TABLE characteristic (
    characteristic_id SERIAL PRIMARY KEY,
    characteristic_name TEXT NOT NULL
);

-- Table category_characteristic
CREATE TABLE category_characteristic (
    category_id INTEGER REFERENCES category(category_id) ON DELETE CASCADE,
    characteristic_id INTEGER REFERENCES characteristic(characteristic_id) ON DELETE CASCADE,
    characteristic_value TEXT,
    PRIMARY KEY (category_id, characteristic_id)
);

-- Table for characteristic values abbreviations
CREATE TABLE category_characteristic_abbreviations (
    id SERIAL PRIMARY KEY,
    characteristic_value TEXT UNIQUE NOT NULL,
    value_abreviation TEXT NOT NULL
);

-- Table instruments
CREATE TABLE instruments (
    instrument_id SERIAL PRIMARY KEY,
    supplier_id INTEGER REFERENCES supplier(supplier_id) ON DELETE CASCADE,
    category_id INTEGER REFERENCES category(category_id) ON DELETE CASCADE,
    reference VARCHAR(100) NOT NULL,
    supplier_description TEXT,
    price NUMERIC(10, 2) NOT NULL,
    obsolete BOOLEAN DEFAULT FALSE
);

CREATE INDEX idx_instruments_supplier_id ON instruments(supplier_id);
CREATE INDEX idx_instruments_category_id ON instruments(category_id);
CREATE INDEX idx_instruments_reference ON instruments(reference);

-- Table group_characteristic
CREATE TABLE sub_group_characteristic (
    sub_group_id INTEGER REFERENCES sub_groups(sub_group_id) ON DELETE CASCADE,
    characteristic_id INTEGER REFERENCES characteristic(characteristic_id) ON DELETE CASCADE,
    order_position INTEGER,
    PRIMARY KEY (sub_group_id, characteristic_id)
);

-- Table alternatives
CREATE TABLE alternatives (
    instruments_id_1 INTEGER NOT NULL REFERENCES instruments(instrument_id) ON DELETE CASCADE,
    instruments_id_2 INTEGER NOT NULL REFERENCES instruments(instrument_id) ON DELETE CASCADE,
    alternative_type TEXT,
    alternative_comment TEXT,
    PRIMARY KEY (instruments_id_1, instruments_id_2)
);

-- Table orders
CREATE TABLE orders (
    order_id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(user_id) ON DELETE SET NULL,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    order_name TEXT
);

-- Table order_items
CREATE TABLE order_items (
    order_id INTEGER NOT NULL REFERENCES orders(order_id) ON DELETE CASCADE,
    instrument_id INTEGER NOT NULL REFERENCES instruments(instrument_id) ON DELETE CASCADE,
    quantity INTEGER NOT NULL,
    PRIMARY KEY (order_id, instrument_id)
);


-- FUNCTION: public.check_alternative_constraints()

-- DROP FUNCTION IF EXISTS public.check_alternative_constraints();

CREATE OR REPLACE FUNCTION public.check_alternative_constraints()
    RETURNS trigger
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE NOT LEAKPROOF
AS $BODY$
BEGIN
    -- Ensure instruments are from different suppliers
    IF (SELECT supplier_id FROM instruments WHERE instrument_id = NEW.instruments_id_1) = 
       (SELECT supplier_id FROM instruments WHERE instrument_id = NEW.instruments_id_2) THEN
        RAISE EXCEPTION 'Instruments must be from different suppliers';
    END IF;

    -- Ensure instruments are from the same groups by checking through category and groups
    IF (SELECT group_id FROM sub_groups
    WHERE sub_group_id = (SELECT sub_group_id FROM category 
                          WHERE category_id = (SELECT category_id FROM instruments WHERE instrument_id = NEW.instruments_id_1))) <> 
   (SELECT group_id FROM sub_groups
    WHERE sub_group_id = (SELECT sub_group_id FROM category 
                          WHERE category_id = (SELECT category_id FROM instruments WHERE instrument_id = NEW.instruments_id_2))) THEN
    RAISE EXCEPTION 'Instruments must be from the same group';
END IF;


    RETURN NEW;
END;
$BODY$;

ALTER FUNCTION public.check_alternative_constraints()
    OWNER TO team03;

CREATE TRIGGER verify_alternative_constraints
BEFORE INSERT OR UPDATE ON alternatives
FOR EACH ROW
EXECUTE FUNCTION check_alternative_constraints();


CREATE OR REPLACE FUNCTION update_shape()
RETURNS TRIGGER AS $$
DECLARE
    characteristic_record RECORD;
    shape_text TEXT := '';
BEGIN
    -- Build the shape string based on the present characteristics and their order
    FOR characteristic_record IN
        SELECT cca.value_abreviation
        FROM category_characteristic cc
        JOIN category_characteristic_abbreviations cca
            ON cca.characteristic_value = cc.characteristic_value
        JOIN sub_group_characteristic sgc_rel
            ON sgc_rel.sub_group_id = (SELECT sub_group_id
                                       FROM sub_groups
                                       WHERE sub_group_id = (SELECT sub_group_id
                                                             FROM category
                                                             WHERE category_id = NEW.category_id))
            AND sgc_rel.characteristic_id = cc.characteristic_id
        WHERE cc.category_id = NEW.category_id
          AND sgc_rel.order_position IS NOT NULL
        ORDER BY sgc_rel.order_position
    LOOP
        -- Concatenate each value_abreviation with a '/' as a separator
        shape_text := shape_text || characteristic_record.value_abreviation || '/';
    END LOOP;

    -- Remove the last '/' separator at the end of the string
    shape_text := RTRIM(shape_text, '/');

    -- Update the shape field in the category table
    UPDATE category
    SET shape = shape_text
    WHERE category_id = NEW.category_id;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


-- Update the trigger to reflect the modified function
CREATE TRIGGER trigger_update_shape
AFTER INSERT OR UPDATE ON category_characteristic
FOR EACH ROW
EXECUTE FUNCTION update_shape();
