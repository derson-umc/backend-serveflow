-- Increase column size before renaming values
ALTER TABLE orders ALTER COLUMN status TYPE VARCHAR(30);

UPDATE orders SET status = 'RASCUNHO'  WHERE status = 'CREATED';
UPDATE orders SET status = 'ENVIADO'   WHERE status = 'CONFIRMED';
UPDATE orders SET status = 'EM_PREPARO' WHERE status = 'IN_PREPARATION';
UPDATE orders SET status = 'PRONTO'    WHERE status = 'READY';
UPDATE orders SET status = 'A_CAMINHO' WHERE status = 'OUT_FOR_DELIVERY';
UPDATE orders SET status = 'ENTREGUE'  WHERE status = 'DELIVERED';
UPDATE orders SET status = 'CANCELADO' WHERE status = 'CANCELLED';
