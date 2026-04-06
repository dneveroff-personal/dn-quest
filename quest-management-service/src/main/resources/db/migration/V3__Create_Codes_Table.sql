-- Создание таблицы codes
CREATE TABLE codes (
    id UUID PRIMARY KEY,
    level_id UUID NOT NULL,
    value VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    hint TEXT,
    points INTEGER DEFAULT 0,
    active BOOLEAN DEFAULT true,
    usage_limit INTEGER, -- лимит использований (null - без лимита)
    usage_count INTEGER DEFAULT 0,
    used_by UUID[], -- массив ID пользователей, использовавших код
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_codes_level_id FOREIGN KEY (level_id) REFERENCES levels(id) ON DELETE CASCADE,
    CONSTRAINT uq_codes_level_value UNIQUE (level_id, value)
);

-- Создание индексов для таблицы codes
CREATE INDEX idx_codes_level_id ON codes(level_id);
CREATE INDEX idx_codes_value ON codes(value);
CREATE INDEX idx_codes_type ON codes(type);
CREATE INDEX idx_codes_active ON codes(active);
CREATE INDEX idx_codes_created_at ON codes(created_at);
CREATE INDEX idx_codes_used_by ON codes USING GIN(used_by);

-- Создание триггера для обновления поля updated_at
CREATE TRIGGER update_codes_updated_at 
    BEFORE UPDATE ON codes 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- Добавление комментариев к таблице и полям
COMMENT ON TABLE codes IS 'Таблица кодов уровней';
COMMENT ON COLUMN codes.id IS 'Уникальный идентификатор кода';
COMMENT ON COLUMN codes.level_id IS 'ID уровня';
COMMENT ON COLUMN codes.value IS 'Значение кода';
COMMENT ON COLUMN codes.type IS 'Тип кода';
COMMENT ON COLUMN codes.hint IS 'Подсказка к коду';
COMMENT ON COLUMN codes.points IS 'Очки за код';
COMMENT ON COLUMN codes.active IS 'Флаг активности кода';
COMMENT ON COLUMN codes.usage_limit IS 'Лимит использований';
COMMENT ON COLUMN codes.usage_count IS 'Количество использований';
COMMENT ON COLUMN codes.used_by IS 'Массив ID пользователей, использовавших код';
COMMENT ON COLUMN codes.created_at IS 'Время создания';
COMMENT ON COLUMN codes.updated_at IS 'Время последнего обновления';