-- Индексы для оптимизации производительности запросов

-- Индексы для таблицы users
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_users_public_name ON users(public_name);
CREATE INDEX IF NOT EXISTS idx_users_created_at ON users(created_at);

-- Индексы для таблицы quests
CREATE INDEX IF NOT EXISTS idx_quests_published ON quests(published);
CREATE INDEX IF NOT EXISTS idx_quests_start_at ON quests(start_at);
CREATE INDEX IF NOT EXISTS idx_quests_end_at ON quests(end_at);
CREATE INDEX IF NOT EXISTS idx_quests_created_at ON quests(created_at);
CREATE INDEX IF NOT EXISTS idx_quests_difficulty ON quests(difficulty);
CREATE INDEX IF NOT EXISTS idx_quests_type ON quests(type);
CREATE INDEX IF NOT EXISTS idx_quests_published_active ON quests(published, end_at);
CREATE INDEX IF NOT EXISTS idx_quests_published_start_end ON quests(published, start_at, end_at);

-- Индексы для таблицы teams
CREATE INDEX IF NOT EXISTS idx_teams_name ON teams(name);
CREATE INDEX IF NOT EXISTS idx_teams_captain_id ON teams(captain_id);
CREATE INDEX IF NOT EXISTS idx_teams_created_at ON teams(created_at);

-- Индексы для таблицы team_members
CREATE INDEX IF NOT EXISTS idx_team_members_team_id ON team_members(team_id);
CREATE INDEX IF NOT EXISTS idx_team_members_user_id ON team_members(user_id);
CREATE INDEX IF NOT EXISTS idx_team_members_role ON team_members(role);
CREATE INDEX IF NOT EXISTS idx_team_members_joined_at ON team_members(joined_at);
CREATE INDEX IF NOT EXISTS idx_team_members_team_user ON team_members(team_id, user_id);
CREATE INDEX IF NOT EXISTS idx_team_members_user_role ON team_members(user_id, role);

-- Индексы для таблицы game_sessions
CREATE INDEX IF NOT EXISTS idx_game_sessions_quest_id ON game_sessions(quest_id);
CREATE INDEX IF NOT EXISTS idx_game_sessions_user_id ON game_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_game_sessions_team_id ON game_sessions(team_id);
CREATE INDEX IF NOT EXISTS idx_game_sessions_status ON game_sessions(status);
CREATE INDEX IF NOT EXISTS idx_game_sessions_started_at ON game_sessions(started_at);
CREATE INDEX IF NOT EXISTS idx_game_sessions_finished_at ON game_sessions(finished_at);
CREATE INDEX IF NOT EXISTS idx_game_sessions_quest_status ON game_sessions(quest_id, status);
CREATE INDEX IF NOT EXISTS idx_game_sessions_user_status ON game_sessions(user_id, status);
CREATE INDEX IF NOT EXISTS idx_game_sessions_team_status ON game_sessions(team_id, status);
CREATE INDEX IF NOT EXISTS idx_game_sessions_status_started ON game_sessions(status, started_at);

-- Индексы для таблицы levels
CREATE INDEX IF NOT EXISTS idx_levels_quest_id ON levels(quest_id);
CREATE INDEX IF NOT EXISTS idx_levels_order_index ON levels(order_index);
CREATE INDEX IF NOT EXISTS idx_levels_quest_order ON levels(quest_id, order_index);

-- Индексы для таблицы codes
CREATE INDEX IF NOT EXISTS idx_codes_level_id ON codes(level_id);
CREATE INDEX IF NOT EXISTS idx_codes_type ON codes(type);
CREATE INDEX IF NOT EXISTS idx_codes_sector_no ON codes(sector_no);
CREATE INDEX IF NOT EXISTS idx_codes_level_type ON codes(level_id, type);
CREATE INDEX IF NOT EXISTS idx_codes_level_sector ON codes(level_id, sector_no);
CREATE INDEX IF NOT EXISTS idx_codes_normalized ON codes(normalized);

-- Индексы для таблицы code_attempts
CREATE INDEX IF NOT EXISTS idx_code_attempts_session_id ON code_attempts(session_id);
CREATE INDEX IF NOT EXISTS idx_code_attempts_level_id ON code_attempts(level_id);
CREATE INDEX IF NOT EXISTS idx_code_attempts_user_id ON code_attempts(user_id);
CREATE INDEX IF NOT EXISTS idx_code_attempts_result ON code_attempts(result);
CREATE INDEX IF NOT EXISTS idx_code_attempts_created_at ON code_attempts(created_at);
CREATE INDEX IF NOT EXISTS idx_code_attempts_session_level ON code_attempts(session_id, level_id);
CREATE INDEX IF NOT EXISTS idx_code_attempts_session_created ON code_attempts(session_id, created_at);
CREATE INDEX IF NOT EXISTS idx_code_attempts_submitted_normalized ON code_attempts(submitted_normalized);
CREATE INDEX IF NOT EXISTS idx_code_attempts_session_level_normalized ON code_attempts(session_id, level_id, submitted_normalized);

-- Индексы для таблицы level_progress
CREATE INDEX IF NOT EXISTS idx_level_progress_session_id ON level_progress(session_id);
CREATE INDEX IF NOT EXISTS idx_level_progress_level_id ON level_progress(level_id);
CREATE INDEX IF NOT EXISTS idx_level_progress_started_at ON level_progress(started_at);
CREATE INDEX IF NOT EXISTS idx_level_progress_closed_at ON level_progress(closed_at);
CREATE INDEX IF NOT EXISTS idx_level_progress_session_level ON level_progress(session_id, level_id);
CREATE INDEX IF NOT EXISTS idx_level_progress_session_closed ON level_progress(session_id, closed_at);

-- Индексы для таблицы level_completions
CREATE INDEX IF NOT EXISTS idx_level_completions_session_id ON level_completions(session_id);
CREATE INDEX IF NOT EXISTS idx_level_completions_level_id ON level_completions(level_id);
CREATE INDEX IF NOT EXISTS idx_level_completions_user_id ON level_completions(user_id);
CREATE INDEX IF NOT EXISTS idx_level_completions_pass_time ON level_completions(pass_time);
CREATE INDEX IF NOT EXISTS idx_level_completions_duration_sec ON level_completions(duration_sec);
CREATE INDEX IF NOT EXISTS idx_level_completions_session_level ON level_completions(session_id, level_id);
CREATE INDEX IF NOT EXISTS idx_level_completions_quest_level ON level_completions(level_id, pass_time);

-- Индексы для таблицы level_hints
CREATE INDEX IF NOT EXISTS idx_level_hints_level_id ON level_hints(level_id);
CREATE INDEX IF NOT EXISTS idx_level_hints_order_index ON level_hints(order_index);
CREATE INDEX IF NOT EXISTS idx_level_hints_offset_sec ON level_hints(offset_sec);
CREATE INDEX IF NOT EXISTS idx_level_hints_level_order ON level_hints(level_id, order_index);

-- Индексы для таблицы team_invitations
CREATE INDEX IF NOT EXISTS idx_team_invitations_team_id ON team_invitations(team_id);
CREATE INDEX IF NOT EXISTS idx_team_invitations_user_id ON team_invitations(user_id);
CREATE INDEX IF NOT EXISTS idx_team_invitations_status ON team_invitations(status);
CREATE INDEX IF NOT EXISTS idx_team_invitations_created_at ON team_invitations(created_at);
CREATE INDEX IF NOT EXISTS idx_team_invitations_team_user ON team_invitations(team_id, user_id);
CREATE INDEX IF NOT EXISTS idx_team_invitations_user_status ON team_invitations(user_id, status);
CREATE INDEX IF NOT EXISTS idx_team_invitations_team_status ON team_invitations(team_id, status);

-- Индексы для таблицы participation_requests
CREATE INDEX IF NOT EXISTS idx_participation_requests_quest_id ON participation_requests(quest_id);
CREATE INDEX IF NOT EXISTS idx_participation_requests_user_id ON participation_requests(user_id);
CREATE INDEX IF NOT EXISTS idx_participation_requests_team_id ON participation_requests(team_id);
CREATE INDEX IF NOT EXISTS idx_participation_requests_status ON participation_requests(status);
CREATE INDEX IF NOT EXISTS idx_participation_requests_created_at ON participation_requests(created_at);
CREATE INDEX IF NOT EXISTS idx_participation_requests_quest_status ON participation_requests(quest_id, status);
CREATE INDEX IF NOT EXISTS idx_participation_requests_user_status ON participation_requests(user_id, status);

-- Композитные индексы для часто используемых комбинаций
CREATE INDEX IF NOT EXISTS idx_game_sessions_quest_status_finished ON game_sessions(quest_id, status, finished_at);
CREATE INDEX IF NOT EXISTS idx_code_attempts_session_level_result ON code_attempts(session_id, level_id, result);
CREATE INDEX IF NOT EXISTS idx_level_progress_session_level_closed ON level_progress(session_id, level_id, closed_at);
CREATE INDEX IF NOT EXISTS idx_users_role_created_at ON users(role, created_at);
CREATE INDEX IF NOT EXISTS idx_quests_published_start_created ON quests(published, start_at, created_at);

-- Индексы для полнотекстового поиска (если поддерживается)
-- CREATE INDEX IF NOT EXISTS idx_users_public_name_fts ON users USING gin(to_tsvector('russian', public_name));
-- CREATE INDEX IF NOT EXISTS idx_quests_title_fts ON quests USING gin(to_tsvector('russian', title));

-- Комментарии для документации индексов
COMMENT ON INDEX idx_users_username IS 'Индекс для быстрого поиска пользователей по имени';
COMMENT ON INDEX idx_users_email IS 'Индекс для быстрого поиска пользователей по email';
COMMENT ON INDEX idx_quests_published_active IS 'Индекс для поиска активных опубликованных квестов';
COMMENT ON INDEX idx_game_sessions_quest_status IS 'Индекс для поиска сессий по квесту и статусу';
COMMENT ON INDEX idx_code_attempts_session_level_created IS 'Индекс для получения последних попыток кода';
COMMENT ON INDEX idx_level_progress_session_closed IS 'Индекс для поиска текущего прогресса уровня';