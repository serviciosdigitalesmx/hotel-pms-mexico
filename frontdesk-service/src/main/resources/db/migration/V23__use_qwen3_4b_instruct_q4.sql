-- AXEL PMS
--
-- Switches the local default from the Qwen3 4B thinking alias introduced
-- in V22 to the explicit Qwen3 4B Instruct 2507 Q4_K_M model.
--
-- Existing DeepSeek tenants are intentionally untouched.
--
-- Output-token and reasoning controls live in AssistantService because
-- they are inference parameters rather than persistent hotel data.

ALTER TABLE hotel_settings
    ALTER COLUMN ai_model
    SET DEFAULT 'qwen3:4b-instruct-2507-q4_K_M';

UPDATE hotel_settings
SET
    ai_model = 'qwen3:4b-instruct-2507-q4_K_M',
    ai_enabled = TRUE
WHERE ai_model = 'qwen3:4b'
   OR ai_model IS NULL
   OR TRIM(ai_model) = '';
