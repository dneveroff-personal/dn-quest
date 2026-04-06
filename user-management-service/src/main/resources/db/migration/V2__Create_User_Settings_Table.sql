-- Создание таблицы настроек пользователей
CREATE TABLE user_settings (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE,
    
    -- Настройки приватности
    profile_public BOOLEAN NOT NULL DEFAULT TRUE,
    show_email BOOLEAN NOT NULL DEFAULT FALSE,
    show_real_name BOOLEAN NOT NULL DEFAULT FALSE,
    show_location BOOLEAN NOT NULL DEFAULT TRUE,
    show_website BOOLEAN NOT NULL DEFAULT TRUE,
    show_statistics BOOLEAN NOT NULL DEFAULT TRUE,
    
    -- Настройки уведомлений
    email_notifications BOOLEAN NOT NULL DEFAULT TRUE,
    team_invitations BOOLEAN NOT NULL DEFAULT TRUE,
    quest_reminders BOOLEAN NOT NULL DEFAULT TRUE,
    achievement_notifications BOOLEAN NOT NULL DEFAULT TRUE,
    friend_requests BOOLEAN NOT NULL DEFAULT TRUE,
    system_notifications BOOLEAN NOT NULL DEFAULT TRUE,
    
    -- Настройки интерфейса
    theme VARCHAR(20) DEFAULT 'light',
    language VARCHAR(10) DEFAULT 'ru',
    timezone VARCHAR(50) DEFAULT 'UTC',
    date_format VARCHAR(20) DEFAULT 'dd.MM.yyyy',
    time_format VARCHAR(10) DEFAULT '24h',
    
    -- Настройки игры
    auto_join_teams BOOLEAN NOT NULL DEFAULT FALSE,
    show_hints BOOLEAN NOT NULL DEFAULT TRUE,
    sound_effects BOOLEAN NOT NULL DEFAULT TRUE,
    music BOOLEAN NOT NULL DEFAULT FALSE,
    animations BOOLEAN NOT NULL DEFAULT TRUE,
    
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Создание индексов для оптимизации
CREATE INDEX idx_user_settings_user_id ON user_settings(user_id);
CREATE INDEX idx_user_settings_profile_public ON user_settings(profile_public);
CREATE INDEX idx_user_settings_email_notifications ON user_settings(email_notifications);
CREATE INDEX idx_user_settings_theme ON user_settings(theme);
CREATE INDEX idx_user_settings_language ON user_settings(language);
CREATE INDEX idx_user_settings_timezone ON user_settings(timezone);

-- Добавление комментариев
COMMENT ON TABLE user_settings IS 'Таблица настроек пользователей';
COMMENT ON COLUMN user_settings.id IS 'Уникальный идентификатор настроек';
COMMENT ON COLUMN user_settings.user_id IS 'ID пользователя';

-- Комментарии для настроек приватности
COMMENT ON COLUMN user_settings.profile_public IS 'Публичный профиль';
COMMENT ON COLUMN user_settings.show_email IS 'Показывать email';
COMMENT ON COLUMN user_settings.show_real_name IS 'Показывать настоящее имя';
COMMENT ON COLUMN user_settings.show_location IS 'Показывать местоположение';
COMMENT ON COLUMN user_settings.show_website IS 'Показывать веб-сайт';
COMMENT ON COLUMN user_settings.show_statistics IS 'Показывать статистику';

-- Комментарии для настроек уведомлений
COMMENT ON COLUMN user_settings.email_notifications IS 'Email уведомления';
COMMENT ON COLUMN user_settings.team_invitations IS 'Приглашения в команды';
COMMENT ON COLUMN user_settings.quest_reminders IS 'Напоминания о квестах';
COMMENT ON COLUMN user_settings.achievement_notifications IS 'Уведомления о достижениях';
COMMENT ON COLUMN user_settings.friend_requests IS 'Запросы в друзья';
COMMENT ON COLUMN user_settings.system_notifications IS 'Системные уведомления';

-- Комментарии для настроек интерфейса
COMMENT ON COLUMN user_settings.theme IS 'Тема оформления';
COMMENT ON COLUMN user_settings.language IS 'Язык интерфейса';
COMMENT ON COLUMN user_settings.timezone IS 'Часовой пояс';
COMMENT ON COLUMN user_settings.date_format IS 'Формат даты';
COMMENT ON COLUMN user_settings.time_format IS 'Формат времени';

-- Комментарии для настроек игры
COMMENT ON COLUMN user_settings.auto_join_teams IS 'Автоматически вступать в команды';
COMMENT ON COLUMN user_settings.show_hints IS 'Показывать подсказки';
COMMENT ON COLUMN user_settings.sound_effects IS 'Звуковые эффекты';
COMMENT ON COLUMN user_settings.music IS 'Музыка';
COMMENT ON COLUMN user_settings.animations IS 'Анимации';

COMMENT ON COLUMN user_settings.created_at IS 'Дата создания настроек';
COMMENT ON COLUMN user_settings.updated_at IS 'Дата обновления настроек';

-- Создание триггера для обновления updated_at
CREATE OR REPLACE FUNCTION update_user_settings_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_user_settings_updated_at 
    BEFORE UPDATE ON user_settings 
    FOR EACH ROW 
    EXECUTE FUNCTION update_user_settings_updated_at();