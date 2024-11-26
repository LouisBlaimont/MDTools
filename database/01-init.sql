
-- Table group
CREATE TABLE "group" (
    group_id SERIAL PRIMARY KEY,
    group_name VARCHAR(100) UNIQUE NOT NULL
);

--Table sub_group
CREATE TABLE sub_group (
    sub_group_id SERIAL PRIMARY KEY,
    sub_group_name VARCHAR(100) NOT NULL,
    group_id INTEGER REFERENCES "group"(group_id) ON DELETE CASCADE
);

-- Table users
CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    role_name VARCHAR(50) NOT NULL, -- Note: 'admin or user'
    job_position VARCHAR(100),
    workplace VARCHAR(100),
    reset_token VARCHAR(40),
    reset_token_expiration TIMESTAMPTZ
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
    shape VARCHAR(255)
);

-- Table characteristic
CREATE TABLE characteristic (
    characteristic_id SERIAL PRIMARY KEY,
    characteristic_name VARCHAR(255)
);

-- Table category_characteristic
CREATE TABLE category_characteristic (
    category_id INTEGER REFERENCES category(category_id),
    characteristic_id INTEGER REFERENCES characteristic(characteristic_id),
    value VARCHAR(255),
    value_abreviation VARCHAR(100),
    PRIMARY KEY (category_id, characteristic_id)
);

-- Table instruments
CREATE TABLE instruments (
    instrument_id SERIAL PRIMARY KEY,
    supplier_id INTEGER REFERENCES supplier(supplier_id),
    category_id INTEGER REFERENCES category(category_id),
    reference VARCHAR(255) NOT NULL,
    supplier_description TEXT,
    price NUMERIC(10, 2) NOT NULL,
    obsolete BOOLEAN DEFAULT FALSE
);

-- Table group_characteristic
CREATE TABLE group_characteristic (
    group_id INTEGER REFERENCES "group"(group_id),
    characteristic_id INTEGER REFERENCES characteristic(characteristic_id),
    order_position INTEGER,
    PRIMARY KEY (group_id, characteristic_id)
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
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table order_items
CREATE TABLE order_items (
    order_id INTEGER REFERENCES orders(order_id),
    instrument_id INTEGER REFERENCES instruments(instrument_id),
    quantity INTEGER NOT NULL,
    PRIMARY KEY (order_id, instrument_id)
);
-- Create table for instrument images
CREATE TABLE instrument_pictures (
  photo_id SERIAL PRIMARY KEY,
  instrument_id INTEGER NOT NULL REFERENCES instruments(instrument_id) ON DELETE CASCADE,
  picture_path VARCHAR(255) NOT NULL
);

-- Create table for sub-group images
CREATE TABLE category_pictures (
  photo_id SERIAL PRIMARY KEY,
  category_id INTEGER NOT NULL REFERENCES category(category_id) ON DELETE CASCADE,
  picture_path VARCHAR(255) NOT NULL
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
    -- Construire la chaîne shape en fonction des caractéristiques présentes et de leur ordre
    FOR characteristic_record IN
        SELECT sgc.value_abreviation
        FROM category_characteristic sgc
        JOIN sub_group_characteristic sgc_rel
            ON sgc_rel.sub_group_id = (SELECT sub_group_id
                                       FROM sub_group
                                       WHERE sub_group_id = (SELECT sub_group_id
                                                             FROM category
                                                             WHERE category_id = NEW.category_id))
            AND sgc_rel.characteristic_id = sgc.characteristic_id
        WHERE sgc.category_id = NEW.category_id
          AND gc.order_position IS NOT NULL
        ORDER BY gc.order_position
    LOOP
        -- Concaténer chaque value_abreviation avec un '/' comme séparateur
        shape_text := shape_text || characteristic_record.value_abreviation || '/';
    END LOOP;

    -- Supprimer le dernier séparateur '/' à la fin de la chaîne
    shape_text := RTRIM(shape_text, '/');

    -- Mettre à jour le champ shape dans category
    UPDATE category
    SET shape = shape_text
    WHERE category_id = NEW.category_id;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_shape
AFTER INSERT OR UPDATE ON category_characteristic
FOR EACH ROW
EXECUTE FUNCTION update_shape();
