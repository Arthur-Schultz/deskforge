CREATE TABLE email_outbox (
    id               UUID         PRIMARY KEY,
    dedup_key        VARCHAR(200) NOT NULL UNIQUE,
    template         VARCHAR(40)  NOT NULL,
    recipient        VARCHAR(160) NOT NULL,
    subject          VARCHAR(200) NOT NULL,
    html_body        TEXT,
    text_body        TEXT,
    status           VARCHAR(20)  NOT NULL,
    attempts         INTEGER      NOT NULL DEFAULT 0,
    next_attempt_at  TIMESTAMPTZ  NOT NULL,
    last_error       VARCHAR(500),
    created_at       TIMESTAMPTZ  NOT NULL,
    sent_at          TIMESTAMPTZ,
    CONSTRAINT chk_email_outbox_status CHECK (status IN ('PENDING', 'SENT', 'FAILED')),
    CONSTRAINT chk_email_outbox_template CHECK (template IN ('PAYMENT_CONFIRMED', 'ORDER_SHIPPED', 'ORDER_CANCELED', 'PASSWORD_RESET', 'PASSWORD_CHANGED')),
    CONSTRAINT chk_email_outbox_attempts CHECK (attempts >= 0),
    CONSTRAINT chk_email_outbox_body CHECK (status <> 'PENDING' OR (html_body IS NOT NULL AND text_body IS NOT NULL)),
    CONSTRAINT chk_email_outbox_sent CHECK ((status = 'SENT') = (sent_at IS NOT NULL))
);

CREATE INDEX idx_email_outbox_pending ON email_outbox (next_attempt_at) WHERE status = 'PENDING';

CREATE TABLE password_reset_tokens (
    id          UUID        PRIMARY KEY,
    user_id     UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token_hash  VARCHAR(64) NOT NULL UNIQUE,
    created_at  TIMESTAMPTZ NOT NULL,
    expires_at  TIMESTAMPTZ NOT NULL,
    used_at     TIMESTAMPTZ,
    CONSTRAINT chk_password_reset_tokens_hash CHECK (token_hash ~ '^[0-9a-f]{64}$'),
    CONSTRAINT chk_password_reset_tokens_expiry CHECK (expires_at > created_at)
);

CREATE INDEX idx_password_reset_tokens_user ON password_reset_tokens (user_id, created_at DESC);
