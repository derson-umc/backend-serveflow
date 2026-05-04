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
    id          BIGSERIAL    NOT NULL,
    user_id     BIGINT       NOT NULL,
    token_hash  VARCHAR(64)  NOT NULL,
    expires_at  TIMESTAMP    NOT NULL,
    used_at     TIMESTAMP,
    CONSTRAINT pk_password_reset_tokens      PRIMARY KEY (id),
    CONSTRAINT uq_password_reset_token_hash  UNIQUE (token_hash)
);

CREATE INDEX IF NOT EXISTS idx_prt_token_hash ON password_reset_tokens (token_hash);
CREATE INDEX IF NOT EXISTS idx_prt_user_id    ON password_reset_tokens (user_id);
