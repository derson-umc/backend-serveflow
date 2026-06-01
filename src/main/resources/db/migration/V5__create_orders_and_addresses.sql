CREATE TABLE IF NOT EXISTS addresses (
    id_address UUID         NOT NULL DEFAULT gen_random_uuid(),
    cep        VARCHAR(10),
    street     VARCHAR(200) NOT NULL,
    city       VARCHAR(100) NOT NULL,
    state      VARCHAR(2)   NOT NULL,
    number     VARCHAR(20)  NOT NULL,
    complement VARCHAR(200),
    CONSTRAINT pk_addresses PRIMARY KEY (id_address)
);

CREATE TABLE IF NOT EXISTS orders (
    id_order      UUID         NOT NULL,
    version       BIGINT,
    customer_name VARCHAR(150) NOT NULL,
    id_address    UUID,
    type          VARCHAR(20)  NOT NULL,
    status        VARCHAR(20)  NOT NULL,
    created_at    TIMESTAMP    NOT NULL,
    observation   VARCHAR(500),
    CONSTRAINT pk_orders         PRIMARY KEY (id_order),
    CONSTRAINT fk_orders_address FOREIGN KEY (id_address) REFERENCES addresses (id_address)
);

CREATE TABLE IF NOT EXISTS order_items (
    id_order_item UUID          NOT NULL,
    id_order      UUID          NOT NULL,
    product_id    UUID,
    product_name  VARCHAR(120)  NOT NULL,
    quantity      INT           NOT NULL,
    unit_price    NUMERIC(10,2) NOT NULL,
    observation   VARCHAR(300),
    CONSTRAINT pk_order_items       PRIMARY KEY (id_order_item),
    CONSTRAINT fk_order_items_order FOREIGN KEY (id_order) REFERENCES orders (id_order)
);

CREATE TABLE IF NOT EXISTS item_additionals (
    id_item_additional UUID          NOT NULL,
    id_order_item      UUID          NOT NULL,
    name               VARCHAR(120)  NOT NULL,
    quantity           INT           NOT NULL,
    unit_price         NUMERIC(10,2) NOT NULL,
    CONSTRAINT pk_item_additionals            PRIMARY KEY (id_item_additional),
    CONSTRAINT fk_item_additionals_order_item FOREIGN KEY (id_order_item) REFERENCES order_items (id_order_item)
);
