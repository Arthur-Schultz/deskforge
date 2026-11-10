CREATE TABLE users (
    id                      UUID PRIMARY KEY,
    full_name               VARCHAR(120) NOT NULL,
    email                   VARCHAR(160) NOT NULL UNIQUE,
    password_hash           VARCHAR(100) NOT NULL,
    role                    VARCHAR(20)  NOT NULL,
    marketing_consent       BOOLEAN      NOT NULL DEFAULT FALSE,
    terms_accepted_at       TIMESTAMPTZ  NOT NULL,
    privacy_policy_version  VARCHAR(20)  NOT NULL,
    anonymized_at           TIMESTAMPTZ,
    created_at              TIMESTAMPTZ  NOT NULL
);

CREATE TABLE categories (
    id          BIGSERIAL PRIMARY KEY,
    slug        VARCHAR(60)  NOT NULL UNIQUE,
    name        VARCHAR(80)  NOT NULL,
    description VARCHAR(255) NOT NULL,
    position    INT          NOT NULL DEFAULT 0
);

CREATE TABLE products (
    id                 BIGSERIAL PRIMARY KEY,
    sku                VARCHAR(40)   NOT NULL UNIQUE,
    slug               VARCHAR(120)  NOT NULL UNIQUE,
    name               VARCHAR(120)  NOT NULL,
    short_description  VARCHAR(255)  NOT NULL,
    description        TEXT          NOT NULL,
    price              NUMERIC(10,2) NOT NULL CHECK (price > 0),
    compare_at_price   NUMERIC(10,2),
    stock              INT           NOT NULL CHECK (stock >= 0),
    art                VARCHAR(30)   NOT NULL,
    accent_color       VARCHAR(7)    NOT NULL,
    featured           BOOLEAN       NOT NULL DEFAULT FALSE,
    active             BOOLEAN       NOT NULL DEFAULT TRUE,
    category_id        BIGINT        NOT NULL REFERENCES categories(id),
    version            BIGINT        NOT NULL DEFAULT 0,
    created_at         TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_products_active ON products(active);

CREATE TABLE product_specs (
    product_id BIGINT       NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    position   INT          NOT NULL,
    label      VARCHAR(60)  NOT NULL,
    value      VARCHAR(160) NOT NULL,
    PRIMARY KEY (product_id, position)
);

CREATE SEQUENCE order_number_seq START WITH 1001;

CREATE TABLE orders (
    id                   UUID PRIMARY KEY,
    number               VARCHAR(20)   NOT NULL UNIQUE,
    user_id              UUID          NOT NULL REFERENCES users(id),
    status               VARCHAR(30)   NOT NULL,
    subtotal             NUMERIC(10,2) NOT NULL,
    shipping_cost        NUMERIC(10,2) NOT NULL,
    total                NUMERIC(10,2) NOT NULL,
    approximate_taxes    NUMERIC(10,2) NOT NULL,
    recipient_name       VARCHAR(120)  NOT NULL,
    zip_code             VARCHAR(9)    NOT NULL,
    street               VARCHAR(160)  NOT NULL,
    street_number        VARCHAR(20)   NOT NULL,
    complement           VARCHAR(80),
    district             VARCHAR(80)   NOT NULL,
    city                 VARCHAR(80)   NOT NULL,
    state                VARCHAR(2)    NOT NULL,
    payment_reference    VARCHAR(80),
    cancel_reason        VARCHAR(255),
    created_at           TIMESTAMPTZ   NOT NULL,
    paid_at              TIMESTAMPTZ,
    shipped_at           TIMESTAMPTZ,
    delivered_at         TIMESTAMPTZ,
    canceled_at          TIMESTAMPTZ,
    expires_at           TIMESTAMPTZ   NOT NULL
);

CREATE INDEX idx_orders_user ON orders(user_id);
CREATE INDEX idx_orders_status_expires ON orders(status, expires_at);

CREATE TABLE order_items (
    id            BIGSERIAL PRIMARY KEY,
    order_id      UUID          NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id    BIGINT        NOT NULL REFERENCES products(id),
    product_name  VARCHAR(120)  NOT NULL,
    product_sku   VARCHAR(40)   NOT NULL,
    unit_price    NUMERIC(10,2) NOT NULL,
    quantity      INT           NOT NULL CHECK (quantity > 0),
    line_total    NUMERIC(10,2) NOT NULL
);

CREATE INDEX idx_order_items_order ON order_items(order_id);

CREATE TABLE cookie_consents (
    id              UUID PRIMARY KEY,
    visitor_id      VARCHAR(64)  NOT NULL,
    analytics       BOOLEAN      NOT NULL,
    marketing       BOOLEAN      NOT NULL,
    policy_version  VARCHAR(20)  NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL
);

CREATE INDEX idx_cookie_consents_visitor ON cookie_consents(visitor_id);

CREATE TABLE data_subject_requests (
    id          UUID PRIMARY KEY,
    protocol    VARCHAR(20)  NOT NULL UNIQUE,
    full_name   VARCHAR(120) NOT NULL,
    email       VARCHAR(160) NOT NULL,
    type        VARCHAR(30)  NOT NULL,
    details     VARCHAR(1000),
    status      VARCHAR(20)  NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL,
    due_at      TIMESTAMPTZ  NOT NULL
);

CREATE TABLE contact_messages (
    id          UUID PRIMARY KEY,
    protocol    VARCHAR(20)   NOT NULL UNIQUE,
    full_name   VARCHAR(120)  NOT NULL,
    email       VARCHAR(160)  NOT NULL,
    subject     VARCHAR(120)  NOT NULL,
    message     VARCHAR(2000) NOT NULL,
    order_number VARCHAR(20),
    created_at  TIMESTAMPTZ   NOT NULL,
    respond_by  TIMESTAMPTZ   NOT NULL
);

CREATE TABLE access_logs (
    id          BIGSERIAL PRIMARY KEY,
    ip_address  VARCHAR(45)  NOT NULL,
    method      VARCHAR(10)  NOT NULL,
    path        VARCHAR(255) NOT NULL,
    status      INT          NOT NULL,
    user_id     UUID,
    created_at  TIMESTAMPTZ  NOT NULL
);

CREATE INDEX idx_access_logs_created ON access_logs(created_at);
