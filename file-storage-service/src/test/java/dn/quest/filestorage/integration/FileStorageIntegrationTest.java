package dn.quest.filestorage.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import dn.quest.shared.base.AbstractIntegrationTestBase;
import dn.quest.shared.util.EnhancedTestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Интеграционные тесты для File Storage Service
 */
class FileStorageIntegrationTest extends AbstractIntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @Override
    protected void cleanupTestData() {
        // Очистка тестовых данных для файлового хранилища
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testUploadFile_Success() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        // When & Then
        mockMvc.perform(multipart("/api/files/upload")
                        .file(file)
                        .param("category", "AVATAR")
                        .param("description", "Test avatar image"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.fileName").value("test-image.jpg"))
                .andExpect(jsonPath("$.originalFileName").value("test-image.jpg"))
                .andExpect(jsonPath("$.contentType").value("image/jpeg"))
                .andExpect(jsonPath("$.size").value(18))
                .andExpect(jsonPath("$.category").value("AVATAR"))
                .andExpect(jsonPath("$.description").value("Test avatar image"))
                .andExpect(jsonPath("$.downloadUrl").exists())
                .andExpect(jsonPath("$.uploadedBy").exists())
                .andExpect(jsonPath("$.uploadedAt").exists());

        // Проверка отправки события в Kafka
        verify(kafkaTemplate).send(anyString(), anyString(), anyString());
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testUploadFile_InvalidFile_ReturnsBadRequest() throws Exception {
        // Given
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.txt",
                MediaType.TEXT_PLAIN_VALUE,
                new byte[0]
        );

        // When & Then
        mockMvc.perform(multipart("/api/files/upload")
                        .file(emptyFile)
                        .param("category", "DOCUMENT"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testUploadFile_UnsupportedFormat_ReturnsBadRequest() throws Exception {
        // Given
        MockMultipartFile unsupportedFile = new MockMultipartFile(
                "file",
                "test.exe",
                "application/octet-stream",
                "executable content".getBytes()
        );

        // When & Then
        mockMvc.perform(multipart("/api/files/upload")
                        .file(unsupportedFile)
                        .param("category", "DOCUMENT"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Unsupported file format"));
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testUploadFile_FileTooLarge_ReturnsBadRequest() throws Exception {
        // Given
        byte[] largeContent = new byte[11 * 1024 * 1024]; // 11MB
        MockMultipartFile largeFile = new MockMultipartFile(
                "file",
                "large-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                largeContent
        );

        // When & Then
        mockMvc.perform(multipart("/api/files/upload")
                        .file(largeFile)
                        .param("category", "AVATAR"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("File size exceeds maximum allowed limit"));
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testGetFile_Success() throws Exception {
        // Given - Сначала загружаем файл
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-document.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "test pdf content".getBytes()
        );

        String uploadResponse = mockMvc.perform(multipart("/api/files/upload")
                        .file(file)
                        .param("category", "DOCUMENT"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long fileId = objectMapper.readTree(uploadResponse).get("id").asLong();

        // When & Then - Получаем информацию о файле
        mockMvc.perform(get("/api/files/" + fileId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(fileId))
                .andExpect(jsonPath("$.fileName").value("test-document.pdf"))
                .andExpect(jsonPath("$.contentType").value("application/pdf"))
                .andExpect(jsonPath("$.category").value("DOCUMENT"));
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testGetFile_NotFound_ReturnsNotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/files/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("File not found"));
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testDownloadFile_Success() throws Exception {
        // Given - Сначала загружаем файл
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "download-test.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "download test content".getBytes()
        );

        String uploadResponse = mockMvc.perform(multipart("/api/files/upload")
                        .file(file)
                        .param("category", "DOCUMENT"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long fileId = objectMapper.readTree(uploadResponse).get("id").asLong();

        // When & Then - Скачиваем файл
        mockMvc.perform(get("/api/files/" + fileId + "/download"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"download-test.txt\""))
                .andExpect(content().bytes("download test content".getBytes()));
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testUpdateFileMetadata_Success() throws Exception {
        // Given - Сначала загружаем файл
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "metadata-test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        String uploadResponse = mockMvc.perform(multipart("/api/files/upload")
                        .file(file)
                        .param("category", "AVATAR"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long fileId = objectMapper.readTree(uploadResponse).get("id").asLong();

        // When & Then - Обновляем метаданные
        var updateRequest = new java.util.HashMap<String, Object>();
        updateRequest.put("description", "Updated description");
        updateRequest.put("category", "PROFILE_IMAGE");
        updateRequest.put("tags", List.of("profile", "avatar", "updated"));

        mockMvc.perform(put("/api/files/" + fileId + "/metadata")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(fileId))
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.category").value("PROFILE_IMAGE"))
                .andExpect(jsonPath("$.tags").isArray())
                .andExpect(jsonPath("$.tags[0]").value("profile"));
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testDeleteFile_Success() throws Exception {
        // Given - Сначала загружаем файл
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "delete-test.png",
                MediaType.IMAGE_PNG_VALUE,
                "test png content".getBytes()
        );

        String uploadResponse = mockMvc.perform(multipart("/api/files/upload")
                        .file(file)
                        .param("category", "AVATAR"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long fileId = objectMapper.readTree(uploadResponse).get("id").asLong();

        // When & Then - Удаляем файл
        mockMvc.perform(delete("/api/files/" + fileId))
                .andExpect(status().isNoContent());

        // Проверяем, что файл удален
        mockMvc.perform(get("/api/files/" + fileId))
                .andExpect(status().isNotFound());

        // Проверка отправки события в Kafka
        verify(kafkaTemplate).send(anyString(), anyString(), anyString());
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testBatchUpload_Success() throws Exception {
        // Given
        MockMultipartFile file1 = new MockMultipartFile(
                "files",
                "batch-file-1.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "batch file 1 content".getBytes()
        );

        MockMultipartFile file2 = new MockMultipartFile(
                "files",
                "batch-file-2.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "batch file 2 content".getBytes()
        );

        // When & Then
        mockMvc.perform(multipart("/api/files/batch-upload")
                        .file(file1)
                        .file(file2)
                        .param("category", "GALLERY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uploadedFiles").isArray())
                .andExpect(jsonPath("$.uploadedFiles").hasJsonPath(2))
                .andExpect(jsonPath("$.totalUploaded").value(2))
                .andExpect(jsonPath("$.totalFailed").value(0));

        // Проверка отправки событий в Kafka
        verify(kafkaTemplate).send(anyString(), anyString(), anyString());
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testSearchFiles_Success() throws Exception {
        // Given - Загружаем несколько файлов
        List<String> fileNames = List.of("search-test-1.jpg", "search-test-2.png", "other-file.pdf");
        
        for (String fileName : fileNames) {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    fileName,
                    fileName.endsWith(".jpg") ? MediaType.IMAGE_JPEG_VALUE : 
                    fileName.endsWith(".png") ? MediaType.IMAGE_PNG_VALUE : MediaType.APPLICATION_PDF_VALUE,
                    ("content of " + fileName).getBytes()
            );

            mockMvc.perform(multipart("/api/files/upload")
                            .file(file)
                            .param("category", "DOCUMENT"))
                    .andExpect(status().isOk());
        }

        // When & Then - Ищем файлы по ключевому слову
        mockMvc.perform(get("/api/files/search")
                        .param("keyword", "search-test")
                        .param("category", "DOCUMENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").hasJsonPath(2))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testGetMyFiles_Success() throws Exception {
        // Given - Загружаем несколько файлов
        for (int i = 0; i < 3; i++) {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "my-file-" + i + ".jpg",
                    MediaType.IMAGE_JPEG_VALUE,
                    ("my file content " + i).getBytes()
            );

            mockMvc.perform(multipart("/api/files/upload")
                            .file(file)
                            .param("category", "AVATAR"))
                    .andExpect(status().isOk());
        }

        // When & Then - Получаем файлы пользователя
        mockMvc.perform(get("/api/files/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").hasJsonPath(3))
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testGetFilesByCategory_Success() throws Exception {
        // Given - Загружаем файлы разных категорий
        MockMultipartFile avatarFile = new MockMultipartFile(
                "file",
                "avatar.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "avatar content".getBytes()
        );

        MockMultipartFile documentFile = new MockMultipartFile(
                "file",
                "document.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "document content".getBytes()
        );

        mockMvc.perform(multipart("/api/files/upload")
                        .file(avatarFile)
                        .param("category", "AVATAR"))
                .andExpect(status().isOk());

        mockMvc.perform(multipart("/api/files/upload")
                        .file(documentFile)
                        .param("category", "DOCUMENT"))
                .andExpect(status().isOk());

        // When & Then - Получаем файлы по категории
        mockMvc.perform(get("/api/files/category/AVATAR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").hasJsonPath(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].category").value("AVATAR"));
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testGeneratePresignedUploadUrl_Success() throws Exception {
        // Given
        var request = new java.util.HashMap<String, Object>();
        request.put("fileName", "presigned-test.jpg");
        request.put("contentType", "image/jpeg");
        request.put("category", "AVATAR");

        // When & Then
        mockMvc.perform(post("/api/files/presigned-upload-url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uploadUrl").exists())
                .andExpect(jsonPath("$.fileName").value("presigned-test.jpg"))
                .andExpect(jsonPath("$.contentType").value("image/jpeg"))
                .andExpect(jsonPath("$.expiresAt").exists());
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testGetStorageStatistics_Success() throws Exception {
        // Given - Загружаем несколько файлов для статистики
        for (int i = 0; i < 2; i++) {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "stats-file-" + i + ".jpg",
                    MediaType.IMAGE_JPEG_VALUE,
                    ("stats content " + i).getBytes()
            );

            mockMvc.perform(multipart("/api/files/upload")
                            .file(file)
                            .param("category", "AVATAR"))
                    .andExpect(status().isOk());
        }

        // When & Then - Получаем статистику хранилища
        mockMvc.perform(get("/api/files/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalFiles").exists())
                .andExpect(jsonPath("$.totalSize").exists())
                .andExpect(jsonPath("$.usedSpace").exists())
                .andExpect(jsonPath("$.availableSpace").exists())
                .andExpect(jsonPath("$.filesByCategory").exists())
                .andExpect(jsonPath("$.uploadStats").exists());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testGetAllFiles_Admin_Success() throws Exception {
        // Given - Загружаем несколько файлов
        for (int i = 0; i < 3; i++) {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "admin-file-" + i + ".jpg",
                    MediaType.IMAGE_JPEG_VALUE,
                    ("admin content " + i).getBytes()
            );

            mockMvc.perform(multipart("/api/files/upload")
                            .file(file)
                            .param("category", "AVATAR"))
                    .andExpect(status().isOk());
        }

        // When & Then - Администратор получает все файлы
        mockMvc.perform(get("/api/files/admin/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").hasJsonPath(3))
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testGetAllFiles_Player_Forbidden() throws Exception {
        // When & Then - Обычный пользователь не может получить все файлы
        mockMvc.perform(get("/api/files/admin/all"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testCleanupExpiredFiles_Success() throws Exception {
        // When & Then - Администратор запускает очистку
        mockMvc.perform(post("/api/files/admin/cleanup-expired"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Cleanup completed"))
                .andExpect(jsonPath("$.deletedFiles").exists())
                .andExpect(jsonPath("$.freedSpace").exists());
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testGetFilePreview_Success() throws Exception {
        // Given - Загружаем изображение
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "preview-test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "preview image content".getBytes()
        );

        String uploadResponse = mockMvc.perform(multipart("/api/files/upload")
                        .file(file)
                        .param("category", "AVATAR"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long fileId = objectMapper.readTree(uploadResponse).get("id").asLong();

        // When & Then - Получаем превью
        mockMvc.perform(get("/api/files/" + fileId + "/preview"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/jpeg"))
                .andExpect(content().bytes("preview image content".getBytes()));
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testGetFilePreview_NotImage_ReturnsBadRequest() throws Exception {
        // Given - Загружаем не изображение
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "not-image.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "text content".getBytes()
        );

        String uploadResponse = mockMvc.perform(multipart("/api/files/upload")
                        .file(file)
                        .param("category", "DOCUMENT"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long fileId = objectMapper.readTree(uploadResponse).get("id").asLong();

        // When & Then
        mockMvc.perform(get("/api/files/" + fileId + "/preview"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Preview is only available for image files"));
    }
}