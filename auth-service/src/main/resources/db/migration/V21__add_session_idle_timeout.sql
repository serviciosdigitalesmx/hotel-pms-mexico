ALTER TABLE user_account
    ADD COLUMN session_idle_timeout_minutes INTEGER NOT NULL DEFAULT 0;

ALTER TABLE user_account
    ADD CONSTRAINT chk_user_account_session_idle_timeout
    CHECK (session_idle_timeout_minutes IN (0, 15, 60));
