ALTER TABLE hotel_settings
    ALTER COLUMN ai_model SET DEFAULT 'llama-3.1-8b-instant';

UPDATE hotel_settings
SET ai_model = 'llama-3.1-8b-instant'
WHERE ai_model = 'llama-3.3-70b-versatile';
