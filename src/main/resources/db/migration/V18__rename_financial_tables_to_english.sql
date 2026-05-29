-- =====================================================================
-- accounts_receivable (contas_receber)
-- =====================================================================
DO $$ BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'contas_receber') THEN
        ALTER TABLE contas_receber RENAME TO accounts_receivable;
    END IF;
END $$;

DO $$ BEGIN IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='accounts_receivable' AND column_name='id_conta_receber') THEN ALTER TABLE accounts_receivable RENAME COLUMN id_conta_receber TO id; END IF; END $$;
DO $$ BEGIN IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='accounts_receivable' AND column_name='descricao')        THEN ALTER TABLE accounts_receivable RENAME COLUMN descricao TO description; END IF; END $$;
DO $$ BEGIN IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='accounts_receivable' AND column_name='vencimento')       THEN ALTER TABLE accounts_receivable RENAME COLUMN vencimento TO due_date; END IF; END $$;
DO $$ BEGIN IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='accounts_receivable' AND column_name='valor')            THEN ALTER TABLE accounts_receivable RENAME COLUMN valor TO amount; END IF; END $$;
DO $$ BEGIN IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='accounts_receivable' AND column_name='recebido_em')      THEN ALTER TABLE accounts_receivable RENAME COLUMN recebido_em TO received_at; END IF; END $$;
DO $$ BEGIN IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='accounts_receivable' AND column_name='valor_recebido')   THEN ALTER TABLE accounts_receivable RENAME COLUMN valor_recebido TO received_amount; END IF; END $$;
DO $$ BEGIN IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='accounts_receivable' AND column_name='categoria')        THEN ALTER TABLE accounts_receivable RENAME COLUMN categoria TO category; END IF; END $$;
DO $$ BEGIN IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='accounts_receivable' AND column_name='pedido_origem_id') THEN ALTER TABLE accounts_receivable RENAME COLUMN pedido_origem_id TO source_order_id; END IF; END $$;
DO $$ BEGIN IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='accounts_receivable' AND column_name='criado_em')        THEN ALTER TABLE accounts_receivable RENAME COLUMN criado_em TO created_at; END IF; END $$;

UPDATE accounts_receivable SET status = 'PENDING'   WHERE status = 'PENDENTE';
UPDATE accounts_receivable SET status = 'RECEIVED'  WHERE status = 'RECEBIDA';
UPDATE accounts_receivable SET status = 'OVERDUE'   WHERE status = 'VENCIDA';
UPDATE accounts_receivable SET status = 'CANCELLED' WHERE status = 'CANCELADA';

ALTER TABLE accounts_receivable DROP CONSTRAINT IF EXISTS chk_conta_receber_status;
ALTER TABLE accounts_receivable DROP CONSTRAINT IF EXISTS chk_conta_receber_valor;

DO $$ BEGIN IF EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE table_schema='public' AND constraint_name='pk_conta_receber') THEN ALTER TABLE accounts_receivable RENAME CONSTRAINT pk_conta_receber TO pk_accounts_receivable; END IF; END $$;

DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE table_schema='public' AND constraint_name='chk_accounts_receivable_status') THEN ALTER TABLE accounts_receivable ADD CONSTRAINT chk_accounts_receivable_status CHECK (status IN ('PENDING','RECEIVED','OVERDUE','CANCELLED')); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE table_schema='public' AND constraint_name='chk_accounts_receivable_amount') THEN ALTER TABLE accounts_receivable ADD CONSTRAINT chk_accounts_receivable_amount CHECK (amount > 0); END IF; END $$;

-- =====================================================================
-- accounts_payable (contas_pagar)
-- =====================================================================
DO $$ BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'contas_pagar') THEN
        ALTER TABLE contas_pagar RENAME TO accounts_payable;
    END IF;
END $$;

DO $$ BEGIN IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='accounts_payable' AND column_name='id_conta_pagar') THEN ALTER TABLE accounts_payable RENAME COLUMN id_conta_pagar TO id; END IF; END $$;
DO $$ BEGIN IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='accounts_payable' AND column_name='descricao')      THEN ALTER TABLE accounts_payable RENAME COLUMN descricao TO description; END IF; END $$;
DO $$ BEGIN IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='accounts_payable' AND column_name='vencimento')     THEN ALTER TABLE accounts_payable RENAME COLUMN vencimento TO due_date; END IF; END $$;
DO $$ BEGIN IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='accounts_payable' AND column_name='valor')          THEN ALTER TABLE accounts_payable RENAME COLUMN valor TO amount; END IF; END $$;
DO $$ BEGIN IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='accounts_payable' AND column_name='pago_em')        THEN ALTER TABLE accounts_payable RENAME COLUMN pago_em TO paid_at; END IF; END $$;
DO $$ BEGIN IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='accounts_payable' AND column_name='valor_pago')     THEN ALTER TABLE accounts_payable RENAME COLUMN valor_pago TO paid_amount; END IF; END $$;
DO $$ BEGIN IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='accounts_payable' AND column_name='categoria')      THEN ALTER TABLE accounts_payable RENAME COLUMN categoria TO category; END IF; END $$;
DO $$ BEGIN IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='accounts_payable' AND column_name='fornecedor')     THEN ALTER TABLE accounts_payable RENAME COLUMN fornecedor TO supplier; END IF; END $$;
DO $$ BEGIN IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='accounts_payable' AND column_name='criado_em')      THEN ALTER TABLE accounts_payable RENAME COLUMN criado_em TO created_at; END IF; END $$;

UPDATE accounts_payable SET status = 'PENDING'   WHERE status = 'PENDENTE';
UPDATE accounts_payable SET status = 'PAID'      WHERE status = 'PAGA';
UPDATE accounts_payable SET status = 'OVERDUE'   WHERE status = 'VENCIDA';
UPDATE accounts_payable SET status = 'CANCELLED' WHERE status = 'CANCELADA';

ALTER TABLE accounts_payable DROP CONSTRAINT IF EXISTS chk_conta_pagar_status;
ALTER TABLE accounts_payable DROP CONSTRAINT IF EXISTS chk_conta_pagar_valor;

DO $$ BEGIN IF EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE table_schema='public' AND constraint_name='pk_conta_pagar') THEN ALTER TABLE accounts_payable RENAME CONSTRAINT pk_conta_pagar TO pk_accounts_payable; END IF; END $$;

DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE table_schema='public' AND constraint_name='chk_accounts_payable_status') THEN ALTER TABLE accounts_payable ADD CONSTRAINT chk_accounts_payable_status CHECK (status IN ('PENDING','PAID','OVERDUE','CANCELLED')); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE table_schema='public' AND constraint_name='chk_accounts_payable_amount') THEN ALTER TABLE accounts_payable ADD CONSTRAINT chk_accounts_payable_amount CHECK (amount > 0); END IF; END $$;

-- =====================================================================
-- financial_audit (auditoria_financeira)
-- =====================================================================
DO $$ BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'auditoria_financeira') THEN
        ALTER TABLE auditoria_financeira RENAME TO financial_audit;
    END IF;
END $$;

DO $$ BEGIN IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='financial_audit' AND column_name='id_auditoria')  THEN ALTER TABLE financial_audit RENAME COLUMN id_auditoria  TO id; END IF; END $$;
DO $$ BEGIN IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='financial_audit' AND column_name='tipo_entidade') THEN ALTER TABLE financial_audit RENAME COLUMN tipo_entidade TO entity_type; END IF; END $$;
DO $$ BEGIN IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='financial_audit' AND column_name='entidade_id')   THEN ALTER TABLE financial_audit RENAME COLUMN entidade_id   TO entity_id; END IF; END $$;
DO $$ BEGIN IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='financial_audit' AND column_name='acao')          THEN ALTER TABLE financial_audit RENAME COLUMN acao          TO action; END IF; END $$;
DO $$ BEGIN IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='financial_audit' AND column_name='realizado_por') THEN ALTER TABLE financial_audit RENAME COLUMN realizado_por TO performed_by; END IF; END $$;
DO $$ BEGIN IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='financial_audit' AND column_name='descricao')     THEN ALTER TABLE financial_audit RENAME COLUMN descricao     TO description; END IF; END $$;
DO $$ BEGIN IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='financial_audit' AND column_name='criado_em')     THEN ALTER TABLE financial_audit RENAME COLUMN criado_em     TO created_at; END IF; END $$;

DO $$ BEGIN IF EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE table_schema='public' AND constraint_name='pk_auditoria_financeira') THEN ALTER TABLE financial_audit RENAME CONSTRAINT pk_auditoria_financeira TO pk_financial_audit; END IF; END $$;

-- =====================================================================
-- Rename indexes
-- =====================================================================
DO $$ BEGIN IF EXISTS (SELECT 1 FROM pg_indexes WHERE schemaname='public' AND indexname='idx_contas_receber_status')    THEN ALTER INDEX idx_contas_receber_status    RENAME TO idx_accounts_receivable_status; END IF; END $$;
DO $$ BEGIN IF EXISTS (SELECT 1 FROM pg_indexes WHERE schemaname='public' AND indexname='idx_contas_receber_vencimento') THEN ALTER INDEX idx_contas_receber_vencimento RENAME TO idx_accounts_receivable_due_date; END IF; END $$;
DO $$ BEGIN IF EXISTS (SELECT 1 FROM pg_indexes WHERE schemaname='public' AND indexname='idx_contas_pagar_status')      THEN ALTER INDEX idx_contas_pagar_status      RENAME TO idx_accounts_payable_status; END IF; END $$;
DO $$ BEGIN IF EXISTS (SELECT 1 FROM pg_indexes WHERE schemaname='public' AND indexname='idx_contas_pagar_vencimento')  THEN ALTER INDEX idx_contas_pagar_vencimento  RENAME TO idx_accounts_payable_due_date; END IF; END $$;
DO $$ BEGIN IF EXISTS (SELECT 1 FROM pg_indexes WHERE schemaname='public' AND indexname='idx_auditoria_entidade_id')    THEN ALTER INDEX idx_auditoria_entidade_id    RENAME TO idx_financial_audit_entity_id; END IF; END $$;
DO $$ BEGIN IF EXISTS (SELECT 1 FROM pg_indexes WHERE schemaname='public' AND indexname='idx_auditoria_criado_em')      THEN ALTER INDEX idx_auditoria_criado_em      RENAME TO idx_financial_audit_created_at; END IF; END $$;
