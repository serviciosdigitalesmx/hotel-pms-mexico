-- AXEL PMS
-- Local Ollama is the default AI provider for installations without
-- an external provider credential.
--
-- DeepSeek remains supported by AssistantService whenever the selected
-- tenant model begins with "deepseek-".
--
-- The encrypted historical API credential is deliberately preserved.
-- Ollama never consumes it.

ALTER TABLE hotel_settings
    ALTER COLUMN ai_model SET DEFAULT 'qwen3:4b';

UPDATE hotel_settings
SET
    ai_model = 'qwen3:4b',
    ai_enabled = TRUE
WHERE ai_model = 'deepseek-v4-flash'
   OR ai_model IS NULL
   OR TRIM(ai_model) = '';
