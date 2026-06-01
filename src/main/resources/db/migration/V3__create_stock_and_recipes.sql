CREATE TABLE IF NOT EXISTS stock_items (
    id_stock_item    UUID          NOT NULL,
    version          BIGINT,
    name             VARCHAR(100)  NOT NULL,
    unit             VARCHAR(20)   NOT NULL,
    current_quantity NUMERIC(12,4) NOT NULL,
    minimum_quantity NUMERIC(12,4) NOT NULL,
    created_at       TIMESTAMP     NOT NULL,
    CONSTRAINT pk_stock_items PRIMARY KEY (id_stock_item)
);

CREATE TABLE IF NOT EXISTS stock_movements (
    id_movement   UUID          NOT NULL,
    stock_item_id UUID          NOT NULL,
    type          VARCHAR(10)   NOT NULL,
    quantity      NUMERIC(12,4) NOT NULL,
    reason        VARCHAR(500),
    reference_id  UUID,
    created_at    TIMESTAMP     NOT NULL,
    CONSTRAINT pk_stock_movements PRIMARY KEY (id_movement)
);

CREATE TABLE IF NOT EXISTS product_recipes (
    id_recipe    UUID         NOT NULL,
    version      BIGINT,
    product_id   UUID         NOT NULL,
    product_name VARCHAR(120) NOT NULL,
    CONSTRAINT pk_product_recipes         PRIMARY KEY (id_recipe),
    CONSTRAINT uq_product_recipes_product UNIQUE (product_id)
);

CREATE TABLE IF NOT EXISTS recipe_ingredients (
    id_ingredient     UUID          NOT NULL,
    id_recipe         UUID          NOT NULL,
    stock_item_id     UUID          NOT NULL,
    stock_item_name   VARCHAR(100)  NOT NULL,
    quantity_per_unit NUMERIC(12,4) NOT NULL,
    unit              VARCHAR(20)   NOT NULL,
    CONSTRAINT pk_recipe_ingredients        PRIMARY KEY (id_ingredient),
    CONSTRAINT fk_recipe_ingredients_recipe FOREIGN KEY (id_recipe) REFERENCES product_recipes (id_recipe)
);
