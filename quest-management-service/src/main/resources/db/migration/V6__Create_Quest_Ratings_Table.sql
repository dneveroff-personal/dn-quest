-- Создание таблицы quest_ratings
CREATE TABLE quest_ratings (
    id BIGSERIAL PRIMARY KEY,
    quest_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    
    CONSTRAINT fk_quest_ratings_quest FOREIGN KEY (quest_id) REFERENCES quests(id) ON DELETE CASCADE,
    CONSTRAINT uq_quest_ratings_user_quest UNIQUE (quest_id, user_id)
);

-- Создание индексов
CREATE INDEX idx_quest_rating_quest_id ON quest_ratings(quest_id);
CREATE INDEX idx_quest_rating_user_id ON quest_ratings(user_id);
CREATE INDEX idx_quest_rating_composite ON quest_ratings(quest_id, user_id);
CREATE INDEX idx_quest_rating_created_at ON quest_ratings(created_at);

-- Добавление комментариев
COMMENT ON TABLE quest_ratings IS 'Таблица для хранения рейтингов квестов';
COMMENT ON COLUMN quest_ratings.id IS 'Уникальный идентификатор рейтинга';
COMMENT ON COLUMN quest_ratings.quest_id IS 'ID квеста';
COMMENT ON COLUMN quest_ratings.user_id IS 'ID пользователя, поставившего рейтинг';
COMMENT ON COLUMN quest_ratings.rating IS 'Рейтинг от 1 до 5';
COMMENT ON COLUMN quest_ratings.created_at IS 'Дата создания рейтинга';
COMMENT ON COLUMN quest_ratings.updated_at IS 'Дата обновления рейтинга';