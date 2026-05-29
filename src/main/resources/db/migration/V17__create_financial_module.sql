CREATE TABLE contas_receber (
    id_conta_receber   UUID         NOT NULL,
    version            BIGINT,
    descricao          VARCHAR(300) NOT NULL,
    vencimento         DATE         NOT NULL,
    valor              NUMERIC(12,2) NOT NULL,
    status             VARCHAR(20)  NOT NULL DEFAULT 'PENDENTE',
    recebido_em        TIMESTAMP,
    valor_recebido     NUMERIC(12,2),
    categoria          VARCHAR(100),
    pedido_origem_id   UUID,
    criado_em          TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_conta_receber PRIMARY KEY (id_conta_receber),
    CONSTRAINT chk_conta_receber_valor CHECK (valor > 0),
    CONSTRAINT chk_conta_receber_status CHECK (status IN ('PENDENTE','RECEBIDA','VENCIDA','CANCELADA'))
);

CREATE TABLE contas_pagar (
    id_conta_pagar  UUID         NOT NULL,
    version         BIGINT,
    descricao       VARCHAR(300) NOT NULL,
    vencimento      DATE         NOT NULL,
    valor           NUMERIC(12,2) NOT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDENTE',
    pago_em         TIMESTAMP,
    valor_pago      NUMERIC(12,2),
    categoria       VARCHAR(100),
    fornecedor      VARCHAR(200),
    criado_em       TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_conta_pagar PRIMARY KEY (id_conta_pagar),
    CONSTRAINT chk_conta_pagar_valor CHECK (valor > 0),
    CONSTRAINT chk_conta_pagar_status CHECK (status IN ('PENDENTE','PAGA','VENCIDA','CANCELADA'))
);

CREATE TABLE auditoria_financeira (
    id_auditoria   UUID         NOT NULL DEFAULT gen_random_uuid(),
    tipo_entidade  VARCHAR(30)  NOT NULL,
    entidade_id    UUID         NOT NULL,
    acao           VARCHAR(30)  NOT NULL,
    realizado_por  VARCHAR(150) NOT NULL,
    descricao      VARCHAR(500) NOT NULL,
    criado_em      TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_auditoria_financeira PRIMARY KEY (id_auditoria)
);

CREATE INDEX IF NOT EXISTS idx_contas_receber_status   ON contas_receber (status);
CREATE INDEX IF NOT EXISTS idx_contas_receber_vencimento ON contas_receber (vencimento);
CREATE INDEX IF NOT EXISTS idx_contas_pagar_status     ON contas_pagar (status);
CREATE INDEX IF NOT EXISTS idx_contas_pagar_vencimento ON contas_pagar (vencimento);
CREATE INDEX IF NOT EXISTS idx_auditoria_entidade_id   ON auditoria_financeira (entidade_id);
CREATE INDEX IF NOT EXISTS idx_auditoria_criado_em     ON auditoria_financeira (criado_em DESC);
