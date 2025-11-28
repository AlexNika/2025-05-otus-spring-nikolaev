-- Вставляем роли
INSERT INTO roles (name, description) VALUES
('ADMIN', 'Administrator with full access'),
('USER', 'Regular user with read-only access'),
('MANAGER', 'MANAGER with some extended rights')
ON CONFLICT (name) DO NOTHING;

-- Вставляем пользователей (пароли: admin123 и user123 - bcrypt encoded)
INSERT INTO users (username, password) VALUES
('admin', '$2a$10$6D3khy3gveLRboq5wKuZ9O6s5pA/ewmC1CG9jieoyNsiMSV/uWtXq'),
('user', '$2a$10$nbxnvh9LgrDaeiW8uccLeudPABcr5b600f9/olv.G8gA6pBeGMEBq')
ON CONFLICT (username) DO NOTHING;

-- Назначаем роли
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username = 'admin' AND r.name = 'ADMIN'
ON CONFLICT (user_id, role_id) DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username = 'admin' AND r.name = 'USER'
ON CONFLICT (user_id, role_id) DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username = 'user' AND r.name = 'USER'
ON CONFLICT (user_id, role_id) DO NOTHING;