CREATE TABLE IF NOT EXISTS import_operations (
    id BIGSERIAL PRIMARY KEY,
    initiator VARCHAR(128) NOT NULL,
    status VARCHAR(32) NOT NULL,
    added_count INTEGER,
    error_message VARCHAR(1024),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_import_operations_initiator_created_at
    ON import_operations (initiator, created_at DESC);
