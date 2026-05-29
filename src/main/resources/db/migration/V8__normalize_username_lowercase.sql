UPDATE users SET username = LOWER(username) WHERE username <> LOWER(username);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'chk_users_username_lowercase'
          AND table_name      = 'users'
          AND table_schema    = 'public'
    ) THEN
        ALTER TABLE users ADD CONSTRAINT chk_users_username_lowercase CHECK (username = LOWER(username));
    END IF;
END $$;
