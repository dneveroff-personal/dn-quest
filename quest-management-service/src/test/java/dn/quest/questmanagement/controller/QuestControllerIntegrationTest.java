package dn.quest.questmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dn.quest.questmanagement.dto.CreateQuestDTO;
import dn.quest.questmanagement.dto.UpdateQuestDTO;
import dn.quest.questmanagement.entity.Quest;
import dn.quest.questmanagement.entity.QuestStatus;
import dn.quest.questmanagement.repository.QuestRepository;
import dn.quest.shared.enums.Difficulty;
import dn.quest.shared.enums.QuestType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.Instant;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Интеграционные тесты для QuestController
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class QuestControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private QuestRepository questRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    private Quest testQuest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Создаем тестовый квест
        testQuest = Quest.builder()
                .number(1L)
                .title("Test Quest")
                .descriptionHtml("<p>Test Description</p>")
                .difficulty(Difficulty.EASY)
                .questType(QuestType.SOLO)
                .authorIds(Set.of(1L))
                .status(QuestStatus.DRAFT)
                .published(false)
                .archived(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .version(1)
                .createdBy(1L)
                .updatedBy(1L)
                .averageRating(0.0)
                .build();

        testQuest = questRepository.save(testQuest);
    }

    @Test
    void createQuest_ShouldReturnCreatedQuest() throws Exception {
        // Given
        CreateQuestDTO createQuestDTO = new CreateQuestDTO();
        createQuestDTO.setTitle("New Quest");
        createQuestDTO.setDescriptionHtml("<p>New Description</p>");
        createQuestDTO.setDifficulty(Difficulty.MEDIUM);
        createQuestDTO.setQuestType(QuestType.TEAM);
        createQuestDTO.setCategory("Adventure");
        createQuestDTO.setMaxParticipants(10);
        createQuestDTO.setMinParticipants(2);
        createQuestDTO.setEstimatedDurationMinutes(60);
        createQuestDTO.setTags(Set.of("adventure", "outdoor"));

        // When & Then
        mockMvc.perform(post("/api/quests")
                .param("userId", "2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createQuestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.title", is("New Quest")))
                .andExpect(jsonPath("$.difficulty", is("MEDIUM")))
                .andExpect(jsonPath("$.questType", is("TEAM")))
                .andExpect(jsonPath("$.category", is("Adventure")))
                .andExpect(jsonPath("$.maxParticipants", is(10)))
                .andExpect(jsonPath("$.minParticipants", is(2)))
                .andExpect(jsonPath("$.estimatedDurationMinutes", is(60)))
                .andExpect(jsonPath("$.tags", hasSize(2)))
                .andExpect(jsonPath("$.status", is("DRAFT")))
                .andExpect(jsonPath("$.published", is(false)))
                .andExpect(jsonPath("$.archived", is(false)));
    }

    @Test
    void getQuestById_ShouldReturnQuest() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/quests/{id}", testQuest.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testQuest.getId().intValue())))
                .andExpect(jsonPath("$.title", is(testQuest.getTitle())))
                .andExpect(jsonPath("$.difficulty", is(testQuest.getDifficulty().name())))
                .andExpect(jsonPath("$.questType", is(testQuest.getQuestType().name())))
                .andExpect(jsonPath("$.status", is(testQuest.getStatus().name())));
    }

    @Test
    void getQuestById_ShouldReturnNotFound_WhenQuestNotExists() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/quests/{id}", 99999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateQuest_ShouldReturnUpdatedQuest() throws Exception {
        // Given
        UpdateQuestDTO updateQuestDTO = new UpdateQuestDTO();
        updateQuestDTO.setTitle("Updated Quest");
        updateQuestDTO.setDescriptionHtml("<p>Updated Description</p>");
        updateQuestDTO.setDifficulty(Difficulty.HARD);
        updateQuestDTO.setCategory("Updated Category");
        updateQuestDTO.setMaxParticipants(15);

        // When & Then
        mockMvc.perform(put("/api/quests/{id}", testQuest.getId())
                .param("userId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateQuestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testQuest.getId().intValue())))
                .andExpect(jsonPath("$.title", is("Updated Quest")))
                .andExpect(jsonPath("$.difficulty", is("HARD")))
                .andExpect(jsonPath("$.category", is("Updated Category")))
                .andExpect(jsonPath("$.maxParticipants", is(15)));
    }

    @Test
    void deleteQuest_ShouldReturnNoContent() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/quests/{id}", testQuest.getId())
                .param("userId", "1"))
                .andExpect(status().isNoContent());

        // Проверяем, что квест удален
        mockMvc.perform(get("/api/quests/{id}", testQuest.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void publishQuest_ShouldReturnPublishedQuest() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/quests/{id}/publish", testQuest.getId())
                .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testQuest.getId().intValue())))
                .andExpect(jsonPath("$.published", is(true)))
                .andExpect(jsonPath("$.status", is("PUBLISHED")))
                .andExpect(jsonPath("$.publishedAt", notNullValue()));
    }

    @Test
    void unpublishQuest_ShouldReturnUnpublishedQuest() throws Exception {
        // Сначала публикуем квест
        testQuest.setPublished(true);
        testQuest.setStatus(QuestStatus.PUBLISHED);
        testQuest.setPublishedAt(Instant.now());
        questRepository.save(testQuest);

        // When & Then
        mockMvc.perform(post("/api/quests/{id}/unpublish", testQuest.getId())
                .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testQuest.getId().intValue())))
                .andExpect(jsonPath("$.published", is(false)))
                .andExpect(jsonPath("$.status", is("DRAFT")))
                .andExpect(jsonPath("$.publishedAt", nullValue()));
    }

    @Test
    void archiveQuest_ShouldReturnArchivedQuest() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/quests/{id}/archive", testQuest.getId())
                .param("userId", "1")
                .param("reason", "Test archive"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testQuest.getId().intValue())))
                .andExpect(jsonPath("$.archived", is(true)))
                .andExpect(jsonPath("$.archiveReason", is("Test archive")))
                .andExpect(jsonPath("$.archivedAt", notNullValue()));
    }

    @Test
    void copyQuest_ShouldReturnCopiedQuest() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/quests/{id}/copy", testQuest.getId())
                .param("userId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.title", containsString("Copy")))
                .andExpect(jsonPath("$.parentQuestId", is(testQuest.getId().intValue())))
                .andExpect(jsonPath("$.status", is("DRAFT")))
                .andExpect(jsonPath("$.published", is(false)));
    }

    @Test
    void getPublishedQuests_ShouldReturnPublishedQuests() throws Exception {
        // Создаем опубликованный квест
        Quest publishedQuest = Quest.builder()
                .number(2L)
                .title("Published Quest")
                .descriptionHtml("<p>Published Description</p>")
                .difficulty(Difficulty.MEDIUM)
                .questType(QuestType.TEAM)
                .authorIds(Set.of(1L))
                .status(QuestStatus.PUBLISHED)
                .published(true)
                .archived(false)
                .publishedAt(Instant.now())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .version(1)
                .createdBy(1L)
                .updatedBy(1L)
                .averageRating(0.0)
                .build();
        questRepository.save(publishedQuest);

        // When & Then
        mockMvc.perform(get("/api/quests/published")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title", is("Published Quest")))
                .andExpect(jsonPath("$.content[0].published", is(true)))
                .andExpect(jsonPath("$.totalElements", is(1)));
    }

    @Test
    void getQuestsByAuthor_ShouldReturnAuthorQuests() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/quests/author/{authorId}", 1L)
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title", is(testQuest.getTitle())))
                .andExpect(jsonPath("$.totalElements", is(1)));
    }

    @Test
    void searchQuests_ShouldReturnMatchingQuests() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/quests/search")
                .param("title", "Test")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title", is(testQuest.getTitle())))
                .andExpect(jsonPath("$.totalElements", is(1)));
    }

    @Test
    void getQuestsByDifficulty_ShouldReturnQuestsWithDifficulty() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/quests/difficulty/{difficulty}", Difficulty.EASY)
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].difficulty", is("EASY")))
                .andExpect(jsonPath("$.totalElements", is(1)));
    }

    @Test
    void getQuestsByType_ShouldReturnQuestsWithType() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/quests/type/{questType}", QuestType.SOLO)
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].questType", is("SOLO")))
                .andExpect(jsonPath("$.totalElements", is(1)));
    }

    @Test
    void getQuestsByCategory_ShouldReturnQuestsWithCategory() throws Exception {
        // Устанавливаем категорию для тестового квеста
        testQuest.setCategory("Adventure");
        questRepository.save(testQuest);

        // When & Then
        mockMvc.perform(get("/api/quests/category/{category}", "Adventure")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].category", is("Adventure")))
                .andExpect(jsonPath("$.totalElements", is(1)));
    }

    @Test
    void createQuest_ShouldReturnBadRequest_WhenInvalidData() throws Exception {
        // Given
        CreateQuestDTO invalidQuestDTO = new CreateQuestDTO();
        invalidQuestDTO.setTitle(""); // Пустое название
        invalidQuestDTO.setDifficulty(null); // Отсутствует сложность

        // When & Then
        mockMvc.perform(post("/api/quests")
                .param("userId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidQuestDTO)))
                .andExpect(status().isBadRequest());
    }
}