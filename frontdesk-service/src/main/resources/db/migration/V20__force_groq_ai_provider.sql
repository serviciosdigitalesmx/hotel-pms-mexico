ALTER TABLE hotel_settings
    ALTER COLUMN ai_model SET DEFAULT 'openai/gpt-oss-20b';

UPDATE hotel_settings
SET ai_model = 'openai/gpt-oss-20b'
WHERE ai_model LIKE 'gemini-%'
   OR ai_model IS NULL
   OR TRIM(ai_model) = '';
