package dn.quest.filestorage.service;

import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.InputStream;

/**
 * Сервис для обработки изображений
 */
public interface ImageProcessingService {

    /**
     * Сжать изображение
     *
     * @param inputStream входной поток изображения
     * @param contentType  MIME тип изображения
     * @param quality      качество сжатия (0.0 - 1.0)
     * @param maxWidth     максимальная ширина
     * @param maxHeight    максимальная высота
     * @return сжатое изображение в виде массива байт
     */
    byte[] compressImage(InputStream inputStream, String contentType, 
                        float quality, int maxWidth, int maxHeight);

    /**
     * Сжать изображение из MultipartFile
     *
     * @param file    файл изображения
     * @param quality качество сжатия (0.0 - 1.0)
     * @param maxWidth максимальная ширина
     * @param maxHeight максимальная высота
     * @return сжатое изображение в виде массива байт
     */
    byte[] compressImage(MultipartFile file, float quality, int maxWidth, int maxHeight);

    /**
     * Сгенерировать миниатюру (thumbnail)
     *
     * @param inputStream входной поток изображения
     * @param contentType  MIME тип изображения
     * @param width        ширина миниатюры
     * @param height       высота миниатюры
     * @return миниатюра в виде массива байт
     */
    byte[] generateThumbnail(InputStream inputStream, String contentType, int width, int height);

    /**
     * Сгенерировать миниатюру из MultipartFile
     *
     * @param file   файл изображения
     * @param width  ширина миниатюры
     * @param height высота миниатюры
     * @return миниатюра в виде массива байт
     */
    byte[] generateThumbnail(MultipartFile file, int width, int height);

    /**
     * Изменить размер изображения
     *
     * @param inputStream входной поток изображения
     * @param contentType  MIME тип изображения
     * @param width        новая ширина
     * @param height       новая высота
     * @param keepAspectRatio сохранять пропорции
     * @return измененное изображение в виде массива байт
     */
    byte[] resizeImage(InputStream inputStream, String contentType, 
                      int width, int height, boolean keepAspectRatio);

    /**
     * Изменить размер изображения из MultipartFile
     *
     * @param file          файл изображения
     * @param width         новая ширина
     * @param height        новая высота
     * @param keepAspectRatio сохранять пропорции
     * @return измененное изображение в виде массива байт
     */
    byte[] resizeImage(MultipartFile file, int width, int height, boolean keepAspectRatio);

    /**
     * Обрезать изображение
     *
     * @param inputStream входной поток изображения
     * @param contentType  MIME тип изображения
     * @param x            координата X начала обрезки
     * @param y            координата Y начала обрезки
     * @param width        ширина области обрезки
     * @param height       высота области обрезки
     * @return обрезанное изображение в виде массива байт
     */
    byte[] cropImage(InputStream inputStream, String contentType, 
                    int x, int y, int width, int height);

    /**
     * Обрезать изображение из MultipartFile
     *
     * @param file   файл изображения
     * @param x      координата X начала обрезки
     * @param y      координата Y начала обрезки
     * @param width  ширина области обрезки
     * @param height высота области обрезки
     * @return обрезанное изображение в виде массива байт
     */
    byte[] cropImage(MultipartFile file, int x, int y, int width, int height);

    /**
     * Повернуть изображение
     *
     * @param inputStream входной поток изображения
     * @param contentType  MIME тип изображения
     * @param degrees      угол поворота в градусах
     * @return повернутое изображение в виде массива байт
     */
    byte[] rotateImage(InputStream inputStream, String contentType, double degrees);

    /**
     * Повернуть изображение из MultipartFile
     *
     * @param file    файл изображения
     * @param degrees угол поворота в градусах
     * @return повернутое изображение в виде массива байт
     */
    byte[] rotateImage(MultipartFile file, double degrees);

    /**
     * Добавить водяной знак
     *
     * @param inputStream входной поток изображения
     * @param contentType  MIME тип изображения
     * @param watermark   водяной знак в виде массива байт
     * @param position     позиция водяного знака
     * @param opacity      прозрачность водяного знака (0.0 - 1.0)
     * @return изображение с водяным знаком в виде массива байт
     */
    byte[] addWatermark(InputStream inputStream, String contentType, 
                       byte[] watermark, String position, float opacity);

    /**
     * Добавить водяной знак из MultipartFile
     *
     * @param file      файл изображения
     * @param watermark файл водяного знака
     * @param position  позиция водяного знака
     * @param opacity   прозрачность водяного знака (0.0 - 1.0)
     * @return изображение с водяным знаком в виде массива байт
     */
    byte[] addWatermark(MultipartFile file, MultipartFile watermark, 
                       String position, float opacity);

    /**
     * Конвертировать изображение в другой формат
     *
     * @param inputStream   входной поток изображения
     * @param sourceFormat  исходный формат
     * @param targetFormat  целевой формат
     * @param quality       качество (для форматов сжатия)
     * @return сконвертированное изображение в виде массива байт
     */
    byte[] convertFormat(InputStream inputStream, String sourceFormat, 
                        String targetFormat, float quality);

    /**
     * Конвертировать изображение из MultipartFile в другой формат
     *
     * @param file         файл изображения
     * @param targetFormat целевой формат
     * @param quality      качество (для форматов сжатия)
     * @return сконвертированное изображение в виде массива байт
     */
    byte[] convertFormat(MultipartFile file, String targetFormat, float quality);

    /**
     * Получить размеры изображения
     *
     * @param inputStream входной поток изображения
     * @param contentType  MIME тип изображения
     * @return массив [ширина, высота]
     */
    int[] getImageDimensions(InputStream inputStream, String contentType);

    /**
     * Получить размеры изображения из MultipartFile
     *
     * @param file файл изображения
     * @return массив [ширина, высота]
     */
    int[] getImageDimensions(MultipartFile file);

    /**
     * Проверить, является ли файл изображением
     *
     * @param contentType MIME тип
     * @return true если это изображение
     */
    boolean isImage(String contentType);

    /**
     * Проверить, поддерживается ли формат изображения
     *
     * @param contentType MIME тип
     * @return true если формат поддерживается
     */
    boolean isSupportedFormat(String contentType);

    /**
     * Получить информацию об изображении
     *
     * @param inputStream входной поток изображения
     * @param contentType  MIME тип изображения
     * @return информация об изображении
     */
    ImageInfo getImageInfo(InputStream inputStream, String contentType);

    /**
     * Получить информацию об изображении из MultipartFile
     *
     * @param file файл изображения
     * @return информация об изображении
     */
    ImageInfo getImageInfo(MultipartFile file);

    /**
     * Класс для хранения информации об изображении
     */
    class ImageInfo {
        private int width;
        private int height;
        private String format;
        private String contentType;
        private long sizeBytes;
        private boolean hasTransparency;
        private String colorSpace;

        // Конструкторы, геттеры и сеттеры
        public ImageInfo() {}

        public ImageInfo(int width, int height, String format, String contentType, 
                        long sizeBytes, boolean hasTransparency, String colorSpace) {
            this.width = width;
            this.height = height;
            this.format = format;
            this.contentType = contentType;
            this.sizeBytes = sizeBytes;
            this.hasTransparency = hasTransparency;
            this.colorSpace = colorSpace;
        }

        public int getWidth() { return width; }
        public void setWidth(int width) { this.width = width; }

        public int getHeight() { return height; }
        public void setHeight(int height) { this.height = height; }

        public String getFormat() { return format; }
        public void setFormat(String format) { this.format = format; }

        public String getContentType() { return contentType; }
        public void setContentType(String contentType) { this.contentType = contentType; }

        public long getSizeBytes() { return sizeBytes; }
        public void setSizeBytes(long sizeBytes) { this.sizeBytes = sizeBytes; }

        public boolean isHasTransparency() { return hasTransparency; }
        public void setHasTransparency(boolean hasTransparency) { this.hasTransparency = hasTransparency; }

        public String getColorSpace() { return colorSpace; }
        public void setColorSpace(String colorSpace) { this.colorSpace = colorSpace; }
    }
}