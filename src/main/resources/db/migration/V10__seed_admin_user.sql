INSERT IGNORE INTO users (email, password_hash, first_name, last_name, phone, status, availability, created_at, updated_at)
VALUES (
    'admin@morago.com',
    '$2a$10$NkZ1X7zJm8X7zJm8X7zJm8uQrE3qW5rT6yU7iO8pL9kM0nB1vC2x',
    'Admin',
    'User',
    '010-9999-9999',
    0,
    0,
    NOW(),
    NOW()
);

INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.email = 'admin@morago.com' AND r.name = 'ROLE_ADMIN';