package dn.quest.filestorage.service.impl;

import dn.quest.filestorage.service.ImageProcessingService;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * Реализация сервиса для обработки изображений
 */
@Service
@Slf4j
public class ImageProcessingServiceImpl implements ImageProcessingService {

    private static final Set<String> SUPPORTED_FORMATS = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp", "image/bmp"
    );

    private static final Set<String> COMPRESSIBLE_FORMATS = Set.of(
            "image/jpeg", "image/jpg", "image/webp"
    );

    @Override
    public byte[] compressImage(InputStream inputStream, String contentType, 
                               float quality, int maxWidth, int maxHeight) {
        try {
            BufferedImage originalImage = ImageIO.read(inputStream);
            if (originalImage == null) {
                throw new IllegalArgumentException("Не удалось прочитать изображение");
            }

            // Изменяем размер если необходимо
            BufferedImage resizedImage = resizeIfNeeded(originalImage, maxWidth, maxHeight);

            // Сжимаем изображение
            return compressImageInternal(resizedImage, contentType, quality);

        } catch (IOException e) {
            log.error("Ошибка при сжатии изображения", e);
            throw new RuntimeException("Не удалось сжать изображение", e);
        }
    }

    @Override
    public byte[] compressImage(MultipartFile file, float quality, int maxWidth, int maxHeight) {
        try {
            return compressImage(file.getInputStream(), file.getContentType(), quality, maxWidth, maxHeight);
        } catch (IOException e) {
            log.error("Ошибка при чтении файла для сжатия", e);
            throw new RuntimeException("Не удалось прочитать файл", e);
        }
    }

    @Override
    public byte[] generateThumbnail(InputStream inputStream, String contentType, int width, int height) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            
            Thumbnails.of(inputStream)
                    .size(width, height)
                    .keepAspectRatio(true)
                    .outputFormat(getFormatName(contentType))
                    .toOutputStream(outputStream);
            
            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("Ошибка при генерации миниатюры", e);
            throw new RuntimeException("Не удалось сгенерировать миниатюру", e);
        }
    }

    @Override
    public byte[] generateThumbnail(MultipartFile file, int width, int height) {
        try {
            return generateThumbnail(file.getInputStream(), file.getContentType(), width, height);
        } catch (IOException e) {
            log.error("Ошибка при чтении файла для генерации миниатюры", e);
            throw new RuntimeException("Не удалось прочитать файл", e);
        }
    }

    @Override
    public byte[] resizeImage(InputStream inputStream, String contentType, 
                             int width, int height, boolean keepAspectRatio) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            
            Thumbnails.Builder<?> builder = Thumbnails.of(inputStream);
            
            if (keepAspectRatio) {
                builder.size(width, height).keepAspectRatio(true);
            } else {
                builder.forceSize(width, height);
            }
            
            builder.outputFormat(getFormatName(contentType))
                   .toOutputStream(outputStream);
            
            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("Ошибка при изменении размера изображения", e);
            throw new RuntimeException("Не удалось изменить размер изображения", e);
        }
    }

    @Override
    public byte[] resizeImage(MultipartFile file, int width, int height, boolean keepAspectRatio) {
        try {
            return resizeImage(file.getInputStream(), file.getContentType(), width, height, keepAspectRatio);
        } catch (IOException e) {
            log.error("Ошибка при чтении файла для изменения размера", e);
            throw new RuntimeException("Не удалось прочитать файл", e);
        }
    }

    @Override
    public byte[] cropImage(InputStream inputStream, String contentType, 
                           int x, int y, int width, int height) {
        try {
            BufferedImage originalImage = ImageIO.read(inputStream);
            if (originalImage == null) {
                throw new IllegalArgumentException("Не удалось прочитать изображение");
            }

            // Проверяем границы обрезки
            if (x < 0 || y < 0 || x + width > originalImage.getWidth() || y + height > originalImage.getHeight()) {
                throw new IllegalArgumentException("Недопустимые параметры обрезки");
            }

            BufferedImage croppedImage = originalImage.getSubimage(x, y, width, height);
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(croppedImage, getFormatName(contentType), outputStream);
            
            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("Ошибка при обрезке изображения", e);
            throw new RuntimeException("Не удалось обрезать изображение", e);
        }
    }

    @Override
    public byte[] cropImage(MultipartFile file, int x, int y, int width, int height) {
        try {
            return cropImage(file.getInputStream(), file.getContentType(), x, y, width, height);
        } catch (IOException e) {
            log.error("Ошибка при чтении файла для обрезки", e);
            throw new RuntimeException("Не удалось прочитать файл", e);
        }
    }

    @Override
    public byte[] rotateImage(InputStream inputStream, String contentType, double degrees) {
        try {
            BufferedImage originalImage = ImageIO.read(inputStream);
            if (originalImage == null) {
                throw new IllegalArgumentException("Не удалось прочитать изображение");
            }

            // Создаем трансформацию поворота
            double radians = Math.toRadians(degrees);
            AffineTransform transform = new AffineTransform();
            transform.rotate(radians, originalImage.getWidth() / 2.0, originalImage.getHeight() / 2.0);

            // Применяем трансформацию
            AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
            BufferedImage rotatedImage = op.filter(originalImage, null);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(rotatedImage, getFormatName(contentType), outputStream);
            
            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("Ошибка при повороте изображения", e);
            throw new RuntimeException("Не удалось повернуть изображение", e);
        }
    }

    @Override
    public byte[] rotateImage(MultipartFile file, double degrees) {
        try {
            return rotateImage(file.getInputStream(), file.getContentType(), degrees);
        } catch (IOException e) {
            log.error("Ошибка при чтении файла для поворота", e);
            throw new RuntimeException("Не удалось прочитать файл", e);
        }
    }

    @Override
    public byte[] addWatermark(InputStream inputStream, String contentType, 
                              byte[] watermark, String position, float opacity) {
        try {
            BufferedImage originalImage = ImageIO.read(inputStream);
            BufferedImage watermarkImage = ImageIO.read(new ByteArrayInputStream(watermark));
            
            if (originalImage == null || watermarkImage == null) {
                throw new IllegalArgumentException("Не удалось прочитать изображения");
            }

            // Создаем копию оригинального изображения
            BufferedImage result = new BufferedImage(
                    originalImage.getWidth(), 
                    originalImage.getHeight(), 
                    BufferedImage.TYPE_INT_ARGB
            );

            Graphics2D g2d = result.createGraphics();
            
            // Рисуем оригинальное изображение
            g2d.drawImage(originalImage, 0, 0, null);
            
            // Устанавливаем прозрачность для водяного знака
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
            
            // Определяем позицию водяного знака
            Point watermarkPosition = calculateWatermarkPosition(
                    originalImage.getWidth(), originalImage.getHeight(),
                    watermarkImage.getWidth(), watermarkImage.getHeight(),
                    position
            );
            
            // Рисуем водяной знак
            g2d.drawImage(watermarkImage, watermarkPosition.x, watermarkPosition.y, null);
            g2d.dispose();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(result, getFormatName(contentType), outputStream);
            
            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("Ошибка при добавлении водяного знака", e);
            throw new RuntimeException("Не удалось добавить водяной знак", e);
        }
    }

    @Override
    public byte[] addWatermark(MultipartFile file, MultipartFile watermark, 
                              String position, float opacity) {
        try {
            return addWatermark(
                    file.getInputStream(), 
                    file.getContentType(),
                    watermark.getBytes(), 
                    position, 
                    opacity
            );
        } catch (IOException e) {
            log.error("Ошибка при чтении файлов для водяного знака", e);
            throw new RuntimeException("Не удалось прочитать файлы", e);
        }
    }

    @Override
    public byte[] convertFormat(InputStream inputStream, String sourceFormat, 
                               String targetFormat, float quality) {
        try {
            BufferedImage originalImage = ImageIO.read(inputStream);
            if (originalImage == null) {
                throw new IllegalArgumentException("Не удалось прочитать изображение");
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            
            // Используем Thumbnailator для конвертации с качеством
            Thumbnails.of(originalImage)
                    .outputFormat(targetFormat.toLowerCase())
                    .outputQuality(quality)
                    .toOutputStream(outputStream);
            
            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("Ошибка при конвертации формата изображения", e);
            throw new RuntimeException("Не удалось конвертировать формат изображения", e);
        }
    }

    @Override
    public byte[] convertFormat(MultipartFile file, String targetFormat, float quality) {
        try {
            return convertFormat(file.getInputStream(), file.getContentType(), targetFormat, quality);
        } catch (IOException e) {
            log.error("Ошибка при чтении файла для конвертации", e);
            throw new RuntimeException("Не удалось прочитать файл", e);
        }
    }

    @Override
    public int[] getImageDimensions(InputStream inputStream, String contentType) {
        try {
            BufferedImage image = ImageIO.read(inputStream);
            if (image == null) {
                throw new IllegalArgumentException("Не удалось прочитать изображение");
            }
            return new int[]{image.getWidth(), image.getHeight()};
        } catch (IOException e) {
            log.error("Ошибка при получении размеров изображения", e);
            throw new RuntimeException("Не удалось получить размеры изображения", e);
        }
    }

    @Override
    public int[] getImageDimensions(MultipartFile file) {
        try {
            return getImageDimensions(file.getInputStream(), file.getContentType());
        } catch (IOException e) {
            log.error("Ошибка при чтении файла для получения размеров", e);
            throw new RuntimeException("Не удалось прочитать файл", e);
        }
    }

    @Override
    public boolean isImage(String contentType) {
        return contentType != null && contentType.startsWith("image/");
    }

    @Override
    public boolean isSupportedFormat(String contentType) {
        return SUPPORTED_FORMATS.contains(contentType);
    }

    @Override
    public ImageInfo getImageInfo(InputStream inputStream, String contentType) {
        try {
            BufferedImage image = ImageIO.read(inputStream);
            if (image == null) {
                throw new IllegalArgumentException("Не удалось прочитать изображение");
            }

            return new ImageInfo(
                    image.getWidth(),
                    image.getHeight(),
                    getFormatName(contentType),
                    contentType,
                    inputStream.available(),
                    image.getTransparency() != Transparency.OPAQUE,
                    image.getColorModel().getColorSpace().getType() == ColorSpace.TYPE_RGB ? "RGB" : "Unknown"
            );

        } catch (IOException e) {
            log.error("Ошибка при получении информации об изображении", e);
            throw new RuntimeException("Не удалось получить информацию об изображении", e);
        }
    }

    @Override
    public ImageInfo getImageInfo(MultipartFile file) {
        try {
            return getImageInfo(file.getInputStream(), file.getContentType());
        } catch (IOException e) {
            log.error("Ошибка при чтении файла для получения информации", e);
            throw new RuntimeException("Не удалось прочитать файл", e);
        }
    }

    // Вспомогательные методы

    private BufferedImage resizeIfNeeded(BufferedImage image, int maxWidth, int maxHeight) {
        int width = image.getWidth();
        int height = image.getHeight();

        if (width <= maxWidth && height <= maxHeight) {
            return image;
        }

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Thumbnails.of(image)
                    .size(maxWidth, maxHeight)
                    .keepAspectRatio(true)
                    .outputFormat("png")
                    .toOutputStream(outputStream);
            
            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            return ImageIO.read(inputStream);
        } catch (IOException e) {
            log.error("Ошибка при изменении размера изображения", e);
            return image;
        }
    }

    private byte[] compressImageInternal(BufferedImage image, String contentType, float quality) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            
            if (COMPRESSIBLE_FORMATS.contains(contentType)) {
                Thumbnails.of(image)
                        .outputQuality(quality)
                        .outputFormat(getFormatName(contentType))
                        .toOutputStream(outputStream);
            } else {
                // Для форматов без сжатия просто сохраняем как есть
                ImageIO.write(image, getFormatName(contentType), outputStream);
            }
            
            return outputStream.toByteArray();
        } catch (IOException e) {
            log.error("Ошибка при внутреннем сжатии изображения", e);
            throw new RuntimeException("Не удалось сжать изображение", e);
        }
    }

    private String getFormatName(String contentType) {
        switch (contentType.toLowerCase()) {
            case "image/jpeg":
            case "image/jpg":
                return "jpg";
            case "image/png":
                return "png";
            case "image/gif":
                return "gif";
            case "image/webp":
                return "webp";
            case "image/bmp":
                return "bmp";
            default:
                return "jpg"; // По умолчанию
        }
    }

    private Point calculateWatermarkPosition(int imageWidth, int imageHeight, 
                                            int watermarkWidth, int watermarkHeight, 
                                            String position) {
        int x, y;
        
        switch (position.toLowerCase()) {
            case "top-left":
                x = 10;
                y = 10;
                break;
            case "top-right":
                x = imageWidth - watermarkWidth - 10;
                y = 10;
                break;
            case "bottom-left":
                x = 10;
                y = imageHeight - watermarkHeight - 10;
                break;
            case "bottom-right":
                x = imageWidth - watermarkWidth - 10;
                y = imageHeight - watermarkHeight - 10;
                break;
            case "center":
                x = (imageWidth - watermarkWidth) / 2;
                y = (imageHeight - watermarkHeight) / 2;
                break;
            default:
                x = imageWidth - watermarkWidth - 10;
                y = imageHeight - watermarkHeight - 10;
        }
        
        return new Point(x, y);
    }
}