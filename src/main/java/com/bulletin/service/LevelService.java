package com.bulletin.service;

import com.bulletin.dto.school.LevelRequest;
import com.bulletin.dto.school.LevelResponse;
import com.bulletin.entity.Level;
import com.bulletin.mapper.LevelMapper;
import com.bulletin.repository.LevelRepository;
import com.bulletin.security.SecurityUtils;
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
    private final SecurityUtils securityUtils;
    private final AutoOrdreService autoOrdreService;

    private boolean isSuperAdmin() {
        return securityUtils.isSuperAdmin();
    }

    private Long requireSchoolId() {
        Long schoolId = securityUtils.getCurrentSchoolId();
        if (schoolId == null) {
            throw new SecurityException("École non définie pour l'utilisateur connecté");
        }
        return schoolId;
    }

    @Transactional
    public LevelResponse createLevel(LevelRequest request) {
        Long schoolId = requireSchoolId();
        // L'ordre est calculé côté serveur (max + 1) : la valeur fournie par le client est ignorée.
        LevelResponse response = AutoOrdreRetry.retry(autoOrdreService,
                () -> levelRepository.maxOrdreBySchoolId(schoolId),
                ordre -> {
                    Level level = levelMapper.toEntity(request);
                    level.setSchoolId(schoolId);
                    level.setOrdre(ordre);
                    Level saved = levelRepository.save(level);
                    log.info("Niveau créé: {}", saved.getId());
                    return levelMapper.toResponse(saved);
                });
        return response;
    }

    @Transactional(readOnly = true)
    public LevelResponse getLevel(Long id) {
        return levelMapper.toResponse(findById(id));
    }

    @Transactional(readOnly = true)
    public List<LevelResponse> getAccessibleLevels() {
        if (isSuperAdmin()) {
            return levelRepository.findAll().stream()
                    .map(levelMapper::toResponse)
                    .toList();
        }
        return levelRepository.findBySchoolId(requireSchoolId()).stream()
                .map(levelMapper::toResponse)
                .toList();
    }

    @Transactional
    public LevelResponse updateLevel(Long id, LevelRequest request) {
        Level level = findById(id);
        Integer existingOrdre = level.getOrdre();
        levelMapper.updateEntity(request, level);
        level.setOrdre(existingOrdre); // l'ordre est immuable après création
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
        Level level = levelRepository.findById(id)
                .orElseThrow(() -> new com.bulletin.exception.ResourceNotFoundException("Niveau non trouvé avec l'ID: " + id));
        securityUtils.assertSchoolAccess(level.getSchoolId());
        return level;
    }
}
