-- Создание таблицы связи пользователей и разрешений
CREATE TABLE user_permissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    permission_id UUID NOT NULL,
    granted_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    granted_by VARCHAR(100),
    
    CONSTRAINT fk_user_permissions_user_id 
        FOREIGN KEY (user_id) REFERENCES auth.users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_permissions_permission_id 
        FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE,
    CONSTRAINT uk_user_permissions_user_permission 
        UNIQUE (user_id, permission_id)
);

-- Создание индексов
CREATE INDEX idx_user_permissions_user_id ON user_permissions(user_id);
CREATE INDEX idx_user_permissions_permission_id ON user_permissions(permission_id);
CREATE INDEX idx_user_permissions_granted_at ON user_permissions(granted_at);
CREATE INDEX idx_user_permissions_granted_by ON user_permissions(granted_by);

-- Добавление комментариев
COMMENT ON TABLE user_permissions IS 'Таблица связи пользователей и разрешений';
COMMENT ON COLUMN user_permissions.id IS 'Уникальный идентификатор записи';
COMMENT ON COLUMN user_permissions.user_id IS 'ID пользователя';
COMMENT ON COLUMN user_permissions.permission_id IS 'ID разрешения';
COMMENT ON COLUMN user_permissions.granted_at IS 'Дата выдачи разрешения';
COMMENT ON COLUMN user_permissions.granted_by IS 'Кто выдал разрешение';

-- Создание базовых разрешений для ролей
-- Администратор получает все разрешения
INSERT INTO user_permissions (user_id, permission_id, granted_by)
SELECT u.id, p.id, 'SYSTEM'
FROM users u, permissions p
WHERE u.role = 'ADMIN';

-- Автор получает разрешения для работы с квестами
INSERT INTO user_permissions (user_id, permission_id, granted_by)
SELECT u.id, p.id, 'SYSTEM'
FROM users u, permissions p
WHERE u.role = 'AUTHOR' 
AND p.name IN (
    'USER_READ', 'USER_UPDATE',
    'QUEST_READ', 'QUEST_CREATE', 'QUEST_UPDATE', 'QUEST_DELETE', 'QUEST_PUBLISH',
    'GAME_PLAY', 'GAME_CREATE',
    'TEAM_CREATE', 'TEAM_JOIN', 'TEAM_MANAGE',
    'NOTIFICATION_READ'
);

-- Игрок получает базовые разрешения
INSERT INTO user_permissions (user_id, permission_id, granted_by)
SELECT u.id, p.id, 'SYSTEM'
FROM users u, permissions p
WHERE u.role = 'PLAYER'
AND p.name IN (
    'USER_READ', 'USER_UPDATE',
    'QUEST_READ',
    'GAME_PLAY', 'GAME_JOIN',
    'TEAM_CREATE', 'TEAM_JOIN',
    'NOTIFICATION_READ'
);