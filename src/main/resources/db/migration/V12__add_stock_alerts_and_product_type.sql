CREATE TABLE stock_alerts (
    id_alert        UUID          PRIMARY KEY,
    stock_item_id   UUID          NOT NULL REFERENCES stock_items(id_stock_item),
    stock_item_name VARCHAR(150)  NOT NULL,
    current_qty     NUMERIC(10,3) NOT NULL,
    minimum_qty     NUMERIC(10,3) NOT NULL,
    resolved        BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP     NOT NULL,
    resolved_at     TIMESTAMP
);

CREATE INDEX idx_stock_alerts_item   ON stock_alerts(stock_item_id);
CREATE INDEX idx_stock_alerts_active ON stock_alerts(stock_item_id) WHERE resolved = FALSE;

ALTER TABLE product_recipes
    ADD COLUMN product_type VARCHAR(20) NOT NULL DEFAULT 'FABRICATED';
