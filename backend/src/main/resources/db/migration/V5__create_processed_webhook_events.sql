CREATE TABLE processed_webhook_events (
    event_id      VARCHAR(255) PRIMARY KEY,
    type          VARCHAR(100) NOT NULL,
    processed_at  TIMESTAMPTZ  NOT NULL
);
