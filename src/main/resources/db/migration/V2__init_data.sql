-- V2__init_data.sql
INSERT INTO users (email, first_name, last_name, password, username) VALUES
                                                                         ('admin@test.com', 'Admin', 'User', '$2a$10$5v5ZIVb4jS1B7dE9gR4D4e3e5Yk6d7Xe1Nc5rJ7z8w9v0a1b2c3d4e5f6', 'admin_user'),
                                                                         ('user1@test.com', 'John', 'Doe', '$2a$10$5v5ZIVb4jS1B7dE9gR4D4e3e5Yk6d7Xe1Nc5rJ7z8w9v0a1b2c3d4e5f6', 'john_doe'),
                                                                         ('user2@test.com', 'Jane', 'Smith', '$2a$10$5v5ZIVb4jS1B7dE9gR4D4e3e5Yk6d7Xe1Nc5rJ7z8w9v0a1b2c3d4e5f6', 'jane_smith'),
                                                                         ('manager@test.com', 'Robert', 'Johnson', '$2a$10$5v5ZIVb4jS1B7dE9gR4D4e3e5Yk6d7Xe1Nc5rJ7z8w9v0a1b2c3d4e5f6', 'robert_j'),
                                                                         ('support@test.com', 'Emily', 'Williams', '$2a$10$5v5ZIVb4jS1B7dE9gR4D4e3e5Yk6d7Xe1Nc5rJ7z8w9v0a1b2c3d4e5f6', 'emily_w');

INSERT INTO user_roles (user_id, role) VALUES
                                           ((SELECT id FROM users WHERE username = 'admin_user'), 'ADMIN'),
                                           ((SELECT id FROM users WHERE username = 'john_doe'), 'USER'),
                                           ((SELECT id FROM users WHERE username = 'jane_smith'), 'USER'),
                                           ((SELECT id FROM users WHERE username = 'robert_j'), 'ADMIN'),
                                           ((SELECT id FROM users WHERE username = 'emily_w'), 'USER');