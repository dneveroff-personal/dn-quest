-- =============================================
-- file-storage-service — V1__init.sql
-- Schema: files
-- =============================================

CREATE TABLE file_metadata (
                               id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                               original_file_name VARCHAR(255) NOT NULL,
                               stored_file_name   VARCHAR(255) NOT NULL,
                               content_type       VARCHAR(100) NOT NULL,
                               file_size          BIGINT       NOT NULL,
                               file_type          VARCHAR(50)  NOT NULL,
                               storage_type       VARCHAR(50)  NOT NULL,
                               storage_path       VARCHAR(500),
                               description        VARCHAR(1000),
                               owner_id           UUID,
                               quest_id           UUID,
                               team_id            UUID,
                               is_public          BOOLEAN   DEFAULT FALSE,
                               is_temporary       BOOLEAN   DEFAULT FALSE,
                               expires_at         TIMESTAMP,
                               download_count     BIGINT    DEFAULT 0,
                               last_accessed_at   TIMESTAMP,
                               checksum           VARCHAR(64),
                               thumbnail_path     VARCHAR(500),
                               metadata_json      TEXT,
                               created_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               updated_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                               CONSTRAINT chk_file_metadata_file_size     CHECK (file_size > 0),
                               CONSTRAINT chk_file_metadata_download_count CHECK (download_count >= 0)
);

CREATE INDEX idx_file_metadata_owner_id       ON file_metadata(owner_id);
CREATE INDEX idx_file_metadata_file_type      ON file_metadata(file_type);
CREATE INDEX idx_file_metadata_storage_type   ON file_metadata(storage_type);
CREATE INDEX idx_file_metadata_is_public      ON file_metadata(is_public);
CREATE INDEX idx_file_metadata_is_temporary   ON file_metadata(is_temporary);
CREATE INDEX idx_file_metadata_expires_at     ON file_metadata(expires_at);
CREATE INDEX idx_file_metadata_content_type   ON file_metadata(content_type);
CREATE INDEX idx_file_metadata_checksum       ON file_metadata(checksum);
CREATE INDEX idx_file_metadata_created_at     ON file_metadata(created_at);
CREATE INDEX idx_file_metadata_stored_file_name ON file_metadata(stored_file_name);
CREATE INDEX idx_file_metadata_temporary_expires ON file_metadata(is_temporary, expires_at)
    WHERE is_temporary = TRUE;

COMMENT ON TABLE  file_metadata                    IS 'Метаданные загруженных файлов';
COMMENT ON COLUMN file_metadata.id                 IS 'Уникальный идентификатор файла';
COMMENT ON COLUMN file_metadata.original_file_name IS 'Оригинальное имя файла';
COMMENT ON COLUMN file_metadata.stored_file_name   IS 'Имя файла в хранилище';
COMMENT ON COLUMN file_metadata.content_type       IS 'MIME тип файла';
COMMENT ON COLUMN file_metadata.file_size          IS 'Размер файла в байтах';
COMMENT ON COLUMN file_metadata.file_type          IS 'Тип файла (AVATAR, QUEST_MEDIA, LEVEL_FILE и т.д.)';
COMMENT ON COLUMN file_metadata.storage_type       IS 'Тип хранилища (LOCAL, MINIO, S3, CDN)';
COMMENT ON COLUMN file_metadata.is_public          IS 'Флаг публичности файла';
COMMENT ON COLUMN file_metadata.is_temporary       IS 'Флаг временного файла';
COMMENT ON COLUMN file_metadata.expires_at         IS 'Время истечения для временных файлов';
COMMENT ON COLUMN file_metadata.download_count     IS 'Количество скачиваний файла';
COMMENT ON COLUMN file_metadata.checksum           IS 'Контрольная сумма файла (SHA-256)';
COMMENT ON COLUMN file_metadata.thumbnail_path     IS 'Путь к миниатюре изображения';

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER trg_file_metadata_updated_at
    BEFORE UPDATE ON file_metadata
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();