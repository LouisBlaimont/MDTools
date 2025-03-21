-- Table pictures
CREATE TABLE pictures (
    id SERIAL PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    reference_type VARCHAR(255) NOT NULL,
    reference_id INTEGER NOT NULL,
    upload_date TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP
);

-- Table group
CREATE TABLE "group" (
    group_id SERIAL PRIMARY KEY,
    group_name VARCHAR(100) UNIQUE NOT NULL,
    picture_id INTEGER REFERENCES pictures(id) ON DELETE SET NULL
);

--Table sub_group
CREATE TABLE sub_group (
    sub_group_id SERIAL PRIMARY KEY,
    sub_group_name VARCHAR(100) NOT NULL,
    group_id INTEGER REFERENCES "group"(group_id) ON DELETE SET NULL,
    picture_id INTEGER REFERENCES pictures(id) ON DELETE SET NULL
);

-- Table users
CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(100) UNIQUE,
    email VARCHAR(255) UNIQUE,
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    role_name VARCHAR(50),
    job_position VARCHAR(100),
    workplace VARCHAR(100)
);

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
    user_id INTEGER REFERENCES users(user_id),
    action TEXT NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table supplier
CREATE TABLE supplier (
    supplier_id SERIAL PRIMARY KEY,
    supplier_name VARCHAR(255) NOT NULL,
    sold_by_md BOOLEAN NOT NULL,
    closed BOOLEAN DEFAULT FALSE
);

-- Table category
CREATE TABLE category (
    category_id SERIAL PRIMARY KEY,
    sub_group_id INTEGER REFERENCES sub_group(sub_group_id),
    shape VARCHAR(255),
    picture_id INTEGER REFERENCES pictures(id) ON DELETE SET NULL
);

-- Table characteristic
CREATE TABLE characteristic (
    characteristic_id SERIAL PRIMARY KEY,
    characteristic_name TEXT
);

-- Table category_characteristic
CREATE TABLE category_characteristic (
    category_id INTEGER REFERENCES category(category_id),
    characteristic_id INTEGER REFERENCES characteristic(characteristic_id),
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
    supplier_id INTEGER REFERENCES supplier(supplier_id),
    category_id INTEGER REFERENCES category(category_id),
    reference VARCHAR(100) NOT NULL,
    supplier_description TEXT,
    price NUMERIC(10, 2) NOT NULL,
    obsolete BOOLEAN DEFAULT FALSE
);

-- Table group_characteristic
CREATE TABLE sub_group_characteristic (
    sub_group_id INTEGER REFERENCES sub_group(sub_group_id),
    characteristic_id INTEGER REFERENCES characteristic(characteristic_id),
    order_position INTEGER,
    PRIMARY KEY (sub_group_id, characteristic_id)
);

-- Table alternatives
CREATE TABLE alternatives (
    instruments_id_1 INTEGER REFERENCES instruments(instrument_id),
    instruments_id_2 INTEGER REFERENCES instruments(instrument_id),
    PRIMARY KEY (instruments_id_1, instruments_id_2)
);

-- Table orders
CREATE TABLE orders (
    order_id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(user_id),
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    order_name TEXT
);

-- Table order_items
CREATE TABLE order_items (
    order_id INTEGER REFERENCES orders(order_id),
    instrument_id INTEGER REFERENCES instruments(instrument_id),
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

    -- Ensure instruments are from the same group by checking through category and group
    IF (SELECT group_id FROM sub_group
    WHERE sub_group_id = (SELECT sub_group_id FROM category 
                          WHERE category_id = (SELECT category_id FROM instruments WHERE instrument_id = NEW.instruments_id_1))) <> 
   (SELECT group_id FROM sub_group
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
                                       FROM sub_group
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
