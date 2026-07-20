package com.bulletin.service;

import com.bulletin.dto.grade.AssessmentTypeRequest;
import com.bulletin.dto.grade.AssessmentTypeResponse;
import com.bulletin.entity.AssessmentType;
import com.bulletin.exception.ResourceNotFoundException;
import com.bulletin.mapper.AssessmentTypeMapper;
import com.bulletin.repository.AssessmentTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssessmentTypeService {

    private final AssessmentTypeRepository assessmentTypeRepository;
    private final AssessmentTypeMapper assessmentTypeMapper;

    @Transactional
    public AssessmentTypeResponse createAssessmentType(AssessmentTypeRequest request) {
        AssessmentType assessmentType = assessmentTypeMapper.toEntity(request);
        AssessmentType saved = assessmentTypeRepository.save(assessmentType);
        log.info("Type d'évaluation créé: {}", saved.getId());
        return assessmentTypeMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public AssessmentTypeResponse getAssessmentType(Long id) {
        return assessmentTypeMapper.toResponse(findById(id));
    }

    @Transactional(readOnly = true)
    public List<AssessmentTypeResponse> getAllAssessmentTypes() {
        return assessmentTypeRepository.findAll().stream()
                .map(assessmentTypeMapper::toResponse)
                .toList();
    }

    @Transactional
    public AssessmentTypeResponse updateAssessmentType(Long id, AssessmentTypeRequest request) {
        AssessmentType assessmentType = findById(id);
        assessmentTypeMapper.updateEntity(request, assessmentType);
        AssessmentType saved = assessmentTypeRepository.save(assessmentType);
        log.info("Type d'évaluation mis à jour: {}", saved.getId());
        return assessmentTypeMapper.toResponse(saved);
    }

    @Transactional
    public void deleteAssessmentType(Long id) {
        AssessmentType assessmentType = findById(id);
        assessmentType.setDeletedAt(java.time.LocalDateTime.now());
        assessmentTypeRepository.save(assessmentType);
        log.info("Type d'évaluation supprimé (soft): {}", id);
    }

    public AssessmentType findById(Long id) {
        return assessmentTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Type d'évaluation non trouvé avec l'ID: " + id));
    }
}
