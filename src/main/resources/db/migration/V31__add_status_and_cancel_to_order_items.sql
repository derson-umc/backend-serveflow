-- Add status and cancel_reason to order_items
ALTER TABLE order_items ADD COLUMN IF NOT EXISTS status       VARCHAR(30);
ALTER TABLE order_items ADD COLUMN IF NOT EXISTS cancel_reason VARCHAR(300);

-- Sync item status from parent order status (best-effort for existing records)
UPDATE order_items oi
SET status = o.status
FROM orders o
WHERE oi.id_order = o.id_order;

-- Ensure status is never null going forward
UPDATE order_items SET status = 'RASCUNHO' WHERE status IS NULL;
ALTER TABLE order_items ALTER COLUMN status SET NOT NULL;
ALTER TABLE order_items ALTER COLUMN status SET DEFAULT 'RASCUNHO';
