package com.bulletin.service;

import com.bulletin.dto.grade.AssessmentRequest;
import com.bulletin.dto.grade.AssessmentResponse;
import com.bulletin.entity.Assessment;
import com.bulletin.entity.AssessmentType;
import com.bulletin.entity.Teacher;
import com.bulletin.entity.TeachingAssignment;
import com.bulletin.entity.Term;
import com.bulletin.exception.ResourceNotFoundException;
import com.bulletin.mapper.AssessmentMapper;
import com.bulletin.repository.AssessmentRepository;
import com.bulletin.repository.AssessmentTypeRepository;
import com.bulletin.repository.TeachingAssignmentRepository;
import com.bulletin.repository.TermRepository;
import com.bulletin.repository.UserTeacherRepository;
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
    private final TermRepository termRepository;
    private final AssessmentMapper assessmentMapper;
    private final UserTeacherRepository userTeacherRepository;
    private final SecurityUtils securityUtils;

    @Transactional
    public AssessmentResponse createAssessment(AssessmentRequest request) {
        TeachingAssignment assignment = findAssignment(request.getAssignmentId());
        assertTeacherOwnsAssignment(assignment);
        Assessment assessment = assessmentMapper.toEntity(request);
        assessment.setAssignment(assignment);
        assessment.setAssessmentType(findAssessmentType(request.getAssessmentTypeId()));
        assessment.setTerm(findTerm(request.getTermId()));
        Assessment saved = assessmentRepository.save(assessment);
        log.info("Évaluation créée: {}", saved.getId());
        return assessmentMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public AssessmentResponse getAssessment(Long id) {
        return assessmentMapper.toResponse(findById(id));
    }

    @Transactional(readOnly = true)
    public List<AssessmentResponse> getAllAssessments() {
        return assessmentRepository.findAll().stream()
                .map(assessmentMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AssessmentResponse> getByAssignment(Long assignmentId) {
        return assessmentRepository.findByAssignmentId(assignmentId).stream()
                .map(assessmentMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AssessmentResponse> getByTerm(Long termId) {
        return assessmentRepository.findByTermId(termId).stream()
                .map(assessmentMapper::toResponse)
                .toList();
    }

    @Transactional
    public AssessmentResponse updateAssessment(Long id, AssessmentRequest request) {
        Assessment assessment = findById(id);
        assessmentMapper.updateEntity(request, assessment);
        TeachingAssignment assignment = findAssignment(request.getAssignmentId());
        assertTeacherOwnsAssignment(assignment);
        assessment.setAssignment(assignment);
        assessment.setAssessmentType(findAssessmentType(request.getAssessmentTypeId()));
        assessment.setTerm(findTerm(request.getTermId()));
        Assessment saved = assessmentRepository.save(assessment);
        log.info("Évaluation mise à jour: {}", saved.getId());
        return assessmentMapper.toResponse(saved);
    }

    @Transactional
    public void deleteAssessment(Long id) {
        Assessment assessment = findById(id);
        assertTeacherOwnsAssignment(assessment.getAssignment());
        assessment.setDeletedAt(java.time.LocalDateTime.now());
        assessmentRepository.save(assessment);
        log.info("Évaluation supprimée (soft): {}", id);
    }

    public Assessment findById(Long id) {
        return assessmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Évaluation non trouvée avec l'ID: " + id));
    }

    private TeachingAssignment findAssignment(Long id) {
        return teachingAssignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Affectation non trouvée avec l'ID: " + id));
    }

    private AssessmentType findAssessmentType(Long id) {
        return assessmentTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Type d'évaluation non trouvé avec l'ID: " + id));
    }

    private Term findTerm(Long id) {
        return termRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trimestre non trouvé avec l'ID: " + id));
    }

    /**
     * Règle section 7 : un professeur ne peut créer/modifier/supprimer que les évaluations
     * de ses propres affectations. La direction a accès à tout.
     */
    private void assertTeacherOwnsAssignment(TeachingAssignment assignment) {
        if (securityUtils.isDirection()) {
            return;
        }
        Long currentUserId = securityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new SecurityException("Authentification requise");
        }
        Teacher teacher = assignment.getTeacher();
        boolean owns = userTeacherRepository.findAll().stream()
                .anyMatch(ut -> ut.getUser() != null && ut.getUser().getId().equals(currentUserId)
                        && ut.getTeacher() != null && teacher != null
                        && ut.getTeacher().getId().equals(teacher.getId()));
        if (!owns) {
            throw new SecurityException("Accès refusé : vous ne pouvez agir que sur vos propres affectations");
        }
    }
}
