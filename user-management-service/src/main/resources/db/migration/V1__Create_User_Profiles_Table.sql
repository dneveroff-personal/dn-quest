-- Создание таблицы профилей пользователей
CREATE TABLE user_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    username VARCHAR(64) NOT NULL,
    email VARCHAR(255),
    public_name VARCHAR(128),
    role VARCHAR(16) NOT NULL DEFAULT 'PLAYER',
    avatar_url VARCHAR(512),
    bio VARCHAR(1000),
    location VARCHAR(128),
    website VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_blocked BOOLEAN NOT NULL DEFAULT FALSE,
    blocked_until TIMESTAMP WITH TIME ZONE,
    block_reason VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_activity_at TIMESTAMP WITH TIME ZONE
);

-- Создание индексов для оптимизации
CREATE INDEX idx_user_profiles_user_id ON user_profiles(user_id);
CREATE INDEX idx_user_profiles_username ON user_profiles(username);
CREATE INDEX idx_user_profiles_email ON user_profiles(email);
CREATE INDEX idx_user_profiles_public_name ON user_profiles(public_name);
CREATE INDEX idx_user_profiles_role ON user_profiles(role);
CREATE INDEX idx_user_profiles_is_active ON user_profiles(is_active);
CREATE INDEX idx_user_profiles_is_blocked ON user_profiles(is_blocked);
CREATE INDEX idx_user_profiles_created_at ON user_profiles(created_at);
CREATE INDEX idx_user_profiles_last_activity_at ON user_profiles(last_activity_at);
CREATE INDEX idx_user_profiles_blocked_until ON user_profiles(blocked_until);

-- Добавление комментариев
COMMENT ON TABLE user_profiles IS 'Таблица профилей пользователей';
COMMENT ON COLUMN user_profiles.id IS 'Уникальный идентификатор профиля';
COMMENT ON COLUMN user_profiles.user_id IS 'ID пользователя из Authentication Service';
COMMENT ON COLUMN user_profiles.username IS 'Имя пользователя';
COMMENT ON COLUMN user_profiles.email IS 'Email пользователя';
COMMENT ON COLUMN user_profiles.public_name IS 'Публичное имя пользователя';
COMMENT ON COLUMN user_profiles.role IS 'Роль пользователя';
COMMENT ON COLUMN user_profiles.avatar_url IS 'URL аватара пользователя';
COMMENT ON COLUMN user_profiles.bio IS 'Биография пользователя';
COMMENT ON COLUMN user_profiles.location IS 'Местоположение пользователя';
COMMENT ON COLUMN user_profiles.website IS 'Веб-сайт пользователя';
COMMENT ON COLUMN user_profiles.is_active IS 'Флаг активности профиля';
COMMENT ON COLUMN user_profiles.is_blocked IS 'Флаг блокировки профиля';
COMMENT ON COLUMN user_profiles.blocked_until IS 'Время окончания блокировки';
COMMENT ON COLUMN user_profiles.block_reason IS 'Причина блокировки';
COMMENT ON COLUMN user_profiles.created_at IS 'Дата создания профиля';
COMMENT ON COLUMN user_profiles.updated_at IS 'Дата обновления профиля';
COMMENT ON COLUMN user_profiles.last_activity_at IS 'Дата последней активности';

-- Создание триггера для обновления updated_at
CREATE OR REPLACE FUNCTION update_user_profiles_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_user_profiles_updated_at 
    BEFORE UPDATE ON user_profiles 
    FOR EACH ROW 
    EXECUTE FUNCTION update_user_profiles_updated_at();