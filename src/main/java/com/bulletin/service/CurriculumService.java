package com.bulletin.service;

import com.bulletin.dto.curriculum.CurriculumRequest;
import com.bulletin.dto.curriculum.CurriculumResponse;
import com.bulletin.entity.*;
import com.bulletin.exception.ResourceNotFoundException;
import com.bulletin.mapper.CurriculumMapper;
import com.bulletin.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CurriculumService {

    private final CurriculumRepository curriculumRepository;
    private final LevelRepository levelRepository;
    private final SectionRepository sectionRepository;
    private final OptionRepository optionRepository;
    private final CurriculumMapper curriculumMapper;

    @Transactional
    public CurriculumResponse createCurriculum(CurriculumRequest request) {
        Curriculum curriculum = curriculumMapper.toEntity(request);
        if (request.getLevelId() != null) {
            curriculum.setLevel(findLevel(request.getLevelId()));
        }
        if (request.getSectionId() != null) {
            curriculum.setSection(findSection(request.getSectionId()));
        }
        if (request.getOptionId() != null) {
            curriculum.setOption(findOption(request.getOptionId()));
        }
        Curriculum saved = curriculumRepository.save(curriculum);
        log.info("Programme créé: {}", saved.getId());
        return curriculumMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public CurriculumResponse getCurriculum(Long id) {
        return curriculumMapper.toResponse(findById(id));
    }

    @Transactional(readOnly = true)
    public List<CurriculumResponse> getAllCurricula() {
        return curriculumRepository.findAll().stream()
                .map(curriculumMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CurriculumResponse> getCurriculaByLevel(Long levelId) {
        return curriculumRepository.findByLevelId(levelId).stream()
                .map(curriculumMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CurriculumResponse> getCurriculaBySection(Long sectionId) {
        return curriculumRepository.findBySectionId(sectionId).stream()
                .map(curriculumMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CurriculumResponse> getCurriculaByOption(Long optionId) {
        return curriculumRepository.findByOptionId(optionId).stream()
                .map(curriculumMapper::toResponse)
                .toList();
    }

    @Transactional
    public CurriculumResponse updateCurriculum(Long id, CurriculumRequest request) {
        Curriculum curriculum = findById(id);
        curriculumMapper.updateEntity(request, curriculum);
        if (request.getLevelId() != null) {
            curriculum.setLevel(findLevel(request.getLevelId()));
        }
        if (request.getSectionId() != null) {
            curriculum.setSection(findSection(request.getSectionId()));
        }
        if (request.getOptionId() != null) {
            curriculum.setOption(findOption(request.getOptionId()));
        }
        Curriculum saved = curriculumRepository.save(curriculum);
        log.info("Programme mis à jour: {}", saved.getId());
        return curriculumMapper.toResponse(saved);
    }

    @Transactional
    public void deleteCurriculum(Long id) {
        Curriculum curriculum = findById(id);
        curriculum.setDeletedAt(java.time.LocalDateTime.now());
        curriculumRepository.save(curriculum);
        log.info("Programme supprimé (soft): {}", id);
    }

    public Curriculum findById(Long id) {
        return curriculumRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Programme non trouvé avec l'ID: " + id));
    }

    private Level findLevel(Long id) {
        return levelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Niveau non trouvé avec l'ID: " + id));
    }

    private Section findSection(Long id) {
        return sectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Section non trouvée avec l'ID: " + id));
    }

    private Option findOption(Long id) {
        return optionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Option non trouvée avec l'ID: " + id));
    }
}
