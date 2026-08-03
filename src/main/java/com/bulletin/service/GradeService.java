package com.bulletin.service;

import com.bulletin.dto.grade.GradeRequest;
import com.bulletin.dto.grade.GradeResponse;
import com.bulletin.entity.Assessment;
import com.bulletin.entity.Grade;
import com.bulletin.entity.Student;
import com.bulletin.exception.ResourceNotFoundException;
import com.bulletin.mapper.GradeMapper;
import com.bulletin.repository.AssessmentRepository;
import com.bulletin.repository.GradeRepository;
import com.bulletin.repository.StudentRepository;
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
public class GradeService {

    private final GradeRepository gradeRepository;
    private final AssessmentRepository assessmentRepository;
    private final StudentRepository studentRepository;
    private final GradeMapper gradeMapper;
    private final SecurityUtils securityUtils;
    private final PeriodClosureService periodClosureService;

    @Transactional
    public GradeResponse createGrade(GradeRequest request) {
        Assessment assessment = findAssessment(request.getAssessmentId());
        periodClosureService.assertPeriodeOuverte(assessment.getPeriod().getId());
        assertCanModifyGrades(assessment.getAssignment(), "NOTE_SAISIR");
        Student student = findStudent(request.getStudentId());
        Grade grade = gradeMapper.toEntity(request);
        grade.setAssessment(assessment);
        grade.setStudent(student);
        Grade saved = gradeRepository.save(grade);
        log.info("Note créée: {}", saved.getId());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public GradeResponse getGrade(Long id) {
        Grade grade = findById(id);
        securityUtils.assertSchoolAccess(grade.getSchoolId());
        return toResponse(grade);
    }

    @Transactional(readOnly = true)
    public Page<GradeResponse> getAccessibleGrades(Pageable pageable) {
        Page<Grade> page = securityUtils.isSuperAdmin()
                ? gradeRepository.findAllComplete(pageable)
                : gradeRepository.findCompleteBySchoolId(securityUtils.requireSchoolId(), pageable);
        return page.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<GradeResponse> getAccessibleGrades() {
        List<Grade> grades = securityUtils.isSuperAdmin()
                ? gradeRepository.findAllComplete()
                : gradeRepository.findCompleteBySchoolId(securityUtils.requireSchoolId());
        return grades.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GradeResponse> getByAssessment(Long assessmentId) {
        Assessment assessment = findAssessment(assessmentId);
        List<Grade> grades = securityUtils.isSuperAdmin()
                ? gradeRepository.findCompleteByAssessmentId(assessmentId)
                : gradeRepository.findByAssessmentIdAndSchoolId(assessmentId, assessment.getSchoolId());
        return grades.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GradeResponse> getByStudent(Long studentId) {
        Student student = findStudent(studentId);
        List<Grade> grades = securityUtils.isSuperAdmin()
                ? gradeRepository.findCompleteByStudentId(studentId)
                : gradeRepository.findByStudentIdAndSchoolId(studentId, student.getSchoolId());
        return grades.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public GradeResponse updateGrade(Long id, GradeRequest request) {
        Grade grade = findById(id);
        securityUtils.assertSchoolAccess(grade.getSchoolId());
        gradeMapper.updateEntity(request, grade);
        Assessment assessment = findAssessment(request.getAssessmentId());
        periodClosureService.assertPeriodeOuverte(assessment.getPeriod().getId());
        assertCanModifyGrades(assessment.getAssignment(), "NOTE_MODIFIER");
        grade.setAssessment(assessment);
        grade.setStudent(findStudent(request.getStudentId()));
        Grade saved = gradeRepository.save(grade);
        log.info("Note mise à jour: {}", saved.getId());
        return toResponse(saved);
    }

    @Transactional
    public void deleteGrade(Long id) {
        Grade grade = findById(id);
        securityUtils.assertSchoolAccess(grade.getSchoolId());
        if (grade.getAssessment() == null || grade.getAssessment().getAssignment() == null) {
            throw new ResourceNotFoundException("Affectation non trouvée pour la note ID: " + id);
        }
        assertCanModifyGrades(grade.getAssessment().getAssignment(), "NOTE_MODIFIER");
        grade.setDeletedAt(java.time.LocalDateTime.now());
        gradeRepository.save(grade);
        log.info("Note supprimée (soft): {}", id);
    }

    public Grade findById(Long id) {
        Grade grade = gradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Note non trouvée avec l'ID: " + id));
        securityUtils.assertSchoolAccess(grade.getSchoolId());
        return grade;
    }

    private Assessment findAssessment(Long id) {
        Assessment assessment = assessmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Évaluation non trouvée avec l'ID: " + id));
        securityUtils.assertSchoolAccess(assessment.getSchoolId());
        return assessment;
    }

    private Student findStudent(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Élève non trouvé avec l'ID: " + id));
        securityUtils.assertSchoolAccess(student.getSchoolId());
        return student;
    }

    /**
     * Autorise la modification des notes si :
     *  - l'utilisateur a la permission granulaire (NOTE_SAISIR ou NOTE_MODIFIER) — ex: un délégué/secrétaire
     *  - OU s'il est le professeur propriétaire de l'affectation (règle métier standard)
     *  - OU s'il fait partie de la direction (bypass dans assertTeacherOwnsAssignment)
     */
    private void assertCanModifyGrades(com.bulletin.entity.TeachingAssignment assignment, String permissionCode) {
        if (securityUtils.hasPermission(permissionCode)) {
            return; // permission granulaire : pas besoin d'être le prof propriétaire
        }
        securityUtils.assertTeacherOwnsAssignment(assignment);
    }

    private GradeResponse toResponse(Grade grade) {
        if (grade.getStudent() == null || grade.getAssessment() == null) {
            log.warn("Note {} ignorée : relations incomplètes", grade.getId());
            throw new ResourceNotFoundException("Note incomplète avec l'ID: " + grade.getId());
        }
        return gradeMapper.toResponse(grade);
    }
}
