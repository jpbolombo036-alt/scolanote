package com.bulletin.service.bulletin;

import com.bulletin.entity.*;
import com.bulletin.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BulletinCalculatorService {

    private final TeachingAssignmentRepository teachingAssignmentRepository;
    private final CurriculumSubjectRepository curriculumSubjectRepository;
    private final AssessmentRepository assessmentRepository;
    private final GradeRepository gradeRepository;
    private final AssessmentTypeRepository assessmentTypeRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final TrimesterRepository trimesterRepository;
    private final PeriodRepository periodRepository;

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    /**
     * Comparateur d'ancienneté des notes (updatedAt puis id, nulls d'abord).
     * Sert à départager d'éventuels doublons historiques (antérieurs à la
     * contrainte d'unicité V33) : la note la plus récente l'emporte,
     * de façon déterministe, dans tous les modes de calcul.
     */
    private static final Comparator<Grade> NOTE_RECENCY_COMPARATOR = Comparator
            .comparing(Grade::getUpdatedAt, Comparator.nullsFirst(Comparator.naturalOrder()))
            .thenComparing(Grade::getId, Comparator.nullsFirst(Comparator.naturalOrder()));

    /**
     * Calcule les résultats par matière pour un élève (inscription) et un trimestre.
     * La moyenne de chaque matière est pondérée par le coefficient de l'évaluation
     * (ou par défaut par le coefficient du type d'évaluation).
     */
    public List<SubjectResult> computeSubjectResults(Enrollment enrollment, Period period) {
        Classroom classroom = enrollment.getClassroom();
        Student student = enrollment.getStudent();

        // Matières enseignées dans cette classe
        List<TeachingAssignment> assignments = teachingAssignmentRepository.findByClassroomId(classroom.getId());

        List<SubjectResult> results = new ArrayList<>();

        for (TeachingAssignment assignment : assignments) {
            Subject subject = assignment.getSubject();

            // Coefficient de la matière dans le programme de la classe
            Integer subjectCoefficient = resolveSubjectCoefficient(classroom, subject);

            // Évaluations de cette matière pour ce trimestre
            List<Assessment> assessments = assessmentRepository
                    .findByAssignmentId(assignment.getId()).stream()
                    .filter(a -> a.getPeriod() != null && a.getPeriod().getId().equals(period.getId()))
                    .toList();

            BigDecimal weightedSum = BigDecimal.ZERO;
            BigDecimal coeffSum = BigDecimal.ZERO;
            BigDecimal maximum = BigDecimal.ZERO;

            for (Assessment assessment : assessments) {
                BigDecimal evalCoeff = resolveAssessmentCoefficient(assessment);
                BigDecimal noteMax = assessment.getNoteMax() != null ? assessment.getNoteMax() : BigDecimal.ZERO;

                List<Grade> grades = gradeRepository.findByAssessmentId(assessment.getId());
                BigDecimal studentNote = resolveStudentNote(grades, student.getId());

                weightedSum = weightedSum.add(studentNote.multiply(evalCoeff));
                coeffSum = coeffSum.add(evalCoeff);
                maximum = maximum.add(noteMax.multiply(BigDecimal.valueOf(subjectCoefficient)));
            }

            BigDecimal moyenne = coeffSum.compareTo(BigDecimal.ZERO) > 0
                    ? weightedSum.divide(coeffSum, 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            BigDecimal points = moyenne.multiply(BigDecimal.valueOf(subjectCoefficient));
            BigDecimal pourcentage = maximum.compareTo(BigDecimal.ZERO) > 0
                    ? points.divide(maximum, 2, RoundingMode.HALF_UP).multiply(ONE_HUNDRED)
                    : BigDecimal.ZERO;

            results.add(SubjectResult.builder()
                    .subject(subject)
                    .coefficient(subjectCoefficient)
                    .moyenne(moyenne)
                    .points(points)
                    .maximum(maximum)
                    .pourcentage(pourcentage)
                    .build());
        }

        return results;
    }

    /**
     * Agrège les résultats par matière : total des points, maximum total, pourcentage global.
     */
    public GlobalResult computeGlobalResult(List<SubjectResult> subjectResults) {
        BigDecimal totalPoints = subjectResults.stream()
                .map(SubjectResult::getPoints)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal maximumPoints = subjectResults.stream()
                .map(SubjectResult::getMaximum)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal pourcentage = maximumPoints.compareTo(BigDecimal.ZERO) > 0
                ? totalPoints.divide(maximumPoints, 2, RoundingMode.HALF_UP).multiply(ONE_HUNDRED)
                : BigDecimal.ZERO;

        return new GlobalResult(totalPoints, maximumPoints, pourcentage);
    }

    /**
     * VERSION BATCH (anti N+1) : calcule les résultats par matière pour TOUTE une classe
     * en quelques requêtes seulement, puis effectue les calculs en mémoire.
     *
     * Au lieu de ~51 requêtes par élève (boucles imbriquées), on charge :
     *   1. les affectations (+ matières) de la classe
     *   2. les évaluations (+ types) de la classe pour la période
     *   3. toutes les notes de la classe pour la période
     *   4. les coefficients des matières concernées
     *
     * @return une map enrollmentId -> liste des SubjectResult de l'élève
     */
    public Map<Long, List<SubjectResult>> computeClassSubjectResults(Classroom classroom, Period period, List<Enrollment> enrollments) {
        Long classroomId = classroom.getId();
        Long periodId = period.getId();

        // 1. Affectations + matières (1 requête)
        List<TeachingAssignment> assignments = teachingAssignmentRepository.findByClassroomIdWithSubject(classroomId);

        // 2. Évaluations + types + affectation (1 requête)
        List<Assessment> assessments = assessmentRepository.findByClassroomIdAndPeriodIdWithDetails(classroomId, periodId);

        // 3. Toutes les notes de la classe pour la période (1 requête)
        List<Grade> grades = gradeRepository.findByClassroomIdAndPeriodId(classroomId, periodId);

        // 4. Coefficients des matières (1 requête)
        List<Long> subjectIds = assignments.stream()
                .map(a -> a.getSubject().getId())
                .distinct()
                .toList();
        Map<Long, Integer> coefficientBySubject = subjectIds.isEmpty()
                ? Map.of()
                : curriculumSubjectRepository.findCoefficientsBySubjectIds(subjectIds).stream()
                        .filter(cs -> cs.getCoefficient() != null)
                        .collect(Collectors.toMap(
                                cs -> cs.getSubject().getId(),
                                CurriculumSubject::getCoefficient,
                                (a, b) -> a));

        // --- Indexation en mémoire ---
        // assessmentId -> assessment
        Map<Long, Assessment> assessmentById = assessments.stream()
                .collect(Collectors.toMap(Assessment::getId, a -> a));
        // assignmentId -> liste d'évaluations
        Map<Long, List<Assessment>> assessmentsByAssignment = assessments.stream()
                .filter(a -> a.getAssignment() != null)
                .collect(Collectors.groupingBy(a -> a.getAssignment().getId()));
        // "assessmentId:studentId" -> note (un élève = une note par évaluation)
        // Tri par ancienneté : en cas de doublons historiques, la note la plus
        // récente écrase les précédentes — même règle que resolveStudentNote().
        Map<String, BigDecimal> noteByAssessmentAndStudent = new HashMap<>();
        grades.stream()
                .filter(g -> g.getAssessment() != null && g.getStudent() != null && g.getNote() != null)
                .sorted(NOTE_RECENCY_COMPARATOR)
                .forEach(g -> noteByAssessmentAndStudent.put(
                        g.getAssessment().getId() + ":" + g.getStudent().getId(),
                        g.getNote()));

        // --- Calcul par élève ---
        Map<Long, List<SubjectResult>> resultsByEnrollment = new HashMap<>();
        for (Enrollment enrollment : enrollments) {
            Long studentId = enrollment.getStudent().getId();
            List<SubjectResult> results = new ArrayList<>();

            for (TeachingAssignment assignment : assignments) {
                Subject subject = assignment.getSubject();
                Integer subjectCoefficient = coefficientBySubject.getOrDefault(subject.getId(), 1);

                BigDecimal weightedSum = BigDecimal.ZERO;
                BigDecimal coeffSum = BigDecimal.ZERO;
                BigDecimal maximum = BigDecimal.ZERO;

                List<Assessment> subjectAssessments = assessmentsByAssignment.getOrDefault(assignment.getId(), List.of());
                for (Assessment assessment : subjectAssessments) {
                    BigDecimal evalCoeff = resolveAssessmentCoefficient(assessment);
                    BigDecimal noteMax = assessment.getNoteMax() != null ? assessment.getNoteMax() : BigDecimal.ZERO;
                    BigDecimal studentNote = noteByAssessmentAndStudent.getOrDefault(
                            assessment.getId() + ":" + studentId, BigDecimal.ZERO);

                    weightedSum = weightedSum.add(studentNote.multiply(evalCoeff));
                    coeffSum = coeffSum.add(evalCoeff);
                    maximum = maximum.add(noteMax.multiply(BigDecimal.valueOf(subjectCoefficient)));
                }

                BigDecimal moyenne = coeffSum.compareTo(BigDecimal.ZERO) > 0
                        ? weightedSum.divide(coeffSum, 2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;
                BigDecimal points = moyenne.multiply(BigDecimal.valueOf(subjectCoefficient));
                BigDecimal pourcentage = maximum.compareTo(BigDecimal.ZERO) > 0
                        ? points.divide(maximum, 2, RoundingMode.HALF_UP).multiply(ONE_HUNDRED)
                        : BigDecimal.ZERO;

                results.add(SubjectResult.builder()
                        .subject(subject)
                        .coefficient(subjectCoefficient)
                        .moyenne(moyenne)
                        .points(points)
                        .maximum(maximum)
                        .pourcentage(pourcentage)
                        .build());
            }
            resultsByEnrollment.put(enrollment.getId(), results);
        }

        log.info("Calcul batch des bulletins: {} élèves, {} matières, {} évaluations, {} notes (4 requêtes)",
                enrollments.size(), assignments.size(), assessments.size(), grades.size());
        return resultsByEnrollment;
    }

    @lombok.Data
    public static class GlobalResult {
        private final BigDecimal totalPoints;
        private final BigDecimal maximumPoints;
        private final BigDecimal pourcentage;
    }

    /**
     * Sélectionne LA note de référence d'un élève pour une évaluation.
     * En cas de doublons historiques, la note la plus récemment mise à jour
     * l'emporte (déterministe, aligné avec le calcul batch).
     */
    private BigDecimal resolveStudentNote(List<Grade> grades, Long studentId) {
        return grades.stream()
                .filter(g -> g.getStudent() != null && g.getStudent().getId().equals(studentId))
                .filter(g -> g.getNote() != null)
                .max(NOTE_RECENCY_COMPARATOR)
                .map(Grade::getNote)
                .orElse(BigDecimal.ZERO);
    }

    private Integer resolveSubjectCoefficient(Classroom classroom, Subject subject) {
        if (classroom.getReportTemplate() == null && classroom.getLevel() == null) {
            return 1;
        }
        // Recherche du coefficient via le programme lié à la classe (level/section/option)
        List<CurriculumSubject> curriculumSubjects = curriculumSubjectRepository.findBySubjectId(subject.getId());
        return curriculumSubjects.stream()
                .filter(cs -> cs.getCoefficient() != null)
                .map(CurriculumSubject::getCoefficient)
                .findFirst()
                .orElse(1);
    }

    private BigDecimal resolveAssessmentCoefficient(Assessment assessment) {
        if (assessment.getAssessmentType() != null && assessment.getAssessmentType().getCoefficient() != null) {
            return BigDecimal.valueOf(assessment.getAssessmentType().getCoefficient());
        }
        return BigDecimal.ONE;
    }

    public Integer computeSubjectRank(Enrollment enrollment, Subject subject, Period period) {
        Classroom classroom = enrollment.getClassroom();
        List<Enrollment> allEnrollments = enrollmentRepository.findByClassroomId(classroom.getId());

        List<BigDecimal> allAverages = allEnrollments.stream()
                .map(e -> computeSubjectResults(e, period).stream()
                        .filter(sr -> sr.getSubject().getId().equals(subject.getId()))
                        .findFirst()
                        .map(SubjectResult::getMoyenne)
                        .orElse(BigDecimal.ZERO)
                )
                .sorted(Comparator.reverseOrder())
                .toList();

        BigDecimal studentAverage = computeSubjectResults(enrollment, period).stream()
                .filter(sr -> sr.getSubject().getId().equals(subject.getId()))
                .findFirst()
                .map(SubjectResult::getMoyenne)
                .orElse(BigDecimal.ZERO);

        return allAverages.indexOf(studentAverage) + 1;
    }

    /**
     * Calcule les résultats par matière pour un élève (inscription) sur toute une année scolaire.
     * Agrège les résultats de toutes les périodes/trimestres.
     */
    public List<SubjectResult> computeAcademicYearSubjectResults(Enrollment enrollment, AcademicYear academicYear) {
        Classroom classroom = enrollment.getClassroom();
        Student student = enrollment.getStudent();

        // Récupérer tous les trimestres et périodes de l'année scolaire
        List<Trimester> trimesters = trimesterRepository.findByAcademicYearId(academicYear.getId());
        List<Period> periods = trimesters.stream()
                .flatMap(t -> t.getPeriods().stream())
                .toList();

        // Matières enseignées dans cette classe
        List<TeachingAssignment> assignments = teachingAssignmentRepository.findByClassroomId(classroom.getId());

        List<SubjectResult> annualResults = new ArrayList<>();

        for (TeachingAssignment assignment : assignments) {
            Subject subject = assignment.getSubject();
            Integer subjectCoefficient = resolveSubjectCoefficient(classroom, subject);

            BigDecimal annualWeightedSum = BigDecimal.ZERO;
            BigDecimal annualCoeffSum = BigDecimal.ZERO;
            BigDecimal annualMaximum = BigDecimal.ZERO;

            for (Period period : periods) {
                // Évaluations de cette matière pour cette période
                List<Assessment> assessments = assessmentRepository
                        .findByAssignmentId(assignment.getId()).stream()
                        .filter(a -> a.getPeriod() != null && a.getPeriod().getId().equals(period.getId()))
                        .toList();

                for (Assessment assessment : assessments) {
                    BigDecimal evalCoeff = resolveAssessmentCoefficient(assessment);
                    BigDecimal noteMax = assessment.getNoteMax() != null ? assessment.getNoteMax() : BigDecimal.ZERO;

                    List<Grade> grades = gradeRepository.findByAssessmentId(assessment.getId());
                    BigDecimal studentNote = resolveStudentNote(grades, student.getId());

                    annualWeightedSum = annualWeightedSum.add(studentNote.multiply(evalCoeff));
                    annualCoeffSum = annualCoeffSum.add(evalCoeff);
                    annualMaximum = annualMaximum.add(noteMax.multiply(BigDecimal.valueOf(subjectCoefficient)));
                }
            }

            BigDecimal annualMoyenne = annualCoeffSum.compareTo(BigDecimal.ZERO) > 0
                    ? annualWeightedSum.divide(annualCoeffSum, 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            BigDecimal annualPoints = annualMoyenne.multiply(BigDecimal.valueOf(subjectCoefficient));
            BigDecimal annualPourcentage = annualMaximum.compareTo(BigDecimal.ZERO) > 0
                    ? annualPoints.divide(annualMaximum, 2, RoundingMode.HALF_UP).multiply(ONE_HUNDRED)
                    : BigDecimal.ZERO;

            annualResults.add(SubjectResult.builder()
                    .subject(subject)
                    .coefficient(subjectCoefficient)
                    .moyenne(annualMoyenne)
                    .points(annualPoints)
                    .maximum(annualMaximum)
                    .pourcentage(annualPourcentage)
                    .build());
        }

        return annualResults;
    }

    /**
     * Calcule les résultats par matière pour un élève (inscription) et un trimestre donné,
     * en agrégeant uniquement les évaluations des périodes de type PERIODE dans ce trimestre.
     */
    public List<SubjectResult> computeSubjectResultsForTrimester(Enrollment enrollment, Trimester trimester) {
        Classroom classroom = enrollment.getClassroom();
        Student student = enrollment.getStudent();

        List<Period> periodePeriods = periodRepository.findByTrimesterId(trimester.getId()).stream()
                .filter(p -> p.getType() == Period.PeriodType.PERIODE)
                .toList();

        List<TeachingAssignment> assignments = teachingAssignmentRepository.findByClassroomId(classroom.getId());
        List<SubjectResult> results = new ArrayList<>();

        for (TeachingAssignment assignment : assignments) {
            Subject subject = assignment.getSubject();
            Integer subjectCoefficient = resolveSubjectCoefficient(classroom, subject);

            BigDecimal weightedSum = BigDecimal.ZERO;
            BigDecimal coeffSum = BigDecimal.ZERO;
            BigDecimal maximum = BigDecimal.ZERO;

            for (Period period : periodePeriods) {
                List<Assessment> assessments = assessmentRepository
                        .findByAssignmentId(assignment.getId()).stream()
                        .filter(a -> a.getPeriod() != null && a.getPeriod().getId().equals(period.getId()))
                        .toList();

                for (Assessment assessment : assessments) {
                    BigDecimal evalCoeff = resolveAssessmentCoefficient(assessment);
                    BigDecimal noteMax = assessment.getNoteMax() != null ? assessment.getNoteMax() : BigDecimal.ZERO;

                    List<Grade> grades = gradeRepository.findByAssessmentId(assessment.getId());
                    BigDecimal studentNote = resolveStudentNote(grades, student.getId());

                    weightedSum = weightedSum.add(studentNote.multiply(evalCoeff));
                    coeffSum = coeffSum.add(evalCoeff);
                    maximum = maximum.add(noteMax.multiply(BigDecimal.valueOf(subjectCoefficient)));
                }
            }

            BigDecimal moyenne = coeffSum.compareTo(BigDecimal.ZERO) > 0
                    ? weightedSum.divide(coeffSum, 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            BigDecimal points = moyenne.multiply(BigDecimal.valueOf(subjectCoefficient));
            BigDecimal pourcentage = maximum.compareTo(BigDecimal.ZERO) > 0
                    ? points.divide(maximum, 2, RoundingMode.HALF_UP).multiply(ONE_HUNDRED)
                    : BigDecimal.ZERO;

            results.add(SubjectResult.builder()
                    .subject(subject)
                    .coefficient(subjectCoefficient)
                    .moyenne(moyenne)
                    .points(points)
                    .maximum(maximum)
                    .pourcentage(pourcentage)
                    .build());
        }

        return results;
    }

    /**
     * Calcule les résultats par matière pour un élève (inscription) sur les épreuves (EXAMEN)
     * d'une année scolaire complète.
     */
    public List<SubjectResult> computeSubjectResultsForExams(Enrollment enrollment, AcademicYear academicYear) {
        Classroom classroom = enrollment.getClassroom();
        Student student = enrollment.getStudent();

        List<Period> examPeriods = periodRepository.findByTrimester_AcademicYearId(academicYear.getId()).stream()
                .filter(p -> p.getType() == Period.PeriodType.EXAMEN)
                .toList();

        List<TeachingAssignment> assignments = teachingAssignmentRepository.findByClassroomId(classroom.getId());
        List<SubjectResult> results = new ArrayList<>();

        for (TeachingAssignment assignment : assignments) {
            Subject subject = assignment.getSubject();
            Integer subjectCoefficient = resolveSubjectCoefficient(classroom, subject);

            BigDecimal weightedSum = BigDecimal.ZERO;
            BigDecimal coeffSum = BigDecimal.ZERO;
            BigDecimal maximum = BigDecimal.ZERO;

            for (Period period : examPeriods) {
                List<Assessment> assessments = assessmentRepository
                        .findByAssignmentId(assignment.getId()).stream()
                        .filter(a -> a.getPeriod() != null && a.getPeriod().getId().equals(period.getId()))
                        .toList();

                for (Assessment assessment : assessments) {
                    BigDecimal evalCoeff = resolveAssessmentCoefficient(assessment);
                    BigDecimal noteMax = assessment.getNoteMax() != null ? assessment.getNoteMax() : BigDecimal.ZERO;

                    List<Grade> grades = gradeRepository.findByAssessmentId(assessment.getId());
                    BigDecimal studentNote = resolveStudentNote(grades, student.getId());

                    weightedSum = weightedSum.add(studentNote.multiply(evalCoeff));
                    coeffSum = coeffSum.add(evalCoeff);
                    maximum = maximum.add(noteMax.multiply(BigDecimal.valueOf(subjectCoefficient)));
                }
            }

            BigDecimal moyenne = coeffSum.compareTo(BigDecimal.ZERO) > 0
                    ? weightedSum.divide(coeffSum, 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            BigDecimal points = moyenne.multiply(BigDecimal.valueOf(subjectCoefficient));
            BigDecimal pourcentage = maximum.compareTo(BigDecimal.ZERO) > 0
                    ? points.divide(maximum, 2, RoundingMode.HALF_UP).multiply(ONE_HUNDRED)
                    : BigDecimal.ZERO;

            results.add(SubjectResult.builder()
                    .subject(subject)
                    .coefficient(subjectCoefficient)
                    .moyenne(moyenne)
                    .points(points)
                    .maximum(maximum)
                    .pourcentage(pourcentage)
                    .build());
        }

        return results;
    }

    /**
     * Calcule le rang annuel d'un élève pour une matière donnée.
     */
    public Integer computeAcademicYearSubjectRank(Enrollment enrollment, Subject subject, AcademicYear academicYear) {
        Classroom classroom = enrollment.getClassroom();
        List<Enrollment> allEnrollments = enrollmentRepository.findByClassroomId(classroom.getId());

        List<BigDecimal> allAnnualAverages = allEnrollments.stream()
                .map(e -> computeAcademicYearSubjectResults(e, academicYear).stream()
                        .filter(sr -> sr.getSubject().getId().equals(subject.getId()))
                        .findFirst()
                        .map(SubjectResult::getMoyenne)
                        .orElse(BigDecimal.ZERO)
                )
                .sorted(Comparator.reverseOrder())
                .toList();

        BigDecimal studentAnnualAverage = computeAcademicYearSubjectResults(enrollment, academicYear).stream()
                .filter(sr -> sr.getSubject().getId().equals(subject.getId()))
                .findFirst()
                .map(SubjectResult::getMoyenne)
                .orElse(BigDecimal.ZERO);

        return allAnnualAverages.indexOf(studentAnnualAverage) + 1;
    }
}
