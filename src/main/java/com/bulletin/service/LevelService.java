package com.bulletin.service;

import com.bulletin.dto.school.LevelRequest;
import com.bulletin.dto.school.LevelResponse;
import com.bulletin.entity.Level;
import com.bulletin.mapper.LevelMapper;
import com.bulletin.repository.LevelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LevelService {

    private final LevelRepository levelRepository;
    private final LevelMapper levelMapper;

    @Transactional
    public LevelResponse createLevel(LevelRequest request) {
        Level level = levelMapper.toEntity(request);
        Level saved = levelRepository.save(level);
        log.info("Niveau créé: {}", saved.getId());
        return levelMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public LevelResponse getLevel(Long id) {
        return levelMapper.toResponse(findById(id));
    }

    @Transactional(readOnly = true)
    public List<LevelResponse> getAllLevels() {
        return levelRepository.findAll().stream()
                .map(levelMapper::toResponse)
                .toList();
    }

    @Transactional
    public LevelResponse updateLevel(Long id, LevelRequest request) {
        Level level = findById(id);
        levelMapper.updateEntity(request, level);
        Level saved = levelRepository.save(level);
        log.info("Niveau mis à jour: {}", saved.getId());
        return levelMapper.toResponse(saved);
    }

    @Transactional
    public void deleteLevel(Long id) {
        Level level = findById(id);
        level.setDeletedAt(java.time.LocalDateTime.now());
        levelRepository.save(level);
        log.info("Niveau supprimé (soft): {}", id);
    }

    public Level findById(Long id) {
        return levelRepository.findById(id)
                .orElseThrow(() -> new com.bulletin.exception.ResourceNotFoundException("Niveau non trouvé avec l'ID: " + id));
    }
}
