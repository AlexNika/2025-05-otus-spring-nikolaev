ALTER TABLE etldataprocessor.s3_events
ADD COLUMN processing_status VARCHAR(50) DEFAULT 'RECEIVED',
ADD COLUMN processing_attempts INT DEFAULT 0,
ADD COLUMN last_error TEXT,
ADD COLUMN processed_at TIMESTAMP with time zone;