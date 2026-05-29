UPDATE products SET version = 0 WHERE version IS NULL;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema  = 'public'
          AND table_name    = 'products'
          AND column_name   = 'version'
          AND is_nullable   = 'YES'
    ) THEN
        ALTER TABLE products ALTER COLUMN version SET NOT NULL;
    END IF;
END $$;

ALTER TABLE products ALTER COLUMN version SET DEFAULT 0;
