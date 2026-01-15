CREATE TABLE IF NOT EXISTS files (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(64) NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    upload_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_files_client FOREIGN KEY (username) REFERENCES clients(username) ON DELETE CASCADE,
    CONSTRAINT chk_file_size_positive CHECK (file_size > 0)
);

CREATE INDEX idx_files_username ON files(username);
CREATE INDEX idx_files_upload_date ON files(upload_date DESC);