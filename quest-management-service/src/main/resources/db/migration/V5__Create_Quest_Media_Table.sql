-- Создание таблицы quest_media
CREATE TABLE quest_media (
    id BIGSERIAL PRIMARY KEY,
    quest_id BIGINT NOT NULL,
    level_id BIGINT, -- может быть null, если медиа относится ко всему квесту
    file_name VARCHAR(255) NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    media_type VARCHAR(50) NOT NULL, -- IMAGE, VIDEO, AUDIO, DOCUMENT
    description TEXT,
    display_order INTEGER DEFAULT 0,
    is_public BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_quest_media_quest_id FOREIGN KEY (quest_id) REFERENCES quests(id) ON DELETE CASCADE,
    CONSTRAINT fk_quest_media_level_id FOREIGN KEY (level_id) REFERENCES levels(id) ON DELETE CASCADE
);

-- Создание индексов для таблицы quest_media
CREATE INDEX idx_quest_media_quest_id ON quest_media(quest_id);
CREATE INDEX idx_quest_media_level_id ON quest_media(level_id);
CREATE INDEX idx_quest_media_media_type ON quest_media(media_type);
CREATE INDEX idx_quest_media_mime_type ON quest_media(mime_type);
CREATE INDEX idx_quest_media_is_public ON quest_media(is_public);
CREATE INDEX idx_quest_media_created_at ON quest_media(created_at);
CREATE INDEX idx_quest_media_display_order ON quest_media(display_order);

-- Создание триггера для обновления поля updated_at
CREATE TRIGGER update_quest_media_updated_at 
    BEFORE UPDATE ON quest_media 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- Добавление комментариев к таблице и полям
COMMENT ON TABLE quest_media IS 'Таблица медиа файлов квестов';
COMMENT ON COLUMN quest_media.id IS 'Уникальный идентификатор медиа файла';
COMMENT ON COLUMN quest_media.quest_id IS 'ID квеста';
COMMENT ON COLUMN quest_media.level_id IS 'ID уровня (null, если медиа относится ко всему квесту)';
COMMENT ON COLUMN quest_media.file_name IS 'Имя файла в системе';
COMMENT ON COLUMN quest_media.original_file_name IS 'Оригинальное имя файла';
COMMENT ON COLUMN quest_media.file_path IS 'Путь к файлу';
COMMENT ON COLUMN quest_media.file_size IS 'Размер файла в байтах';
COMMENT ON COLUMN quest_media.mime_type IS 'MIME тип файла';
COMMENT ON COLUMN quest_media.media_type IS 'Тип медиа (IMAGE, VIDEO, AUDIO, DOCUMENT)';
COMMENT ON COLUMN quest_media.description IS 'Описание медиа файла';
COMMENT ON COLUMN quest_media.display_order IS 'Порядок отображения';
COMMENT ON COLUMN quest_media.is_public IS 'Флаг публичности медиа файла';
COMMENT ON COLUMN quest_media.created_at IS 'Время создания';
COMMENT ON COLUMN quest_media.updated_at IS 'Время последнего обновления';