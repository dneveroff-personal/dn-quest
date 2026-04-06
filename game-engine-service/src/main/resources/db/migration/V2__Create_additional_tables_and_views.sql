-- Создание таблицы квестов (если еще не существует)
CREATE TABLE IF NOT EXISTS quests (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    type VARCHAR(50) NOT NULL DEFAULT 'ADVENTURE',
    difficulty VARCHAR(50) NOT NULL DEFAULT 'MEDIUM',
    author_id UUID NOT NULL,
    is_published BOOLEAN DEFAULT FALSE,
    is_archived BOOLEAN DEFAULT FALSE,
    rating DECIMAL(3,2) DEFAULT 0.0,
    total_ratings INTEGER DEFAULT 0,
    estimated_duration_minutes INTEGER,
    max_participants INTEGER DEFAULT 10,
    min_participants INTEGER DEFAULT 1,
    level_ids UUID[],
    tags TEXT[],
    settings JSONB,
    metadata JSONB,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMP WITH TIME ZONE,
    
    CONSTRAINT fk_quests_author FOREIGN KEY (author_id) REFERENCES users(id),
    CONSTRAINT chk_quests_type CHECK (type IN ('ADVENTURE', 'MYSTERY', 'PUZZLE', 'ESCAPE', 'EDUCATIONAL', 'COMPETITIVE')),
    CONSTRAINT chk_quests_difficulty CHECK (difficulty IN ('EASY', 'MEDIUM', 'HARD', 'EXPERT')),
    CONSTRAINT chk_quests_rating CHECK (rating >= 0.0 AND rating <= 5.0),
    CONSTRAINT chk_quests_total_ratings CHECK (total_ratings >= 0),
    CONSTRAINT chk_quests_estimated_duration CHECK (estimated_duration_minutes > 0),
    CONSTRAINT chk_quests_max_participants CHECK (max_participants > 0 AND max_participants <= 1000),
    CONSTRAINT chk_quests_min_participants CHECK (min_participants > 0 AND min_participants <= max_participants)
);

-- Создание таблицы уровней (если еще не существует)
CREATE TABLE IF NOT EXISTS levels (
    id UUID PRIMARY KEY,
    quest_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    order_number INTEGER NOT NULL,
    difficulty VARCHAR(50) NOT NULL DEFAULT 'MEDIUM',
    estimated_time_minutes INTEGER,
    max_attempts INTEGER DEFAULT 0,
    hint_penalty_points DECIMAL(10,2) DEFAULT 10.0,
    time_limit_minutes INTEGER,
    is_required BOOLEAN DEFAULT TRUE,
    is_bonus BOOLEAN DEFAULT FALSE,
    settings JSONB,
    metadata JSONB,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_levels_quest FOREIGN KEY (quest_id) REFERENCES quests(id) ON DELETE CASCADE,
    CONSTRAINT chk_levels_difficulty CHECK (difficulty IN ('EASY', 'MEDIUM', 'HARD', 'EXPERT')),
    CONSTRAINT chk_levels_estimated_time CHECK (estimated_time_minutes > 0),
    CONSTRAINT chk_levels_max_attempts CHECK (max_attempts >= 0),
    CONSTRAINT chk_levels_hint_penalty_points CHECK (hint_penalty_points >= 0),
    CONSTRAINT chk_levels_time_limit CHECK (time_limit_minutes > 0),
    CONSTRAINT chk_levels_order_number CHECK (order_number > 0),
    CONSTRAINT uq_levels_quest_order UNIQUE(quest_id, order_number)
);

-- Создание таблицы кодов (если еще не существует)
CREATE TABLE IF NOT EXISTS codes (
    id UUID PRIMARY KEY,
    level_id UUID NOT NULL,
    code_value VARCHAR(255) NOT NULL,
    sector VARCHAR(10) NOT NULL,
    code_type VARCHAR(50) NOT NULL DEFAULT 'MAIN',
    points DECIMAL(10,2) NOT NULL DEFAULT 0.0,
    is_active BOOLEAN DEFAULT TRUE,
    is_bonus BOOLEAN DEFAULT FALSE,
    bonus_multiplier DECIMAL(3,2) DEFAULT 1.0,
    description TEXT,
    hint TEXT,
    metadata JSONB,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_codes_level FOREIGN KEY (level_id) REFERENCES levels(id) ON DELETE CASCADE,
    CONSTRAINT chk_codes_sector CHECK (sector ~ '^[A-Za-z]$'),
    CONSTRAINT chk_codes_code_type CHECK (code_type IN ('MAIN', 'BONUS', 'SECRET', 'TIME_BONUS', 'PENALTY')),
    CONSTRAINT chk_codes_points CHECK (points >= 0),
    CONSTRAINT chk_codes_bonus_multiplier CHECK (bonus_multiplier > 0),
    CONSTRAINT uq_codes_level_sector_type UNIQUE(level_id, sector, code_type)
);

-- Создание таблицы подсказок (если еще не существует)
CREATE TABLE IF NOT EXISTS level_hints (
    id UUID PRIMARY KEY,
    level_id UUID NOT NULL,
    hint_text TEXT NOT NULL,
    order_number INTEGER NOT NULL,
    penalty_points DECIMAL(10,2) DEFAULT 10.0,
    is_active BOOLEAN DEFAULT TRUE,
    requires_code VARCHAR(255),
    metadata JSONB,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_level_hints_level FOREIGN KEY (level_id) REFERENCES levels(id) ON DELETE CASCADE,
    CONSTRAINT chk_level_hints_penalty_points CHECK (penalty_points >= 0),
    CONSTRAINT chk_level_hints_order_number CHECK (order_number > 0),
    CONSTRAINT uq_level_hints_level_order UNIQUE(level_id, order_number)
);

-- Создание таблицы пользователей (если еще не существует)
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    display_name VARCHAR(255),
    avatar_url VARCHAR(500),
    bio TEXT,
    rating DECIMAL(10,2) DEFAULT 1000.0,
    level INTEGER DEFAULT 1,
    experience_points BIGINT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    is_verified BOOLEAN DEFAULT FALSE,
    last_login_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    settings JSONB,
    preferences JSONB,
    metadata JSONB,
    
    CONSTRAINT chk_users_rating CHECK (rating >= 0),
    CONSTRAINT chk_users_level CHECK (level > 0),
    CONSTRAINT chk_users_experience_points CHECK (experience_points >= 0)
);

-- Создание таблицы команд (если еще не существует)
CREATE TABLE IF NOT EXISTS teams (
    id UUID PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    description TEXT,
    captain_id UUID NOT NULL,
    logo_url VARCHAR(500),
    rating DECIMAL(10,2) DEFAULT 1000.0,
    total_games_played INTEGER DEFAULT 0,
    total_games_won INTEGER DEFAULT 0,
    total_playtime_seconds BIGINT DEFAULT 0,
    max_members INTEGER DEFAULT 10,
    is_active BOOLEAN DEFAULT TRUE,
    is_public BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    settings JSONB,
    metadata JSONB,
    
    CONSTRAINT fk_teams_captain FOREIGN KEY (captain_id) REFERENCES users(id),
    CONSTRAINT chk_teams_rating CHECK (rating >= 0),
    CONSTRAINT chk_teams_total_games_played CHECK (total_games_played >= 0),
    CONSTRAINT chk_teams_total_games_won CHECK (total_games_won >= 0 AND total_games_won <= total_games_played),
    CONSTRAINT chk_teams_total_playtime_seconds CHECK (total_playtime_seconds >= 0),
    CONSTRAINT chk_teams_max_members CHECK (max_members > 0 AND max_members <= 100)
);

-- Создание таблицы участников команд (если еще не существует)
CREATE TABLE IF NOT EXISTS team_members (
    id UUID PRIMARY KEY,
    team_id UUID NOT NULL,
    user_id UUID NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'MEMBER',
    joined_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    left_at TIMESTAMP WITH TIME ZONE,
    is_active BOOLEAN DEFAULT TRUE,
    games_played INTEGER DEFAULT 0,
    games_won INTEGER DEFAULT 0,
    total_playtime_seconds BIGINT DEFAULT 0,
    rating DECIMAL(10,2) DEFAULT 1000.0,
    metadata JSONB,
    
    CONSTRAINT fk_team_members_team FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE CASCADE,
    CONSTRAINT fk_team_members_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_team_members_role CHECK (role IN ('CAPTAIN', 'MEMBER', 'MODERATOR')),
    CONSTRAINT chk_team_members_games_played CHECK (games_played >= 0),
    CONSTRAINT chk_team_members_games_won CHECK (games_won >= 0 AND games_won <= games_played),
    CONSTRAINT chk_team_members_total_playtime_seconds CHECK (total_playtime_seconds >= 0),
    CONSTRAINT chk_team_members_rating CHECK (rating >= 0),
    CONSTRAINT uq_team_members_team_user UNIQUE(team_id, user_id)
);

-- Создание представлений для статистики

-- Представление для статистики игровых сессий
CREATE OR REPLACE VIEW session_statistics AS
SELECT 
    gs.id,
    gs.name,
    gs.status,
    gs.owner_id,
    gs.quest_id,
    gs.team_id,
    gs.created_at,
    gs.started_at,
    gs.finished_at,
    gs.duration_seconds,
    gs.total_score,
    COUNT(DISTINCT sp.user_id) as participant_count,
    COUNT(DISTINCT ca.id) as total_attempts,
    COUNT(DISTINCT CASE WHEN ca.result = 'CORRECT' THEN ca.id END) as correct_attempts,
    COUNT(DISTINCT lc.id) as completed_levels,
    AVG(lc.final_score) as avg_level_score,
    AVG(lc.time_spent_seconds) as avg_level_time
FROM game_sessions gs
LEFT JOIN session_participants sp ON gs.id = sp.session_id AND sp.is_active = true
LEFT JOIN code_attempts ca ON gs.id = ca.session_id
LEFT JOIN level_completions lc ON gs.id = lc.session_id
GROUP BY gs.id, gs.name, gs.status, gs.owner_id, gs.quest_id, gs.team_id, 
         gs.created_at, gs.started_at, gs.finished_at, gs.duration_seconds, gs.total_score;

-- Представление для статистики пользователей
CREATE OR REPLACE VIEW user_statistics AS
SELECT 
    u.id,
    u.username,
    u.display_name,
    u.rating,
    u.level,
    u.experience_points,
    COUNT(DISTINCT gs.id) as total_sessions,
    COUNT(DISTINCT CASE WHEN gs.status = 'COMPLETED' THEN gs.id END) as completed_sessions,
    COUNT(DISTINCT ca.id) as total_attempts,
    COUNT(DISTINCT CASE WHEN ca.result = 'CORRECT' THEN ca.id END) as correct_attempts,
    COUNT(DISTINCT lc.id) as completed_levels,
    AVG(lc.final_score) as avg_completion_score,
    AVG(lc.time_spent_seconds) as avg_completion_time,
    SUM(CASE WHEN gs.finished_at IS NOT NULL THEN gs.duration_seconds ELSE 0 END) as total_playtime_seconds
FROM users u
LEFT JOIN session_participants sp ON u.id = sp.user_id AND sp.is_active = true
LEFT JOIN game_sessions gs ON sp.session_id = gs.id
LEFT JOIN code_attempts ca ON u.id = ca.user_id
LEFT JOIN level_completions lc ON u.id = lc.user_id
GROUP BY u.id, u.username, u.display_name, u.rating, u.level, u.experience_points;

-- Представление для статистики команд
CREATE OR REPLACE VIEW team_statistics AS
SELECT 
    t.id,
    t.name,
    t.rating,
    t.total_games_played,
    t.total_games_won,
    t.total_playtime_seconds,
    COUNT(DISTINCT tm.user_id) as member_count,
    COUNT(DISTINCT gs.id) as team_sessions,
    COUNT(DISTINCT CASE WHEN gs.status = 'COMPLETED' THEN gs.id END) as completed_sessions,
    AVG(lc.final_score) as avg_completion_score,
    AVG(lc.time_spent_seconds) as avg_completion_time,
    CASE 
        WHEN t.total_games_played > 0 
        THEN ROUND((t.total_games_won::DECIMAL / t.total_games_played) * 100, 2)
        ELSE 0 
    END as win_rate_percentage
FROM teams t
LEFT JOIN team_members tm ON t.id = tm.team_id AND tm.is_active = true
LEFT JOIN game_sessions gs ON t.id = gs.team_id
LEFT JOIN level_completions lc ON gs.id = lc.session_id
GROUP BY t.id, t.name, t.rating, t.total_games_played, t.total_games_won, t.total_playtime_seconds;

-- Представление для лидербордов сессий
CREATE OR REPLACE VIEW session_leaderboard AS
SELECT 
    gs.id as session_id,
    gs.name as session_name,
    sp.user_id,
    u.username,
    u.display_name,
    COALESCE(SUM(lc.final_score), 0) as total_score,
    COUNT(DISTINCT lc.id) as completed_levels,
    SUM(COALESCE(lc.time_spent_seconds, 0)) as total_time_seconds,
    RANK() OVER (PARTITION BY gs.id ORDER BY COALESCE(SUM(lc.final_score), 0) DESC) as rank,
    ROW_NUMBER() OVER (PARTITION BY gs.id ORDER BY COALESCE(SUM(lc.final_score), 0) DESC) as row_number
FROM game_sessions gs
JOIN session_participants sp ON gs.id = sp.session_id AND sp.is_active = true
JOIN users u ON sp.user_id = u.id
LEFT JOIN level_completions lc ON gs.id = lc.session_id AND sp.user_id = lc.user_id
GROUP BY gs.id, gs.name, sp.user_id, u.username, u.display_name;

-- Создание индексов для новых таблиц
CREATE INDEX IF NOT EXISTS idx_quests_author_id ON quests(author_id);
CREATE INDEX IF NOT EXISTS idx_quests_type ON quests(type);
CREATE INDEX IF NOT EXISTS idx_quests_difficulty ON quests(difficulty);
CREATE INDEX IF NOT EXISTS idx_quests_is_published ON quests(is_published);
CREATE INDEX IF NOT EXISTS idx_quests_rating ON quests(rating);
CREATE INDEX IF NOT EXISTS idx_quests_created_at ON quests(created_at);

CREATE INDEX IF NOT EXISTS idx_levels_quest_id ON levels(quest_id);
CREATE INDEX IF NOT EXISTS idx_levels_difficulty ON levels(difficulty);
CREATE INDEX IF NOT EXISTS idx_levels_order_number ON levels(order_number);
CREATE INDEX IF NOT EXISTS idx_levels_is_required ON levels(is_required);
CREATE INDEX IF NOT EXISTS idx_levels_is_bonus ON levels(is_bonus);

CREATE INDEX IF NOT EXISTS idx_codes_level_id ON codes(level_id);
CREATE INDEX IF NOT EXISTS idx_codes_sector ON codes(sector);
CREATE INDEX IF NOT EXISTS idx_codes_code_type ON codes(code_type);
CREATE INDEX IF NOT EXISTS idx_codes_is_active ON codes(is_active);
CREATE INDEX IF NOT EXISTS idx_codes_is_bonus ON codes(is_bonus);
CREATE INDEX IF NOT EXISTS idx_codes_points ON codes(points);

CREATE INDEX IF NOT EXISTS idx_level_hints_level_id ON level_hints(level_id);
CREATE INDEX IF NOT EXISTS idx_level_hints_order_number ON level_hints(order_number);
CREATE INDEX IF NOT EXISTS idx_level_hints_is_active ON level_hints(is_active);

CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_rating ON users(rating);
CREATE INDEX IF NOT EXISTS idx_users_level ON users(level);
CREATE INDEX IF NOT EXISTS idx_users_is_active ON users(is_active);
CREATE INDEX IF NOT EXISTS idx_users_created_at ON users(created_at);

CREATE INDEX IF NOT EXISTS idx_teams_captain_id ON teams(captain_id);
CREATE INDEX IF NOT EXISTS idx_teams_rating ON teams(rating);
CREATE INDEX IF NOT EXISTS idx_teams_is_active ON teams(is_active);
CREATE INDEX IF NOT EXISTS idx_teams_is_public ON teams(is_public);
CREATE INDEX IF NOT EXISTS idx_teams_created_at ON teams(created_at);

CREATE INDEX IF NOT EXISTS idx_team_members_team_id ON team_members(team_id);
CREATE INDEX IF NOT EXISTS idx_team_members_user_id ON team_members(user_id);
CREATE INDEX IF NOT EXISTS idx_team_members_role ON team_members(role);
CREATE INDEX IF NOT EXISTS idx_team_members_is_active ON team_members(is_active);
CREATE INDEX IF NOT EXISTS idx_team_members_joined_at ON team_members(joined_at);

-- Создание триггера для обновления updated_at
CREATE OR REPLACE FUNCTION trigger_set_timestamp()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = CURRENT_TIMESTAMP;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Применение триггера к таблицам
CREATE TRIGGER set_timestamp_quests BEFORE UPDATE ON quests FOR EACH ROW EXECUTE FUNCTION trigger_set_timestamp();
CREATE TRIGGER set_timestamp_levels BEFORE UPDATE ON levels FOR EACH ROW EXECUTE FUNCTION trigger_set_timestamp();
CREATE TRIGGER set_timestamp_codes BEFORE UPDATE ON codes FOR EACH ROW EXECUTE FUNCTION trigger_set_timestamp();
CREATE TRIGGER set_timestamp_level_hints BEFORE UPDATE ON level_hints FOR EACH ROW EXECUTE FUNCTION trigger_set_timestamp();
CREATE TRIGGER set_timestamp_users BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION trigger_set_timestamp();
CREATE TRIGGER set_timestamp_teams BEFORE UPDATE ON teams FOR EACH ROW EXECUTE FUNCTION trigger_set_timestamp();
CREATE TRIGGER set_timestamp_team_members BEFORE UPDATE ON team_members FOR EACH ROW EXECUTE FUNCTION trigger_set_timestamp();

-- Добавление комментариев к представлениям
COMMENT ON VIEW session_statistics IS 'Статистика игровых сессий';
COMMENT ON VIEW user_statistics IS 'Статистика пользователей';
COMMENT ON VIEW team_statistics IS 'Статистика команд';
COMMENT ON VIEW session_leaderboard IS 'Лидерборды сессий';