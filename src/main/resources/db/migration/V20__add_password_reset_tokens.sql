CREATE TABLE password_reset_tokens (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username    VARCHAR(64)  NOT NULL,
    token       VARCHAR(6)   NOT NULL,
    expires_at  TIMESTAMP    NOT NULL,
    used        BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_prt_username ON password_reset_tokens (username);
CREATE INDEX idx_prt_token    ON password_reset_tokens (token);
