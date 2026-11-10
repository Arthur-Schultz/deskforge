INSERT INTO users (id, full_name, email, password_hash, role, marketing_consent, terms_accepted_at, privacy_policy_version, created_at) VALUES
    ('7b1f6c2e-3a51-4c8e-9a0d-1f2e3d4c5b6a', 'Admin DeskForge', 'admin@deskforge.dev', '$2b$10$J.KjDt4eoi4APc5fd4/Bcu0k6sKnlMMBxq6iltycNc75pMIw/tQiS', 'ADMIN', FALSE, NOW(), '2026-11', NOW()),
    ('2c9d8e7f-6a5b-4c3d-8e1f-0a9b8c7d6e5f', 'Cliente Demo', 'cliente@deskforge.dev', '$2b$10$3C4OBNefulC8rYsFINt3QObKdEKNd1nsxheihG0JzgrVpySO1f1sC', 'CUSTOMER', FALSE, NOW(), '2026-11', NOW());
