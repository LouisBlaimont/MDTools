

-- Table group
CREATE TABLE "group" (
    group_id SERIAL PRIMARY KEY,
    group_name VARCHAR(100) UNIQUE NOT NULL
);

-- Table users
CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    role_name VARCHAR(50) NOT NULL, -- Note: 'admin or user'
    job_position VARCHAR(100),
    workplace VARCHAR(100)
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

-- Table sub_group
CREATE TABLE sub_group (
    sub_group_id SERIAL PRIMARY KEY,
    group_id INTEGER REFERENCES "group"(group_id),
    shape VARCHAR(255)
);

-- Table characteristic
CREATE TABLE characteristic (
    characteristic_id SERIAL PRIMARY KEY,
    characteristic_name VARCHAR(255)
);

-- Table sub_group_characteristic
CREATE TABLE sub_group_characteristic (
    sub_group_id INTEGER REFERENCES sub_group(sub_group_id),
    characteristic_id INTEGER REFERENCES characteristic(characteristic_id),
    value VARCHAR(255),
    value_abreviation VARCHAR(100),
    PRIMARY KEY (sub_group_id, characteristic_id)
);

-- Table instruments
CREATE TABLE instruments (
    instrument_id SERIAL PRIMARY KEY,
    supplier_id INTEGER REFERENCES supplier(supplier_id),
    sub_group_id INTEGER REFERENCES sub_group(sub_group_id),
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

    -- Ensure instruments are from the same group by checking through sub_group and group
    IF (SELECT group_id FROM sub_group 
        WHERE sub_group_id = (SELECT sub_group_id FROM instruments WHERE instrument_id = NEW.instruments_id_1)) <> 
       (SELECT group_id FROM sub_group 
        WHERE sub_group_id = (SELECT sub_group_id FROM instruments WHERE instrument_id = NEW.instruments_id_2)) THEN
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
        FROM sub_group_characteristic sgc
        JOIN group_characteristic gc ON gc.group_id = (SELECT group_id FROM sub_group WHERE sub_group_id = NEW.sub_group_id)
                                     AND gc.characteristic_id = sgc.characteristic_id
        WHERE sgc.sub_group_id = NEW.sub_group_id
          AND gc.order_position IS NOT NULL
        ORDER BY gc.order_position
    LOOP
        -- Concaténer chaque value_abreviation avec un '/' comme séparateur
        shape_text := shape_text || characteristic_record.value_abreviation || '/';
    END LOOP;

    -- Supprimer le dernier séparateur '/' à la fin de la chaîne
    shape_text := RTRIM(shape_text, '/');

    -- Mettre à jour le champ shape dans sub_group
    UPDATE sub_group
    SET shape = shape_text
    WHERE sub_group_id = NEW.sub_group_id;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_shape
AFTER INSERT OR UPDATE ON sub_group_characteristic
FOR EACH ROW
EXECUTE FUNCTION update_shape();
