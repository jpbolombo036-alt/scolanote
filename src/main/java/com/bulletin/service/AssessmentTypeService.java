package com.bulletin.service;

import com.bulletin.dto.grade.AssessmentTypeRequest;
import com.bulletin.dto.grade.AssessmentTypeResponse;
import com.bulletin.entity.AssessmentType;
import com.bulletin.exception.ResourceNotFoundException;
import com.bulletin.mapper.AssessmentTypeMapper;
import com.bulletin.repository.AssessmentRepository;
import com.bulletin.repository.AssessmentTypeRepository;
import com.bulletin.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssessmentTypeService {

    private final AssessmentTypeRepository assessmentTypeRepository;
    private final AssessmentTypeMapper assessmentTypeMapper;
    private final SecurityUtils securityUtils;
    private final AssessmentRepository assessmentRepository;

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
    public AssessmentTypeResponse createAssessmentType(AssessmentTypeRequest request) {
        AssessmentType assessmentType = assessmentTypeMapper.toEntity(request);
        assessmentType.setSchoolId(requireSchoolId());
        AssessmentType saved = assessmentTypeRepository.save(assessmentType);
        log.info("Type d'évaluation créé: {}", saved.getId());
        return assessmentTypeMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public AssessmentTypeResponse getAssessmentType(Long id) {
        return assessmentTypeMapper.toResponse(findById(id));
    }

    @Transactional(readOnly = true)
    public Page<AssessmentTypeResponse> getAccessibleAssessmentTypes(Pageable pageable) {
        if (isSuperAdmin()) {
            return assessmentTypeRepository.findAll(pageable)
                    .map(assessmentTypeMapper::toResponse);
        }
        return assessmentTypeRepository.findBySchoolId(requireSchoolId(), pageable)
                .map(assessmentTypeMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<AssessmentTypeResponse> getAccessibleAssessmentTypes() {
        if (isSuperAdmin()) {
            return assessmentTypeRepository.findAll().stream()
                    .map(assessmentTypeMapper::toResponse)
                    .toList();
        }
        return assessmentTypeRepository.findBySchoolId(requireSchoolId()).stream()
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
        // Garde d'intégrité : interdire la suppression d'un type encore utilisé
        // par des évaluations actives (sinon erreur FK ou évaluations orphelines).
        long usages = assessmentRepository.countByAssessmentTypeId(id);
        if (usages > 0) {
            throw new IllegalStateException(
                    "Impossible de supprimer ce type d'évaluation : " + usages
                            + " évaluation(s) l'utilisent encore. Supprimez ou modifiez d'abord ces évaluations.");
        }
        assessmentType.setDeletedAt(java.time.LocalDateTime.now());
        assessmentTypeRepository.save(assessmentType);
        log.info("Type d'évaluation supprimé (soft): {}", id);
    }

    public AssessmentType findById(Long id) {
        AssessmentType assessmentType = assessmentTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Type d'évaluation non trouvé avec l'ID: " + id));
        securityUtils.assertSchoolAccess(assessmentType.getSchoolId());
        return assessmentType;
    }
}
