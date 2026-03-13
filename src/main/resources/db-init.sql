INSERT INTO users(name, email, password_hash, role, blocked, created_at)
SELECT 'Administrador', 'admin@universidad.edu', '$2a$10$bz.7voFvh4sKQcfYJh9T6.uH2xA0ATQ5Uza8j5nNQ9m4zWlPTJx7S', 'ADMIN', FALSE, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'admin@universidad.edu');
