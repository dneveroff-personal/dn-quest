package dn.quest.gameengine.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * DTO для запроса на отправку кода
 */
@Schema(description = "Запрос на отправку кода для проверки")
public record SubmitCodeRequest(
    
    @Schema(description = "Код для проверки", example = "ABC123", required = true)
    @NotBlank(message = "Код не может быть пустым")
    @Pattern(regexp = "^[A-Za-z0-9]{3,20}$", message = "Код должен содержать от 3 до 20 символов (буквы и цифры)")
    String code,
    
    @Schema(description = "Сектор кода", example = "A", required = true)
    @NotBlank(message = "Сектор не может быть пустым")
    @Pattern(regexp = "^[A-Za-z]$", message = "Сектор должен быть одной буквой")
    String sector,
    
    @Schema(description = "Дополнительные параметры или метаданные")
    CodeMetadataDTO metadata
) {
    
    /**
     * DTO для метаданных кода
     */
    @Schema(description = "Метаданные кода")
    public record CodeMetadataDTO(
        
        @Schema(description = "Время ввода кода в миллисекундах", example = "1234567890")
        Long inputTimestamp,
        
        @Schema(description = "Координаты на карте (X)", example = "100.5")
        Double coordinateX,
        
        @Schema(description = "Координаты на карте (Y)", example = "200.3")
        Double coordinateY,
        
        @Schema(description = "Устройство ввода", example = "mobile")
        String inputDevice,
        
        @Schema(description = "IP адрес", example = "192.168.1.1")
        String ipAddress,
        
        @Schema(description = "User agent", example = "Mozilla/5.0...")
        String userAgent,
        
        @Schema(description = "Дополнительные заметки", example = "Код найден под камнем")
        String notes,
        
        @Schema(description = "Фотография доказательства", example = "https://example.com/photo.jpg")
        String photoUrl,
        
        @Schema(description = "GPS координаты", example = "55.7558,37.6173")
        String gpsCoordinates
    ) {}
}