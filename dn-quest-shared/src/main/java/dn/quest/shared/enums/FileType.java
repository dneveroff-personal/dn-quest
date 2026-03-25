package dn.quest.shared.enums;

import java.util.Map;
import java.util.Set;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Типы файлов
 */
public enum FileType {
    /**
     * Изображение - jpg, png, gif, bmp, webp, svg
     */
    IMAGE("Изображение", Set.of("jpg", "jpeg", "png", "gif", "bmp", "webp", "svg", "ico", "tiff", "tif")),

    /**
     * Документ - doc, docx, pdf, txt, xls, xlsx, ppt, pptx, odt, rtf
     */
    DOCUMENT("Документ", Set.of("doc", "docx", "pdf", "txt", "xls", "xlsx", "ppt", "pptx", "odt", "rtf", "csv", "md")),

    /**
     * Аудио - mp3, wav, ogg, flac, aac, m4a
     */
    AUDIO("Аудио", Set.of("mp3", "wav", "ogg", "flac", "aac", "m4a", "wma", "aiff")),

    /**
     * Видео - mp4, avi, mkv, mov, wmv, webm, flv
     */
    VIDEO("Видео", Set.of("mp4", "avi", "mkv", "mov", "wmv", "webm", "flv", "m4v", "mpeg", "mpg")),

    /**
     * Архив - zip, rar, 7z, tar, gz
     */
    ARCHIVE("Архив", Set.of("zip", "rar", "7z", "tar", "gz", "bz2", "xz", "iso")),

    /**
     * Другой тип файла
     */
    OTHER("Другое", new HashSet<>());

    private final String displayName;
    private final Set<String> extensions;
    private static final Map<String, FileType> EXTENSION_MAP = new HashMap<>();

    static {
        for (FileType type : values()) {
            for (String ext : type.extensions) {
                EXTENSION_MAP.put(ext.toLowerCase(), type);
            }
        }
    }

    FileType(String displayName, Set<String> extensions) {
        this.displayName = displayName;
        this.extensions = extensions;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Set<String> getExtensions() {
        return extensions;
    }

    /**
     * Проверяет, является ли файл изображением
     */
    public boolean isImage() {
        return this == IMAGE;
    }

    /**
     * Проверяет, является ли файл документом
     */
    public boolean isDocument() {
        return this == DOCUMENT;
    }

    /**
     * Проверяет, является ли файл аудио
     */
    public boolean isAudio() {
        return this == AUDIO;
    }

    /**
     * Проверяет, является ли файл видео
     */
    public boolean isVideo() {
        return this == VIDEO;
    }

    /**
     * Проверяет, является ли файл архивом
     */
    public boolean isArchive() {
        return this == ARCHIVE;
    }

    /**
     * Проверяет, является ли файл другим типом
     */
    public boolean isOther() {
        return this == OTHER;
    }

    /**
     * Определяет тип файла по его расширению
     *
     * @param filename имя файла или расширение
     * @return тип файла
     */
    public static FileType fromFileName(String filename) {
        if (filename == null || filename.isEmpty()) {
            return OTHER;
        }
        
        String extension = extractExtension(filename);
        if (extension == null || extension.isEmpty()) {
            return OTHER;
        }
        
        FileType type = EXTENSION_MAP.get(extension.toLowerCase());
        return type != null ? type : OTHER;
    }

    /**
     * Извлекает расширение из имени файла
     *
     * @param filename имя файла
     * @return расширение файла или null
     */
    private static String extractExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0 && lastDot < filename.length() - 1) {
            return filename.substring(lastDot + 1);
        }
        return null;
    }
}