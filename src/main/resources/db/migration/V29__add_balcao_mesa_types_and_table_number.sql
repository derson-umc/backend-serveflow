-- Rename LOCAL to BALCAO to align with new order type names
UPDATE orders SET type = 'BALCAO' WHERE type = 'LOCAL';

-- Add table number column for MESA type orders
ALTER TABLE orders ADD COLUMN IF NOT EXISTS table_number VARCHAR(30);
