UPDATE users SET username = LOWER(username) WHERE username <> LOWER(username);

ALTER TABLE users ADD CONSTRAINT chk_users_username_lowercase CHECK (username = LOWER(username));

