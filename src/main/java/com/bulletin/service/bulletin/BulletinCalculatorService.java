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
import java.util.List;

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

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

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

                List<Grade> grades = gradeRepository.findByAssessmentId(assessment.getId()).stream()
                        .filter(g -> g.getStudent() != null && g.getStudent().getId().equals(student.getId()))
                        .filter(g -> g.getNote() != null)
                        .toList();

                BigDecimal studentNote = grades.isEmpty() ? BigDecimal.ZERO : grades.get(0).getNote();

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

    @lombok.Data
    public static class GlobalResult {
        private final BigDecimal totalPoints;
        private final BigDecimal maximumPoints;
        private final BigDecimal pourcentage;
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
}
