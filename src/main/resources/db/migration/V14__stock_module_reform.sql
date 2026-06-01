ALTER TABLE stock_items
    ADD COLUMN IF NOT EXISTS category     VARCHAR(100),
    ADD COLUMN IF NOT EXISTS supplier     VARCHAR(200),
    ADD COLUMN IF NOT EXISTS average_cost NUMERIC(10,4),
    ADD COLUMN IF NOT EXISTS status       VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';

ALTER TABLE stock_movements
    ALTER COLUMN type TYPE VARCHAR(30);

ALTER TABLE stock_movements
    ADD COLUMN IF NOT EXISTS stock_item_name VARCHAR(150),
    ADD COLUMN IF NOT EXISTS balance_before  NUMERIC(12,4),
    ADD COLUMN IF NOT EXISTS balance_after   NUMERIC(12,4);

UPDATE stock_movements
SET type = 'ORDER_CONSUMPTION'
WHERE type = 'EXIT'
  AND reference_id IS NOT NULL;
