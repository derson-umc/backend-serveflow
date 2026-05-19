CREATE TABLE IF NOT EXISTS menus (
    id_menu         UUID         NOT NULL,
    version         BIGINT,
    name            VARCHAR(100) NOT NULL,
    status          VARCHAR(20)  NOT NULL,
    active_order_id UUID,
    created_at      TIMESTAMP    NOT NULL,
    CONSTRAINT pk_menus PRIMARY KEY (id_menu)
);

CREATE TABLE IF NOT EXISTS menu_items (
    id_menu_item  UUID          NOT NULL,
    id_menu       UUID          NOT NULL,
    product_id    UUID          NOT NULL,
    name          VARCHAR(120)  NOT NULL,
    description   VARCHAR(500),
    price         NUMERIC(10,2) NOT NULL,
    available     BOOLEAN       NOT NULL DEFAULT TRUE,
    removed       BOOLEAN       NOT NULL DEFAULT FALSE,
    removed_by    VARCHAR(120),
    CONSTRAINT pk_menu_items      PRIMARY KEY (id_menu_item),
    CONSTRAINT fk_menu_items_menu FOREIGN KEY (id_menu) REFERENCES menus (id_menu)
);
