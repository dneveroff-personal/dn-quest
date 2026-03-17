-- Дополнительные индексы для улучшения производительности

-- Композитные индексы для часто используемых запросов
CREATE INDEX IF NOT EXISTS idx_team_members_team_role_active ON team_members(team_id, role, is_active);
CREATE INDEX IF NOT EXISTS idx_team_members_user_active ON team_members(user_id, is_active);
CREATE INDEX IF NOT EXISTS idx_team_members_joined_active ON team_members(joined_at, is_active);

-- Индексы для приглашений с учетом времени
CREATE INDEX IF NOT EXISTS idx_invitations_status_expires ON team_invitations(status, expires_at);
CREATE INDEX IF NOT EXISTS idx_invitations_team_status_created ON team_invitations(team_id, status, created_at);
CREATE INDEX IF NOT EXISTS idx_invitations_user_status_created ON team_invitations(user_id, status, created_at);

-- Индексы для статистики
CREATE INDEX IF NOT EXISTS idx_statistics_rating_active ON team_statistics(rating DESC) 
    WHERE team_id IN (SELECT id FROM teams WHERE is_active = true);
CREATE INDEX IF NOT EXISTS idx_statistics_games_played ON team_statistics(total_games_played DESC);
CREATE INDEX IF NOT EXISTS idx_statistics_win_rate ON team_statistics(win_rate DESC) 
    WHERE total_games_played > 0;

-- Индексы для поиска команд
CREATE INDEX IF NOT EXISTS idx_teams_search ON teams(is_active, is_private, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_teams_name_private_active ON teams(LOWER(name), is_private, is_active);

-- Частичный индекс для активных участников
CREATE INDEX IF NOT EXISTS idx_team_members_active_only ON team_members(team_id, user_id) 
    WHERE is_active = true;

-- Частичный индекс для активных приглашений
CREATE INDEX IF NOT EXISTS idx_invitations_pending_active ON team_invitations(team_id, user_id, created_at) 
    WHERE status = 'PENDING' AND (expires_at IS NULL OR expires_at > CURRENT_TIMESTAMP);

-- Индексы для настроек с фильтрацией
CREATE INDEX IF NOT EXISTS idx_team_settings_searchable ON team_settings(allow_search, public_profile) 
    WHERE team_id IN (SELECT id FROM teams WHERE is_active = true AND is_private = false);

-- Добавление ограничений CHECK для данных
ALTER TABLE teams ADD CONSTRAINT chk_teams_max_members 
    CHECK (max_members IS NULL OR max_members > 0);

ALTER TABLE team_members ADD CONSTRAINT chk_team_members_role 
    CHECK (role IN ('CAPTAIN', 'MEMBER', 'MODERATOR'));

ALTER TABLE team_invitations ADD CONSTRAINT chk_team_invitations_status 
    CHECK (status IN ('PENDING', 'ACCEPTED', 'DECLINED', 'EXPIRED'));

ALTER TABLE team_settings ADD CONSTRAINT chk_team_settings_expiry_hours 
    CHECK (invitation_expiry_hours IS NULL OR invitation_expiry_hours > 0);

ALTER TABLE team_settings ADD CONSTRAINT chk_team_settings_max_invitations 
    CHECK (max_pending_invitations IS NULL OR max_pending_invitations > 0);

ALTER TABLE team_statistics ADD CONSTRAINT chk_team_statistics_members 
    CHECK (total_members >= 0 AND active_members >= 0 AND active_members <= total_members);

ALTER TABLE team_statistics ADD CONSTRAINT chk_team_statistics_invitations 
    CHECK (total_invitations_sent >= 0 AND total_invitations_accepted >= 0 AND 
           total_invitations_declined >= 0 AND 
           total_invitations_accepted + total_invitations_declined <= total_invitations_sent);

ALTER TABLE team_statistics ADD CONSTRAINT chk_team_statistics_games 
    CHECK (total_games_played >= 0 AND total_games_won >= 0 AND total_games_lost >= 0 AND
           total_games_won + total_games_lost <= total_games_played);

ALTER TABLE team_statistics ADD CONSTRAINT chk_team_statistics_scores 
    CHECK (total_score >= 0 AND average_score >= 0 AND rating >= 0 AND win_rate >= 0 AND win_rate <= 100);

-- Создание функций для автоматического обновления статистики
CREATE OR REPLACE FUNCTION update_team_member_count()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' AND NEW.is_active = true THEN
        UPDATE team_statistics 
        SET active_members = active_members + 1,
            total_members = total_members + 1,
            last_activity_at = CURRENT_TIMESTAMP
        WHERE team_id = NEW.team_id;
    ELSIF TG_OP = 'UPDATE' THEN
        IF OLD.is_active = true AND NEW.is_active = false THEN
            UPDATE team_statistics 
            SET active_members = active_members - 1,
                last_activity_at = CURRENT_TIMESTAMP
            WHERE team_id = NEW.team_id;
        ELSIF OLD.is_active = false AND NEW.is_active = true THEN
            UPDATE team_statistics 
            SET active_members = active_members + 1,
                last_activity_at = CURRENT_TIMESTAMP
            WHERE team_id = NEW.team_id;
        END IF;
    ELSIF TG_OP = 'DELETE' AND OLD.is_active = true THEN
        UPDATE team_statistics 
        SET active_members = active_members - 1,
            total_members = total_members - 1,
            last_activity_at = CURRENT_TIMESTAMP
        WHERE team_id = OLD.team_id;
    END IF;
    
    RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;

-- Создание триггеров для обновления статистики
CREATE TRIGGER trigger_update_team_member_count
    AFTER INSERT OR UPDATE OR DELETE ON team_members
    FOR EACH ROW EXECUTE FUNCTION update_team_member_count();

CREATE OR REPLACE FUNCTION update_invitation_statistics()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        UPDATE team_statistics 
        SET total_invitations_sent = total_invitations_sent + 1,
            last_activity_at = CURRENT_TIMESTAMP
        WHERE team_id = NEW.team_id;
    ELSIF TG_OP = 'UPDATE' THEN
        IF OLD.status = 'PENDING' THEN
            IF NEW.status = 'ACCEPTED' THEN
                UPDATE team_statistics 
                SET total_invitations_accepted = total_invitations_accepted + 1,
                    last_activity_at = CURRENT_TIMESTAMP
                WHERE team_id = NEW.team_id;
            ELSIF NEW.status = 'DECLINED' THEN
                UPDATE team_statistics 
                SET total_invitations_declined = total_invitations_declined + 1,
                    last_activity_at = CURRENT_TIMESTAMP
                WHERE team_id = NEW.team_id;
            END IF;
        END IF;
    END IF;
    
    RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_invitation_statistics
    AFTER INSERT OR UPDATE ON team_invitations
    FOR EACH ROW EXECUTE FUNCTION update_invitation_statistics();

-- Создание представлений для аналитики
CREATE OR REPLACE VIEW team_analytics AS
SELECT 
    t.id,
    t.name,
    t.is_private,
    t.created_at,
    COALESCE(ts.active_members, 0) as active_members,
    COALESCE(ts.total_games_played, 0) as games_played,
    COALESCE(ts.total_games_won, 0) as games_won,
    COALESCE(ts.win_rate, 0) as win_rate,
    COALESCE(ts.rating, 1000) as rating,
    COALESCE(ts.rank, 0) as rank,
    COALESCE(tis.total_invitations_sent, 0) as invitations_sent,
    COALESCE(tis.total_invitations_accepted, 0) as invitations_accepted,
    CASE 
        WHEN tis.total_invitations_sent > 0 THEN 
            ROUND((tis.total_invitations_accepted::DECIMAL / tis.total_invitations_sent) * 100, 2)
        ELSE 0 
    END as invitation_acceptance_rate
FROM teams t
LEFT JOIN team_statistics ts ON t.id = ts.team_id
LEFT JOIN (
    SELECT 
        team_id,
        SUM(CASE WHEN status = 'PENDING' THEN 1 ELSE 0 END) as total_invitations_sent,
        SUM(CASE WHEN status = 'ACCEPTED' THEN 1 ELSE 0 END) as total_invitations_accepted
    FROM team_invitations 
    GROUP BY team_id
) tis ON t.id = tis.team_id
WHERE t.is_active = true;

-- Создание материализованного представления для топ команд (можно обновлять периодически)
CREATE MATERIALIZED VIEW IF NOT EXISTS top_teams_by_rating AS
SELECT 
    t.id,
    t.name,
    t.logo_url,
    COALESCE(ts.rating, 1000) as rating,
    COALESCE(ts.rank, 0) as rank,
    COALESCE(ts.active_members, 0) as active_members,
    COALESCE(ts.total_games_played, 0) as games_played,
    COALESCE(ts.win_rate, 0) as win_rate
FROM teams t
LEFT JOIN team_statistics ts ON t.id = ts.team_id
WHERE t.is_active = true
ORDER BY ts.rating DESC NULLS LAST;

-- Создание индекса для материализованного представления
CREATE INDEX IF NOT EXISTS idx_top_teams_rating ON top_teams_by_rating(rating DESC);

-- Функция для обновления материализованного представления
CREATE OR REPLACE FUNCTION refresh_top_teams()
RETURNS void AS $$
BEGIN
    REFRESH MATERIALIZED VIEW CONCURRENTLY top_teams_by_rating;
END;
$$ LANGUAGE plpgsql;

-- Добавление комментариев к таблицам
COMMENT ON TABLE users IS 'Пользователи системы';
COMMENT ON TABLE teams IS 'Команды';
COMMENT ON TABLE team_members IS 'Участники команд';
COMMENT ON TABLE team_invitations IS 'Приглашения в команды';
COMMENT ON TABLE team_settings IS 'Настройки команд';
COMMENT ON TABLE team_statistics IS 'Статистика команд';

COMMENT ON COLUMN teams.max_members IS 'Максимальное количество участников в команде';
COMMENT ON COLUMN teams.is_private IS 'Приватность команды (true - приватная, false - публичная)';
COMMENT ON COLUMN team_members.role IS 'Роль участника: CAPTAIN, MODERATOR, MEMBER';
COMMENT ON COLUMN team_invitations.status IS 'Статус приглашения: PENDING, ACCEPTED, DECLINED, EXPIRED';
COMMENT ON COLUMN team_settings.invitation_expiry_hours IS 'Срок действия приглашения в часах';
COMMENT ON COLUMN team_statistics.rating IS 'Рейтинг команды';
COMMENT ON COLUMN team_statistics.win_rate IS 'Процент побед';