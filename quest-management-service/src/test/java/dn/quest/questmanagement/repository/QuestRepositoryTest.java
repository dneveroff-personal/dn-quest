package dn.quest.questmanagement.repository;

import dn.quest.questmanagement.entity.Quest;
import dn.quest.questmanagement.entity.QuestStatus;
import dn.quest.shared.enums.Difficulty;
import dn.quest.shared.enums.QuestType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Интеграционные тесты для QuestRepository с использованием Testcontainers
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@ActiveProfiles("test")
class QuestRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private QuestRepository questRepository;

    private Quest testQuest1;
    private Quest testQuest2;
    private Quest publishedQuest;

    @BeforeEach
    void setUp() {
        questRepository.deleteAll();

        // Создаем тестовые квесты
        testQuest1 = Quest.builder()
                .number(1L)
                .title("Test Quest 1")
                .descriptionHtml("<p>Test Description 1</p>")
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

        testQuest2 = Quest.builder()
                .number(2L)
                .title("Test Quest 2")
                .descriptionHtml("<p>Test Description 2</p>")
                .difficulty(Difficulty.MEDIUM)
                .questType(QuestType.TEAM)
                .authorIds(Set.of(2L))
                .status(QuestStatus.DRAFT)
                .published(false)
                .archived(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .version(1)
                .createdBy(2L)
                .updatedBy(2L)
                .averageRating(4.5)
                .build();

        publishedQuest = Quest.builder()
                .number(3L)
                .title("Published Quest")
                .descriptionHtml("<p>Published Description</p>")
                .difficulty(Difficulty.HARD)
                .questType(QuestType.TEAM)
                .authorIds(Set.of(1L, 2L))
                .status(QuestStatus.PUBLISHED)
                .published(true)
                .archived(false)
                .publishedAt(Instant.now())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .version(1)
                .createdBy(1L)
                .updatedBy(1L)
                .averageRating(3.8)
                .build();

        testQuest1 = questRepository.save(testQuest1);
        testQuest2 = questRepository.save(testQuest2);
        publishedQuest = questRepository.save(publishedQuest);
    }

    @Test
    void findByNumber_ShouldReturnQuest_WhenQuestExists() {
        // When
        Optional<Quest> result = questRepository.findByNumber(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testQuest1.getId(), result.get().getId());
        assertEquals(testQuest1.getTitle(), result.get().getTitle());
    }

    @Test
    void findByNumber_ShouldReturnEmpty_WhenQuestNotExists() {
        // When
        Optional<Quest> result = questRepository.findByNumber(999L);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void findActiveQuests_ShouldReturnActiveQuests() {
        // Given
        Instant now = Instant.now();

        // When
        List<Quest> result = questRepository.findActiveQuests(now);

        // Then
        assertEquals(1, result.size());
        assertEquals(publishedQuest.getId(), result.get(0).getId());
    }

    @Test
    void findPublishedQuests_ShouldReturnPublishedQuests() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);

        // When
        Page<Quest> result = questRepository.findPublishedQuests(pageable);

        // Then
        assertEquals(1, result.getContent().size());
        assertEquals(publishedQuest.getId(), result.getContent().get(0).getId());
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void findByStatus_ShouldReturnQuestsWithStatus() {
        // When
        List<Quest> result = questRepository.findByStatus(QuestStatus.DRAFT);

        // Then
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(q -> q.getId().equals(testQuest1.getId())));
        assertTrue(result.stream().anyMatch(q -> q.getId().equals(testQuest2.getId())));
    }

    @Test
    void findByAuthorId_ShouldReturnAuthorQuests() {
        // When
        List<Quest> result = questRepository.findByAuthorId(1L);

        // Then
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(q -> q.getId().equals(testQuest1.getId())));
        assertTrue(result.stream().anyMatch(q -> q.getId().equals(publishedQuest.getId())));
    }

    @Test
    void findByDifficulty_ShouldReturnQuestsWithDifficulty() {
        // When
        List<Quest> result = questRepository.findByDifficulty(Difficulty.EASY);

        // Then
        assertEquals(1, result.size());
        assertEquals(testQuest1.getId(), result.get(0).getId());
    }

    @Test
    void findByQuestType_ShouldReturnQuestsWithType() {
        // When
        List<Quest> result = questRepository.findByQuestType(QuestType.TEAM);

        // Then
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(q -> q.getId().equals(testQuest2.getId())));
        assertTrue(result.stream().anyMatch(q -> q.getId().equals(publishedQuest.getId())));
    }

    @Test
    void findByCategory_ShouldReturnQuestsWithCategory() {
        // Given
        testQuest1.setCategory("Adventure");
        questRepository.save(testQuest1);

        // When
        List<Quest> result = questRepository.findByCategory("Adventure");

        // Then
        assertEquals(1, result.size());
        assertEquals(testQuest1.getId(), result.get(0).getId());
    }

    @Test
    void findByTag_ShouldReturnQuestsWithTag() {
        // Given
        testQuest1.setTags(Set.of("adventure", "outdoor"));
        questRepository.save(testQuest1);

        // When
        List<Quest> result = questRepository.findByTag("adventure");

        // Then
        assertEquals(1, result.size());
        assertEquals(testQuest1.getId(), result.get(0).getId());
    }

    @Test
    void findByTitleContainingIgnoreCase_ShouldReturnMatchingQuests() {
        // When
        List<Quest> result = questRepository.findByTitleContainingIgnoreCase("quest");

        // Then
        assertEquals(3, result.size());
    }

    @Test
    void findByCreatedBy_ShouldReturnCreatedQuests() {
        // When
        List<Quest> result = questRepository.findByCreatedBy(1L);

        // Then
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(q -> q.getId().equals(testQuest1.getId())));
        assertTrue(result.stream().anyMatch(q -> q.getId().equals(publishedQuest.getId())));
    }

    @Test
    void findTemplates_ShouldReturnTemplateQuests() {
        // Given
        testQuest1.setIsTemplate(true);
        questRepository.save(testQuest1);

        // When
        List<Quest> result = questRepository.findTemplates();

        // Then
        assertEquals(1, result.size());
        assertEquals(testQuest1.getId(), result.get(0).getId());
    }

    @Test
    void findByParentQuestId_ShouldReturnChildQuests() {
        // Given
        Quest childQuest = Quest.builder()
                .number(4L)
                .title("Child Quest")
                .descriptionHtml("<p>Child Description</p>")
                .difficulty(Difficulty.EASY)
                .questType(QuestType.SOLO)
                .authorIds(Set.of(3L))
                .status(QuestStatus.DRAFT)
                .published(false)
                .archived(false)
                .parentQuestId(testQuest1.getId())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .version(1)
                .createdBy(3L)
                .updatedBy(3L)
                .averageRating(0.0)
                .build();
        questRepository.save(childQuest);

        // When
        List<Quest> result = questRepository.findByParentQuestId(testQuest1.getId());

        // Then
        assertEquals(1, result.size());
        assertEquals(childQuest.getId(), result.get(0).getId());
    }

    @Test
    void countByStatus_ShouldReturnCorrectCount() {
        // When
        long result = questRepository.countByStatus(QuestStatus.DRAFT);

        // Then
        assertEquals(2, result);
    }

    @Test
    void countPublishedQuests_ShouldReturnCorrectCount() {
        // When
        long result = questRepository.countPublishedQuests();

        // Then
        assertEquals(1, result);
    }

    @Test
    void countActiveQuests_ShouldReturnCorrectCount() {
        // Given
        Instant now = Instant.now();

        // When
        long result = questRepository.countActiveQuests(now);

        // Then
        assertEquals(1, result);
    }

    @Test
    void countByAuthorId_ShouldReturnCorrectCount() {
        // When
        long result = questRepository.countByAuthorId(1L);

        // Then
        assertEquals(2, result);
    }

    @Test
    void existsByNumber_ShouldReturnTrue_WhenQuestExists() {
        // When
        boolean result = questRepository.existsByNumber(1L);

        // Then
        assertTrue(result);
    }

    @Test
    void existsByNumber_ShouldReturnFalse_WhenQuestNotExists() {
        // When
        boolean result = questRepository.existsByNumber(999L);

        // Then
        assertFalse(result);
    }

    @Test
    void existsByTitleIgnoreCase_ShouldReturnTrue_WhenQuestExists() {
        // When
        boolean result = questRepository.existsByTitleIgnoreCase("test quest 1");

        // Then
        assertTrue(result);
    }

    @Test
    void findMaxNumber_ShouldReturnMaxNumber() {
        // When
        Long result = questRepository.findMaxNumber();

        // Then
        assertEquals(3L, result);
    }

    @Test
    void updateStatus_ShouldUpdateStatus() {
        // Given
        List<Long> questIds = List.of(testQuest1.getId(), testQuest2.getId());

        // When
        int result = questRepository.updateStatus(questIds, QuestStatus.PUBLISHED);

        // Then
        assertEquals(2, result);
        
        Quest updatedQuest1 = questRepository.findById(testQuest1.getId()).orElseThrow();
        Quest updatedQuest2 = questRepository.findById(testQuest2.getId()).orElseThrow();
        
        assertEquals(QuestStatus.PUBLISHED, updatedQuest1.getStatus());
        assertEquals(QuestStatus.PUBLISHED, updatedQuest2.getStatus());
    }

    @Test
    void archiveQuests_ShouldArchiveQuests() {
        // Given
        List<Long> questIds = List.of(testQuest1.getId(), testQuest2.getId());

        // When
        int result = questRepository.archiveQuests(questIds, "Test archive");

        // Then
        assertEquals(2, result);
        
        Quest archivedQuest1 = questRepository.findById(testQuest1.getId()).orElseThrow();
        Quest archivedQuest2 = questRepository.findById(testQuest2.getId()).orElseThrow();
        
        assertTrue(archivedQuest1.getArchived());
        assertTrue(archivedQuest2.getArchived());
        assertEquals("Test archive", archivedQuest1.getArchiveReason());
        assertEquals("Test archive", archivedQuest2.getArchiveReason());
    }

    @Test
    void findTopRatedQuests_ShouldReturnQuestsOrderedByRating() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);

        // When
        Page<Quest> result = questRepository.findTopRatedQuests(pageable);

        // Then
        assertEquals(1, result.getContent().size());
        assertEquals(publishedQuest.getId(), result.getContent().get(0).getId());
    }

    @Test
    void updateAverageRating_ShouldUpdateRating() {
        // When
        questRepository.updateAverageRating(testQuest1.getId(), 4.2);

        // Then
        Quest updatedQuest = questRepository.findById(testQuest1.getId()).orElseThrow();
        assertEquals(4.2, updatedQuest.getAverageRating());
    }
}