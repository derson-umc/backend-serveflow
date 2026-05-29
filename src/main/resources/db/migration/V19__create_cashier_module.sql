CREATE TABLE IF NOT EXISTS cash_sessions (
    id                   UUID          NOT NULL,
    version              BIGINT,
    status               VARCHAR(10)   NOT NULL DEFAULT 'OPEN',
    initial_balance      NUMERIC(12,2) NOT NULL DEFAULT 0,
    observation          VARCHAR(500),
    opened_at            TIMESTAMP     NOT NULL DEFAULT NOW(),
    closed_at            TIMESTAMP,
    opened_by            VARCHAR(150)  NOT NULL,
    closed_by            VARCHAR(150),
    closing_observation  VARCHAR(500),
    CONSTRAINT pk_cash_sessions PRIMARY KEY (id),
    CONSTRAINT chk_cash_session_status CHECK (status IN ('OPEN','CLOSED')),
    CONSTRAINT chk_cash_session_balance CHECK (initial_balance >= 0)
);

CREATE TABLE IF NOT EXISTS cash_movements (
    id            UUID          NOT NULL DEFAULT gen_random_uuid(),
    session_id    UUID          NOT NULL,
    type          VARCHAR(10)   NOT NULL,
    amount        NUMERIC(12,2) NOT NULL,
    description   VARCHAR(300)  NOT NULL,
    category      VARCHAR(100),
    performed_by  VARCHAR(150)  NOT NULL,
    created_at    TIMESTAMP     NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_cash_movements PRIMARY KEY (id),
    CONSTRAINT fk_cash_movement_session FOREIGN KEY (session_id) REFERENCES cash_sessions(id),
    CONSTRAINT chk_cash_movement_type CHECK (type IN ('INCOME','EXPENSE')),
    CONSTRAINT chk_cash_movement_amount CHECK (amount > 0)
);

CREATE INDEX IF NOT EXISTS idx_cash_sessions_status    ON cash_sessions (status);
CREATE INDEX IF NOT EXISTS idx_cash_sessions_opened_at ON cash_sessions (opened_at DESC);
CREATE INDEX IF NOT EXISTS idx_cash_movements_session  ON cash_movements (session_id);
CREATE INDEX IF NOT EXISTS idx_cash_movements_created  ON cash_movements (created_at);
