CREATE TABLE catalog_import_jobs (
    id UUID PRIMARY KEY,
    hotel_id UUID NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(120) NOT NULL,
    raw_text TEXT NOT NULL,
    extracted_rows TEXT NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    reviewed_at TIMESTAMP NULL
);
CREATE INDEX idx_catalog_import_jobs_hotel ON catalog_import_jobs(hotel_id, created_at DESC);
