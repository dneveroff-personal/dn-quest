-- Создание таблицы quest_reviews
CREATE TABLE quest_reviews (
    id BIGSERIAL PRIMARY KEY,
    quest_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    title VARCHAR(200),
    content TEXT,
    rating INTEGER CHECK (rating >= 1 AND rating <= 5),
    is_visible BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    
    CONSTRAINT fk_quest_reviews_quest FOREIGN KEY (quest_id) REFERENCES quests(id) ON DELETE CASCADE,
    CONSTRAINT uq_quest_reviews_user_quest UNIQUE (quest_id, user_id)
);

-- Создание индексов
CREATE INDEX idx_quest_review_quest_id ON quest_reviews(quest_id);
CREATE INDEX idx_quest_review_user_id ON quest_reviews(user_id);
CREATE INDEX idx_quest_review_visible ON quest_reviews(is_visible);
CREATE INDEX idx_quest_review_composite ON quest_reviews(quest_id, user_id);
CREATE INDEX idx_quest_review_created_at ON quest_reviews(created_at);
CREATE INDEX idx_quest_review_rating ON quest_reviews(rating);

-- Добавление комментариев
COMMENT ON TABLE quest_reviews IS 'Таблица для хранения отзывов на квесты';
COMMENT ON COLUMN quest_reviews.id IS 'Уникальный идентификатор отзыва';
COMMENT ON COLUMN quest_reviews.quest_id IS 'ID квеста';
COMMENT ON COLUMN quest_reviews.user_id IS 'ID пользователя, оставившего отзыв';
COMMENT ON COLUMN quest_reviews.title IS 'Заголовок отзыва';
COMMENT ON COLUMN quest_reviews.content IS 'Содержание отзыва';
COMMENT ON COLUMN quest_reviews.rating IS 'Рейтинг от 1 до 5 (может быть null)';
COMMENT ON COLUMN quest_reviews.is_visible IS 'Видимость отзыва (для модерации)';
COMMENT ON COLUMN quest_reviews.created_at IS 'Дата создания отзыва';
COMMENT ON COLUMN quest_reviews.updated_at IS 'Дата обновления отзыва';