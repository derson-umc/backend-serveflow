UPDATE orders     SET status = 'PENDENTE' WHERE status = 'RASCUNHO';
UPDATE order_items SET status = 'PENDENTE' WHERE status = 'RASCUNHO';

ALTER TABLE order_items ALTER COLUMN status SET DEFAULT 'PENDENTE';
