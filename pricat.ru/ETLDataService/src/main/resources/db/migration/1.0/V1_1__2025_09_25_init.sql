CREATE SCHEMA IF NOT EXISTS etldataprocessor;
CREATE TABLE IF NOT EXISTS etldataprocessor.companies (
    id BIGSERIAL,
    correlation_id varchar(39) UNIQUE not null,
    company_name_ru varchar(255) not null,
    company_name_en varchar(255) not null,
    company_folder varchar(255) not null,
    is_active boolean,
    created_at timestamp with time zone default current_timestamp,
    updated_at timestamp with time zone default current_timestamp,
    version BIGSERIAL,
    primary key (id)
);
CREATE INDEX ix_company_name_en ON etldataprocessor.companies (company_name_en);
CREATE INDEX ix_active_company_name_en ON etldataprocessor.companies (company_name_en, is_active);
