ALTER TABLE users
    ADD CONSTRAINT chk_users_role CHECK (role IN ('CUSTOMER', 'ADMIN')),
    ADD CONSTRAINT chk_users_email_lowercase CHECK (email = lower(email)),
    ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW();

ALTER TABLE products
    ADD CONSTRAINT chk_products_art CHECK (art IN ('KEYBOARD', 'MOUSE', 'HEADSET', 'DESKMAT', 'MICROPHONE', 'WEBCAM', 'LIGHTBAR', 'MONITOR_ARM')),
    ADD CONSTRAINT chk_products_accent_color CHECK (accent_color ~ '^#[0-9A-Fa-f]{6}$'),
    ADD CONSTRAINT chk_products_compare_at_price CHECK (compare_at_price IS NULL OR compare_at_price > price),
    ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW();

ALTER TABLE orders
    ADD CONSTRAINT chk_orders_status CHECK (status IN ('AWAITING_PAYMENT', 'PAID', 'SHIPPED', 'DELIVERED', 'CANCELED')),
    ADD CONSTRAINT chk_orders_amounts CHECK (subtotal >= 0 AND shipping_cost >= 0 AND approximate_taxes >= 0 AND total = subtotal + shipping_cost),
    ADD CONSTRAINT chk_orders_state CHECK (state ~ '^[A-Z]{2}$'),
    ADD CONSTRAINT chk_orders_zip_code CHECK (zip_code ~ '^\d{5}-\d{3}$'),
    ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW();

ALTER TABLE order_items
    ADD CONSTRAINT chk_order_items_line_total CHECK (line_total = unit_price * quantity);

ALTER TABLE data_subject_requests
    ADD CONSTRAINT chk_dsr_type CHECK (type IN ('CONFIRMATION', 'ACCESS', 'CORRECTION', 'ANONYMIZATION', 'PORTABILITY', 'DELETION', 'SHARING_INFO', 'CONSENT_REVOCATION')),
    ADD CONSTRAINT chk_dsr_status CHECK (status IN ('OPEN', 'IN_PROGRESS', 'DONE'));

CREATE FUNCTION set_updated_at() RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_products_updated_at BEFORE UPDATE ON products
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_orders_updated_at BEFORE UPDATE ON orders
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
