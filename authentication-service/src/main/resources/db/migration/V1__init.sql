-- =============================================
-- authentication-service — V1__init.sql
-- Schema: auth
-- =============================================

-- =============================================
-- Таблица пользователей
-- =============================================
CREATE TABLE users (
                       id                          UUID PRIMARY KEY,
                       username                    VARCHAR(64)  NOT NULL UNIQUE,
                       password_hash               VARCHAR(255) NOT NULL,
                       email                       VARCHAR(255) UNIQUE,
                       public_name                 VARCHAR(128),
                       role                        VARCHAR(16)  NOT NULL DEFAULT 'PLAYER',
                       is_active                   BOOLEAN      NOT NULL DEFAULT TRUE,
                       is_email_verified           BOOLEAN      NOT NULL DEFAULT FALSE,
                       password_reset_token        VARCHAR(255),
                       password_reset_expires_at   TIMESTAMP WITH TIME ZONE,
                       refresh_token               VARCHAR(255),
                       refresh_token_expires_at    TIMESTAMP WITH TIME ZONE,
                       last_login_at               TIMESTAMP WITH TIME ZONE,
                       created_at                  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at                  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_username             ON users(username);
CREATE INDEX idx_users_email                ON users(email);
CREATE INDEX idx_users_role                 ON users(role);
CREATE INDEX idx_users_is_active            ON users(is_active);
CREATE INDEX idx_users_created_at           ON users(created_at);
CREATE INDEX idx_users_refresh_token        ON users(refresh_token);
CREATE INDEX idx_users_password_reset_token ON users(password_reset_token);

COMMENT ON TABLE  users                             IS 'Таблица пользователей системы';
COMMENT ON COLUMN users.id                          IS 'Уникальный идентификатор пользователя';
COMMENT ON COLUMN users.username                    IS 'Имя пользователя для входа';
COMMENT ON COLUMN users.password_hash               IS 'Хеш пароля пользователя';
COMMENT ON COLUMN users.email                       IS 'Email пользователя';
COMMENT ON COLUMN users.public_name                 IS 'Публичное имя пользователя';
COMMENT ON COLUMN users.role                        IS 'Роль пользователя в системе';
COMMENT ON COLUMN users.is_active                   IS 'Флаг активности пользователя';
COMMENT ON COLUMN users.is_email_verified           IS 'Флаг верификации email';
COMMENT ON COLUMN users.password_reset_token        IS 'Токен для сброса пароля';
COMMENT ON COLUMN users.password_reset_expires_at   IS 'Время истечения токена сброса пароля';
COMMENT ON COLUMN users.refresh_token               IS 'Refresh токен для обновления сессии';
COMMENT ON COLUMN users.refresh_token_expires_at    IS 'Время истечения refresh токена';
COMMENT ON COLUMN users.last_login_at               IS 'Дата последнего входа';
COMMENT ON COLUMN users.created_at                  IS 'Дата создания пользователя';
COMMENT ON COLUMN users.updated_at                  IS 'Дата обновления пользователя';

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =============================================
-- Таблица разрешений
-- =============================================
CREATE TABLE permissions (
                             id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                             name        VARCHAR(100) NOT NULL UNIQUE,
                             description VARCHAR(255) NOT NULL,
                             category    VARCHAR(50)  NOT NULL,
                             created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_permissions_name     ON permissions(name);
CREATE INDEX idx_permissions_category ON permissions(category);

COMMENT ON TABLE  permissions             IS 'Таблица разрешений системы';
COMMENT ON COLUMN permissions.id          IS 'Уникальный идентификатор разрешения';
COMMENT ON COLUMN permissions.name        IS 'Название разрешения';
COMMENT ON COLUMN permissions.description IS 'Описание разрешения';
COMMENT ON COLUMN permissions.category   IS 'Категория разрешения';
COMMENT ON COLUMN permissions.created_at  IS 'Дата создания разрешения';

INSERT INTO permissions (name, description, category) VALUES
                                                          ('USER_READ',           'Чтение информации о пользователе',  'USER'),
                                                          ('USER_UPDATE',         'Обновление информации о пользователе', 'USER'),
                                                          ('USER_DELETE',         'Удаление пользователя',             'USER'),
                                                          ('QUEST_READ',          'Чтение информации о квестах',        'QUEST'),
                                                          ('QUEST_CREATE',        'Создание квестов',                   'QUEST'),
                                                          ('QUEST_UPDATE',        'Обновление квестов',                 'QUEST'),
                                                          ('QUEST_DELETE',        'Удаление квестов',                   'QUEST'),
                                                          ('QUEST_PUBLISH',       'Публикация квестов',                 'QUEST'),
                                                          ('GAME_PLAY',           'Участие в играх',                    'GAME'),
                                                          ('GAME_CREATE',         'Создание игровых сессий',            'GAME'),
                                                          ('GAME_JOIN',           'Присоединение к игровым сессиям',    'GAME'),
                                                          ('TEAM_CREATE',         'Создание команд',                    'TEAM'),
                                                          ('TEAM_JOIN',           'Присоединение к командам',           'TEAM'),
                                                          ('TEAM_MANAGE',         'Управление командами',               'TEAM'),
                                                          ('ADMIN_USERS',         'Управление пользователями',          'ADMIN'),
                                                          ('ADMIN_PERMISSIONS',   'Управление разрешениями',            'ADMIN'),
                                                          ('ADMIN_SYSTEM',        'Управление системой',                'ADMIN'),
                                                          ('ADMIN_STATISTICS',    'Просмотр статистики',                'ADMIN'),
                                                          ('NOTIFICATION_READ',   'Чтение уведомлений',                 'NOTIFICATION'),
                                                          ('NOTIFICATION_SEND',   'Отправка уведомлений',               'NOTIFICATION');

-- =============================================
-- Таблица связи пользователей и разрешений
-- =============================================
CREATE TABLE user_permissions (
                                  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                  user_id       UUID        NOT NULL,
                                  permission_id UUID        NOT NULL,
                                  granted_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  granted_by    VARCHAR(100),

                                  CONSTRAINT fk_user_permissions_user
                                      FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                                  CONSTRAINT fk_user_permissions_permission
                                      FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE,
                                  CONSTRAINT uk_user_permissions_user_permission
                                      UNIQUE (user_id, permission_id)
);

CREATE INDEX idx_user_permissions_user_id       ON user_permissions(user_id);
CREATE INDEX idx_user_permissions_permission_id ON user_permissions(permission_id);
CREATE INDEX idx_user_permissions_granted_at    ON user_permissions(granted_at);
CREATE INDEX idx_user_permissions_granted_by    ON user_permissions(granted_by);

COMMENT ON TABLE  user_permissions             IS 'Таблица связи пользователей и разрешений';
COMMENT ON COLUMN user_permissions.id          IS 'Уникальный идентификатор записи';
COMMENT ON COLUMN user_permissions.user_id     IS 'ID пользователя';
COMMENT ON COLUMN user_permissions.permission_id IS 'ID разрешения';
COMMENT ON COLUMN user_permissions.granted_at  IS 'Дата выдачи разрешения';
COMMENT ON COLUMN user_permissions.granted_by  IS 'Кто выдал разрешение';