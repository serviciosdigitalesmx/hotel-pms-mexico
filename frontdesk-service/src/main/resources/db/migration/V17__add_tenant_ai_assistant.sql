ALTER TABLE hotel_settings
    ADD COLUMN ai_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN ai_model VARCHAR(150) NOT NULL DEFAULT 'llama-3.3-70b-versatile',
    ADD COLUMN ai_instructions VARCHAR(1000),
    ADD COLUMN ai_api_key_encrypted TEXT;
