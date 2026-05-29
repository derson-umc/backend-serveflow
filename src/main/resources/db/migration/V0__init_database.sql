CREATE TABLE users (
    id          BIGSERIAL    NOT NULL,
    username    VARCHAR(50)  NOT NULL,
    password    VARCHAR(255) NOT NULL,
    role        VARCHAR(20)  NOT NULL,
    jobposition VARCHAR(60)  NOT NULL,
    email       VARCHAR(120),
    CONSTRAINT pk_users                   PRIMARY KEY (id),
    CONSTRAINT uq_users_username          UNIQUE (username),
    CONSTRAINT chk_users_username_lowercase CHECK (username = LOWER(username))
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_users_email ON users (email);

CREATE TABLE password_reset_tokens (
    id         UUID        NOT NULL DEFAULT gen_random_uuid(),
    username   VARCHAR(64) NOT NULL,
    token      VARCHAR(6)  NOT NULL,
    expires_at TIMESTAMP   NOT NULL,
    used       BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP   NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_password_reset_tokens PRIMARY KEY (id)
);

CREATE INDEX idx_prt_username ON password_reset_tokens (username);
CREATE INDEX idx_prt_token    ON password_reset_tokens (token);

CREATE TABLE refresh_tokens (
    id         BIGSERIAL   NOT NULL,
    user_id    BIGINT      NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    expires_at TIMESTAMP   NOT NULL,
    CONSTRAINT pk_refresh_tokens      PRIMARY KEY (id),
    CONSTRAINT uq_refresh_token_hash  UNIQUE (token_hash),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_rt_token_hash ON refresh_tokens (token_hash);
CREATE INDEX idx_rt_user_id    ON refresh_tokens (user_id);

CREATE TABLE products (
    id_product              UUID          NOT NULL,
    version                 BIGINT        NOT NULL DEFAULT 0,
    name                    VARCHAR(100)  NOT NULL,
    description             VARCHAR(500),
    category                VARCHAR(100)  NOT NULL,
    brand                   VARCHAR(80)   NOT NULL,
    price                   NUMERIC(10,2) NOT NULL,
    portion                 VARCHAR(50)   NOT NULL,
    active                  BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at              TIMESTAMP     NOT NULL,
    image_url               VARCHAR(2048),
    requires_technical_sheet BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_products PRIMARY KEY (id_product)
);

CREATE TABLE stock_items (
    id_stock_item    UUID          NOT NULL,
    version          BIGINT,
    name             VARCHAR(100)  NOT NULL,
    unit             VARCHAR(20)   NOT NULL,
    current_quantity NUMERIC(12,4) NOT NULL,
    minimum_quantity NUMERIC(12,4) NOT NULL,
    created_at       TIMESTAMP     NOT NULL,
    category         VARCHAR(100),
    supplier         VARCHAR(200),
    average_cost     NUMERIC(10,4),
    status           VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT pk_stock_items PRIMARY KEY (id_stock_item)
);

CREATE TABLE stock_movements (
    id_movement     UUID          NOT NULL,
    stock_item_id   UUID          NOT NULL,
    type            VARCHAR(30)   NOT NULL,
    quantity        NUMERIC(12,4) NOT NULL,
    reason          VARCHAR(500),
    reference_id    UUID,
    created_at      TIMESTAMP     NOT NULL,
    stock_item_name VARCHAR(150),
    balance_before  NUMERIC(12,4),
    balance_after   NUMERIC(12,4),
    CONSTRAINT pk_stock_movements PRIMARY KEY (id_movement)
);

CREATE TABLE stock_alerts (
    id_alert        UUID          NOT NULL,
    stock_item_id   UUID          NOT NULL,
    stock_item_name VARCHAR(150)  NOT NULL,
    current_qty     NUMERIC(10,3) NOT NULL,
    minimum_qty     NUMERIC(10,3) NOT NULL,
    resolved        BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP     NOT NULL,
    resolved_at     TIMESTAMP,
    CONSTRAINT pk_stock_alerts  PRIMARY KEY (id_alert),
    CONSTRAINT fk_stock_alerts_item FOREIGN KEY (stock_item_id) REFERENCES stock_items (id_stock_item)
);

CREATE INDEX idx_stock_alerts_item   ON stock_alerts (stock_item_id);
CREATE INDEX idx_stock_alerts_active ON stock_alerts (stock_item_id) WHERE resolved = FALSE;

CREATE TABLE product_recipes (
    id_recipe       UUID         NOT NULL,
    version         BIGINT,
    product_id      UUID         NOT NULL,
    product_name    VARCHAR(120) NOT NULL,
    preparation_mode TEXT,
    product_type    VARCHAR(20)  NOT NULL DEFAULT 'FABRICATED',
    CONSTRAINT pk_product_recipes         PRIMARY KEY (id_recipe),
    CONSTRAINT uq_product_recipes_product UNIQUE (product_id)
);

CREATE TABLE recipe_ingredients (
    id_ingredient     UUID          NOT NULL,
    id_recipe         UUID          NOT NULL,
    stock_item_id     UUID          NOT NULL,
    stock_item_name   VARCHAR(100)  NOT NULL,
    quantity_per_unit NUMERIC(12,4) NOT NULL,
    unit              VARCHAR(20)   NOT NULL,
    validity          DATE,
    CONSTRAINT pk_recipe_ingredients        PRIMARY KEY (id_ingredient),
    CONSTRAINT fk_recipe_ingredients_recipe FOREIGN KEY (id_recipe)     REFERENCES product_recipes (id_recipe),
    CONSTRAINT fk_recipe_ingredients_stock  FOREIGN KEY (stock_item_id) REFERENCES stock_items (id_stock_item) ON DELETE RESTRICT
);

CREATE TABLE menus (
    id_menu         UUID         NOT NULL,
    version         BIGINT,
    name            VARCHAR(100) NOT NULL,
    status          VARCHAR(20)  NOT NULL,
    active_order_id UUID,
    created_at      TIMESTAMP    NOT NULL,
    day_of_week     VARCHAR(15),
    shift           VARCHAR(20),
    CONSTRAINT pk_menus PRIMARY KEY (id_menu)
);

CREATE TABLE menu_items (
    id_menu_item UUID          NOT NULL,
    id_menu      UUID          NOT NULL,
    product_id   UUID,
    name         VARCHAR(120)  NOT NULL,
    description  VARCHAR(500),
    price        NUMERIC(10,2) NOT NULL,
    available    BOOLEAN       NOT NULL DEFAULT TRUE,
    removed      BOOLEAN       NOT NULL DEFAULT FALSE,
    removed_by   VARCHAR(120),
    CONSTRAINT pk_menu_items      PRIMARY KEY (id_menu_item),
    CONSTRAINT fk_menu_items_menu FOREIGN KEY (id_menu) REFERENCES menus (id_menu)
);

CREATE TABLE addresses (
    id_address UUID         NOT NULL DEFAULT gen_random_uuid(),
    cep        VARCHAR(10),
    street     VARCHAR(200) NOT NULL,
    city       VARCHAR(100) NOT NULL,
    state      VARCHAR(2)   NOT NULL,
    number     VARCHAR(20)  NOT NULL,
    complement VARCHAR(200),
    CONSTRAINT pk_addresses PRIMARY KEY (id_address)
);

CREATE TABLE orders (
    id_order      UUID         NOT NULL,
    version       BIGINT,
    customer_name VARCHAR(150) NOT NULL,
    id_address    UUID,
    type          VARCHAR(20)  NOT NULL,
    status        VARCHAR(20)  NOT NULL,
    created_at    TIMESTAMP    NOT NULL,
    observation   VARCHAR(500),
    payment_method VARCHAR(20),
    CONSTRAINT pk_orders         PRIMARY KEY (id_order),
    CONSTRAINT fk_orders_address FOREIGN KEY (id_address) REFERENCES addresses (id_address)
);

CREATE TABLE order_items (
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

CREATE TABLE item_additionals (
    id_item_additional UUID          NOT NULL,
    id_order_item      UUID          NOT NULL,
    name               VARCHAR(120)  NOT NULL,
    quantity           INT           NOT NULL,
    unit_price         NUMERIC(10,2) NOT NULL,
    CONSTRAINT pk_item_additionals            PRIMARY KEY (id_item_additional),
    CONSTRAINT fk_item_additionals_order_item FOREIGN KEY (id_order_item) REFERENCES order_items (id_order_item)
);

CREATE TABLE accounts_receivable (
    id              UUID          NOT NULL,
    version         BIGINT,
    description     VARCHAR(300)  NOT NULL,
    due_date        DATE          NOT NULL,
    amount          NUMERIC(12,2) NOT NULL,
    status          VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    received_at     TIMESTAMP,
    received_amount NUMERIC(12,2),
    category        VARCHAR(100),
    source_order_id UUID,
    created_at      TIMESTAMP     NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_accounts_receivable        PRIMARY KEY (id),
    CONSTRAINT chk_accounts_receivable_amount CHECK (amount > 0),
    CONSTRAINT chk_accounts_receivable_status CHECK (status IN ('PENDING', 'RECEIVED', 'OVERDUE', 'CANCELLED'))
);

CREATE INDEX idx_accounts_receivable_status   ON accounts_receivable (status);
CREATE INDEX idx_accounts_receivable_due_date ON accounts_receivable (due_date);

CREATE TABLE accounts_payable (
    id          UUID          NOT NULL,
    version     BIGINT,
    description VARCHAR(300)  NOT NULL,
    due_date    DATE          NOT NULL,
    amount      NUMERIC(12,2) NOT NULL,
    status      VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    paid_at     TIMESTAMP,
    paid_amount NUMERIC(12,2),
    category    VARCHAR(100),
    supplier    VARCHAR(200),
    created_at  TIMESTAMP     NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_accounts_payable        PRIMARY KEY (id),
    CONSTRAINT chk_accounts_payable_amount CHECK (amount > 0),
    CONSTRAINT chk_accounts_payable_status CHECK (status IN ('PENDING', 'PAID', 'OVERDUE', 'CANCELLED'))
);

CREATE INDEX idx_accounts_payable_status   ON accounts_payable (status);
CREATE INDEX idx_accounts_payable_due_date ON accounts_payable (due_date);

CREATE TABLE financial_audit (
    id           UUID         NOT NULL DEFAULT gen_random_uuid(),
    entity_type  VARCHAR(30)  NOT NULL,
    entity_id    UUID         NOT NULL,
    action       VARCHAR(30)  NOT NULL,
    performed_by VARCHAR(150) NOT NULL,
    description  VARCHAR(500) NOT NULL,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_financial_audit PRIMARY KEY (id)
);

CREATE INDEX idx_financial_audit_entity_id  ON financial_audit (entity_id);
CREATE INDEX idx_financial_audit_created_at ON financial_audit (created_at DESC);

CREATE TABLE cash_sessions (
    id                  UUID          NOT NULL,
    version             BIGINT,
    status              VARCHAR(10)   NOT NULL DEFAULT 'OPEN',
    initial_balance     NUMERIC(12,2) NOT NULL DEFAULT 0,
    observation         VARCHAR(500),
    opened_at           TIMESTAMP     NOT NULL DEFAULT NOW(),
    closed_at           TIMESTAMP,
    opened_by           VARCHAR(150)  NOT NULL,
    closed_by           VARCHAR(150),
    closing_observation VARCHAR(500),
    CONSTRAINT pk_cash_sessions         PRIMARY KEY (id),
    CONSTRAINT chk_cash_session_status  CHECK (status IN ('OPEN', 'CLOSED')),
    CONSTRAINT chk_cash_session_balance CHECK (initial_balance >= 0)
);

CREATE INDEX idx_cash_sessions_status    ON cash_sessions (status);
CREATE INDEX idx_cash_sessions_opened_at ON cash_sessions (opened_at DESC);

CREATE TABLE cash_movements (
    id           UUID          NOT NULL DEFAULT gen_random_uuid(),
    session_id   UUID          NOT NULL,
    type         VARCHAR(10)   NOT NULL,
    amount       NUMERIC(12,2) NOT NULL,
    description  VARCHAR(300)  NOT NULL,
    category     VARCHAR(100),
    performed_by VARCHAR(150)  NOT NULL,
    created_at   TIMESTAMP     NOT NULL DEFAULT NOW(),
    origem       VARCHAR(20)   NOT NULL DEFAULT 'MANUAL',
    CONSTRAINT pk_cash_movements         PRIMARY KEY (id),
    CONSTRAINT fk_cash_movement_session  FOREIGN KEY (session_id) REFERENCES cash_sessions (id),
    CONSTRAINT chk_cash_movement_type    CHECK (type IN ('INCOME', 'EXPENSE')),
    CONSTRAINT chk_cash_movement_amount  CHECK (amount > 0)
);

CREATE INDEX idx_cash_movements_session ON cash_movements (session_id);
CREATE INDEX idx_cash_movements_created ON cash_movements (created_at);
CREATE INDEX idx_cash_movements_origem  ON cash_movements (origem);

CREATE TABLE access_log (
    id          BIGSERIAL    NOT NULL,
    user_id     BIGINT,
    ip          VARCHAR(45),
    endpoint    VARCHAR(255),
    http_method VARCHAR(10),
    http_status INTEGER,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_access_log PRIMARY KEY (id)
);

CREATE INDEX idx_access_log_user_id    ON access_log (user_id);
CREATE INDEX idx_access_log_ip         ON access_log (ip);
CREATE INDEX idx_access_log_created_at ON access_log (created_at);

CREATE TABLE audit_log (
    id            BIGSERIAL    NOT NULL,
    user_id       BIGINT,
    action        VARCHAR(100) NOT NULL,
    entity        VARCHAR(100),
    entity_id     BIGINT,
    previous_data TEXT,
    new_data      TEXT,
    ip            VARCHAR(45),
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_audit_log PRIMARY KEY (id)
);

CREATE INDEX idx_audit_log_user_id    ON audit_log (user_id);
CREATE INDEX idx_audit_log_action     ON audit_log (action);
CREATE INDEX idx_audit_log_created_at ON audit_log (created_at);

CREATE TABLE error_log (
    id         BIGSERIAL NOT NULL,
    message    TEXT      NOT NULL,
    stacktrace TEXT,
    service    VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_error_log PRIMARY KEY (id)
);

CREATE INDEX idx_error_log_created_at ON error_log (created_at);
