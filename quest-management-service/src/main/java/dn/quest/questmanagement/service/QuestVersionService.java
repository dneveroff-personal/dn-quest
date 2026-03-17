package dn.quest.questmanagement.service;

import dn.quest.questmanagement.dto.QuestDTO;
import dn.quest.questmanagement.dto.QuestCreateUpdateDTO;
import dn.quest.questmanagement.entity.QuestStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Сервис для управления версиями квестов
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuestVersionService {

    private final QuestService questService;
    private final LevelService levelService;
    private final CodeService codeService;
    private final LevelHintService levelHintService;

    /**
     * Создать новую версию квеста
     */
    @Transactional
    public QuestVersionDTO createNewVersion(Long questId, String versionDescription, Long userId) {
        log.info("Creating new version for quest: {} by user: {}", questId, userId);

        QuestDTO currentQuest = questService.getQuestById(questId);
        
        // Проверка прав
        if (!questService.canEditQuest(questId, userId)) {
            throw new RuntimeException("User does not have permission to create version of this quest");
        }

        // Создание новой версии
        QuestCreateUpdateDTO newVersionDTO = new QuestCreateUpdateDTO();
        newVersionDTO.setTitle(currentQuest.getTitle());
        newVersionDTO.setDescription(currentQuest.getDescription());
        newVersionDTO.setDifficulty(currentQuest.getDifficulty());
        newVersionDTO.setQuestType(currentQuest.getQuestType());
        newVersionDTO.setCategory(currentQuest.getCategory());
        newVersionDTO.setEstimatedDuration(currentQuest.getEstimatedDuration());
        newVersionDTO.setMaxParticipants(currentQuest.getMaxParticipants());
        newVersionDTO.setMinParticipants(currentQuest.getMinParticipants());
        newVersionDTO.setStartLocation(currentQuest.getStartLocation());
        newVersionDTO.setEndLocation(currentQuest.getEndLocation());
        newVersionDTO.setRules(currentQuest.getRules());
        newVersionDTO.setPrizes(currentQuest.getPrizes());
        newVersionDTO.setRequirements(currentQuest.getRequirements());
        newVersionDTO.setTags(currentQuest.getTags());
        newVersionDTO.setIsPublic(currentQuest.getIsPublic());
        newVersionDTO.setIsTemplate(false); // Версии не могут быть шаблонами

        QuestDTO newVersion = questService.createQuest(newVersionDTO, userId);
        
        // Копирование уровней, кодов и подсказок
        copyQuestContent(questId, newVersion.getId());
        
        // Создание записи о версии
        QuestVersionRecord versionRecord = new QuestVersionRecord();
        versionRecord.setQuestId(questId);
        versionRecord.setVersionId(newVersion.getId());
        versionRecord.setVersionNumber(newVersion.getVersion());
        versionRecord.setDescription(versionDescription);
        versionRecord.setCreatedBy(userId);
        versionRecord.setCreatedAt(LocalDateTime.now());
        
        // В реальном приложении здесь было бы сохранение в базу данных
        
        log.info("New version created successfully: {} for quest: {}", newVersion.getId(), questId);
        
        return convertToVersionDTO(newVersion, versionRecord);
    }

    /**
     * Получить все версии квеста
     */
    @Transactional(readOnly = true)
    public List<QuestVersionDTO> getQuestVersions(Long questId) {
        log.info("Getting versions for quest: {}", questId);
        
        List<QuestVersionDTO> versions = new ArrayList<>();
        
        // В реальном приложении здесь был бы запрос к базе данных для получения версий
        // Для примера возвращаем текущую версию
        try {
            QuestDTO currentQuest = questService.getQuestById(questId);
            QuestVersionRecord versionRecord = new QuestVersionRecord();
            versionRecord.setQuestId(questId);
            versionRecord.setVersionId(currentQuest.getId());
            versionRecord.setVersionNumber(currentQuest.getVersion());
            versionRecord.setDescription("Current version");
            versionRecord.setCreatedBy(currentQuest.getAuthorIds().iterator().next());
            versionRecord.setCreatedAt(currentQuest.getCreatedAt());
            
            versions.add(convertToVersionDTO(currentQuest, versionRecord));
        } catch (Exception e) {
            log.error("Error getting versions for quest: {}", questId, e);
        }
        
        return versions;
    }

    /**
     * Сравнить две версии квеста
     */
    @Transactional(readOnly = true)
    public QuestVersionComparisonDTO compareVersions(Long questId, Long version1Id, Long version2Id) {
        log.info("Comparing versions {} and {} for quest: {}", version1Id, version2Id, questId);
        
        try {
            QuestDTO version1 = questService.getQuestById(version1Id);
            QuestDTO version2 = questService.getQuestById(version2Id);
            
            QuestVersionComparisonDTO comparison = new QuestVersionComparisonDTO();
            comparison.setQuestId(questId);
            comparison.setVersion1Id(version1Id);
            comparison.setVersion2Id(version2Id);
            comparison.setVersion1Number(version1.getVersion());
            comparison.setVersion2Number(version2.getVersion());
            
            // Сравнение основных полей
            List<String> differences = new ArrayList<>();
            
            if (!version1.getTitle().equals(version2.getTitle())) {
                differences.add("Title changed from '" + version1.getTitle() + "' to '" + version2.getTitle() + "'");
            }
            
            if (!version1.getDescription().equals(version2.getDescription())) {
                differences.add("Description changed");
            }
            
            if (!version1.getDifficulty().equals(version2.getDifficulty())) {
                differences.add("Difficulty changed from '" + version1.getDifficulty() + "' to '" + version2.getDifficulty() + "'");
            }
            
            // Сравнение уровней
            List<LevelDTO> levels1 = levelService.getLevelsByQuestId(version1Id);
            List<LevelDTO> levels2 = levelService.getLevelsByQuestId(version2Id);
            
            if (levels1.size() != levels2.size()) {
                differences.add("Number of levels changed from " + levels1.size() + " to " + levels2.size());
            }
            
            comparison.setDifferences(differences);
            
            return comparison;
            
        } catch (Exception e) {
            log.error("Error comparing versions {} and {} for quest: {}", version1Id, version2Id, questId, e);
            throw new RuntimeException("Error comparing versions", e);
        }
    }

    /**
     * Восстановить квест из версии
     */
    @Transactional
    public QuestDTO restoreFromVersion(Long questId, Long versionId, Long userId) {
        log.info("Restoring quest {} from version {} by user: {}", questId, versionId, userId);
        
        // Проверка прав
        if (!questService.canEditQuest(questId, userId)) {
            throw new RuntimeException("User does not have permission to restore this quest");
        }
        
        QuestDTO versionQuest = questService.getQuestById(versionId);
        QuestDTO currentQuest = questService.getQuestById(questId);
        
        // Создание резервной копии текущей версии
        createBackupVersion(questId, "Backup before restore from version " + versionId, userId);
        
        // Обновление текущего квеста данными из версии
        QuestCreateUpdateDTO updateDTO = new QuestCreateUpdateDTO();
        updateDTO.setTitle(versionQuest.getTitle());
        updateDTO.setDescription(versionQuest.getDescription());
        updateDTO.setDifficulty(versionQuest.getDifficulty());
        updateDTO.setQuestType(versionQuest.getQuestType());
        updateDTO.setCategory(versionQuest.getCategory());
        updateDTO.setEstimatedDuration(versionQuest.getEstimatedDuration());
        updateDTO.setMaxParticipants(versionQuest.getMaxParticipants());
        updateDTO.setMinParticipants(versionQuest.getMinParticipants());
        updateDTO.setStartLocation(versionQuest.getStartLocation());
        updateDTO.setEndLocation(versionQuest.getEndLocation());
        updateDTO.setRules(versionQuest.getRules());
        updateDTO.setPrizes(versionQuest.getPrizes());
        updateDTO.setRequirements(versionQuest.getRequirements());
        updateDTO.setTags(versionQuest.getTags());
        updateDTO.setIsPublic(versionQuest.getIsPublic());
        updateDTO.setIsTemplate(currentQuest.getIsTemplate()); // Сохраняем флаг шаблона
        
        // Удаление текущих уровней
        List<LevelDTO> currentLevels = levelService.getLevelsByQuestId(questId);
        for (LevelDTO level : currentLevels) {
            levelService.deleteLevel(level.getId());
        }
        
        // Восстановление уровней из версии
        copyQuestContent(versionId, questId);
        
        // Обновление квеста
        QuestDTO restoredQuest = questService.updateQuest(questId, updateDTO, userId);
        
        log.info("Quest restored successfully from version: {}", versionId);
        
        return restoredQuest;
    }

    /**
     * Создать резервную копию версии
     */
    private void createBackupVersion(Long questId, String description, Long userId) {
        try {
            QuestDTO currentQuest = questService.getQuestById(questId);
            
            QuestCreateUpdateDTO backupDTO = new QuestCreateUpdateDTO();
            backupDTO.setTitle(currentQuest.getTitle() + " (Backup)");
            backupDTO.setDescription(currentQuest.getDescription());
            backupDTO.setDifficulty(currentQuest.getDifficulty());
            backupDTO.setQuestType(currentQuest.getQuestType());
            backupDTO.setCategory(currentQuest.getCategory());
            backupDTO.setEstimatedDuration(currentQuest.getEstimatedDuration());
            backupDTO.setMaxParticipants(currentQuest.getMaxParticipants());
            backupDTO.setMinParticipants(currentQuest.getMinParticipants());
            backupDTO.setStartLocation(currentQuest.getStartLocation());
            backupDTO.setEndLocation(currentQuest.getEndLocation());
            backupDTO.setRules(currentQuest.getRules());
            backupDTO.setPrizes(currentQuest.getPrizes());
            backupDTO.setRequirements(currentQuest.getRequirements());
            backupDTO.setTags(currentQuest.getTags());
            backupDTO.setIsPublic(false); // Резервные копии не публичные
            backupDTO.setIsTemplate(false);
            
            QuestDTO backup = questService.createQuest(backupDTO, userId);
            copyQuestContent(questId, backup.getId());
            
            log.info("Backup version created: {} for quest: {}", backup.getId(), questId);
            
        } catch (Exception e) {
            log.error("Error creating backup version for quest: {}", questId, e);
        }
    }

    /**
     * Копировать содержимое квеста (уровни, коды, подсказки)
     */
    private void copyQuestContent(Long sourceQuestId, Long targetQuestId) {
        levelService.copyLevelsForQuest(sourceQuestId, targetQuestId);
    }

    /**
     * Конвертация в DTO версии
     */
    private QuestVersionDTO convertToVersionDTO(QuestDTO quest, QuestVersionRecord record) {
        QuestVersionDTO dto = new QuestVersionDTO();
        dto.setVersionId(quest.getId());
        dto.setQuestId(record.getQuestId());
        dto.setVersionNumber(record.getVersionNumber());
        dto.setDescription(record.getDescription());
        dto.setCreatedBy(record.getCreatedBy());
        dto.setCreatedAt(record.getCreatedAt());
        dto.setTitle(quest.getTitle());
        dto.setStatus(quest.getStatus());
        dto.setIsCurrent(quest.getStatus() != QuestStatus.ARCHIVED);
        
        return dto;
    }

    /**
     * Запись о версии квеста
     */
    private static class QuestVersionRecord {
        private Long questId;
        private Long versionId;
        private Integer versionNumber;
        private String description;
        private Long createdBy;
        private LocalDateTime createdAt;

        // Getters and setters
        public Long getQuestId() { return questId; }
        public void setQuestId(Long questId) { this.questId = questId; }

        public Long getVersionId() { return versionId; }
        public void setVersionId(Long versionId) { this.versionId = versionId; }

        public Integer getVersionNumber() { return versionNumber; }
        public void setVersionNumber(Integer versionNumber) { this.versionNumber = versionNumber; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public Long getCreatedBy() { return createdBy; }
        public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    /**
     * DTO версии квеста
     */
    public static class QuestVersionDTO {
        private Long versionId;
        private Long questId;
        private Integer versionNumber;
        private String description;
        private Long createdBy;
        private LocalDateTime createdAt;
        private String title;
        private String status;
        private Boolean isCurrent;

        // Getters and setters
        public Long getVersionId() { return versionId; }
        public void setVersionId(Long versionId) { this.versionId = versionId; }

        public Long getQuestId() { return questId; }
        public void setQuestId(Long questId) { this.questId = questId; }

        public Integer getVersionNumber() { return versionNumber; }
        public void setVersionNumber(Integer versionNumber) { this.versionNumber = versionNumber; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public Long getCreatedBy() { return createdBy; }
        public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public Boolean getIsCurrent() { return isCurrent; }
        public void setIsCurrent(Boolean isCurrent) { this.isCurrent = isCurrent; }
    }

    /**
     * DTO сравнения версий
     */
    public static class QuestVersionComparisonDTO {
        private Long questId;
        private Long version1Id;
        private Long version2Id;
        private Integer version1Number;
        private Integer version2Number;
        private List<String> differences;

        // Getters and setters
        public Long getQuestId() { return questId; }
        public void setQuestId(Long questId) { this.questId = questId; }

        public Long getVersion1Id() { return version1Id; }
        public void setVersion1Id(Long version1Id) { this.version1Id = version1Id; }

        public Long getVersion2Id() { return version2Id; }
        public void setVersion2Id(Long version2Id) { this.version2Id = version2Id; }

        public Integer getVersion1Number() { return version1Number; }
        public void setVersion1Number(Integer version1Number) { this.version1Number = version1Number; }

        public Integer getVersion2Number() { return version2Number; }
        public void setVersion2Number(Integer version2Number) { this.version2Number = version2Number; }

        public List<String> getDifferences() { return differences; }
        public void setDifferences(List<String> differences) { this.differences = differences; }
    }
}