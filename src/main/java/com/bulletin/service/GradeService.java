package com.bulletin.service;

import com.bulletin.dto.grade.GradeRequest;
import com.bulletin.dto.grade.GradeResponse;
import com.bulletin.entity.Assessment;
import com.bulletin.entity.Grade;
import com.bulletin.entity.Student;
import com.bulletin.entity.TeachingAssignment;
import com.bulletin.entity.Teacher;
import com.bulletin.exception.ResourceNotFoundException;
import com.bulletin.mapper.GradeMapper;
import com.bulletin.repository.AssessmentRepository;
import com.bulletin.repository.GradeRepository;
import com.bulletin.repository.StudentRepository;
import com.bulletin.repository.TeachingAssignmentRepository;
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
public class GradeService {

    private final GradeRepository gradeRepository;
    private final AssessmentRepository assessmentRepository;
    private final StudentRepository studentRepository;
    private final GradeMapper gradeMapper;
    private final TeachingAssignmentRepository teachingAssignmentRepository;
    private final UserTeacherRepository userTeacherRepository;
    private final SecurityUtils securityUtils;

    @Transactional
    public GradeResponse createGrade(GradeRequest request) {
        Assessment assessment = findAssessment(request.getAssessmentId());
        assertTeacherOwnsAssignment(assessment.getAssignment());
        Grade grade = gradeMapper.toEntity(request);
        grade.setAssessment(assessment);
        grade.setStudent(findStudent(request.getStudentId()));
        Grade saved = gradeRepository.save(grade);
        log.info("Note créée: {}", saved.getId());
        return gradeMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public GradeResponse getGrade(Long id) {
        return gradeMapper.toResponse(findById(id));
    }

    @Transactional(readOnly = true)
    public List<GradeResponse> getAllGrades() {
        return gradeRepository.findAll().stream()
                .map(gradeMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GradeResponse> getByAssessment(Long assessmentId) {
        return gradeRepository.findByAssessmentId(assessmentId).stream()
                .map(gradeMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GradeResponse> getByStudent(Long studentId) {
        return gradeRepository.findByStudentId(studentId).stream()
                .map(gradeMapper::toResponse)
                .toList();
    }

    @Transactional
    public GradeResponse updateGrade(Long id, GradeRequest request) {
        Grade grade = findById(id);
        gradeMapper.updateEntity(request, grade);
        Assessment assessment = findAssessment(request.getAssessmentId());
        assertTeacherOwnsAssignment(assessment.getAssignment());
        grade.setAssessment(assessment);
        grade.setStudent(findStudent(request.getStudentId()));
        Grade saved = gradeRepository.save(grade);
        log.info("Note mise à jour: {}", saved.getId());
        return gradeMapper.toResponse(saved);
    }

    @Transactional
    public void deleteGrade(Long id) {
        Grade grade = findById(id);
        assertTeacherOwnsAssignment(grade.getAssessment().getAssignment());
        grade.setDeletedAt(java.time.LocalDateTime.now());
        gradeRepository.save(grade);
        log.info("Note supprimée (soft): {}", id);
    }

    public Grade findById(Long id) {
        return gradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Note non trouvée avec l'ID: " + id));
    }

    private Assessment findAssessment(Long id) {
        return assessmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Évaluation non trouvée avec l'ID: " + id));
    }

    private Student findStudent(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Élève non trouvé avec l'ID: " + id));
    }

    /**
     * Règle section 7 : un professeur ne peut encoder/modifier que les notes
     * correspondant à ses propres affectations. La direction (admin/directeur/préfet) a accès à tout.
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
