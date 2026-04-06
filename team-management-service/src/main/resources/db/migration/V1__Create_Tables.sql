-- Создание таблицы пользователей
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    avatar_url VARCHAR(500),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE
);

-- Создание таблицы команд
CREATE TABLE IF NOT EXISTS teams (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL UNIQUE,
    description VARCHAR(500),
    logo_url VARCHAR(500),
    captain_id UUID NOT NULL,
    max_members INTEGER DEFAULT 10,
    is_private BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_teams_captain FOREIGN KEY (captain_id) REFERENCES users(id) ON DELETE RESTRICT
);

-- Создание таблицы участников команд
CREATE TABLE IF NOT EXISTS team_members (
    id UUID PRIMARY KEY,
    team_id UUID NOT NULL,
    user_id UUID NOT NULL,
    role VARCHAR(16) NOT NULL DEFAULT 'MEMBER',
    joined_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    left_at TIMESTAMP WITH TIME ZONE,
    is_active BOOLEAN DEFAULT TRUE,
    CONSTRAINT uk_team_user UNIQUE (team_id, user_id),
    CONSTRAINT fk_team_members_team FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE CASCADE,
    CONSTRAINT fk_team_members_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Создание таблицы приглашений в команды
CREATE TABLE IF NOT EXISTS team_invitations (
    id UUID PRIMARY KEY,
    team_id UUID NOT NULL,
    user_id UUID NOT NULL,
    invited_by_id UUID NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    invitation_message VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    responded_at TIMESTAMP WITH TIME ZONE,
    expires_at TIMESTAMP WITH TIME ZONE,
    response_message VARCHAR(500),
    CONSTRAINT uk_team_user_invite UNIQUE (team_id, user_id),
    CONSTRAINT fk_team_invitations_team FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE CASCADE,
    CONSTRAINT fk_team_invitations_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_team_invitations_invited_by FOREIGN KEY (invited_by_id) REFERENCES users(id) ON DELETE RESTRICT
);

-- Создание таблицы настроек команд
CREATE TABLE IF NOT EXISTS team_settings (
    id UUID PRIMARY KEY,
    team_id UUID NOT NULL UNIQUE,
    allow_member_invites BOOLEAN DEFAULT FALSE,
    require_captain_approval BOOLEAN DEFAULT TRUE,
    auto_accept_invites BOOLEAN DEFAULT FALSE,
    invitation_expiry_hours INTEGER DEFAULT 168,
    max_pending_invitations INTEGER DEFAULT 10,
    allow_member_leave BOOLEAN DEFAULT TRUE,
    require_captain_for_disband BOOLEAN DEFAULT TRUE,
    enable_team_chat BOOLEAN DEFAULT TRUE,
    enable_team_statistics BOOLEAN DEFAULT TRUE,
    public_profile BOOLEAN DEFAULT FALSE,
    allow_search BOOLEAN DEFAULT TRUE,
    team_tags VARCHAR(500),
    welcome_message VARCHAR(1000),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_team_settings_team FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE CASCADE
);

-- Создание таблицы статистики команд
CREATE TABLE IF NOT EXISTS team_statistics (
    id UUID PRIMARY KEY,
    team_id UUID NOT NULL UNIQUE,
    total_members INTEGER DEFAULT 0,
    active_members INTEGER DEFAULT 0,
    total_invitations_sent BIGINT DEFAULT 0,
    total_invitations_accepted BIGINT DEFAULT 0,
    total_invitations_declined BIGINT DEFAULT 0,
    total_games_played BIGINT DEFAULT 0,
    total_games_won BIGINT DEFAULT 0,
    total_games_lost BIGINT DEFAULT 0,
    total_quests_completed BIGINT DEFAULT 0,
    total_score BIGINT DEFAULT 0,
    average_score DOUBLE PRECISION DEFAULT 0.0,
    rating DOUBLE PRECISION DEFAULT 1000.0,
    rank INTEGER,
    win_rate DOUBLE PRECISION DEFAULT 0.0,
    last_activity_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_team_statistics_team FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE CASCADE
);

-- Создание индексов для таблицы users
CREATE INDEX IF NOT EXISTS idx_user_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_user_email ON users(email);

-- Создание индексов для таблицы teams
CREATE INDEX IF NOT EXISTS idx_team_captain ON teams(captain_id);
CREATE INDEX IF NOT EXISTS idx_team_created_at ON teams(created_at);
CREATE INDEX IF NOT EXISTS idx_team_name_search ON teams USING gin(to_tsvector('russian', name));
CREATE INDEX IF NOT EXISTS idx_team_active_private ON teams(is_active, is_private);

-- Создание индексов для таблицы team_members
CREATE INDEX IF NOT EXISTS idx_team_member_user ON team_members(user_id);
CREATE INDEX IF NOT EXISTS idx_team_member_role ON team_members(role);
CREATE INDEX IF NOT EXISTS idx_team_member_joined_at ON team_members(joined_at);
CREATE INDEX IF NOT EXISTS idx_team_member_active ON team_members(is_active);
CREATE INDEX IF NOT EXISTS idx_team_member_team_active ON team_members(team_id, is_active);

-- Создание индексов для таблицы team_invitations
CREATE INDEX IF NOT EXISTS idx_invitation_user ON team_invitations(user_id);
CREATE INDEX IF NOT EXISTS idx_invitation_status ON team_invitations(status);
CREATE INDEX IF NOT EXISTS idx_invitation_created_at ON team_invitations(created_at);
CREATE INDEX IF NOT EXISTS idx_invitation_expires_at ON team_invitations(expires_at);
CREATE INDEX IF NOT EXISTS idx_invitation_user_status ON team_invitations(user_id, status);
CREATE INDEX IF NOT EXISTS idx_invitation_team_status ON team_invitations(team_id, status);

-- Создание индексов для таблицы team_settings
CREATE INDEX IF NOT EXISTS idx_settings_team ON team_settings(team_id);
CREATE INDEX IF NOT EXISTS idx_settings_public ON team_settings(public_profile, allow_search);

-- Создание индексов для таблицы team_statistics
CREATE INDEX IF NOT EXISTS idx_statistics_team ON team_statistics(team_id);
CREATE INDEX IF NOT EXISTS idx_statistics_rating ON team_statistics(rating DESC);
CREATE INDEX IF NOT EXISTS idx_statistics_updated_at ON team_statistics(updated_at);
CREATE INDEX IF NOT EXISTS idx_statistics_rank ON team_statistics(rank);

-- Создание триггеров для обновления updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Применение триггеров к таблицам
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_teams_updated_at BEFORE UPDATE ON teams
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_team_invitations_updated_at BEFORE UPDATE ON team_invitations
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_team_settings_updated_at BEFORE UPDATE ON team_settings
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_team_statistics_updated_at BEFORE UPDATE ON team_statistics
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Создание представлений для удобных запросов
CREATE OR REPLACE VIEW active_teams AS
SELECT t.*, 
       COUNT(tm.id) as member_count,
       COUNT(CASE WHEN tm.is_active = true THEN 1 END) as active_member_count
FROM teams t
LEFT JOIN team_members tm ON t.id = tm.team_id
WHERE t.is_active = true
GROUP BY t.id;

CREATE OR REPLACE VIEW team_member_details AS
SELECT 
    tm.id,
    tm.team_id,
    t.name as team_name,
    tm.user_id,
    u.username,
    u.first_name,
    u.last_name,
    u.email,
    tm.role,
    tm.joined_at,
    tm.left_at,
    tm.is_active
FROM team_members tm
JOIN teams t ON tm.team_id = t.id
JOIN users u ON tm.user_id = u.id;

CREATE OR REPLACE VIEW pending_invitations AS
SELECT 
    ti.id,
    ti.team_id,
    t.name as team_name,
    ti.user_id,
    u.username as user_username,
    u.email as user_email,
    ti.invited_by_id,
    inv.username as invited_by_username,
    ti.invitation_message,
    ti.created_at,
    ti.expires_at
FROM team_invitations ti
JOIN teams t ON ti.team_id = t.id
JOIN users u ON ti.user_id = u.id
JOIN users inv ON ti.invited_by_id = inv.id
WHERE ti.status = 'PENDING' 
  AND (ti.expires_at IS NULL OR ti.expires_at > CURRENT_TIMESTAMP);

-- Вставка начальных данных (если необходимо)
-- Здесь можно добавить начальных пользователей или тестовые данные