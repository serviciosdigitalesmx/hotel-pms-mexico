-- AXEL PMS — DeepSeek provider migration.
--
-- V17-V20 configured historical Groq/OpenAI-compatible model names.
-- AssistantService now uses the official DeepSeek API directly.
--
-- Existing encrypted credentials are deliberately PRESERVED because Flyway
-- must not destroy credentials needed for rollback/audit. AI is disabled for
-- every migrated legacy row so an old Groq/OpenAI API key is never sent to
-- DeepSeek accidentally.
--
-- Re-enable AI only after installing a valid DeepSeek API key for that hotel.

ALTER TABLE hotel_settings
    ALTER COLUMN ai_model SET DEFAULT 'deepseek-v4-flash';

UPDATE hotel_settings
SET
    ai_model = 'deepseek-v4-flash',
    ai_enabled = FALSE
WHERE ai_model IS NULL
   OR TRIM(ai_model) = ''
   OR ai_model NOT IN (
       'deepseek-v4-flash',
       'deepseek-v4-pro'
   );
