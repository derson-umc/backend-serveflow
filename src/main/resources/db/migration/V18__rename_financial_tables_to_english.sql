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

UPDATE accounts_receivable SET status = 'PENDING'   WHERE status = 'PENDENTE';
UPDATE accounts_receivable SET status = 'RECEIVED'  WHERE status = 'RECEBIDA';
UPDATE accounts_receivable SET status = 'OVERDUE'   WHERE status = 'VENCIDA';
UPDATE accounts_receivable SET status = 'CANCELLED' WHERE status = 'CANCELADA';

ALTER TABLE accounts_receivable DROP CONSTRAINT chk_conta_receber_status;
ALTER TABLE accounts_receivable DROP CONSTRAINT chk_conta_receber_valor;
ALTER TABLE accounts_receivable RENAME CONSTRAINT pk_conta_receber TO pk_accounts_receivable;
ALTER TABLE accounts_receivable ADD CONSTRAINT chk_accounts_receivable_status
    CHECK (status IN ('PENDING','RECEIVED','OVERDUE','CANCELLED'));
ALTER TABLE accounts_receivable ADD CONSTRAINT chk_accounts_receivable_amount CHECK (amount > 0);

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

UPDATE accounts_payable SET status = 'PENDING'   WHERE status = 'PENDENTE';
UPDATE accounts_payable SET status = 'PAID'       WHERE status = 'PAGA';
UPDATE accounts_payable SET status = 'OVERDUE'    WHERE status = 'VENCIDA';
UPDATE accounts_payable SET status = 'CANCELLED'  WHERE status = 'CANCELADA';

ALTER TABLE accounts_payable DROP CONSTRAINT chk_conta_pagar_status;
ALTER TABLE accounts_payable DROP CONSTRAINT chk_conta_pagar_valor;
ALTER TABLE accounts_payable RENAME CONSTRAINT pk_conta_pagar TO pk_accounts_payable;
ALTER TABLE accounts_payable ADD CONSTRAINT chk_accounts_payable_status
    CHECK (status IN ('PENDING','PAID','OVERDUE','CANCELLED'));
ALTER TABLE accounts_payable ADD CONSTRAINT chk_accounts_payable_amount CHECK (amount > 0);

ALTER TABLE auditoria_financeira RENAME TO financial_audit;

ALTER TABLE financial_audit RENAME COLUMN id_auditoria TO id;
ALTER TABLE financial_audit RENAME COLUMN tipo_entidade TO entity_type;
ALTER TABLE financial_audit RENAME COLUMN entidade_id TO entity_id;
ALTER TABLE financial_audit RENAME COLUMN acao TO action;
ALTER TABLE financial_audit RENAME COLUMN realizado_por TO performed_by;
ALTER TABLE financial_audit RENAME COLUMN descricao TO description;
ALTER TABLE financial_audit RENAME COLUMN criado_em TO created_at;

ALTER TABLE financial_audit RENAME CONSTRAINT pk_auditoria_financeira TO pk_financial_audit;

ALTER INDEX idx_contas_receber_status    RENAME TO idx_accounts_receivable_status;
ALTER INDEX idx_contas_receber_vencimento RENAME TO idx_accounts_receivable_due_date;
ALTER INDEX idx_contas_pagar_status      RENAME TO idx_accounts_payable_status;
ALTER INDEX idx_contas_pagar_vencimento  RENAME TO idx_accounts_payable_due_date;
ALTER INDEX idx_auditoria_entidade_id    RENAME TO idx_financial_audit_entity_id;
ALTER INDEX idx_auditoria_criado_em      RENAME TO idx_financial_audit_created_at;
