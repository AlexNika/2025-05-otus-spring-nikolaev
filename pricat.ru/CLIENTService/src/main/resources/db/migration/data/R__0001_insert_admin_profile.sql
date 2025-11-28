INSERT INTO clients (
    id,
    username,
    email,
    name,
    roles,
    company_name,
    is_supplier,
    pricelist_obtaining_way,
    pricelist_format
) VALUES (
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'::UUID,
    'admin',
    'admin@pricat.ru',
    'System Administrator',
    'ADMIN,USER',
    'PRICAT.RU',
    false,
    'MANUAL',
    'XLSX'
);