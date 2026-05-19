ALTER TABLE stock_items
    ADD COLUMN category     VARCHAR(100),
    ADD COLUMN supplier     VARCHAR(200),
    ADD COLUMN average_cost NUMERIC(10, 4),
    ADD COLUMN status       VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';

ALTER TABLE stock_movements
    ALTER COLUMN type TYPE VARCHAR(30);

ALTER TABLE stock_movements
    ADD COLUMN stock_item_name VARCHAR(150),
    ADD COLUMN balance_before  NUMERIC(12, 4),
    ADD COLUMN balance_after   NUMERIC(12, 4);

UPDATE stock_movements
SET type = 'ORDER_CONSUMPTION'
WHERE type = 'EXIT'
  AND reference_id IS NOT NULL;
