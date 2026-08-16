package com.bulletin.service.bulletin;

import com.bulletin.entity.Assessment;
import com.bulletin.entity.Classroom;
import com.bulletin.entity.Curriculum;
import com.bulletin.entity.CurriculumSubject;
import com.bulletin.entity.Enrollment;
import com.bulletin.entity.Grade;
import com.bulletin.entity.Level;
import com.bulletin.entity.Period;
import com.bulletin.entity.Section;
import com.bulletin.entity.Student;
import com.bulletin.entity.Subject;
import com.bulletin.entity.TeachingAssignment;
import com.bulletin.repository.AssessmentRepository;
import com.bulletin.repository.AssessmentTypeRepository;
import com.bulletin.repository.CurriculumRepository;
import com.bulletin.repository.CurriculumSubjectRepository;
import com.bulletin.repository.EnrollmentRepository;
import com.bulletin.repository.GradeRepository;
import com.bulletin.repository.PeriodRepository;
import com.bulletin.repository.TeachingAssignmentRepository;
import com.bulletin.repository.TrimesterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Régression Idée C : le coefficient d'une matière doit venir du programme
 * EXACT de la classe (level + section + option), pas du premier
 * CurriculumSubject trouvé pour cette matière (ordre SQL non garanti).
 *
 * Scénario : Maths coef 6 en 4e Scientifique, coef 2 en 4e Pédagogie.
 * Un élève de 4e Scientifique avec 15/20 doit obtenir 15 × 6 = 90 points.
 */
@ExtendWith(MockitoExtension.class)
class BulletinCalculatorServiceTest {

    @Mock
    TeachingAssignmentRepository teachingAssignmentRepository;
    @Mock
    CurriculumSubjectRepository curriculumSubjectRepository;
    @Mock
    AssessmentRepository assessmentRepository;
    @Mock
    GradeRepository gradeRepository;
    @Mock
    AssessmentTypeRepository assessmentTypeRepository;
    @Mock
    EnrollmentRepository enrollmentRepository;
    @Mock
    TrimesterRepository trimesterRepository;
    @Mock
    PeriodRepository periodRepository;
    @Mock
    CurriculumRepository curriculumRepository;

    BulletinCalculatorService calculator;

    @BeforeEach
    void setUp() {
        calculator = new BulletinCalculatorService(teachingAssignmentRepository,
                curriculumSubjectRepository, assessmentRepository, gradeRepository,
                assessmentTypeRepository, enrollmentRepository, trimesterRepository,
                periodRepository, curriculumRepository);
    }

    @Test
    void computeSubjectResults_usesCoefficientFromClassroomExactCurriculum() {
        // Programmes : « 4e Scientifique » (Maths coef 6) et « 4e Pédagogie » (Maths coef 2)
        Level level4e = new Level();
        level4e.setId(1L);
        Section scientifique = new Section();
        scientifique.setId(1L);
        Section pedagogie = new Section();
        pedagogie.setId(2L);

        Curriculum curSci = new Curriculum();
        curSci.setId(10L);
        curSci.setLevel(level4e);
        curSci.setSection(scientifique);
        Curriculum curPed = new Curriculum();
        curPed.setId(11L);
        curPed.setLevel(level4e);
        curPed.setSection(pedagogie);
        when(curriculumRepository.findByLevelId(1L)).thenReturn(List.of(curSci, curPed));

        Subject maths = new Subject();
        maths.setId(50L);
        maths.setNom("Maths");
        CurriculumSubject mathsSci = new CurriculumSubject();
        mathsSci.setSubject(maths);
        mathsSci.setCoefficient(6);
        when(curriculumSubjectRepository.findByCurriculumId(10L)).thenReturn(List.of(mathsSci));

        // Classe : 4e Scientifique
        Classroom classe4eSci = new Classroom();
        classe4eSci.setId(7L);
        classe4eSci.setLevel(level4e);
        classe4eSci.setSection(scientifique);

        TeachingAssignment assignment = new TeachingAssignment();
        assignment.setId(3L);
        assignment.setClassroom(classe4eSci);
        assignment.setSubject(maths);
        when(teachingAssignmentRepository.findByClassroomId(7L)).thenReturn(List.of(assignment));

        Period period = new Period();
        period.setId(5L);
        Assessment assessment = new Assessment();
        assessment.setId(100L);
        assessment.setAssignment(assignment);
        assessment.setPeriod(period);
        assessment.setNoteMax(new BigDecimal("20"));
        when(assessmentRepository.findByAssignmentId(3L)).thenReturn(List.of(assessment));

        Student student = new Student();
        student.setId(1L);
        Grade grade = new Grade();
        grade.setStudent(student);
        grade.setAssessment(assessment);
        grade.setNote(new BigDecimal("15"));
        when(gradeRepository.findByAssessmentId(100L)).thenReturn(List.of(grade));

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setClassroom(classe4eSci);

        List<SubjectResult> results = calculator.computeSubjectResults(enrollment, period);

        // coefficient 6 (Scientifique) et PAS 2 (Pédagogie) : 15 × 6 = 90, max = 20 × 6 = 120, % = 75
        assertEquals(1, results.size());
        SubjectResult sr = results.get(0);
        assertEquals(6, sr.getCoefficient());
        assertEquals(0, new BigDecimal("15.00").compareTo(sr.getMoyenne()));
        assertEquals(0, new BigDecimal("90.00").compareTo(sr.getPoints()));
        assertEquals(0, new BigDecimal("75.00").compareTo(sr.getPourcentage()));
    }

    @Test
    void computeSubjectResults_defaultsToCoefficient1_whenClassroomHasNoCurriculum() {
        // Classe sans niveau → aucun programme → coefficient 1 par défaut
        Classroom classe = new Classroom();
        classe.setId(7L);

        Subject maths = new Subject();
        maths.setId(50L);
        TeachingAssignment assignment = new TeachingAssignment();
        assignment.setId(3L);
        assignment.setClassroom(classe);
        assignment.setSubject(maths);
        when(teachingAssignmentRepository.findByClassroomId(7L)).thenReturn(List.of(assignment));

        Period period = new Period();
        period.setId(5L);
        Assessment assessment = new Assessment();
        assessment.setId(100L);
        assessment.setAssignment(assignment);
        assessment.setPeriod(period);
        assessment.setNoteMax(new BigDecimal("20"));
        when(assessmentRepository.findByAssignmentId(3L)).thenReturn(List.of(assessment));

        Student student = new Student();
        student.setId(1L);
        Grade grade = new Grade();
        grade.setStudent(student);
        grade.setAssessment(assessment);
        grade.setNote(new BigDecimal("15"));
        when(gradeRepository.findByAssessmentId(100L)).thenReturn(List.of(grade));

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setClassroom(classe);

        List<SubjectResult> results = calculator.computeSubjectResults(enrollment, period);

        assertEquals(1, results.size());
        assertEquals(1, results.get(0).getCoefficient());
        assertEquals(0, new BigDecimal("15.00").compareTo(results.get(0).getPoints()));
    }
}

