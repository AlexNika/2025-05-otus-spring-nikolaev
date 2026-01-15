CREATE TABLE IF NOT EXISTS etldataprocessor.s3_events
(
    id BIGSERIAL ,
    correlation_id varchar(39) UNIQUE not null,
    event_type varchar(255) not null,
    bucket_name varchar(255) not null,
    object_key varchar(255) not null,
    object_size bigint,
    object_etag varchar(255),
    object_content_type varchar(255),
    event_time timestamp with time zone,
    full_event_data jsonb,
    created_at timestamp with time zone default current_timestamp,
    updated_at timestamp with time zone default current_timestamp,
    version BIGSERIAL,
    primary key (id)
);
CREATE INDEX ix_event_type ON etldataprocessor.s3_events (event_type);