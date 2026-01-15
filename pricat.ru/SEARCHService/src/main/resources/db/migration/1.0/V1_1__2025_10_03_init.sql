CREATE SCHEMA IF NOT EXISTS datasearchengine;
CREATE TABLE IF NOT EXISTS datasearchengine.pricelist_current_state (
    id BIGSERIAL,
    company varchar(255) not null,
    product_id varchar(255) not null,
    product_name text not null,
    description text,
    price decimal(19, 4) not null,
    currency varchar(3) not null,
    stock_quantity integer not null,
    category varchar(255),
    manufacturer varchar(255),
    supplier_code varchar(255),
    file_processed_at timestamp with time zone not null,
    batch_id uuid not null,
    primary key (id)
);
ALTER TABLE datasearchengine.pricelist_current_state
    ADD CONSTRAINT uc_b2acf8761ee565f6f4cc6d7d4 UNIQUE (company, product_id);

CREATE TABLE IF NOT EXISTS datasearchengine.pricelist_processing_history
(
    batch_id uuid not null,
    company varchar(255) not null,
    file_processed_at timestamp with time zone not null,
    received_at timestamp with time zone not null,
    indexed_at timestamp with time zone,
    total_items integer not null,
    processed_items integer,
    status varchar(20) not null,
    error_message text,
    primary key (batch_id)
);