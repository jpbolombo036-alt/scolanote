package com.bulletin.service;

import com.bulletin.dto.grade.AssessmentRequest;
import com.bulletin.dto.grade.AssessmentResponse;
import com.bulletin.entity.Assessment;
import com.bulletin.entity.AssessmentType;
import com.bulletin.entity.Period;
import com.bulletin.entity.TeachingAssignment;
import com.bulletin.exception.ResourceNotFoundException;
import com.bulletin.mapper.AssessmentMapper;
import com.bulletin.repository.AssessmentRepository;
import com.bulletin.repository.AssessmentTypeRepository;
import com.bulletin.repository.PeriodRepository;
import com.bulletin.repository.TeachingAssignmentRepository;
import com.bulletin.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssessmentService {

    private final AssessmentRepository assessmentRepository;
    private final TeachingAssignmentRepository teachingAssignmentRepository;
    private final AssessmentTypeRepository assessmentTypeRepository;
    private final PeriodRepository periodRepository;
    private final AssessmentMapper assessmentMapper;
    private final SecurityUtils securityUtils;
    private final PeriodClosureService periodClosureService;

    @Transactional
    public AssessmentResponse createAssessment(AssessmentRequest request) {
        periodClosureService.assertPeriodeOuverte(request.getPeriodId());
        TeachingAssignment assignment = findAssignment(request.getAssignmentId());
        securityUtils.assertTeacherOwnsAssignment(assignment);
        Assessment assessment = assessmentMapper.toEntity(request);
        assessment.setAssignment(assignment);
        assessment.setAssessmentType(findAssessmentType(request.getAssessmentTypeId()));
        assessment.setPeriod(findPeriod(request.getPeriodId()));
        Assessment saved = assessmentRepository.save(assessment);
        log.info("Évaluation créée: {}", saved.getId());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public AssessmentResponse getAssessment(Long id) {
        Assessment assessment = findById(id);
        securityUtils.assertSchoolAccess(assessment.getSchoolId());
        return toResponse(assessment);
    }

    @Transactional(readOnly = true)
    public List<AssessmentResponse> getAccessibleAssessments() {
        List<Assessment> assessments = securityUtils.isSuperAdmin()
                ? assessmentRepository.findAllComplete()
                : assessmentRepository.findCompleteBySchoolId(securityUtils.requireSchoolId());
        return assessments.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AssessmentResponse> getByAssignment(Long assignmentId) {
        TeachingAssignment assignment = findAssignment(assignmentId);
        securityUtils.assertSchoolAccess(assignment.getSchoolId());
        List<Assessment> assessments = securityUtils.isSuperAdmin()
                ? assessmentRepository.findCompleteByAssignmentId(assignmentId)
                : assessmentRepository.findByAssignmentIdAndSchoolId(assignmentId, securityUtils.requireSchoolId());
        return assessments.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AssessmentResponse> getByTerm(Long periodId) {
        findPeriod(periodId);
        List<Assessment> assessments = securityUtils.isSuperAdmin()
                ? assessmentRepository.findCompleteByPeriodId(periodId)
                : assessmentRepository.findByPeriodIdAndSchoolId(periodId, securityUtils.requireSchoolId());
        return assessments.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public AssessmentResponse updateAssessment(Long id, AssessmentRequest request) {
        periodClosureService.assertPeriodeOuverte(request.getPeriodId());
        Assessment assessment = findById(id);
        securityUtils.assertSchoolAccess(assessment.getSchoolId());
        assessmentMapper.updateEntity(request, assessment);
        TeachingAssignment assignment = findAssignment(request.getAssignmentId());
        securityUtils.assertTeacherOwnsAssignment(assignment);
        assessment.setAssignment(assignment);
        assessment.setAssessmentType(findAssessmentType(request.getAssessmentTypeId()));
        assessment.setPeriod(findPeriod(request.getPeriodId()));
        Assessment saved = assessmentRepository.save(assessment);
        log.info("Évaluation mise à jour: {}", saved.getId());
        return toResponse(saved);
    }

    @Transactional
    public void deleteAssessment(Long id) {
        Assessment assessment = findById(id);
        securityUtils.assertSchoolAccess(assessment.getSchoolId());
        securityUtils.assertTeacherOwnsAssignment(assessment.getAssignment());
        assessment.setDeletedAt(java.time.LocalDateTime.now());
        assessmentRepository.save(assessment);
        log.info("Évaluation supprimée (soft): {}", id);
    }

    public Assessment findById(Long id) {
        Assessment assessment = assessmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Évaluation non trouvée avec l'ID: " + id));
        securityUtils.assertSchoolAccess(assessment.getSchoolId());
        return assessment;
    }

    private TeachingAssignment findAssignment(Long id) {
        TeachingAssignment assignment = teachingAssignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Affectation non trouvée avec l'ID: " + id));
        securityUtils.assertSchoolAccess(assignment.getSchoolId());
        return assignment;
    }

    private AssessmentType findAssessmentType(Long id) {
        return assessmentTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Type d'évaluation non trouvé avec l'ID: " + id));
    }

    private Period findPeriod(Long id) {
        return periodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Période non trouvée avec l'ID: " + id));
    }

    private AssessmentResponse toResponse(Assessment assessment) {
        if (assessment.getAssessmentType() == null || assessment.getPeriod() == null || assessment.getAssignment() == null) {
            log.warn("Évaluation {} ignorée : relations incomplètes", assessment.getId());
            throw new ResourceNotFoundException("Évaluation incomplète avec l'ID: " + assessment.getId());
        }
        return assessmentMapper.toResponse(assessment);
    }
}
