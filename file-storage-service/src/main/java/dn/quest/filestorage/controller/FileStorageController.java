package dn.quest.filestorage.controller;

import dn.quest.filestorage.dto.*;
import dn.quest.filestorage.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

/**
 * Контроллер для работы с файлами
 */
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "File Storage API", description = "API для управления файлами")
public class FileStorageController {

    private final FileStorageService fileStorageService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Загрузить файл", description = "Загружает один файл в хранилище")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Файл успешно загружен",
                    content = @Content(schema = @Schema(implementation = FileResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации"),
            @ApiResponse(responseCode = "413", description = "Размер файла превышен"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    public ResponseEntity<FileResponseDTO> uploadFile(
            @Parameter(description = "Файл для загрузки", required = true)
            @RequestParam("file") MultipartFile file,
            
            @Parameter(description = "Параметры загрузки файла", required = true)
            @Valid @ModelAttribute FileUploadRequestDTO request,
            
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String username = userDetails.getUsername();
        log.info("Запрос на загрузку файла: {} пользователем: {}", file.getOriginalFilename(), username);
        
        FileResponseDTO response = fileStorageService.uploadFile(file, request, username);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/batch-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Пакетная загрузка файлов", description = "Загружает несколько файлов в хранилище")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Файлы успешно загружены",
                    content = @Content(schema = @Schema(implementation = BatchFileUploadResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    public ResponseEntity<BatchFileUploadResponseDTO> uploadFiles(
            @Parameter(description = "Файлы для загрузки", required = true)
            @RequestParam("files") List<MultipartFile> files,
            
            @Parameter(description = "Параметры загрузки файлов", required = true)
            @Valid @ModelAttribute BatchFileUploadRequestDTO request,
            
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String username = userDetails.getUsername();
        log.info("Запрос на пакетную загрузку {} файлов пользователем: {}", files.size(), username);
        
        BatchFileUploadResponseDTO response = fileStorageService.uploadFiles(files, request, username);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{fileId}")
    @Operation(summary = "Получить метаданные файла", description = "Возвращает метаданные файла по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Метаданные файла получены",
                    content = @Content(schema = @Schema(implementation = FileResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Файл не найден"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    public ResponseEntity<FileResponseDTO> getFileMetadata(
            @Parameter(description = "ID файла", required = true)
            @PathVariable UUID fileId,
            
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String username = userDetails.getUsername();
        FileResponseDTO response = fileStorageService.getFileMetadata(fileId, username);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{fileId}/download")
    @Operation(summary = "Скачать файл", description = "Скачивает файл по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Файл скачан"),
            @ApiResponse(responseCode = "404", description = "Файл не найден"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    public ResponseEntity<InputStreamResource> downloadFile(
            @Parameter(description = "ID файла", required = true)
            @PathVariable UUID fileId,
            
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String username = userDetails.getUsername();
        log.info("Запрос на скачивание файла: {} пользователем: {}", fileId, username);
        
        FileResponseDTO metadata = fileStorageService.getFileMetadata(fileId, username);
        InputStream inputStream = fileStorageService.downloadFile(fileId, username);
        
        InputStreamResource resource = new InputStreamResource(inputStream);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + metadata.getOriginalFileName() + "\"")
                .contentType(MediaType.parseMediaType(metadata.getContentType()))
                .contentLength(metadata.getFileSize())
                .body(resource);
    }

    @GetMapping("/download/{fileName}")
    @Operation(summary = "Скачать файл по имени", description = "Скачивает файл по имени хранения")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Файл скачан"),
            @ApiResponse(responseCode = "404", description = "Файл не найден")
    })
    public ResponseEntity<InputStreamResource> downloadFileByName(
            @Parameter(description = "Имя файла", required = true)
            @PathVariable String fileName,
            
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String username = userDetails.getUsername();
        log.info("Запрос на скачивание файла по имени: {} пользователем: {}", fileName, username);
        
        FileResponseDTO metadata = fileStorageService.getFileMetadataByName(fileName, username);
        InputStream inputStream = fileStorageService.downloadFileByName(fileName, username);
        
        InputStreamResource resource = new InputStreamResource(inputStream);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + metadata.getOriginalFileName() + "\"")
                .contentType(MediaType.parseMediaType(metadata.getContentType()))
                .contentLength(metadata.getFileSize())
                .body(resource);
    }

    @GetMapping("/{fileId}/info")
    @Operation(summary = "Получить информацию о файле", description = "Возвращает подробную информацию о файле")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Информация о файле получена"),
            @ApiResponse(responseCode = "404", description = "Файл не найден")
    })
    public ResponseEntity<FileResponseDTO> getFileInfo(
            @Parameter(description = "ID файла", required = true)
            @PathVariable UUID fileId,
            
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String username = userDetails.getUsername();
        FileResponseDTO response = fileStorageService.getFileMetadata(fileId, username);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{fileId}")
    @Operation(summary = "Удалить файл", description = "Удаляет файл по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Файл удален"),
            @ApiResponse(responseCode = "404", description = "Файл не найден"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    public ResponseEntity<Void> deleteFile(
            @Parameter(description = "ID файла", required = true)
            @PathVariable UUID fileId,
            
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String username = userDetails.getUsername();
        log.info("Запрос на удаление файла: {} пользователем: {}", fileId, username);
        
        fileStorageService.deleteFile(fileId, username);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{fileId}/presigned-url")
    @Operation(summary = "Сгенерировать предписанный URL", description = "Генерирует временный URL для доступа к файлу")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "URL сгенерирован"),
            @ApiResponse(responseCode = "404", description = "Файл не найден")
    })
    public ResponseEntity<String> generatePresignedUrl(
            @Parameter(description = "ID файла", required = true)
            @PathVariable UUID fileId,
            
            @Parameter(description = "Время действия URL в секундах", example = "3600")
            @RequestParam(defaultValue = "3600") long durationSeconds,
            
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String username = userDetails.getUsername();
        Duration duration = Duration.ofSeconds(durationSeconds);
        
        String url = fileStorageService.generatePresignedDownloadUrl(fileId, duration, username);
        return ResponseEntity.ok(url);
    }

    @PostMapping("/presigned-upload-url")
    @Operation(summary = "Сгенерировать URL для загрузки", description = "Генерирует временный URL для прямой загрузки файла")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "URL сгенерирован"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации")
    })
    public ResponseEntity<String> generatePresignedUploadUrl(
            @Parameter(description = "Параметры для генерации URL", required = true)
            @Valid @RequestBody PresignedUploadUrlRequestDTO request,
            
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String username = userDetails.getUsername();
        Duration duration = Duration.ofSeconds(request.getDurationSeconds());
        
        String url = fileStorageService.generatePresignedUploadUrl(
                request.getFileName(), 
                request.getContentType(), 
                duration, 
                username);
        
        return ResponseEntity.ok(url);
    }

    @PostMapping("/search")
    @Operation(summary = "Поиск файлов", description = "Ищет файлы по заданным критериям")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Результаты поиска",
                    content = @Content(schema = @Schema(implementation = FileSearchResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации")
    })
    public ResponseEntity<FileSearchResponseDTO> searchFiles(
            @Parameter(description = "Критерии поиска", required = true)
            @Valid @RequestBody FileSearchRequestDTO request,
            
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String username = userDetails.getUsername();
        FileSearchResponseDTO response = fileStorageService.searchFiles(request, username);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-files")
    @Operation(summary = "Получить файлы пользователя", description = "Возвращает файлы текущего пользователя с пагинацией")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список файлов получен")
    })
    public ResponseEntity<Page<FileResponseDTO>> getMyFiles(
            @Parameter(description = "Номер страницы", example = "0")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Размер страницы", example = "20")
            @RequestParam(defaultValue = "20") int size,
            
            @Parameter(description = "Поле сортировки", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            
            @Parameter(description = "Направление сортировки", example = "desc")
            @RequestParam(defaultValue = "desc") String sortDirection,
            
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String username = userDetails.getUsername();
        UUID userId = getUserIdFromUsername(username);
        
        Pageable pageable = PageRequest.of(page, size, 
                org.springframework.data.domain.Sort.by(
                        sortDirection.equals("desc") 
                                ? org.springframework.data.domain.Sort.Direction.DESC 
                                : org.springframework.data.domain.Sort.Direction.ASC, 
                        sortBy));
        
        Page<FileResponseDTO> response = fileStorageService.getFilesByOwner(userId, pageable, username);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/public")
    @Operation(summary = "Получить публичные файлы", description = "Возвращает публичные файлы с пагинацией")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список публичных файлов получен")
    })
    public ResponseEntity<Page<FileResponseDTO>> getPublicFiles(
            @Parameter(description = "Номер страницы", example = "0")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Размер страницы", example = "20")
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<FileResponseDTO> response = fileStorageService.getPublicFiles(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/statistics")
    @Operation(summary = "Получить статистику хранилища", description = "Возвращает статистику использования хранилища")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Статистика получена"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StorageStatisticsDTO> getStorageStatistics() {
        StorageStatisticsDTO response = fileStorageService.getStorageStatistics();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-statistics")
    @Operation(summary = "Получить статистику пользователя", description = "Возвращает статистику файлов пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Статистика получена")
    })
    public ResponseEntity<UserStorageStatisticsDTO> getUserStatistics(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String username = userDetails.getUsername();
        UUID userId = getUserIdFromUsername(username);
        
        UserStorageStatisticsDTO response = fileStorageService.getUserStorageStatistics(userId, username);
        return ResponseEntity.ok(response);
    }

    // Вспомогательные методы
    private UUID getUserIdFromUsername(String username) {
        // Здесь должна быть логика получения ID пользователя
        // Временно возвращаем заглушку
        return UUID.randomUUID();
    }
}