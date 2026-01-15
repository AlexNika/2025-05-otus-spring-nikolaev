CREATE TABLE IF NOT EXISTS clients (
    id UUID PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(255),
    roles VARCHAR(200) NOT NULL DEFAULT 'USER',
    company_name VARCHAR(255) NOT NULL DEFAULT 'Unknown company',
    mobile_phone VARCHAR(20),
    avatar_url TEXT,
    is_supplier BOOLEAN NOT NULL DEFAULT FALSE,
    company_folder VARCHAR(255),
    pricelist_obtaining_way VARCHAR(20) NOT NULL DEFAULT 'MANUAL',
    pricelist_format VARCHAR(10) NOT NULL DEFAULT 'XLSX',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_clients_username ON clients(username);
CREATE INDEX IF NOT EXISTS idx_clients_email ON clients(email);
CREATE INDEX IF NOT EXISTS idx_clients_company_name ON clients(company_name);
CREATE INDEX IF NOT EXISTS idx_clients_is_supplier ON clients(is_supplier);
CREATE INDEX IF NOT EXISTS idx_clients_created_at ON clients(created_at);

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_clients_updated_at
    BEFORE UPDATE ON clients
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();