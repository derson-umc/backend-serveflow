DO $$
BEGIN
    IF EXISTS (SELECT FROM pg_tables WHERE schemaname = 'public' AND tablename = 'contas_pagar') THEN
        IF NOT EXISTS (SELECT FROM pg_tables WHERE schemaname = 'public' AND tablename = 'accounts_payable') THEN
            ALTER TABLE contas_pagar RENAME TO accounts_payable;
            ALTER TABLE accounts_payable RENAME COLUMN id_conta_pagar TO id;
            ALTER TABLE accounts_payable RENAME COLUMN descricao TO description;
            ALTER TABLE accounts_payable RENAME COLUMN vencimento TO due_date;
            ALTER TABLE accounts_payable RENAME COLUMN valor TO amount;
            ALTER TABLE accounts_payable RENAME COLUMN pago_em TO paid_at;
            ALTER TABLE accounts_payable RENAME COLUMN valor_pago TO paid_amount;
            ALTER TABLE accounts_payable RENAME COLUMN categoria TO category;
            ALTER TABLE accounts_payable RENAME COLUMN fornecedor TO supplier;
            ALTER TABLE accounts_payable RENAME COLUMN criado_em TO created_at;
        ELSE
            DROP TABLE contas_pagar;
        END IF;
    END IF;

    IF EXISTS (SELECT FROM pg_tables WHERE schemaname = 'public' AND tablename = 'contas_receber') THEN
        IF NOT EXISTS (SELECT FROM pg_tables WHERE schemaname = 'public' AND tablename = 'accounts_receivable') THEN
            ALTER TABLE contas_receber RENAME TO accounts_receivable;
            ALTER TABLE accounts_receivable RENAME COLUMN id_conta_receber TO id;
            ALTER TABLE accounts_receivable RENAME COLUMN descricao TO description;
            ALTER TABLE accounts_receivable RENAME COLUMN vencimento TO due_date;
            ALTER TABLE accounts_receivable RENAME COLUMN valor TO amount;
            ALTER TABLE accounts_receivable RENAME COLUMN recebido_em TO received_at;
            ALTER TABLE accounts_receivable RENAME COLUMN valor_recebido TO received_amount;
            ALTER TABLE accounts_receivable RENAME COLUMN categoria TO category;
            ALTER TABLE accounts_receivable RENAME COLUMN pedido_origem_id TO source_order_id;
            ALTER TABLE accounts_receivable RENAME COLUMN criado_em TO created_at;
        ELSE
            DROP TABLE contas_receber;
        END IF;
    END IF;

    IF EXISTS (SELECT FROM pg_tables WHERE schemaname = 'public' AND tablename = 'auditoria_financeira') THEN
        IF NOT EXISTS (SELECT FROM pg_tables WHERE schemaname = 'public' AND tablename = 'financial_audit') THEN
            ALTER TABLE auditoria_financeira RENAME TO financial_audit;
            ALTER TABLE financial_audit RENAME COLUMN id_auditoria TO id;
            ALTER TABLE financial_audit RENAME COLUMN tipo_entidade TO entity_type;
            ALTER TABLE financial_audit RENAME COLUMN entidade_id TO entity_id;
            ALTER TABLE financial_audit RENAME COLUMN acao TO action;
            ALTER TABLE financial_audit RENAME COLUMN realizado_por TO performed_by;
            ALTER TABLE financial_audit RENAME COLUMN descricao TO description;
            ALTER TABLE financial_audit RENAME COLUMN criado_em TO created_at;
        ELSE
            DROP TABLE auditoria_financeira;
        END IF;
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS accounts_payable (
    id          UUID          NOT NULL,
    version     BIGINT,
    description VARCHAR(300)  NOT NULL,
    due_date    DATE          NOT NULL,
    amount      NUMERIC(12,2) NOT NULL,
    status      VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    paid_at     TIMESTAMP,
    paid_amount NUMERIC(12,2),
    category    VARCHAR(100),
    supplier    VARCHAR(200),
    created_at  TIMESTAMP     NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_accounts_payable PRIMARY KEY (id),
    CONSTRAINT chk_accounts_payable_amount CHECK (amount > 0),
    CONSTRAINT chk_accounts_payable_status CHECK (status IN ('PENDING','PAID','OVERDUE','CANCELLED'))
);

CREATE TABLE IF NOT EXISTS accounts_receivable (
    id              UUID          NOT NULL,
    version         BIGINT,
    description     VARCHAR(300)  NOT NULL,
    due_date        DATE          NOT NULL,
    amount          NUMERIC(12,2) NOT NULL,
    status          VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    received_at     TIMESTAMP,
    received_amount NUMERIC(12,2),
    category        VARCHAR(100),
    source_order_id UUID,
    created_at      TIMESTAMP     NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_accounts_receivable PRIMARY KEY (id),
    CONSTRAINT chk_accounts_receivable_amount CHECK (amount > 0),
    CONSTRAINT chk_accounts_receivable_status CHECK (status IN ('PENDING','RECEIVED','OVERDUE','CANCELLED'))
);

CREATE TABLE IF NOT EXISTS financial_audit (
    id           UUID         NOT NULL DEFAULT gen_random_uuid(),
    entity_type  VARCHAR(30)  NOT NULL,
    entity_id    UUID         NOT NULL,
    action       VARCHAR(30)  NOT NULL,
    performed_by VARCHAR(150) NOT NULL,
    description  VARCHAR(500) NOT NULL,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_financial_audit PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_accounts_payable_status   ON accounts_payable (status);
CREATE INDEX IF NOT EXISTS idx_accounts_payable_due_date ON accounts_payable (due_date);
CREATE INDEX IF NOT EXISTS idx_accounts_receivable_status   ON accounts_receivable (status);
CREATE INDEX IF NOT EXISTS idx_accounts_receivable_due_date ON accounts_receivable (due_date);
CREATE INDEX IF NOT EXISTS idx_financial_audit_entity_id ON financial_audit (entity_id);
CREATE INDEX IF NOT EXISTS idx_financial_audit_created_at ON financial_audit (created_at DESC);
