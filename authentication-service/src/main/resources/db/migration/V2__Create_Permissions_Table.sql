-- Создание таблицы разрешений
CREATE TABLE permissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255) NOT NULL,
    category VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Создание индексов
CREATE INDEX idx_permissions_name ON permissions(name);
CREATE INDEX idx_permissions_category ON permissions(category);

-- Добавление комментариев
COMMENT ON TABLE permissions IS 'Таблица разрешений системы';
COMMENT ON COLUMN permissions.id IS 'Уникальный идентификатор разрешения';
COMMENT ON COLUMN permissions.name IS 'Название разрешения';
COMMENT ON COLUMN permissions.description IS 'Описание разрешения';
COMMENT ON COLUMN permissions.category IS 'Категория разрешения';
COMMENT ON COLUMN permissions.created_at IS 'Дата создания разрешения';

-- Вставка базовых разрешений
INSERT INTO permissions (name, description, category) VALUES
-- Пользовательские разрешения
('USER_READ', 'Чтение информации о пользователе', 'USER'),
('USER_UPDATE', 'Обновление информации о пользователе', 'USER'),
('USER_DELETE', 'Удаление пользователя', 'USER'),

-- Квестовые разрешения
('QUEST_READ', 'Чтение информации о квестах', 'QUEST'),
('QUEST_CREATE', 'Создание квестов', 'QUEST'),
('QUEST_UPDATE', 'Обновление квестов', 'QUEST'),
('QUEST_DELETE', 'Удаление квестов', 'QUEST'),
('QUEST_PUBLISH', 'Публикация квестов', 'QUEST'),

-- Игровые разрешения
('GAME_PLAY', 'Участие в играх', 'GAME'),
('GAME_CREATE', 'Создание игровых сессий', 'GAME'),
('GAME_JOIN', 'Присоединение к игровым сессиям', 'GAME'),

-- Командные разрешения
('TEAM_CREATE', 'Создание команд', 'TEAM'),
('TEAM_JOIN', 'Присоединение к командам', 'TEAM'),
('TEAM_MANAGE', 'Управление командами', 'TEAM'),

-- Административные разрешения
('ADMIN_USERS', 'Управление пользователями', 'ADMIN'),
('ADMIN_PERMISSIONS', 'Управление разрешениями', 'ADMIN'),
('ADMIN_SYSTEM', 'Управление системой', 'ADMIN'),
('ADMIN_STATISTICS', 'Просмотр статистики', 'ADMIN'),

-- Уведомления
('NOTIFICATION_READ', 'Чтение уведомлений', 'NOTIFICATION'),
('NOTIFICATION_SEND', 'Отправка уведомлений', 'NOTIFICATION');