-- Создание таблицы user_statistics
CREATE TABLE user_statistics (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    date DATE NOT NULL,
    registrations INTEGER DEFAULT 0,
    logins INTEGER DEFAULT 0,
    game_sessions INTEGER DEFAULT 0,
    completed_quests INTEGER DEFAULT 0,
    created_quests INTEGER DEFAULT 0,
    created_teams INTEGER DEFAULT 0,
    team_memberships INTEGER DEFAULT 0,
    total_game_time_minutes BIGINT DEFAULT 0,
    uploaded_files INTEGER DEFAULT 0,
    total_file_size_bytes BIGINT DEFAULT 0,
    successful_code_submissions INTEGER DEFAULT 0,
    failed_code_submissions INTEGER DEFAULT 0,
    completed_levels INTEGER DEFAULT 0,
    avg_level_completion_time_seconds DOUBLE PRECISION,
    current_rating DOUBLE PRECISION,
    rating_change DOUBLE PRECISION,
    last_active_at TIMESTAMP,
    last_ip VARCHAR(45),
    last_user_agent TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Индексы для user_statistics
CREATE INDEX idx_user_statistics_user_id ON user_statistics(user_id);
CREATE INDEX idx_user_statistics_date ON user_statistics(date);
CREATE INDEX idx_user_statistics_user_date ON user_statistics(user_id, date);
CREATE INDEX idx_user_statistics_last_active ON user_statistics(last_active_at);

-- Создание таблицы quest_statistics
CREATE TABLE quest_statistics (
    id UUID PRIMARY KEY,
    quest_id UUID NOT NULL,
    quest_title VARCHAR(255),
    author_id UUID,
    date DATE NOT NULL,
    creations INTEGER DEFAULT 0,
    updates INTEGER DEFAULT 0,
    publications INTEGER DEFAULT 0,
    deletions INTEGER DEFAULT 0,
    views INTEGER DEFAULT 0,
    unique_views INTEGER DEFAULT 0,
    starts INTEGER DEFAULT 0,
    completions INTEGER DEFAULT 0,
    unique_participants INTEGER DEFAULT 0,
    avg_completion_time_minutes DOUBLE PRECISION,
    completion_rate DOUBLE PRECISION,
    current_rating DOUBLE PRECISION,
    rating_count INTEGER DEFAULT 0,
    avg_rating DOUBLE PRECISION,
    comments_count INTEGER DEFAULT 0,
    likes_count INTEGER DEFAULT 0,
    favorites_count INTEGER DEFAULT 0,
    shares_count INTEGER DEFAULT 0,
    difficulty_level INTEGER,
    category VARCHAR(100),
    tags TEXT,
    status VARCHAR(50),
    max_participants INTEGER,
    current_participants INTEGER DEFAULT 0,
    levels_count INTEGER,
    total_game_time_minutes BIGINT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Индексы для quest_statistics
CREATE INDEX idx_quest_statistics_quest_id ON quest_statistics(quest_id);
CREATE INDEX idx_quest_statistics_date ON quest_statistics(date);
CREATE INDEX idx_quest_statistics_author_id ON quest_statistics(author_id);
CREATE INDEX idx_quest_statistics_quest_date ON quest_statistics(quest_id, date);
CREATE INDEX idx_quest_statistics_author_date ON quest_statistics(author_id, date);

-- Создание таблицы game_statistics
CREATE TABLE game_statistics (
    id UUID PRIMARY KEY,
    session_id VARCHAR(255) NOT NULL,
    user_id UUID,
    team_id UUID,
    quest_id UUID NOT NULL,
    date DATE NOT NULL,
    session_type VARCHAR(50),
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    duration_minutes BIGINT,
    current_level INTEGER,
    total_levels INTEGER,
    completed_levels INTEGER DEFAULT 0,
    status VARCHAR(50),
    is_completed BOOLEAN DEFAULT FALSE,
    code_submissions INTEGER DEFAULT 0,
    successful_submissions INTEGER DEFAULT 0,
    failed_submissions INTEGER DEFAULT 0,
    total_attempts INTEGER DEFAULT 0,
    avg_level_time_minutes DOUBLE PRECISION,
    fastest_level_completion_seconds BIGINT,
    slowest_level_completion_seconds BIGINT,
    hints_used INTEGER DEFAULT 0,
    bonuses_earned INTEGER DEFAULT 0,
    score INTEGER DEFAULT 0,
    max_score INTEGER DEFAULT 0,
    completion_percentage DOUBLE PRECISION,
    start_ip VARCHAR(45),
    user_agent TEXT,
    device_type VARCHAR(50),
    browser VARCHAR(100),
    operating_system VARCHAR(100),
    metadata TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Индексы для game_statistics
CREATE INDEX idx_game_statistics_session_id ON game_statistics(session_id);
CREATE INDEX idx_game_statistics_date ON game_statistics(date);
CREATE INDEX idx_game_statistics_user_id ON game_statistics(user_id);
CREATE INDEX idx_game_statistics_quest_id ON game_statistics(quest_id);
CREATE INDEX idx_game_statistics_team_id ON game_statistics(team_id);
CREATE INDEX idx_game_statistics_user_date ON game_statistics(user_id, date);
CREATE INDEX idx_game_statistics_quest_date ON game_statistics(quest_id, date);

-- Создание таблицы team_statistics
CREATE TABLE team_statistics (
    id UUID PRIMARY KEY,
    team_id UUID NOT NULL,
    team_name VARCHAR(255),
    captain_id UUID,
    date DATE NOT NULL,
    creations INTEGER DEFAULT 0,
    updates INTEGER DEFAULT 0,
    deletions INTEGER DEFAULT 0,
    current_members_count INTEGER DEFAULT 0,
    max_members INTEGER,
    member_additions INTEGER DEFAULT 0,
    member_removals INTEGER DEFAULT 0,
    total_unique_members INTEGER DEFAULT 0,
    played_quests INTEGER DEFAULT 0,
    completed_quests INTEGER DEFAULT 0,
    quest_wins INTEGER DEFAULT 0,
    total_game_time_minutes BIGINT DEFAULT 0,
    avg_quest_completion_time_minutes DOUBLE PRECISION,
    current_rating DOUBLE PRECISION,
    rating_change DOUBLE PRECISION,
    successful_code_submissions INTEGER DEFAULT 0,
    failed_code_submissions INTEGER DEFAULT 0,
    completed_levels INTEGER DEFAULT 0,
    avg_level_completion_time_seconds DOUBLE PRECISION,
    team_type VARCHAR(50),
    status VARCHAR(50),
    tags TEXT,
    profile_views INTEGER DEFAULT 0,
    unique_profile_views INTEGER DEFAULT 0,
    invitations_sent INTEGER DEFAULT 0,
    invitations_accepted INTEGER DEFAULT 0,
    invitations_declined INTEGER DEFAULT 0,
    last_activity_at TIMESTAMP,
    last_ip VARCHAR(45),
    last_user_agent TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Индексы для team_statistics
CREATE INDEX idx_team_statistics_team_id ON team_statistics(team_id);
CREATE INDEX idx_team_statistics_date ON team_statistics(date);
CREATE INDEX idx_team_statistics_captain_id ON team_statistics(captain_id);
CREATE INDEX idx_team_statistics_team_date ON team_statistics(team_id, date);
CREATE INDEX idx_team_statistics_captain_date ON team_statistics(captain_id, date);

-- Создание таблицы file_statistics
CREATE TABLE file_statistics (
    id UUID PRIMARY KEY,
    file_id VARCHAR(255) NOT NULL,
    user_id UUID,
    date DATE NOT NULL,
    uploads INTEGER DEFAULT 0,
    downloads INTEGER DEFAULT 0,
    updates INTEGER DEFAULT 0,
    deletions INTEGER DEFAULT 0,
    total_size_bytes BIGINT DEFAULT 0,
    avg_file_size_bytes DOUBLE PRECISION,
    file_type VARCHAR(100),
    mime_type VARCHAR(255),
    storage_type VARCHAR(50),
    is_public BOOLEAN DEFAULT FALSE,
    access_count INTEGER DEFAULT 0,
    unique_access_count INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Индексы для file_statistics
CREATE INDEX idx_file_statistics_file_id ON file_statistics(file_id);
CREATE INDEX idx_file_statistics_date ON file_statistics(date);
CREATE INDEX idx_file_statistics_user_id ON file_statistics(user_id);
CREATE INDEX idx_file_statistics_file_date ON file_statistics(file_id, date);
CREATE INDEX idx_file_statistics_user_date ON file_statistics(user_id, date);

-- Создание таблицы leaderboards
CREATE TABLE leaderboards (
    id UUID PRIMARY KEY,
    leaderboard_type VARCHAR(50) NOT NULL,
    period VARCHAR(50) NOT NULL,
    date DATE,
    entity_id UUID NOT NULL,
    entity_name VARCHAR(255),
    rank INTEGER NOT NULL,
    previous_rank INTEGER,
    rank_change INTEGER,
    score DOUBLE PRECISION,
    previous_score DOUBLE PRECISION,
    score_change DOUBLE PRECISION,
    category VARCHAR(100),
    tags TEXT,
    metrics TEXT,
    avatar_url VARCHAR(500),
    profile_url VARCHAR(500),
    achievements_count INTEGER DEFAULT 0,
    level INTEGER,
    progress_percentage DOUBLE PRECISION,
    status VARCHAR(50),
    is_active BOOLEAN DEFAULT TRUE,
    last_updated_at TIMESTAMP,
    participations_count INTEGER DEFAULT 0,
    wins_count INTEGER DEFAULT 0,
    win_rate DOUBLE PRECISION,
    avg_completion_time DOUBLE PRECISION,
    ratings_count INTEGER DEFAULT 0,
    avg_rating DOUBLE PRECISION,
    views_count INTEGER DEFAULT 0,
    likes_count INTEGER DEFAULT 0,
    comments_count INTEGER DEFAULT 0,
    metadata TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Индексы для leaderboards
CREATE INDEX idx_leaderboards_type ON leaderboards(leaderboard_type);
CREATE INDEX idx_leaderboards_period ON leaderboards(period);
CREATE INDEX idx_leaderboards_date ON leaderboards(date);
CREATE INDEX idx_leaderboards_entity_id ON leaderboards(entity_id);
CREATE INDEX idx_leaderboards_type_period ON leaderboards(leaderboard_type, period);
CREATE INDEX idx_leaderboards_rank ON leaderboards(rank);

-- Создание таблицы system_statistics
CREATE TABLE system_statistics (
    id UUID PRIMARY KEY,
    date DATE NOT NULL,
    metric VARCHAR(255) NOT NULL,
    value DOUBLE PRECISION,
    text_value TEXT,
    count BIGINT DEFAULT 0,
    category VARCHAR(100),
    subcategory VARCHAR(100),
    unit VARCHAR(50),
    min_value DOUBLE PRECISION,
    max_value DOUBLE PRECISION,
    avg_value DOUBLE PRECISION,
    total_value DOUBLE PRECISION,
    previous_value DOUBLE PRECISION,
    percentage_change DOUBLE PRECISION,
    metadata TEXT,
    status VARCHAR(50),
    is_active BOOLEAN DEFAULT TRUE,
    last_updated_at TIMESTAMP,
    data_source VARCHAR(100),
    update_frequency VARCHAR(50),
    priority INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Индексы для system_statistics
CREATE INDEX idx_system_statistics_date ON system_statistics(date);
CREATE INDEX idx_system_statistics_metric ON system_statistics(metric);
CREATE INDEX idx_system_statistics_date_metric ON system_statistics(date, metric);

-- Создание таблицы daily_aggregated_statistics
CREATE TABLE daily_aggregated_statistics (
    id UUID PRIMARY KEY,
    date DATE NOT NULL,
    total_users BIGINT DEFAULT 0,
    active_users BIGINT DEFAULT 0,
    new_registrations BIGINT DEFAULT 0,
    total_sessions BIGINT DEFAULT 0,
    completed_sessions BIGINT DEFAULT 0,
    total_quests BIGINT DEFAULT 0,
    new_quests BIGINT DEFAULT 0,
    completed_quests BIGINT DEFAULT 0,
    total_teams BIGINT DEFAULT 0,
    new_teams BIGINT DEFAULT 0,
    total_files BIGINT DEFAULT 0,
    new_files BIGINT DEFAULT 0,
    total_storage_used_bytes BIGINT DEFAULT 0,
    avg_session_duration_minutes DOUBLE PRECISION,
    avg_completion_rate DOUBLE PRECISION,
    total_game_time_minutes BIGINT DEFAULT 0,
    unique_visitors BIGINT DEFAULT 0,
    page_views BIGINT DEFAULT 0,
    bounce_rate DOUBLE PRECISION,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Индексы для daily_aggregated_statistics
CREATE INDEX idx_daily_aggregated_statistics_date ON daily_aggregated_statistics(date);

-- Уникальные ограничения
ALTER TABLE user_statistics ADD CONSTRAINT uk_user_statistics_user_date UNIQUE (user_id, date);
ALTER TABLE quest_statistics ADD CONSTRAINT uk_quest_statistics_quest_date UNIQUE (quest_id, date);
ALTER TABLE system_statistics ADD CONSTRAINT uk_system_statistics_date_metric UNIQUE (date, metric);
ALTER TABLE daily_aggregated_statistics ADD CONSTRAINT uk_daily_aggregated_statistics_date UNIQUE (date);

-- Триггеры для обновления updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_user_statistics_updated_at BEFORE UPDATE ON user_statistics FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_quest_statistics_updated_at BEFORE UPDATE ON quest_statistics FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_game_statistics_updated_at BEFORE UPDATE ON game_statistics FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_team_statistics_updated_at BEFORE UPDATE ON team_statistics FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_file_statistics_updated_at BEFORE UPDATE ON file_statistics FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_leaderboards_updated_at BEFORE UPDATE ON leaderboards FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_system_statistics_updated_at BEFORE UPDATE ON system_statistics FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_daily_aggregated_statistics_updated_at BEFORE UPDATE ON daily_aggregated_statistics FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();