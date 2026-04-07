-- =============================================
-- team-management-service — V1__init.sql
-- Schema: teams
-- =============================================

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ language 'plpgsql';

-- =============================================
-- Локальная копия пользователей (денормализация)
-- =============================================
CREATE TABLE users (
                       id         UUID PRIMARY KEY,
                       username   VARCHAR(50)  NOT NULL UNIQUE,
                       email      VARCHAR(100) NOT NULL UNIQUE,
                       first_name VARCHAR(50),
                       last_name  VARCHAR(50),
                       avatar_url VARCHAR(500),
                       is_active  BOOLEAN  DEFAULT TRUE,
                       created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_user_username ON users(username);
CREATE INDEX idx_user_email    ON users(email);

CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =============================================
-- Таблица команд
-- =============================================
CREATE TABLE teams (
                       id          UUID PRIMARY KEY,
                       name        VARCHAR(120) NOT NULL UNIQUE,
                       description VARCHAR(500),
                       logo_url    VARCHAR(500),
                       captain_id  UUID         NOT NULL,
                       max_members INTEGER      DEFAULT 10,
                       is_private  BOOLEAN      DEFAULT FALSE,
                       is_active   BOOLEAN      DEFAULT TRUE,
                       created_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                       updated_at  TIMESTAMP WITH TIME ZONE,

                       CONSTRAINT fk_teams_captain FOREIGN KEY (captain_id) REFERENCES users(id) ON DELETE RESTRICT,
                       CONSTRAINT chk_teams_max_members CHECK (max_members IS NULL OR max_members > 0)
);

CREATE INDEX idx_team_captain        ON teams(captain_id);
CREATE INDEX idx_team_created_at     ON teams(created_at);
CREATE INDEX idx_team_active_private ON teams(is_active, is_private);
CREATE INDEX idx_team_name_search    ON teams USING gin(to_tsvector('russian', name));

CREATE TRIGGER trg_teams_updated_at
    BEFORE UPDATE ON teams
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =============================================
-- Таблица участников команд
-- =============================================
CREATE TABLE team_members (
                              id        UUID PRIMARY KEY,
                              team_id   UUID    NOT NULL,
                              user_id   UUID    NOT NULL,
                              role      VARCHAR(16) NOT NULL DEFAULT 'MEMBER',
                              joined_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                              left_at   TIMESTAMP WITH TIME ZONE,
                              is_active BOOLEAN DEFAULT TRUE,

                              CONSTRAINT uk_team_user          UNIQUE (team_id, user_id),
                              CONSTRAINT fk_team_members_team  FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE CASCADE,
                              CONSTRAINT fk_team_members_user  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                              CONSTRAINT chk_team_members_role CHECK (role IN ('CAPTAIN', 'MEMBER', 'MODERATOR'))
);

CREATE INDEX idx_team_member_user        ON team_members(user_id);
CREATE INDEX idx_team_member_role        ON team_members(role);
CREATE INDEX idx_team_member_joined_at   ON team_members(joined_at);
CREATE INDEX idx_team_member_active      ON team_members(is_active);
CREATE INDEX idx_team_member_team_active ON team_members(team_id, is_active);
CREATE INDEX idx_team_members_active_only ON team_members(team_id, user_id) WHERE is_active = true;

COMMENT ON COLUMN team_members.role IS 'Роль участника: CAPTAIN, MODERATOR, MEMBER';

-- =============================================
-- Таблица приглашений в команды
-- =============================================
CREATE TABLE team_invitations (
                                  id                 UUID PRIMARY KEY,
                                  team_id            UUID    NOT NULL,
                                  user_id            UUID    NOT NULL,
                                  invited_by_id      UUID    NOT NULL,
                                  status             VARCHAR(16) NOT NULL DEFAULT 'PENDING',
                                  invitation_message VARCHAR(500),
                                  response_message   VARCHAR(500),
                                  created_at         TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                  updated_at         TIMESTAMP WITH TIME ZONE,
                                  responded_at       TIMESTAMP WITH TIME ZONE,
                                  expires_at         TIMESTAMP WITH TIME ZONE,

                                  CONSTRAINT uk_team_user_invite            UNIQUE (team_id, user_id),
                                  CONSTRAINT fk_team_invitations_team       FOREIGN KEY (team_id)       REFERENCES teams(id) ON DELETE CASCADE,
                                  CONSTRAINT fk_team_invitations_user       FOREIGN KEY (user_id)       REFERENCES users(id) ON DELETE CASCADE,
                                  CONSTRAINT fk_team_invitations_invited_by FOREIGN KEY (invited_by_id) REFERENCES users(id) ON DELETE RESTRICT,
                                  CONSTRAINT chk_team_invitations_status    CHECK (status IN ('PENDING', 'ACCEPTED', 'DECLINED', 'EXPIRED'))
);

CREATE INDEX idx_invitation_user          ON team_invitations(user_id);
CREATE INDEX idx_invitation_status        ON team_invitations(status);
CREATE INDEX idx_invitation_created_at    ON team_invitations(created_at);
CREATE INDEX idx_invitation_expires_at    ON team_invitations(expires_at);
CREATE INDEX idx_invitation_user_status   ON team_invitations(user_id, status);
CREATE INDEX idx_invitation_team_status   ON team_invitations(team_id, status);

CREATE TRIGGER trg_team_invitations_updated_at
    BEFORE UPDATE ON team_invitations
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

COMMENT ON COLUMN team_invitations.status IS 'Статус приглашения: PENDING, ACCEPTED, DECLINED, EXPIRED';

-- =============================================
-- Таблица настроек команд
-- =============================================
CREATE TABLE team_settings (
                               id                        UUID PRIMARY KEY,
                               team_id                   UUID    NOT NULL UNIQUE,
                               allow_member_invites      BOOLEAN DEFAULT FALSE,
                               require_captain_approval  BOOLEAN DEFAULT TRUE,
                               auto_accept_invites        BOOLEAN DEFAULT FALSE,
                               invitation_expiry_hours   INTEGER DEFAULT 168,
                               max_pending_invitations   INTEGER DEFAULT 10,
                               allow_member_leave        BOOLEAN DEFAULT TRUE,
                               require_captain_for_disband BOOLEAN DEFAULT TRUE,
                               enable_team_chat          BOOLEAN DEFAULT TRUE,
                               enable_team_statistics    BOOLEAN DEFAULT TRUE,
                               public_profile            BOOLEAN DEFAULT FALSE,
                               allow_search              BOOLEAN DEFAULT TRUE,
                               team_tags                 VARCHAR(500),
                               welcome_message           VARCHAR(1000),
                               created_at                TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                               updated_at                TIMESTAMP WITH TIME ZONE,

                               CONSTRAINT fk_team_settings_team         FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE CASCADE,
                               CONSTRAINT chk_team_settings_expiry_hours CHECK (invitation_expiry_hours IS NULL OR invitation_expiry_hours > 0),
                               CONSTRAINT chk_team_settings_max_invitations CHECK (max_pending_invitations IS NULL OR max_pending_invitations > 0)
);

CREATE INDEX idx_settings_team       ON team_settings(team_id);
CREATE INDEX idx_settings_public     ON team_settings(public_profile, allow_search);
CREATE INDEX idx_settings_searchable ON team_settings(allow_search, public_profile);

CREATE TRIGGER trg_team_settings_updated_at
    BEFORE UPDATE ON team_settings
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =============================================
-- Таблица статистики команд
-- =============================================
CREATE TABLE team_statistics (
                                 id                          UUID PRIMARY KEY,
                                 team_id                     UUID   NOT NULL UNIQUE,
                                 total_members               INTEGER DEFAULT 0,
                                 active_members              INTEGER DEFAULT 0,
                                 total_invitations_sent      BIGINT  DEFAULT 0,
                                 total_invitations_accepted  BIGINT  DEFAULT 0,
                                 total_invitations_declined  BIGINT  DEFAULT 0,
                                 total_games_played          BIGINT  DEFAULT 0,
                                 total_games_won             BIGINT  DEFAULT 0,
                                 total_games_lost            BIGINT  DEFAULT 0,
                                 total_quests_completed      BIGINT  DEFAULT 0,
                                 total_score                 BIGINT  DEFAULT 0,
                                 average_score               DOUBLE PRECISION DEFAULT 0.0,
                                 rating                      DOUBLE PRECISION DEFAULT 1000.0,
                                 rank                        INTEGER,
                                 win_rate                    DOUBLE PRECISION DEFAULT 0.0,
                                 last_activity_at            TIMESTAMP WITH TIME ZONE,
                                 created_at                  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                 updated_at                  TIMESTAMP WITH TIME ZONE,

                                 CONSTRAINT fk_team_statistics_team FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE CASCADE,
                                 CONSTRAINT chk_team_statistics_members
                                     CHECK (total_members >= 0 AND active_members >= 0 AND active_members <= total_members),
                                 CONSTRAINT chk_team_statistics_scores
                                     CHECK (total_score >= 0 AND average_score >= 0 AND rating >= 0 AND win_rate >= 0 AND win_rate <= 100)
);

CREATE INDEX idx_statistics_team       ON team_statistics(team_id);
CREATE INDEX idx_statistics_rating     ON team_statistics(rating DESC);
CREATE INDEX idx_statistics_updated_at ON team_statistics(updated_at);
CREATE INDEX idx_statistics_rank       ON team_statistics(rank);

CREATE TRIGGER trg_team_statistics_updated_at
    BEFORE UPDATE ON team_statistics
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =============================================
-- Триггеры авто-обновления статистики
-- =============================================
CREATE OR REPLACE FUNCTION update_team_member_count()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' AND NEW.is_active = true THEN
UPDATE team_statistics
SET active_members   = active_members + 1,
    total_members    = total_members + 1,
    last_activity_at = CURRENT_TIMESTAMP
WHERE team_id = NEW.team_id;
ELSIF TG_OP = 'UPDATE' THEN
        IF OLD.is_active = true AND NEW.is_active = false THEN
UPDATE team_statistics
SET active_members   = active_members - 1,
    last_activity_at = CURRENT_TIMESTAMP
WHERE team_id = NEW.team_id;
ELSIF OLD.is_active = false AND NEW.is_active = true THEN
UPDATE team_statistics
SET active_members   = active_members + 1,
    last_activity_at = CURRENT_TIMESTAMP
WHERE team_id = NEW.team_id;
END IF;
    ELSIF TG_OP = 'DELETE' AND OLD.is_active = true THEN
UPDATE team_statistics
SET active_members   = active_members - 1,
    total_members    = total_members - 1,
    last_activity_at = CURRENT_TIMESTAMP
WHERE team_id = OLD.team_id;
END IF;
RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_update_team_member_count
    AFTER INSERT OR UPDATE OR DELETE ON team_members
    FOR EACH ROW EXECUTE FUNCTION update_team_member_count();

CREATE OR REPLACE FUNCTION update_invitation_statistics()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
UPDATE team_statistics
SET total_invitations_sent = total_invitations_sent + 1,
    last_activity_at       = CURRENT_TIMESTAMP
WHERE team_id = NEW.team_id;
ELSIF TG_OP = 'UPDATE' AND OLD.status = 'PENDING' THEN
        IF NEW.status = 'ACCEPTED' THEN
UPDATE team_statistics
SET total_invitations_accepted = total_invitations_accepted + 1,
    last_activity_at           = CURRENT_TIMESTAMP
WHERE team_id = NEW.team_id;
ELSIF NEW.status = 'DECLINED' THEN
UPDATE team_statistics
SET total_invitations_declined = total_invitations_declined + 1,
    last_activity_at           = CURRENT_TIMESTAMP
WHERE team_id = NEW.team_id;
END IF;
END IF;
RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_update_invitation_statistics
    AFTER INSERT OR UPDATE ON team_invitations
                        FOR EACH ROW EXECUTE FUNCTION update_invitation_statistics();

-- =============================================
-- Представления
-- =============================================
CREATE OR REPLACE VIEW active_teams AS
SELECT t.*,
       COUNT(tm.id)                                      AS member_count,
       COUNT(CASE WHEN tm.is_active = true THEN 1 END)   AS active_member_count
FROM teams t
         LEFT JOIN team_members tm ON t.id = tm.team_id
WHERE t.is_active = true
GROUP BY t.id;

CREATE OR REPLACE VIEW pending_invitations AS
SELECT ti.id,
       ti.team_id,
       t.name                AS team_name,
       ti.user_id,
       u.username            AS user_username,
       u.email               AS user_email,
       ti.invited_by_id,
       inv.username          AS invited_by_username,
       ti.invitation_message,
       ti.created_at,
       ti.expires_at
FROM team_invitations ti
         JOIN teams t   ON ti.team_id       = t.id
         JOIN users u   ON ti.user_id       = u.id
         JOIN users inv ON ti.invited_by_id = inv.id
WHERE ti.status = 'PENDING'
  AND (ti.expires_at IS NULL OR ti.expires_at > CURRENT_TIMESTAMP);

COMMENT ON TABLE teams IS 'Команды';
COMMENT ON TABLE team_members IS 'Участники команд';
COMMENT ON TABLE team_invitations IS 'Приглашения в команды';
COMMENT ON TABLE team_settings IS 'Настройки команд';
COMMENT ON TABLE team_statistics IS 'Статистика команд';