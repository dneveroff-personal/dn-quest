package dn.quest.questmanagement.service;

import dn.quest.questmanagement.dto.QuestRatingDTO;
import dn.quest.questmanagement.dto.QuestReviewDTO;
import dn.quest.questmanagement.entity.Quest;
import dn.quest.questmanagement.entity.QuestRating;
import dn.quest.questmanagement.entity.QuestReview;
import dn.quest.questmanagement.repository.QuestRepository;
import dn.quest.questmanagement.repository.QuestRatingRepository;
import dn.quest.questmanagement.repository.QuestReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

/**
 * Сервис для управления рейтингами и отзывами на квесты
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuestRatingService {

    private final QuestRatingRepository ratingRepository;
    private final QuestReviewRepository reviewRepository;
    private final QuestRepository questRepository;
    private final QuestService questService;

    /**
     * Добавить или обновить рейтинг квеста
     */
    @Transactional
    public QuestRatingDTO rateQuest(Long questId, Long userId, Integer rating) {
        log.info("User {} rating quest {} with {}", userId, questId, rating);

        // Проверяем существование квеста
        Quest quest = questRepository.findById(questId)
                .orElseThrow(() -> new RuntimeException("Quest not found with id: " + questId));

        // Проверяем, что пользователь не является автором квеста
        if (quest.getAuthorIds().contains(userId)) {
            throw new RuntimeException("Authors cannot rate their own quests");
        }

        // Проверяем существующий рейтинг
        Optional<QuestRating> existingRating = ratingRepository.findByQuestIdAndUserId(questId, userId);

        QuestRating questRating;
        if (existingRating.isPresent()) {
            // Обновляем существующий рейтинг
            questRating = existingRating.get();
            questRating.setRating(rating);
            questRating.setUpdatedAt(LocalDateTime.now());
            log.info("Updated existing rating for quest {} by user {}", questId, userId);
        } else {
            // Создаем новый рейтинг
            questRating = new QuestRating();
            questRating.setQuestId(questId);
            questRating.setUserId(userId);
            questRating.setRating(rating);
            questRating.setCreatedAt(LocalDateTime.now());
            log.info("Created new rating for quest {} by user {}", questId, userId);
        }

        QuestRating savedRating = ratingRepository.save(questRating);

        // Обновляем средний рейтинг квеста
        updateQuestAverageRating(questId);

        return convertToDTO(savedRating);
    }

    /**
     * Добавить отзыв на квест
     */
    @Transactional
    public QuestReviewDTO reviewQuest(Long questId, Long userId, String title, String content, Integer rating) {
        log.info("User {} reviewing quest {} with rating {}", userId, questId, rating);

        // Проверяем существование квеста
        Quest quest = questRepository.findById(questId)
                .orElseThrow(() -> new RuntimeException("Quest not found with id: " + questId));

        // Проверяем, что пользователь не является автором квеста
        if (quest.getAuthorIds().contains(userId)) {
            throw new RuntimeException("Authors cannot review their own quests");
        }

        // Проверяем существующий отзыв
        Optional<QuestReview> existingReview = reviewRepository.findByQuestIdAndUserId(questId, userId);

        QuestReview questReview;
        if (existingReview.isPresent()) {
            // Обновляем существующий отзыв
            questReview = existingReview.get();
            questReview.setTitle(title);
            questReview.setContent(content);
            questReview.setRating(rating);
            questReview.setUpdatedAt(LocalDateTime.now());
            log.info("Updated existing review for quest {} by user {}", questId, userId);
        } else {
            // Создаем новый отзыв
            questReview = new QuestReview();
            questReview.setQuestId(questId);
            questReview.setUserId(userId);
            questReview.setTitle(title);
            questReview.setContent(content);
            questReview.setRating(rating);
            questReview.setCreatedAt(LocalDateTime.now());
            questReview.setIsVisible(true);
            log.info("Created new review for quest {} by user {}", questId, userId);
        }

        QuestReview savedReview = reviewRepository.save(questReview);

        // Обновляем рейтинг, если указан
        if (rating != null && rating >= 1 && rating <= 5) {
            rateQuest(questId, userId, rating);
        }

        return convertToDTO(savedReview);
    }

    /**
     * Получить рейтинг пользователя для квеста
     */
    public QuestRatingDTO getUserRating(Long questId, Long userId) {
        log.debug("Getting user {} rating for quest {}", userId, questId);

        return ratingRepository.findByQuestIdAndUserId(questId, userId)
                .map(this::convertToDTO)
                .orElse(null);
    }

    /**
     * Получить отзыв пользователя для квеста
     */
    public QuestReviewDTO getUserReview(Long questId, Long userId) {
        log.debug("Getting user {} review for quest {}", userId, questId);

        return reviewRepository.findByQuestIdAndUserId(questId, userId)
                .map(this::convertToDTO)
                .orElse(null);
    }

    /**
     * Получить все рейтинги квеста
     */
    public List<QuestRatingDTO> getQuestRatings(Long questId) {
        log.debug("Getting all ratings for quest {}", questId);

        return ratingRepository.findByQuestId(questId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Получить все отзывы квеста с пагинацией
     */
    public Page<QuestReviewDTO> getQuestReviews(Long questId, Pageable pageable) {
        log.debug("Getting reviews for quest {} with pagination", questId);

        return reviewRepository.findByQuestIdAndIsVisibleTrue(questId, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Получить статистику рейтингов квеста
     */
    public QuestRatingStats getQuestRatingStats(Long questId) {
        log.debug("Getting rating stats for quest {}", questId);

        List<QuestRating> ratings = ratingRepository.findByQuestId(questId);
        
        if (ratings.isEmpty()) {
            return new QuestRatingStats(0, 0.0, 0, 0, 0, 0, 0);
        }

        int totalRatings = ratings.size();
        double averageRating = ratings.stream()
                .mapToInt(QuestRating::getRating)
                .average()
                .orElse(0.0);

        long count1 = ratings.stream().mapToLong(r -> r.getRating() == 1 ? 1 : 0).sum();
        long count2 = ratings.stream().mapToLong(r -> r.getRating() == 2 ? 1 : 0).sum();
        long count3 = ratings.stream().mapToLong(r -> r.getRating() == 3 ? 1 : 0).sum();
        long count4 = ratings.stream().mapToLong(r -> r.getRating() == 4 ? 1 : 0).sum();
        long count5 = ratings.stream().mapToLong(r -> r.getRating() == 5 ? 1 : 0).sum();

        return new QuestRatingStats(totalRatings, averageRating, count1, count2, count3, count4, count5);
    }

    /**
     * Удалить рейтинг
     */
    @Transactional
    public void deleteRating(Long questId, Long userId) {
        log.info("Deleting rating for quest {} by user {}", questId, userId);

        ratingRepository.deleteByQuestIdAndUserId(questId, userId);
        
        // Обновляем средний рейтинг квеста
        updateQuestAverageRating(questId);
    }

    /**
     * Удалить отзыв
     */
    @Transactional
    public void deleteReview(Long questId, Long userId) {
        log.info("Deleting review for quest {} by user {}", questId, userId);

        reviewRepository.deleteByQuestIdAndUserId(questId, userId);
    }

    /**
     * Скрыть/показать отзыв (для модерации)
     */
    @Transactional
    public void toggleReviewVisibility(Long reviewId, boolean isVisible) {
        log.info("Toggling review {} visibility to {}", reviewId, isVisible);

        QuestReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found with id: " + reviewId));

        review.setIsVisible(isVisible);
        review.setUpdatedAt(LocalDateTime.now());
        
        reviewRepository.save(review);
    }

    /**
     * Получить лучшие квесты по рейтингу
     */
    public List<QuestDTO> getTopRatedQuests(int limit) {
        log.info("Getting top {} rated quests", limit);

        return questRepository.findTopRatedQuests(org.springframework.data.domain.PageRequest.of(0, limit))
                .stream()
                .map(this::convertQuestToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Обновить средний рейтинг квеста
     */
    private void updateQuestAverageRating(Long questId) {
        log.debug("Updating average rating for quest {}", questId);

        List<QuestRating> ratings = ratingRepository.findByQuestId(questId);
        
        if (ratings.isEmpty()) {
            // Если нет рейтингов, устанавливаем 0
            questRepository.updateAverageRating(questId, 0.0);
        } else {
            double averageRating = ratings.stream()
                    .mapToInt(QuestRating::getRating)
                    .average()
                    .orElse(0.0);
            
            questRepository.updateAverageRating(questId, averageRating);
        }
    }

    /**
     * Конвертация Entity в DTO для рейтинга
     */
    private QuestRatingDTO convertToDTO(QuestRating rating) {
        QuestRatingDTO dto = new QuestRatingDTO();
        dto.setId(rating.getId());
        dto.setQuestId(rating.getQuestId());
        dto.setUserId(rating.getUserId());
        dto.setRating(rating.getRating());
        dto.setCreatedAt(rating.getCreatedAt());
        dto.setUpdatedAt(rating.getUpdatedAt());
        return dto;
    }

    /**
     * Конвертация Entity в DTO для отзыва
     */
    private QuestReviewDTO convertToDTO(QuestReview review) {
        QuestReviewDTO dto = new QuestReviewDTO();
        dto.setId(review.getId());
        dto.setQuestId(review.getQuestId());
        dto.setUserId(review.getUserId());
        dto.setTitle(review.getTitle());
        dto.setContent(review.getContent());
        dto.setRating(review.getRating());
        dto.setIsVisible(review.getIsVisible());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setUpdatedAt(review.getUpdatedAt());
        return dto;
    }

    /**
     * Конвертация Entity в DTO для квеста
     */
    private QuestDTO convertQuestToDTO(Quest quest) {
        QuestDTO dto = new QuestDTO();
        dto.setId(quest.getId());
        dto.setNumber(quest.getNumber());
        dto.setTitle(quest.getTitle());
        dto.setDescription(quest.getDescription());
        dto.setDifficulty(quest.getDifficulty());
        dto.setQuestType(quest.getQuestType());
        dto.setCategory(quest.getCategory());
        dto.setEstimatedDuration(quest.getEstimatedDuration());
        dto.setMaxParticipants(quest.getMaxParticipants());
        dto.setMinParticipants(quest.getMinParticipants());
        dto.setStartLocation(quest.getStartLocation());
        dto.setEndLocation(quest.getEndLocation());
        dto.setRules(quest.getRules());
        dto.setPrizes(quest.getPrizes());
        dto.setRequirements(quest.getRequirements());
        dto.setTags(quest.getTags());
        dto.setIsPublic(quest.getIsPublic());
        dto.setIsTemplate(quest.getIsTemplate());
        dto.setAuthorIds(quest.getAuthorIds());
        dto.setStatus(quest.getStatus());
        dto.setVersion(quest.getVersion());
        dto.setStartTime(quest.getStartTime());
        dto.setEndTime(quest.getEndTime());
        dto.setCreatedAt(quest.getCreatedAt());
        dto.setUpdatedAt(quest.getUpdatedAt());
        dto.setPublishedAt(quest.getPublishedAt());
        dto.setArchivedAt(quest.getArchivedAt());
        dto.setArchiveReason(quest.getArchiveReason());
        dto.setAverageRating(quest.getAverageRating());
        return dto;
    }

    /**
     * Статистика рейтингов
     */
    public static class QuestRatingStats {
        private final int totalRatings;
        private final double averageRating;
        private final long count1;
        private final long count2;
        private final long count3;
        private final long count4;
        private final long count5;

        public QuestRatingStats(int totalRatings, double averageRating, long count1, long count2, long count3, long count4, long count5) {
            this.totalRatings = totalRatings;
            this.averageRating = averageRating;
            this.count1 = count1;
            this.count2 = count2;
            this.count3 = count3;
            this.count4 = count4;
            this.count5 = count5;
        }

        public int getTotalRatings() { return totalRatings; }
        public double getAverageRating() { return averageRating; }
        public long getCount1() { return count1; }
        public long getCount2() { return count2; }
        public long getCount3() { return count3; }
        public long getCount4() { return count4; }
        public long getCount5() { return count5; }
    }
}