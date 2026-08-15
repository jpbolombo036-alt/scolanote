package com.bulletin.service;

import com.bulletin.dto.grade.GradeRequest;
import com.bulletin.dto.grade.GradeResponse;
import com.bulletin.dto.grade.MissingGradeStudentResponse;
import com.bulletin.entity.Assessment;
import com.bulletin.entity.Enrollment;
import com.bulletin.entity.Grade;
import com.bulletin.entity.Student;
import com.bulletin.exception.DuplicateResourceException;
import com.bulletin.exception.ResourceNotFoundException;
import com.bulletin.mapper.GradeMapper;
import com.bulletin.repository.AssessmentRepository;
import com.bulletin.repository.EnrollmentRepository;
import com.bulletin.repository.GradeRepository;
import com.bulletin.repository.StudentRepository;
import com.bulletin.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Optional;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    private final EnrollmentRepository enrollmentRepository;

    @Transactional
    public GradeResponse createGrade(GradeRequest request) {
        Assessment assessment = findAssessment(request.getAssessmentId());
        periodClosureService.assertPeriodeOuverte(assessment.getPeriod().getId());
        assertCanModifyGrades(assessment.getAssignment(), "NOTE_SAISIR");
        Student student = findStudent(request.getStudentId());
        assertNoDuplicate(request.getAssessmentId(), request.getStudentId(), null);
        assertNoteCoherence(request, assessment);
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
        // Filtrage résilient : une note orpheline (élève/évaluation soft-deletée)
        // est ignorée au lieu de faire échouer toute la page.
        List<GradeResponse> content = page.getContent().stream()
                .map(this::toResponseSafe)
                .flatMap(Optional::stream)
                .toList();
        return new PageImpl<>(content, pageable, page.getTotalElements());
    }

    @Transactional(readOnly = true)
    public List<GradeResponse> getAccessibleGrades() {
        List<Grade> grades = securityUtils.isSuperAdmin()
                ? gradeRepository.findAllComplete()
                : gradeRepository.findCompleteBySchoolId(securityUtils.requireSchoolId());
        return grades.stream()
                .map(this::toResponseSafe)
                .flatMap(Optional::stream)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GradeResponse> getByAssessment(Long assessmentId) {
        Assessment assessment = findAssessment(assessmentId);
        List<Grade> grades = securityUtils.isSuperAdmin()
                ? gradeRepository.findCompleteByAssessmentId(assessmentId)
                : gradeRepository.findByAssessmentIdAndSchoolId(assessmentId, assessment.getSchoolId());
        return grades.stream()
                .map(this::toResponseSafe)
                .flatMap(Optional::stream)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GradeResponse> getByStudent(Long studentId) {
        Student student = findStudent(studentId);
        List<Grade> grades = securityUtils.isSuperAdmin()
                ? gradeRepository.findCompleteByStudentId(studentId)
                : gradeRepository.findByStudentIdAndSchoolId(studentId, student.getSchoolId());
        return grades.stream()
                .map(this::toResponseSafe)
                .flatMap(Optional::stream)
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
        assertNoDuplicate(request.getAssessmentId(), request.getStudentId(), id);
        assertNoteCoherence(request, assessment);
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
        // Cohérence avec create/update : impossible de supprimer une note
        // d'une période verrouillée (sauf direction — géré dans assertPeriodeOuverte)
        if (grade.getAssessment().getPeriod() != null) {
            periodClosureService.assertPeriodeOuverte(grade.getAssessment().getPeriod().getId());
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

    /**
     * Retourne les élèves de la classe (de l'évaluation) qui n'ont PAS encore
     * de note pour cette évaluation — triés par numéro d'ordre (ordre d'appel).
     *
     * Règle métier : un élève marqué absent (ou avec toute note active existante)
     * est considéré comme déjà encodé et n'apparaît plus dans cette liste.
     * Utilisé par la saisie en grille : les élèves disparaissent au fur et à
     * mesure de l'encodage.
     */
    @Transactional(readOnly = true)
    public List<MissingGradeStudentResponse> getStudentsWithoutGrade(Long assessmentId) {
        Assessment assessment = findAssessment(assessmentId);
        if (assessment.getAssignment() == null || assessment.getAssignment().getClassroom() == null) {
            throw new ResourceNotFoundException(
                    "Classe introuvable pour l'évaluation ID: " + assessmentId);
        }
        Long classroomId = assessment.getAssignment().getClassroom().getId();

        List<Enrollment> enrollments = enrollmentRepository.findByClassroomId(classroomId);

        List<Grade> grades = securityUtils.isSuperAdmin()
                ? gradeRepository.findCompleteByAssessmentId(assessmentId)
                : gradeRepository.findByAssessmentIdAndSchoolId(assessmentId, assessment.getSchoolId());

        Set<Long> notedStudentIds = grades.stream()
                .filter(g -> g.getStudent() != null)
                .map(g -> g.getStudent().getId())
                .collect(Collectors.toSet());

        return enrollments.stream()
                .filter(e -> e.getStudent() != null && !notedStudentIds.contains(e.getStudent().getId()))
                .sorted(Comparator.comparing(Enrollment::getNumeroOrdre,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .map(e -> MissingGradeStudentResponse.builder()
                        .studentId(e.getStudent().getId())
                        .matricule(e.getStudent().getMatricule())
                        .nom(e.getStudent().getNom())
                        .postnom(e.getStudent().getPostnom())
                        .prenom(e.getStudent().getPrenom())
                        .numeroOrdre(e.getNumeroOrdre())
                        .build())
                .toList();
    }

    /**
     * Garantit la règle « 1 élève = 1 note par évaluation ».
     * Les notes soft-deletées sont invisibles (filtre global), donc une
     * re-saisie après suppression reste possible.
     *
     * @param excludeId ID de la note en cours de modification (null en création)
     * @throws DuplicateResourceException si une note active existe déjà pour ce couple
     */
    private void assertNoDuplicate(Long assessmentId, Long studentId, Long excludeId) {
        gradeRepository.findByAssessmentIdAndStudentId(assessmentId, studentId).stream()
                .filter(existing -> excludeId == null || !existing.getId().equals(excludeId))
                .findFirst()
                .ifPresent(existing -> {
                    throw new DuplicateResourceException(
                            "Cet élève est déjà noté pour cette évaluation (note ID: " + existing.getId()
                                    + "). Utilisez la modification.");
                });
    }

    /**
     * Validations métier de la note :
     *  - un élève marqué absent ne peut pas avoir de note ;
     *  - la note ne peut pas être négative ;
     *  - la note ne peut pas dépasser le barème (noteMax) de l'évaluation.
     *
     * @throws IllegalArgumentException (HTTP 400) en cas d'incohérence
     */
    private void assertNoteCoherence(GradeRequest request, Assessment assessment) {
        if (request.isAbsence() && request.getNote() != null) {
            throw new IllegalArgumentException(
                    "Un élève marqué absent ne peut pas avoir de note. Laissez la note vide.");
        }
        if (request.getNote() == null) {
            return;
        }
        if (request.getNote().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("La note ne peut pas être négative");
        }
        BigDecimal noteMax = assessment.getNoteMax();
        if (noteMax != null && request.getNote().compareTo(noteMax) > 0) {
            throw new IllegalArgumentException(
                    "La note (" + request.getNote() + ") dépasse le barème de l'évaluation (" + noteMax + ")");
        }
    }

    /**
     * Variante résiliente de {@link #toResponse(Grade)} pour les listes :
     * une note orpheline (élève ou évaluation soft-deletée) est ignorée
     * avec un avertissement au lieu de faire échouer toute la liste.
     */
    private Optional<GradeResponse> toResponseSafe(Grade grade) {
        if (grade.getStudent() == null || grade.getAssessment() == null) {
            log.warn("Note {} ignorée : relations incomplètes", grade.getId());
            return Optional.empty();
        }
        return Optional.of(gradeMapper.toResponse(grade));
    }

    private GradeResponse toResponse(Grade grade) {
        if (grade.getStudent() == null || grade.getAssessment() == null) {
            log.warn("Note {} ignorée : relations incomplètes", grade.getId());
            throw new ResourceNotFoundException("Note incomplète avec l'ID: " + grade.getId());
        }
        return gradeMapper.toResponse(grade);
    }
}
