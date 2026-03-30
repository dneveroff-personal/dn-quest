package dn.quest.questmanagement.service;

import dn.quest.questmanagement.dto.QuestDTO;
import dn.quest.questmanagement.dto.CreateQuestDTO;
import dn.quest.questmanagement.dto.UpdateQuestDTO;
import dn.quest.questmanagement.entity.Quest;
import dn.quest.questmanagement.entity.QuestStatus;
import dn.quest.questmanagement.repository.QuestRepository;
import dn.quest.questmanagement.service.impl.QuestServiceImpl;
import dn.quest.shared.enums.Difficulty;
import dn.quest.shared.enums.QuestType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit тесты для QuestService
 */
@ExtendWith(MockitoExtension.class)
class QuestServiceTest {

    @Mock
    private QuestRepository questRepository;

    @Mock
    private QuestValidationService questValidationService;

    @Mock
    private QuestVersionService questVersionService;

    @Mock
    private KafkaEventProducer kafkaEventProducer;

    @InjectMocks
    private QuestServiceImpl questService;

    private Quest testQuest;
    private CreateQuestDTO createQuestDTO;
    private UpdateQuestDTO updateQuestDTO;

    @BeforeEach
    void setUp() {
        testQuest = Quest.builder()
                .id(1L)
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

        createQuestDTO = new CreateQuestDTO();
        createQuestDTO.setTitle("New Quest");
        createQuestDTO.setDescriptionHtml("<p>New Description</p>");
        createQuestDTO.setDifficulty(Difficulty.MEDIUM);
        createQuestDTO.setQuestType(QuestType.TEAM);
        createQuestDTO.setCategory("Adventure");
        createQuestDTO.setMaxParticipants(10);
        createQuestDTO.setMinParticipants(2);
        createQuestDTO.setEstimatedDurationMinutes(60);
        createQuestDTO.setTags(Set.of("adventure", "outdoor"));

        updateQuestDTO = new UpdateQuestDTO();
        updateQuestDTO.setTitle("Updated Quest");
        updateQuestDTO.setDescriptionHtml("<p>Updated Description</p>");
        updateQuestDTO.setDifficulty(Difficulty.HARD);
        updateQuestDTO.setCategory("Updated Category");
        updateQuestDTO.setMaxParticipants(15);
    }

    @Test
    void createQuest_ShouldReturnCreatedQuest() {
        // Given
        Quest savedQuest = Quest.builder()
                .id(2L)
                .number(2L)
                .title(createQuestDTO.getTitle())
                .descriptionHtml(createQuestDTO.getDescriptionHtml())
                .difficulty(createQuestDTO.getDifficulty())
                .questType(createQuestDTO.getQuestType())
                .category(createQuestDTO.getCategory())
                .maxParticipants(createQuestDTO.getMaxParticipants())
                .minParticipants(createQuestDTO.getMinParticipants())
                .estimatedDurationMinutes(createQuestDTO.getEstimatedDurationMinutes())
                .tags(createQuestDTO.getTags())
                .status(QuestStatus.DRAFT)
                .published(false)
                .archived(false)
                .version(1)
                .createdBy(1L)
                .updatedBy(1L)
                .averageRating(0.0)
                .build();

        when(questRepository.findMaxNumber()).thenReturn(1L);
        when(questRepository.save(any(Quest.class))).thenReturn(savedQuest);

        // When
        QuestDTO result = questService.createQuest(createQuestDTO, 1L);

        // Then
        assertNotNull(result);
        assertEquals(savedQuest.getId(), result.getId());
        assertEquals(savedQuest.getTitle(), result.getTitle());
        assertEquals(savedQuest.getDifficulty(), result.getDifficulty());
        assertEquals(savedQuest.getQuestType(), result.getQuestType());
        verify(questRepository).save(any(Quest.class));
        verify(kafkaEventProducer).publishQuestCreatedEvent(any());
    }

    @Test
    void getQuestById_ShouldReturnQuest_WhenQuestExists() {
        // Given
        when(questRepository.findById(1L)).thenReturn(Optional.of(testQuest));

        // When
        QuestDTO result = questService.getQuestById(1L);

        // Then
        assertNotNull(result);
        assertEquals(testQuest.getId(), result.getId());
        assertEquals(testQuest.getTitle(), result.getTitle());
        verify(questRepository).findById(1L);
    }

    @Test
    void getQuestById_ShouldThrowException_WhenQuestNotFound() {
        // Given
        when(questRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> questService.getQuestById(1L));
        verify(questRepository).findById(1L);
    }

    @Test
    void updateQuest_ShouldReturnUpdatedQuest() {
        // Given
        when(questRepository.findById(1L)).thenReturn(Optional.of(testQuest));
        when(questRepository.save(any(Quest.class))).thenReturn(testQuest);

        // When
        QuestDTO result = questService.updateQuest(1L, updateQuestDTO, 1L);

        // Then
        assertNotNull(result);
        verify(questRepository).findById(1L);
        verify(questRepository).save(any(Quest.class));
        verify(kafkaEventProducer).publishQuestUpdatedEvent(any());
    }

    @Test
    void deleteQuest_ShouldDeleteQuest() {
        // Given
        when(questRepository.findById(1L)).thenReturn(Optional.of(testQuest));
        doNothing().when(questRepository).delete(testQuest);

        // When
        questService.deleteQuest(1L, 1L);

        // Then
        verify(questRepository).findById(1L);
        verify(questRepository).delete(testQuest);
        verify(kafkaEventProducer).publishQuestDeletedEvent(any());
    }

    @Test
    void publishQuest_ShouldPublishQuest() {
        // Given
        when(questRepository.findById(1L)).thenReturn(Optional.of(testQuest));
        when(questRepository.save(any(Quest.class))).thenReturn(testQuest);
        when(questValidationService.validateQuestForPublishing(any())).thenReturn(true);

        // When
        QuestDTO result = questService.publishQuest(1L, 1L);

        // Then
        assertNotNull(result);
        verify(questRepository).findById(1L);
        verify(questRepository).save(any(Quest.class));
        verify(questValidationService).validateQuestForPublishing(any());
        verify(kafkaEventProducer).publishQuestPublishedEvent(any());
    }

    @Test
    void unpublishQuest_ShouldUnpublishQuest() {
        // Given
        testQuest.setPublished(true);
        testQuest.setStatus(QuestStatus.PUBLISHED);
        when(questRepository.findById(1L)).thenReturn(Optional.of(testQuest));
        when(questRepository.save(any(Quest.class))).thenReturn(testQuest);

        // When
        QuestDTO result = questService.unpublishQuest(1L, 1L);

        // Then
        assertNotNull(result);
        verify(questRepository).findById(1L);
        verify(questRepository).save(any(Quest.class));
        verify(kafkaEventProducer).publishQuestUnpublishedEvent(any());
    }

    @Test
    void getPublishedQuests_ShouldReturnPublishedQuests() {
        // Given
        List<Quest> quests = Collections.singletonList(testQuest);
        Pageable pageable = PageRequest.of(0, 20);
        Page<Quest> questPage = new PageImpl<>(quests, pageable, quests.size());

        when(questRepository.findPublishedQuests(pageable)).thenReturn(questPage);

        // When
        Page<QuestDTO> result = questService.getPublishedQuests(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testQuest.getTitle(), result.getContent().get(0).getTitle());
        verify(questRepository).findPublishedQuests(pageable);
    }

    @Test
    void getQuestsByAuthor_ShouldReturnAuthorQuests() {
        // Given
        List<Quest> quests = Collections.singletonList(testQuest);
        Pageable pageable = PageRequest.of(0, 20);
        Page<Quest> questPage = new PageImpl<>(quests, pageable, quests.size());

        when(questRepository.findByAuthorId(1L, pageable)).thenReturn(questPage);

        // When
        Page<QuestDTO> result = questService.getQuestsByAuthor(1L, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testQuest.getTitle(), result.getContent().get(0).getTitle());
        verify(questRepository).findByAuthorId(1L, pageable);
    }

    @Test
    void searchQuests_ShouldReturnMatchingQuests() {
        // Given
        List<Quest> quests = Collections.singletonList(testQuest);
        Pageable pageable = PageRequest.of(0, 20);
        Page<Quest> questPage = new PageImpl<>(quests, pageable, quests.size());

        when(questRepository.findByTitleContainingIgnoreCase("Test", pageable)).thenReturn(questPage);

        // When
        Page<QuestDTO> result = questService.searchQuests("Test", pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testQuest.getTitle(), result.getContent().get(0).getTitle());
        verify(questRepository).findByTitleContainingIgnoreCase("Test", pageable);
    }

    @Test
    void archiveQuest_ShouldArchiveQuest() {
        // Given
        when(questRepository.findById(1L)).thenReturn(Optional.of(testQuest));
        when(questRepository.save(any(Quest.class))).thenReturn(testQuest);

        // When
        QuestDTO result = questService.archiveQuest(1L, "Test reason", 1L);

        // Then
        assertNotNull(result);
        verify(questRepository).findById(1L);
        verify(questRepository).save(any(Quest.class));
        verify(kafkaEventProducer).publishQuestArchivedEvent(any());
    }

    @Test
    void copyQuest_ShouldCreateCopy() {
        // Given
        Quest copiedQuest = Quest.builder()
                .id(3L)
                .number(3L)
                .title(testQuest.getTitle() + " (Copy)")
                .descriptionHtml(testQuest.getDescriptionHtml())
                .difficulty(testQuest.getDifficulty())
                .questType(testQuest.getQuestType())
                .authorIds(Set.of(2L))
                .status(QuestStatus.DRAFT)
                .published(false)
                .archived(false)
                .parentQuestId(testQuest.getId())
                .version(1)
                .createdBy(2L)
                .updatedBy(2L)
                .averageRating(0.0)
                .build();

        when(questRepository.findById(1L)).thenReturn(Optional.of(testQuest));
        when(questRepository.findMaxNumber()).thenReturn(2L);
        when(questRepository.save(any(Quest.class))).thenReturn(copiedQuest);

        // When
        QuestDTO result = questService.copyQuest(1L, 2L);

        // Then
        assertNotNull(result);
        assertEquals(copiedQuest.getId(), result.getId());
        assertTrue(result.getTitle().contains("Copy"));
        assertEquals(2L, result.getParentQuestId());
        verify(questRepository).findById(1L);
        verify(questRepository).save(any(Quest.class));
    }
}