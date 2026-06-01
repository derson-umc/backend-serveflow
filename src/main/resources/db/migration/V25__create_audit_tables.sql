CREATE TABLE IF NOT EXISTS access_log (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT,
    ip          VARCHAR(45),
    endpoint    VARCHAR(255),
    http_method VARCHAR(10),
    http_status INTEGER,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_access_log_user_id    ON access_log (user_id);
CREATE INDEX IF NOT EXISTS idx_access_log_ip         ON access_log (ip);
CREATE INDEX IF NOT EXISTS idx_access_log_created_at ON access_log (created_at);

CREATE TABLE IF NOT EXISTS audit_log (
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT,
    action        VARCHAR(100) NOT NULL,
    entity        VARCHAR(100),
    entity_id     BIGINT,
    previous_data TEXT,
    new_data      TEXT,
    ip            VARCHAR(45),
    created_at    TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_audit_log_user_id    ON audit_log (user_id);
CREATE INDEX IF NOT EXISTS idx_audit_log_action     ON audit_log (action);
CREATE INDEX IF NOT EXISTS idx_audit_log_created_at ON audit_log (created_at);

CREATE TABLE IF NOT EXISTS error_log (
    id         BIGSERIAL PRIMARY KEY,
    message    TEXT      NOT NULL,
    stacktrace TEXT,
    service    VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_error_log_created_at ON error_log (created_at);
