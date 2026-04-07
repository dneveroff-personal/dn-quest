-- =============================================
-- user-management-service — V1__init.sql
-- Schema: users
-- =============================================

-- =============================================
-- Таблица профилей пользователей
-- =============================================
CREATE TABLE user_profiles (
                               id               UUID PRIMARY KEY,
                               user_id          UUID         NOT NULL UNIQUE,
                               username         VARCHAR(64)  NOT NULL,
                               email            VARCHAR(255),
                               public_name      VARCHAR(128),
                               role             VARCHAR(16)  NOT NULL DEFAULT 'PLAYER',
                               avatar_url       VARCHAR(512),
                               bio              VARCHAR(1000),
                               location         VARCHAR(128),
                               website          VARCHAR(255),
                               is_active        BOOLEAN      NOT NULL DEFAULT TRUE,
                               is_blocked       BOOLEAN      NOT NULL DEFAULT FALSE,
                               blocked_until    TIMESTAMP WITH TIME ZONE,
                               block_reason     VARCHAR(500),
                               created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               updated_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               last_activity_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_user_profiles_user_id          ON user_profiles(user_id);
CREATE INDEX idx_user_profiles_username         ON user_profiles(username);
CREATE INDEX idx_user_profiles_email            ON user_profiles(email);
CREATE INDEX idx_user_profiles_public_name      ON user_profiles(public_name);
CREATE INDEX idx_user_profiles_role             ON user_profiles(role);
CREATE INDEX idx_user_profiles_is_active        ON user_profiles(is_active);
CREATE INDEX idx_user_profiles_is_blocked       ON user_profiles(is_blocked);
CREATE INDEX idx_user_profiles_created_at       ON user_profiles(created_at);
CREATE INDEX idx_user_profiles_last_activity_at ON user_profiles(last_activity_at);
CREATE INDEX idx_user_profiles_blocked_until    ON user_profiles(blocked_until);

COMMENT ON TABLE  user_profiles                  IS 'Таблица профилей пользователей';
COMMENT ON COLUMN user_profiles.id               IS 'Уникальный идентификатор профиля';
COMMENT ON COLUMN user_profiles.user_id          IS 'ID пользователя из Authentication Service';
COMMENT ON COLUMN user_profiles.username         IS 'Имя пользователя';
COMMENT ON COLUMN user_profiles.email            IS 'Email пользователя';
COMMENT ON COLUMN user_profiles.public_name      IS 'Публичное имя пользователя';
COMMENT ON COLUMN user_profiles.role             IS 'Роль пользователя';
COMMENT ON COLUMN user_profiles.avatar_url       IS 'URL аватара пользователя';
COMMENT ON COLUMN user_profiles.bio              IS 'Биография пользователя';
COMMENT ON COLUMN user_profiles.location         IS 'Местоположение пользователя';
COMMENT ON COLUMN user_profiles.website          IS 'Веб-сайт пользователя';
COMMENT ON COLUMN user_profiles.is_active        IS 'Флаг активности профиля';
COMMENT ON COLUMN user_profiles.is_blocked       IS 'Флаг блокировки профиля';
COMMENT ON COLUMN user_profiles.blocked_until    IS 'Время окончания блокировки';
COMMENT ON COLUMN user_profiles.block_reason     IS 'Причина блокировки';
COMMENT ON COLUMN user_profiles.created_at       IS 'Дата создания профиля';
COMMENT ON COLUMN user_profiles.updated_at       IS 'Дата обновления профиля';
COMMENT ON COLUMN user_profiles.last_activity_at IS 'Дата последней активности';

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER trg_user_profiles_updated_at
    BEFORE UPDATE ON user_profiles
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =============================================
-- Таблица настроек пользователей
-- =============================================
CREATE TABLE user_settings (
                               id                        UUID PRIMARY KEY,
                               user_id                   UUID NOT NULL UNIQUE,
                               profile_public            BOOLEAN NOT NULL DEFAULT TRUE,
                               show_email                BOOLEAN NOT NULL DEFAULT FALSE,
                               show_real_name            BOOLEAN NOT NULL DEFAULT FALSE,
                               show_location             BOOLEAN NOT NULL DEFAULT TRUE,
                               show_website              BOOLEAN NOT NULL DEFAULT TRUE,
                               show_statistics           BOOLEAN NOT NULL DEFAULT TRUE,
                               email_notifications       BOOLEAN NOT NULL DEFAULT TRUE,
                               team_invitations          BOOLEAN NOT NULL DEFAULT TRUE,
                               quest_reminders           BOOLEAN NOT NULL DEFAULT TRUE,
                               achievement_notifications BOOLEAN NOT NULL DEFAULT TRUE,
                               friend_requests           BOOLEAN NOT NULL DEFAULT TRUE,
                               system_notifications      BOOLEAN NOT NULL DEFAULT TRUE,
                               theme                     VARCHAR(20) DEFAULT 'light',
                               language                  VARCHAR(10) DEFAULT 'ru',
                               timezone                  VARCHAR(50) DEFAULT 'UTC',
                               date_format               VARCHAR(20) DEFAULT 'dd.MM.yyyy',
                               time_format               VARCHAR(10) DEFAULT '24h',
                               auto_join_teams           BOOLEAN NOT NULL DEFAULT FALSE,
                               show_hints                BOOLEAN NOT NULL DEFAULT TRUE,
                               sound_effects             BOOLEAN NOT NULL DEFAULT TRUE,
                               music                     BOOLEAN NOT NULL DEFAULT FALSE,
                               animations                BOOLEAN NOT NULL DEFAULT TRUE,
                               created_at                TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               updated_at                TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_settings_user_id             ON user_settings(user_id);
CREATE INDEX idx_user_settings_profile_public      ON user_settings(profile_public);
CREATE INDEX idx_user_settings_email_notifications ON user_settings(email_notifications);
CREATE INDEX idx_user_settings_theme               ON user_settings(theme);
CREATE INDEX idx_user_settings_language            ON user_settings(language);
CREATE INDEX idx_user_settings_timezone            ON user_settings(timezone);

CREATE TRIGGER trg_user_settings_updated_at
    BEFORE UPDATE ON user_settings
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =============================================
-- Таблица статистики пользователей
-- =============================================
CREATE TABLE user_statistics (
                                 id                       UUID PRIMARY KEY,
                                 user_id                  UUID    NOT NULL UNIQUE,
                                 total_score              BIGINT  NOT NULL DEFAULT 0,
                                 level                    INTEGER NOT NULL DEFAULT 1,
                                 experience_points        BIGINT  NOT NULL DEFAULT 0,
                                 experience_to_next_level BIGINT  NOT NULL DEFAULT 100,
                                 quests_completed         INTEGER NOT NULL DEFAULT 0,
                                 quests_started           INTEGER NOT NULL DEFAULT 0,
                                 quests_abandoned         INTEGER NOT NULL DEFAULT 0,
                                 total_playtime_minutes   BIGINT  NOT NULL DEFAULT 0,
                                 levels_completed         INTEGER NOT NULL DEFAULT 0,
                                 codes_solved             INTEGER NOT NULL DEFAULT 0,
                                 hints_used               INTEGER NOT NULL DEFAULT 0,
                                 attempts_made            INTEGER NOT NULL DEFAULT 0,
                                 teams_joined             INTEGER NOT NULL DEFAULT 0,
                                 teams_created            INTEGER NOT NULL DEFAULT 0,
                                 teams_led                INTEGER NOT NULL DEFAULT 0,
                                 invitations_sent         INTEGER NOT NULL DEFAULT 0,
                                 invitations_received     INTEGER NOT NULL DEFAULT 0,
                                 achievements_unlocked    INTEGER NOT NULL DEFAULT 0,
                                 rare_achievements        INTEGER NOT NULL DEFAULT 0,
                                 legendary_achievements   INTEGER NOT NULL DEFAULT 0,
                                 login_count              INTEGER NOT NULL DEFAULT 0,
                                 current_streak_days      INTEGER NOT NULL DEFAULT 0,
                                 longest_streak_days      INTEGER NOT NULL DEFAULT 0,
                                 last_login_at            TIMESTAMP WITH TIME ZONE,
                                 last_activity_at         TIMESTAMP WITH TIME ZONE,
                                 first_login_at           TIMESTAMP WITH TIME ZONE,
                                 created_at               TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 updated_at               TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_statistics_user_id              ON user_statistics(user_id);
CREATE INDEX idx_user_statistics_total_score          ON user_statistics(total_score);
CREATE INDEX idx_user_statistics_level                ON user_statistics(level);
CREATE INDEX idx_user_statistics_experience_points    ON user_statistics(experience_points);
CREATE INDEX idx_user_statistics_quests_completed     ON user_statistics(quests_completed);
CREATE INDEX idx_user_statistics_codes_solved         ON user_statistics(codes_solved);
CREATE INDEX idx_user_statistics_achievements_unlocked ON user_statistics(achievements_unlocked);
CREATE INDEX idx_user_statistics_login_count          ON user_statistics(login_count);
CREATE INDEX idx_user_statistics_current_streak_days  ON user_statistics(current_streak_days);
CREATE INDEX idx_user_statistics_last_activity_at     ON user_statistics(last_activity_at);
CREATE INDEX idx_user_statistics_last_login_at        ON user_statistics(last_login_at);

CREATE TRIGGER trg_user_statistics_updated_at
    BEFORE UPDATE ON user_statistics
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();