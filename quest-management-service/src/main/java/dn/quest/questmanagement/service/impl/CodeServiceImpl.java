package dn.quest.questmanagement.service.impl;

import dn.quest.questmanagement.dto.CodeDTO;
import dn.quest.questmanagement.entity.Code;
import dn.quest.questmanagement.repository.CodeRepository;
import dn.quest.questmanagement.repository.LevelRepository;
import dn.quest.questmanagement.service.CodeService;
import dn.quest.questmanagement.exception.CodeNotFoundException;
import dn.quest.questmanagement.exception.LevelNotFoundException;
import dn.quest.questmanagement.exception.CodeValidationException;
import dn.quest.shared.enums.CodeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
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
        dto.isValid();

        // Создание нового кода
        Code code = new Code();
        code.setLevelId(levelId);
        code.setCodeValue(dto.getCodeValue());
        code.setCodeType(dto.getCodeType());
        code.setActive(dto.getActive() != null ? dto.getActive() : true);
        code.setUsageCount(0);
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
        dto.isValid();

        // Обновление полей
        code.setCodeValue(dto.getCodeValue());
        code.setCodeType(dto.getCodeType());
        if (dto.getActive() != null) {
            code.setActive(dto.getActive());
        }
        code.setUsageCount(dto.getUsageCount());
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
    public List<CodeDTO> getCodesByType(Long levelId, CodeType type) {
        List<Code> codes = codeRepository.findByLevelIdAndCodeType(levelId, type);
        return codes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CodeValidationResult validateCode(Long levelId, String codeValue) {
        log.info("Validating code '{}' for level with ID: {}", codeValue, levelId);

        // Поиск активного кода с указанным значением
        Optional<Code> codeOpt = codeRepository.findByLevelIdAndCodeValue(levelId, codeValue);

        if (codeOpt.isEmpty()) {
            return new CodeValidationResult(false, "Invalid code", null);
        }

        Code code = codeOpt.get();

        // Проверка лимита использования
        if (code.hasUsageLimit() && !code.canBeUsed()) {
            return new CodeValidationResult(false, "Code usage limit exceeded", code.getId());
        }

        return new CodeValidationResult(true, "Code is valid", code.getId());
    }

    @Override
    @Transactional
    public void copyCodesForLevel(Long sourceLevelId, Long targetLevelId) {
        log.info("Copying codes from level {} to level {}", sourceLevelId, targetLevelId);

        List<Code> sourceCodes = codeRepository.findByLevelId(sourceLevelId);

        for (Code sourceCode : sourceCodes) {
            Code copy = new Code();
            copy.setLevelId(targetLevelId);
            copy.setCodeValue(sourceCode.getCodeValue());
            copy.setCodeType(sourceCode.getCodeType());
            copy.setActive(sourceCode.getActive());
            copy.setUsageCount(0);
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
        // Todo: Надо связать сущность кода с юзером, чтобы было понятно какой юзер код ввел.
        long uniqueUsersCount = 0;

        return new CodeUsageStatistics(totalCodes, activeCodes, usedCodes, unusedCodes, uniqueUsersCount);
    }

    @Override
    @Transactional
    public void resetCodeUsage(Long levelId) {
        log.info("Resetting code usage for level with ID: {}", levelId);

        List<Code> codes = codeRepository.findByLevelId(levelId);

        for (Code code : codes) {
            code.setUsageCount(0);
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
        dto.setCodeValue(code.getCodeValue());
        dto.setCodeType(code.getCodeType());
        dto.setActive(code.getActive());
        dto.setUsageCount(code.getUsageCount());
        dto.setUsageCount(code.getUsageCount());
        dto.setCreatedAt(code.getCreatedAt().toInstant(ZoneOffset.UTC));
        dto.setUpdatedAt(code.getUpdatedAt().toInstant(ZoneOffset.UTC));

        return dto;
    }
}