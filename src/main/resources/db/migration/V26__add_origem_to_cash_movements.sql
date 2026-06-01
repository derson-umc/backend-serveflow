ALTER TABLE cash_movements
    ADD COLUMN IF NOT EXISTS origem VARCHAR(20) NOT NULL DEFAULT 'MANUAL';

CREATE INDEX IF NOT EXISTS idx_cash_movements_origem ON cash_movements (origem);
