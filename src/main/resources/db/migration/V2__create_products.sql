CREATE TABLE IF NOT EXISTS products (
    id_product  UUID          NOT NULL,
    version     BIGINT,
    name        VARCHAR(100)  NOT NULL,
    description VARCHAR(500),
    category    VARCHAR(100)  NOT NULL,
    brand       VARCHAR(80)   NOT NULL,
    price       NUMERIC(10,2) NOT NULL,
    portion     VARCHAR(50)   NOT NULL,
    active      BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP     NOT NULL,
    CONSTRAINT pk_products PRIMARY KEY (id_product)
);
