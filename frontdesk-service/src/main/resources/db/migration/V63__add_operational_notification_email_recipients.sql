ALTER TABLE operational_notification_preferences
    ADD COLUMN email_recipients JSONB NOT NULL DEFAULT '{}'::jsonb;
