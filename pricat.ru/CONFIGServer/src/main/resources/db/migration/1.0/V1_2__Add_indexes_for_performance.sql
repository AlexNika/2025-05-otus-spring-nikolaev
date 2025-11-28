-- Index for fast configuration lookup
CREATE UNIQUE INDEX IF NOT EXISTS idx_config_properties_lookup
ON config_properties (application, profile, label, prop_key);

-- Index for application-specific queries
CREATE INDEX IF NOT EXISTS idx_config_properties_application
ON config_properties (application);

-- Index for profile-specific queries
CREATE INDEX IF NOT EXISTS idx_config_properties_profile
ON config_properties (profile);

-- Function to automatically update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger to automatically update updated_at
CREATE TRIGGER update_config_properties_updated_at
    BEFORE UPDATE ON config_properties
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();