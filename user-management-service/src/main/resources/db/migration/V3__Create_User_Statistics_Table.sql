-- Создание таблицы статистики пользователей
CREATE TABLE user_statistics (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    
    -- Общая статистика
    total_score BIGINT NOT NULL DEFAULT 0,
    level INTEGER NOT NULL DEFAULT 1,
    experience_points BIGINT NOT NULL DEFAULT 0,
    experience_to_next_level BIGINT NOT NULL DEFAULT 100,
    
    -- Статистика квестов
    quests_completed INTEGER NOT NULL DEFAULT 0,
    quests_started INTEGER NOT NULL DEFAULT 0,
    quests_abandoned INTEGER NOT NULL DEFAULT 0,
    total_playtime_minutes BIGINT NOT NULL DEFAULT 0,
    
    -- Статистика уровней
    levels_completed INTEGER NOT NULL DEFAULT 0,
    codes_solved INTEGER NOT NULL DEFAULT 0,
    hints_used INTEGER NOT NULL DEFAULT 0,
    attempts_made INTEGER NOT NULL DEFAULT 0,
    
    -- Статистика команд
    teams_joined INTEGER NOT NULL DEFAULT 0,
    teams_created INTEGER NOT NULL DEFAULT 0,
    teams_led INTEGER NOT NULL DEFAULT 0,
    invitations_sent INTEGER NOT NULL DEFAULT 0,
    invitations_received INTEGER NOT NULL DEFAULT 0,
    
    -- Достижения
    achievements_unlocked INTEGER NOT NULL DEFAULT 0,
    rare_achievements INTEGER NOT NULL DEFAULT 0,
    legendary_achievements INTEGER NOT NULL DEFAULT 0,
    
    -- Активность
    login_count INTEGER NOT NULL DEFAULT 0,
    current_streak_days INTEGER NOT NULL DEFAULT 0,
    longest_streak_days INTEGER NOT NULL DEFAULT 0,
    last_login_at TIMESTAMP WITH TIME ZONE,
    last_activity_at TIMESTAMP WITH TIME ZONE,
    first_login_at TIMESTAMP WITH TIME ZONE,
    
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Создание индексов для оптимизации
CREATE INDEX idx_user_statistics_user_id ON user_statistics(user_id);
CREATE INDEX idx_user_statistics_total_score ON user_statistics(total_score);
CREATE INDEX idx_user_statistics_level ON user_statistics(level);
CREATE INDEX idx_user_statistics_experience_points ON user_statistics(experience_points);
CREATE INDEX idx_user_statistics_quests_completed ON user_statistics(quests_completed);
CREATE INDEX idx_user_statistics_codes_solved ON user_statistics(codes_solved);
CREATE INDEX idx_user_statistics_achievements_unlocked ON user_statistics(achievements_unlocked);
CREATE INDEX idx_user_statistics_login_count ON user_statistics(login_count);
CREATE INDEX idx_user_statistics_current_streak_days ON user_statistics(current_streak_days);
CREATE INDEX idx_user_statistics_last_activity_at ON user_statistics(last_activity_at);
CREATE INDEX idx_user_statistics_last_login_at ON user_statistics(last_login_at);

-- Добавление комментариев
COMMENT ON TABLE user_statistics IS 'Таблица статистики пользователей';
COMMENT ON COLUMN user_statistics.id IS 'Уникальный идентификатор статистики';
COMMENT ON COLUMN user_statistics.user_id IS 'ID пользователя';

-- Комментарии для общей статистики
COMMENT ON COLUMN user_statistics.total_score IS 'Общий счет';
COMMENT ON COLUMN user_statistics.level IS 'Уровень';
COMMENT ON COLUMN user_statistics.experience_points IS 'Очки опыта';
COMMENT ON COLUMN user_statistics.experience_to_next_level IS 'Опыт до следующего уровня';

-- Комментарии для статистики квестов
COMMENT ON COLUMN user_statistics.quests_completed IS 'Квестов завершено';
COMMENT ON COLUMN user_statistics.quests_started IS 'Квестов начато';
COMMENT ON COLUMN user_statistics.quests_abandoned IS 'Квестов брошено';
COMMENT ON COLUMN user_statistics.total_playtime_minutes IS 'Общее время игры в минутах';

-- Комментарии для статистики уровней
COMMENT ON COLUMN user_statistics.levels_completed IS 'Уровней завершено';
COMMENT ON COLUMN user_statistics.codes_solved IS 'Кодов решено';
COMMENT ON COLUMN user_statistics.hints_used IS 'Подсказок использовано';
COMMENT ON COLUMN user_statistics.attempts_made IS 'Попыток сделано';

-- Комментарии для статистики команд
COMMENT ON COLUMN user_statistics.teams_joined IS 'Команд присоединилось';
COMMENT ON COLUMN user_statistics.teams_created IS 'Команд создано';
COMMENT ON COLUMN user_statistics.teams_led IS 'Команд возглавлено';
COMMENT ON COLUMN user_statistics.invitations_sent IS 'Приглашений отправлено';
COMMENT ON COLUMN user_statistics.invitations_received IS 'Приглашений получено';

-- Комментарии для достижений
COMMENT ON COLUMN user_statistics.achievements_unlocked IS 'Достижений разблокировано';
COMMENT ON COLUMN user_statistics.rare_achievements IS 'Редких достижений';
COMMENT ON COLUMN user_statistics.legendary_achievements IS 'Легендарных достижений';

-- Комментарии для активности
COMMENT ON COLUMN user_statistics.login_count IS 'Количество входов';
COMMENT ON COLUMN user_statistics.current_streak_days IS 'Текущая серия дней';
COMMENT ON COLUMN user_statistics.longest_streak_days IS 'Самая длинная серия дней';
COMMENT ON COLUMN user_statistics.last_login_at IS 'Дата последнего входа';
COMMENT ON COLUMN user_statistics.last_activity_at IS 'Дата последней активности';
COMMENT ON COLUMN user_statistics.first_login_at IS 'Дата первого входа';

COMMENT ON COLUMN user_statistics.created_at IS 'Дата создания статистики';
COMMENT ON COLUMN user_statistics.updated_at IS 'Дата обновления статистики';

-- Создание триггера для обновления updated_at
CREATE OR REPLACE FUNCTION update_user_statistics_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_user_statistics_updated_at 
    BEFORE UPDATE ON user_statistics 
    FOR EACH ROW 
    EXECUTE FUNCTION update_user_statistics_updated_at();