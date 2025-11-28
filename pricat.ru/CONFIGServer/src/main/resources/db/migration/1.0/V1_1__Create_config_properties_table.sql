CREATE TABLE config_properties (
    id BIGSERIAL PRIMARY KEY,
    application VARCHAR(255) NOT NULL,
    profile VARCHAR(255) NOT NULL DEFAULT 'default',
    label VARCHAR(255) DEFAULT 'master',
    prop_key VARCHAR(512) NOT NULL,
    prop_value TEXT,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) DEFAULT 'system',
    updated_by VARCHAR(100) DEFAULT 'system'
);

COMMENT ON TABLE config_properties IS 'Centralized configuration storage for Spring Cloud Config';
COMMENT ON COLUMN config_properties.application IS 'Application name (e.g., auth-service, gateway)';
COMMENT ON COLUMN config_properties.profile IS 'Spring profile (e.g., dev, prod, default)';
COMMENT ON COLUMN config_properties.label IS 'Config label/branch (e.g., master, feature-x)';
COMMENT ON COLUMN config_properties.prop_key IS 'Configuration property key';
COMMENT ON COLUMN config_properties.prop_value IS 'Configuration property value';