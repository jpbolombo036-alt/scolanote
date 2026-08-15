package com.bulletin.service;

import com.bulletin.dto.grade.GradeRequest;
import com.bulletin.dto.grade.GradeResponse;
import com.bulletin.dto.grade.MissingGradeStudentResponse;
import com.bulletin.entity.Assessment;
import com.bulletin.entity.Classroom;
import com.bulletin.entity.Enrollment;
import com.bulletin.entity.Grade;
import com.bulletin.entity.Period;
import com.bulletin.entity.Student;
import com.bulletin.entity.TeachingAssignment;
import com.bulletin.exception.DuplicateResourceException;
import com.bulletin.mapper.GradeMapper;
import com.bulletin.repository.AssessmentRepository;
import com.bulletin.repository.EnrollmentRepository;
import com.bulletin.repository.GradeRepository;
import com.bulletin.repository.StudentRepository;
import com.bulletin.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Règle métier couverte : 1 élève = 1 note par évaluation.
 *  - création → 409 (DuplicateResourceException) si le couple existe déjà
 *  - modification → 409 si le nouveau couple percute une AUTRE note
 *  - la note soft-deletée étant invisible pour le repository, la re-saisie
 *    après suppression reste possible (le repository retourne une liste vide)
 *  - la liste des élèves « non notés » exclut ceux déjà encodés
 */
@ExtendWith(MockitoExtension.class)
class GradeServiceTest {

    @Mock
    GradeRepository gradeRepository;
    @Mock
    AssessmentRepository assessmentRepository;
    @Mock
    StudentRepository studentRepository;
    @Mock
    GradeMapper gradeMapper;
    @Mock
    SecurityUtils securityUtils;
    @Mock
    PeriodClosureService periodClosureService;
    @Mock
    EnrollmentRepository enrollmentRepository;

    GradeService gradeService;

    private static final Long SCHOOL_ID = 10L;
    private static final Long ASSESSMENT_ID = 100L;
    private static final Long STUDENT_ID = 1L;
    private static final Long PERIOD_ID = 5L;

    @BeforeEach
    void setUp() {
        gradeService = new GradeService(gradeRepository, assessmentRepository, studentRepository,
                gradeMapper, securityUtils, periodClosureService, enrollmentRepository);
    }

    private Assessment newAssessment() {
        Period period = new Period();
        period.setId(PERIOD_ID);
        TeachingAssignment assignment = new TeachingAssignment();
        assignment.setId(3L);

        Assessment assessment = new Assessment();
        assessment.setId(ASSESSMENT_ID);
        assessment.setPeriod(period);
        assessment.setAssignment(assignment);
        assessment.setSchoolId(SCHOOL_ID);
        return assessment;
    }

    private Student newStudent(Long id) {
        Student student = new Student();
        student.setId(id);
        student.setSchoolId(SCHOOL_ID);
        student.setNom("Eleve" + id);
        return student;
    }

    private GradeRequest newRequest() {
        return GradeRequest.builder()
                .assessmentId(ASSESSMENT_ID)
                .studentId(STUDENT_ID)
                .note(new BigDecimal("12.50"))
                .build();
    }

    @Test
    void createGrade_saves_whenStudentNotYetGraded() {
        stubCreatePath();
        // Aucune note active existante pour ce couple (les soft-deletées sont filtrées en amont)
        when(gradeRepository.findByAssessmentIdAndStudentId(ASSESSMENT_ID, STUDENT_ID))
                .thenReturn(List.of());

        Grade mapped = new Grade();
        when(gradeMapper.toEntity(any(GradeRequest.class))).thenReturn(mapped);
        when(gradeRepository.save(any(Grade.class))).thenAnswer(inv -> {
            Grade g = inv.getArgument(0);
            g.setId(555L);
            return g;
        });
        when(gradeMapper.toResponse(mapped))
                .thenReturn(GradeResponse.builder().id(555L).build());

        GradeResponse result = gradeService.createGrade(newRequest());

        assertEquals(555L, result.getId());
        verify(gradeRepository).save(any(Grade.class));
    }

    @Test
    void createGrade_throwsDuplicate_whenStudentAlreadyGraded() {
        stubCreatePath();
        Grade existing = new Grade();
        existing.setId(999L);
        when(gradeRepository.findByAssessmentIdAndStudentId(ASSESSMENT_ID, STUDENT_ID))
                .thenReturn(List.of(existing));

        DuplicateResourceException ex = assertThrows(DuplicateResourceException.class,
                () -> gradeService.createGrade(newRequest()));

        assertTrue(ex.getMessage().contains("déjà noté"));
        verify(gradeRepository, never()).save(any(Grade.class));
    }

    @Test
    void updateGrade_throwsDuplicate_whenPairCollidesWithAnotherGrade() {
        Grade grade = new Grade();
        grade.setId(50L);
        grade.setSchoolId(SCHOOL_ID);
        when(gradeRepository.findById(50L)).thenReturn(Optional.of(grade));

        when(assessmentRepository.findById(ASSESSMENT_ID)).thenReturn(Optional.of(newAssessment()));
        when(securityUtils.hasPermission("NOTE_MODIFIER")).thenReturn(true);
        when(studentRepository.findById(STUDENT_ID)).thenReturn(Optional.of(newStudent(STUDENT_ID)));

        // Une AUTRE note (id=999) occupe déjà le couple (évaluation, élève)
        Grade other = new Grade();
        other.setId(999L);
        when(gradeRepository.findByAssessmentIdAndStudentId(ASSESSMENT_ID, STUDENT_ID))
                .thenReturn(List.of(other));

        assertThrows(DuplicateResourceException.class,
                () -> gradeService.updateGrade(50L, newRequest()));
        verify(gradeRepository, never()).save(any(Grade.class));
    }

    @Test
    void updateGrade_saves_whenOnlyExistingGradeIsItself() {
        Grade grade = new Grade();
        grade.setId(50L);
        grade.setSchoolId(SCHOOL_ID);
        when(gradeRepository.findById(50L)).thenReturn(Optional.of(grade));

        when(assessmentRepository.findById(ASSESSMENT_ID)).thenReturn(Optional.of(newAssessment()));
        when(securityUtils.hasPermission("NOTE_MODIFIER")).thenReturn(true);
        when(studentRepository.findById(STUDENT_ID)).thenReturn(Optional.of(newStudent(STUDENT_ID)));

        // La seule note trouvée est la note courante → pas de conflit
        when(gradeRepository.findByAssessmentIdAndStudentId(ASSESSMENT_ID, STUDENT_ID))
                .thenReturn(List.of(grade));

        when(gradeRepository.save(any(Grade.class))).thenAnswer(inv -> inv.getArgument(0));
        when(gradeMapper.toResponse(grade))
                .thenReturn(GradeResponse.builder().id(50L).build());

        GradeResponse result = gradeService.updateGrade(50L, newRequest());

        assertEquals(50L, result.getId());
        verify(gradeRepository).save(grade);
    }

    @Test
    void getStudentsWithoutGrade_excludesAlreadyNoted_andSortsByNumeroOrdre() {
        // Classe 7 rattachée à l'évaluation 100
        Classroom classroom = new Classroom();
        classroom.setId(7L);
        Assessment assessment = newAssessment();
        assessment.getAssignment().setClassroom(classroom);
        when(assessmentRepository.findById(ASSESSMENT_ID)).thenReturn(Optional.of(assessment));

        // 3 inscrits ; l'élève 2 est déjà noté
        Enrollment e1 = Enrollment.builder().student(newStudent(1L)).numeroOrdre(2).build();
        Enrollment e2 = Enrollment.builder().student(newStudent(2L)).numeroOrdre(1).build();
        Enrollment e3 = Enrollment.builder().student(newStudent(3L)).numeroOrdre(3).build();
        when(enrollmentRepository.findByClassroomId(7L)).thenReturn(List.of(e1, e2, e3));

        Grade noted = new Grade();
        noted.setStudent(newStudent(2L));
        when(securityUtils.isSuperAdmin()).thenReturn(false);
        when(gradeRepository.findByAssessmentIdAndSchoolId(ASSESSMENT_ID, SCHOOL_ID))
                .thenReturn(List.of(noted));

        List<MissingGradeStudentResponse> result = gradeService.getStudentsWithoutGrade(ASSESSMENT_ID);

        // L'élève 2 (déjà noté) est exclu ; tri par numeroOrdre : élève 1 (n°2) puis élève 3 (n°3)
        assertEquals(2, result.size());
        assertEquals(List.of(1L, 3L),
                result.stream().map(MissingGradeStudentResponse::getStudentId).toList());
        assertEquals(2, result.get(0).getNumeroOrdre());
        assertEquals(3, result.get(1).getNumeroOrdre());
    }

    @Test
    void createGrade_throwsBadRequest_whenNoteExceedsNoteMax() {
        Assessment assessment = newAssessment();
        assessment.setNoteMax(new BigDecimal("20"));
        when(assessmentRepository.findById(ASSESSMENT_ID)).thenReturn(Optional.of(assessment));
        when(securityUtils.hasPermission("NOTE_SAISIR")).thenReturn(true);
        when(studentRepository.findById(STUDENT_ID)).thenReturn(Optional.of(newStudent(STUDENT_ID)));
        when(gradeRepository.findByAssessmentIdAndStudentId(ASSESSMENT_ID, STUDENT_ID))
                .thenReturn(List.of());

        GradeRequest request = GradeRequest.builder()
                .assessmentId(ASSESSMENT_ID).studentId(STUDENT_ID)
                .note(new BigDecimal("25")).build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> gradeService.createGrade(request));

        assertTrue(ex.getMessage().contains("dépasse le barème"));
        verify(gradeRepository, never()).save(any(Grade.class));
    }

    @Test
    void createGrade_throwsBadRequest_whenAbsentWithNote() {
        Assessment assessment = newAssessment();
        assessment.setNoteMax(new BigDecimal("20"));
        when(assessmentRepository.findById(ASSESSMENT_ID)).thenReturn(Optional.of(assessment));
        when(securityUtils.hasPermission("NOTE_SAISIR")).thenReturn(true);
        when(studentRepository.findById(STUDENT_ID)).thenReturn(Optional.of(newStudent(STUDENT_ID)));
        when(gradeRepository.findByAssessmentIdAndStudentId(ASSESSMENT_ID, STUDENT_ID))
                .thenReturn(List.of());

        GradeRequest request = GradeRequest.builder()
                .assessmentId(ASSESSMENT_ID).studentId(STUDENT_ID)
                .absence(true).note(new BigDecimal("10")).build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> gradeService.createGrade(request));

        assertTrue(ex.getMessage().contains("absent"));
        verify(gradeRepository, never()).save(any(Grade.class));
    }

    @Test
    void deleteGrade_checksPeriodClosure_beforeDeleting() {
        Grade grade = new Grade();
        grade.setId(50L);
        grade.setSchoolId(SCHOOL_ID);
        grade.setAssessment(newAssessment());
        when(gradeRepository.findById(50L)).thenReturn(Optional.of(grade));
        when(securityUtils.hasPermission("NOTE_MODIFIER")).thenReturn(true);

        gradeService.deleteGrade(50L);

        // La suppression doit être soumise au contrôle de période verrouillée
        verify(periodClosureService).assertPeriodeOuverte(PERIOD_ID);
        assertNotNull(grade.getDeletedAt());
        verify(gradeRepository).save(grade);
    }

    /** Stubs communs du chemin nominal de création (évaluation + permission + élève). */
    private void stubCreatePath() {
        when(assessmentRepository.findById(ASSESSMENT_ID)).thenReturn(Optional.of(newAssessment()));
        when(securityUtils.hasPermission("NOTE_SAISIR")).thenReturn(true);
        when(studentRepository.findById(STUDENT_ID)).thenReturn(Optional.of(newStudent(STUDENT_ID)));
    }
}
