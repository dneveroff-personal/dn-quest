package dn.quest.questmanagement.service.impl;

import dn.quest.questmanagement.dto.CodeDTO;
import dn.quest.questmanagement.entity.Code;
import dn.quest.questmanagement.entity.CodeType;
import dn.quest.questmanagement.repository.CodeRepository;
import dn.quest.questmanagement.repository.LevelRepository;
import dn.quest.questmanagement.service.CodeService;
import dn.quest.questmanagement.exception.CodeNotFoundException;
import dn.quest.questmanagement.exception.LevelNotFoundException;
import dn.quest.questmanagement.exception.CodeValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Реализация сервиса для управления кодами уровней
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CodeServiceImpl implements CodeService {

    private final CodeRepository codeRepository;
    private final LevelRepository levelRepository;

    @Override
    @Transactional
    public CodeDTO createCode(CodeDTO dto, Long levelId) {
        log.info("Creating new code for level with ID: {}", levelId);

        // Проверка существования уровня
        if (!levelRepository.existsById(levelId)) {
            throw new LevelNotFoundException("Level not found with ID: " + levelId);
        }

        // Валидация DTO
        dto.validate();

        // Проверка уникальности кода в рамках квеста
        Long questId = getQuestIdByLevelId(levelId);
        if (!isCodeUniqueInQuest(questId, dto.getValue(), null)) {
            throw new CodeValidationException("Code value must be unique within the quest");
        }

        // Создание нового кода
        Code code = new Code();
        code.setLevelId(levelId);
        code.setValue(dto.getValue());
        code.setType(dto.getType());
        code.setHint(dto.getHint());
        code.setPoints(dto.getPoints());
        code.setActive(dto.getActive() != null ? dto.getActive() : true);
        code.setUsageLimit(dto.getUsageLimit());
        code.setUsageCount(0);
        code.setUsedBy(new HashSet<>());
        code.setCreatedAt(LocalDateTime.now());
        code.setUpdatedAt(LocalDateTime.now());

        Code savedCode = codeRepository.save(code);
        log.info("Code created successfully with ID: {}", savedCode.getId());

        return convertToDTO(savedCode);
    }

    @Override
    @Transactional
    public CodeDTO updateCode(Long id, CodeDTO dto) {
        log.info("Updating code with ID: {}", id);

        Code code = getCodeEntityById(id);
        
        // Валидация DTO
        dto.validate();

        // Проверка уникальности кода в рамках квеста (если значение изменилось)
        if (!code.getValue().equals(dto.getValue())) {
            Long questId = getQuestIdByLevelId(code.getLevelId());
            if (!isCodeUniqueInQuest(questId, dto.getValue(), id)) {
                throw new CodeValidationException("Code value must be unique within the quest");
            }
        }

        // Обновление полей
        code.setValue(dto.getValue());
        code.setType(dto.getType());
        code.setHint(dto.getHint());
        code.setPoints(dto.getPoints());
        if (dto.getActive() != null) {
            code.setActive(dto.getActive());
        }
        code.setUsageLimit(dto.getUsageLimit());
        code.setUpdatedAt(LocalDateTime.now());

        Code savedCode = codeRepository.save(code);
        log.info("Code updated successfully with ID: {}", savedCode.getId());

        return convertToDTO(savedCode);
    }

    @Override
    @Transactional
    public void deleteCode(Long id) {
        log.info("Deleting code with ID: {}", id);

        Code code = getCodeEntityById(id);
        codeRepository.delete(code);
        log.info("Code deleted successfully with ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public CodeDTO getCodeById(Long id) {
        Code code = getCodeEntityById(id);
        return convertToDTO(code);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CodeDTO> getCodesByLevelId(Long levelId) {
        List<Code> codes = codeRepository.findByLevelId(levelId);
        return codes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CodeDTO> getActiveCodesByLevelId(Long levelId) {
        List<Code> codes = codeRepository.findByLevelIdAndActive(levelId, true);
        return codes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CodeDTO> getCodesByType(Long levelId, CodeType type) {
        List<Code> codes = codeRepository.findByLevelIdAndType(levelId, type);
        return codes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CodeValidationResult validateCode(Long levelId, String codeValue) {
        log.info("Validating code '{}' for level with ID: {}", codeValue, levelId);

        // Поиск активного кода с указанным значением
        Optional<Code> codeOpt = codeRepository.findByLevelIdAndValueAndActive(levelId, codeValue, true);
        
        if (codeOpt.isEmpty()) {
            return new CodeValidationResult(false, "Invalid code", null);
        }

        Code code = codeOpt.get();

        // Проверка лимита использования
        if (code.getUsageLimit() != null && code.getUsageCount() >= code.getUsageLimit()) {
            return new CodeValidationResult(false, "Code usage limit exceeded", code.getId());
        }

        return new CodeValidationResult(true, "Code is valid", code.getId());
    }

    @Override
    @Transactional
    public CodeUsageResult useCode(Long codeId, Long userId) {
        log.info("Using code with ID: {} by user: {}", codeId, userId);

        Code code = getCodeEntityById(codeId);

        // Проверка, что код активен
        if (!code.getActive()) {
            return new CodeUsageResult(false, "Code is not active", false);
        }

        // Проверка, что пользователь еще не использовал этот код
        boolean wasAlreadyUsed = code.getUsedBy().contains(userId);
        if (wasAlreadyUsed) {
            return new CodeUsageResult(false, "Code already used by this user", true);
        }

        // Проверка лимита использования
        if (code.getUsageLimit() != null && code.getUsageCount() >= code.getUsageLimit()) {
            return new CodeUsageResult(false, "Code usage limit exceeded", false);
        }

        // Использование кода
        code.getUsedBy().add(userId);
        code.setUsageCount(code.getUsageCount() + 1);
        code.setUpdatedAt(LocalDateTime.now());

        codeRepository.save(code);

        log.info("Code used successfully with ID: {} by user: {}", codeId, userId);

        return new CodeUsageResult(true, "Code used successfully", false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CodeDTO> getUnusedCodesByLevelId(Long levelId, Long userId) {
        List<Code> codes = codeRepository.findByLevelIdAndActive(levelId, true);
        return codes.stream()
                .filter(code -> !code.getUsedBy().contains(userId))
                .filter(code -> code.getUsageLimit() == null || code.getUsageCount() < code.getUsageLimit())
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CodeDTO> getUsedCodesByLevelId(Long levelId, Long userId) {
        List<Code> codes = codeRepository.findByLevelIdAndActive(levelId, true);
        return codes.stream()
                .filter(code -> code.getUsedBy().contains(userId))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isCodeUniqueInQuest(Long questId, String codeValue, Long excludeCodeId) {
        List<Code> existingCodes = codeRepository.findByQuestIdAndValue(questId, codeValue);
        
        if (excludeCodeId == null) {
            return existingCodes.isEmpty();
        }
        
        return existingCodes.stream()
                .allMatch(code -> code.getId().equals(excludeCodeId));
    }

    @Override
    @Transactional
    public void copyCodesForLevel(Long sourceLevelId, Long targetLevelId) {
        log.info("Copying codes from level {} to level {}", sourceLevelId, targetLevelId);

        List<Code> sourceCodes = codeRepository.findByLevelId(sourceLevelId);
        
        for (Code sourceCode : sourceCodes) {
            Code copy = new Code();
            copy.setLevelId(targetLevelId);
            copy.setValue(sourceCode.getValue());
            copy.setType(sourceCode.getType());
            copy.setHint(sourceCode.getHint());
            copy.setPoints(sourceCode.getPoints());
            copy.setActive(sourceCode.getActive());
            copy.setUsageLimit(sourceCode.getUsageLimit());
            copy.setUsageCount(0);
            copy.setUsedBy(new HashSet<>());
            copy.setCreatedAt(LocalDateTime.now());
            copy.setUpdatedAt(LocalDateTime.now());

            codeRepository.save(copy);
        }

        log.info("Codes copied successfully from level {} to level {}", sourceLevelId, targetLevelId);
    }

    @Override
    @Transactional(readOnly = true)
    public CodeUsageStatistics getCodeUsageStatistics(Long levelId) {
        List<Code> codes = codeRepository.findByLevelId(levelId);
        
        long totalCodes = codes.size();
        long activeCodes = codes.stream().filter(Code::getActive).count();
        long usedCodes = codes.stream().filter(code -> code.getUsageCount() > 0).count();
        long unusedCodes = totalCodes - usedCodes;
        
        Set<Long> uniqueUsers = codes.stream()
                .flatMap(code -> code.getUsedBy().stream())
                .collect(Collectors.toSet());
        long uniqueUsersCount = uniqueUsers.size();

        return new CodeUsageStatistics(totalCodes, activeCodes, usedCodes, unusedCodes, uniqueUsersCount);
    }

    @Override
    @Transactional
    public void resetCodeUsage(Long levelId) {
        log.info("Resetting code usage for level with ID: {}", levelId);

        List<Code> codes = codeRepository.findByLevelId(levelId);
        
        for (Code code : codes) {
            code.setUsageCount(0);
            code.setUsedBy(new HashSet<>());
            code.setUpdatedAt(LocalDateTime.now());
        }

        codeRepository.saveAll(codes);
        
        log.info("Code usage reset successfully for level with ID: {}", levelId);
    }

    @Override
    @Transactional
    public CodeDTO toggleCodeActive(Long codeId, boolean active) {
        log.info("Toggling code with ID: {} to active: {}", codeId, active);

        Code code = getCodeEntityById(codeId);
        code.setActive(active);
        code.setUpdatedAt(LocalDateTime.now());

        Code savedCode = codeRepository.save(code);
        log.info("Code active status changed successfully with ID: {}", savedCode.getId());

        return convertToDTO(savedCode);
    }

    // Вспомогательные методы

    private Code getCodeEntityById(Long id) {
        return codeRepository.findById(id)
                .orElseThrow(() -> new CodeNotFoundException("Code not found with ID: " + id));
    }

    private Long getQuestIdByLevelId(Long levelId) {
        return levelRepository.findById(levelId)
                .map(level -> level.getQuestId())
                .orElseThrow(() -> new LevelNotFoundException("Level not found with ID: " + levelId));
    }

    private CodeDTO convertToDTO(Code code) {
        CodeDTO dto = new CodeDTO();
        dto.setId(code.getId());
        dto.setLevelId(code.getLevelId());
        dto.setValue(code.getValue());
        dto.setType(code.getType());
        dto.setHint(code.getHint());
        dto.setPoints(code.getPoints());
        dto.setActive(code.getActive());
        dto.setUsageLimit(code.getUsageLimit());
        dto.setUsageCount(code.getUsageCount());
        dto.setUsedBy(code.getUsedBy());
        dto.setCreatedAt(code.getCreatedAt());
        dto.setUpdatedAt(code.getUpdatedAt());
        
        return dto;
    }
}