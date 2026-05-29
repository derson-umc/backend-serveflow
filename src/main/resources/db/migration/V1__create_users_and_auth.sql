CREATE TABLE IF NOT EXISTS users (
    id           BIGSERIAL     NOT NULL,
    username     VARCHAR(50)   NOT NULL,
    password     VARCHAR(255)  NOT NULL,
    role         VARCHAR(20)   NOT NULL,
    jobposition  VARCHAR(60)   NOT NULL,
    CONSTRAINT pk_users          PRIMARY KEY (id),
    CONSTRAINT uq_users_username UNIQUE (username)
);

CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username    VARCHAR(64)  NOT NULL,
    token       VARCHAR(6)   NOT NULL,
    expires_at  TIMESTAMP    NOT NULL,
    used        BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
    );

CREATE INDEX IF NOT EXISTS idx_prt_username ON password_reset_tokens (username);
CREATE INDEX IF NOT EXISTS idx_prt_token    ON password_reset_tokens (token);

