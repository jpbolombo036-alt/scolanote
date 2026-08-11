package com.bulletin.service;

import com.bulletin.dto.bulletin.BulletinGenerateRequest;
import com.bulletin.mapper.AcademicYearReportCardMapper;
import com.bulletin.dto.bulletin.AcademicYearReportCardResponse;
import com.bulletin.dto.bulletin.AcademicYearReportCardDetailResponse;
import com.bulletin.dto.bulletin.ReportCardDetailResponse;
import com.bulletin.dto.bulletin.ReportCardResponse;
import com.bulletin.entity.*;
import com.bulletin.entity.Discipline;
import com.bulletin.exception.ResourceNotFoundException;
import com.bulletin.mapper.ReportCardMapper;
import com.bulletin.repository.*;
import com.bulletin.repository.AcademicYearRepository;
import com.bulletin.repository.AcademicYearReportCardDetailRepository;
import com.bulletin.repository.AcademicYearReportCardRepository;
import com.bulletin.repository.AttendanceRepository;
import com.bulletin.repository.DisciplineRepository;
import com.bulletin.repository.UserStudentRepository;
import com.bulletin.repository.UserTeacherRepository;
import com.bulletin.security.SecurityUtils;
import com.bulletin.service.bulletin.BulletinCalculatorService;
import com.bulletin.service.bulletin.BulletinPdfService;
import com.bulletin.service.bulletin.SubjectResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.bulletin.repository.AcademicYearReportCardRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportCardService {

    private final ReportCardRepository reportCardRepository;
    private final AcademicYearReportCardRepository academicYearReportCardRepository;
    private final AcademicYearRepository academicYearRepository;
    private final ReportCardDetailRepository reportCardDetailRepository;
    private final AcademicYearReportCardDetailRepository academicYearReportCardDetailRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ClassroomRepository classroomRepository;
    private final PeriodRepository periodRepository;
    private final ReportCardMapper reportCardMapper;
    private final BulletinCalculatorService calculator;
    private final AcademicYearReportCardMapper academicYearReportCardMapper; // Injecter le nouveau mapper
    private final SecurityUtils securityUtils;
    private final UserStudentRepository userStudentRepository;
    private final UserTeacherRepository userTeacherRepository;
    private final AttendanceRepository attendanceRepository;
    private final DisciplineRepository disciplineRepository;
    private final TeachingAssignmentRepository teachingAssignmentRepository;
    private final AssessmentRepository assessmentRepository;
    private final GradeRepository gradeRepository;
    private final TrimesterRepository trimesterRepository;
    private final BulletinPdfService bulletinPdfService;
    private final PeriodClosureService periodClosureService;

    @Value("${app.bulletin.mention.excellent:85}")
    private BigDecimal mentionExcellent;
    @Value("${app.bulletin.mention.tres-bien:70}")
    private BigDecimal mentionTresBien;
    @Value("${app.bulletin.mention.bien:60}")
    private BigDecimal mentionBien;
    @Value("${app.bulletin.mention.satisfaction:50}")
    private BigDecimal mentionSatisfaction;
    @Value("${app.bulletin.decision.admis:50}")
    private BigDecimal decisionAdmis;

    @Transactional
    public List<ReportCardResponse> generateBulletins(BulletinGenerateRequest request) {
        Classroom classroom = classroomRepository.findById(request.getClassroomId())
                .orElseThrow(() -> new ResourceNotFoundException("Classe non trouvée avec l'ID: " + request.getClassroomId()));
        Period period = periodRepository.findById(request.getPeriodId())
                .orElseThrow(() -> new ResourceNotFoundException("Période non trouvée avec l'ID: " + request.getPeriodId()));

        assertCanGenerateBulletins(classroom);

        List<Enrollment> enrollments = enrollmentRepository.findByClassroomId(classroom.getId());

        // ===== CALCUL BATCH (anti N+1) : charge tout en 4 requêtes puis calcule en mémoire =====
        Map<Long, List<SubjectResult>> resultsByEnrollment =
                calculator.computeClassSubjectResults(classroom, period, enrollments);

        // Pourcentage global par élève (en mémoire)
        Map<Long, BigDecimal> percentagesByEnrollment = enrollments.stream()
                .collect(Collectors.toMap(
                        Enrollment::getId,
                        e -> calculator.computeGlobalResult(
                                resultsByEnrollment.getOrDefault(e.getId(), List.of())).getPourcentage()
                ));

        List<Long> ranking = percentagesByEnrollment.entrySet().stream()
                .sorted(Map.Entry.<Long, BigDecimal>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .toList();

        // Rangs par matière (calculés en mémoire, sans requêtes supplémentaires)
        Map<Long, Map<Long, Integer>> subjectRanksByEnrollment = computeSubjectRanks(enrollments, resultsByEnrollment);

        List<ReportCardResponse> responses = new java.util.ArrayList<>();

        for (Enrollment enrollment : enrollments) {
            List<SubjectResult> subjectResults = resultsByEnrollment.getOrDefault(enrollment.getId(), List.of());
            BulletinCalculatorService.GlobalResult global = calculator.computeGlobalResult(subjectResults);
            Map<Long, Integer> subjectRanks = subjectRanksByEnrollment.getOrDefault(enrollment.getId(), Map.of());

            int rang = ranking.indexOf(enrollment.getId()) + 1;
            String mention = resolveMention(global.getPourcentage());
            String decision = global.getPourcentage().compareTo(decisionAdmis) >= 0 ? "ADMIS" : "ECHEC";

            reportCardRepository.findByEnrollmentId(enrollment.getId()).stream()
                    .filter(rc -> rc.getPeriod() != null && rc.getPeriod().getId().equals(period.getId()))
                    .forEach(rc -> {
                        reportCardDetailRepository.findByReportCardId(rc.getId())
                                .forEach(reportCardDetailRepository::delete);
                        rc.setDeletedAt(java.time.LocalDateTime.now());
                        reportCardRepository.save(rc);
                    });

            Map<Long, String> appreciations = new HashMap<>();
            for (TeachingAssignment assignment : teachingAssignmentRepository.findByClassroomId(classroom.getId())) {
                List<Assessment> assessments = assessmentRepository.findByAssignmentId(assignment.getId()).stream()
                        .filter(a -> a.getPeriod() != null && a.getPeriod().getId().equals(period.getId()))
                        .toList();

                for (Assessment assessment : assessments) {
                    Grade grade = gradeRepository.findByAssessmentId(assessment.getId()).stream()
                            .filter(g -> g.getStudent() != null && g.getStudent().getId().equals(enrollment.getStudent().getId()))
                            .findFirst()
                            .orElse(null);

                    if (grade != null && grade.getObservation() != null) {
                        if (assessment.getAssignment() != null && assessment.getAssignment().getSubject() != null
                                && assessment.getAssignment().getSubject().getId() != null) {
                            appreciations.put(assessment.getAssignment().getSubject().getId(), grade.getObservation());
                        }
                    }
                }
            }

            long absences = attendanceRepository.countByStudentIdAndPeriodIdAndRetardFalseAndAbsenceTrue(
                    enrollment.getStudent().getId(), period.getId());
            long retards = attendanceRepository.countByStudentIdAndPeriodIdAndRetardTrueAndAbsenceFalse(
                    enrollment.getStudent().getId(), period.getId());

            Discipline discipline = disciplineRepository.findByStudentIdAndPeriodId(enrollment.getStudent().getId(), period.getId());

            ReportCard reportCard = ReportCard.builder()
                    .enrollment(enrollment)
                    .period(period)
                    .pourcentage(global.getPourcentage())
                    .totalPoints(global.getTotalPoints())
                    .maximumPoints(global.getMaximumPoints())
                    .rang(rang)
                    .mention(mention)
                    .decision(decision)
                    .totalAbsences((int) absences)
                    .totalRetards((int) retards)
                    .conduite(discipline != null ? discipline.getConduite() : null)
                    .application(discipline != null ? discipline.getApplication() : null)
                    .build();
            reportCard = reportCardRepository.save(reportCard);

            for (SubjectResult sr : subjectResults) {
                ReportCardDetail detail = ReportCardDetail.builder()
                        .reportCard(reportCard)
                        .subject(sr.getSubject())
                        .coefficient(sr.getCoefficient())
                        .moyenne(sr.getMoyenne())
                        .points(sr.getPoints())
                        .maximum(sr.getMaximum())
                        .pourcentage(sr.getPourcentage())
                        .rangMatiere(subjectRanks.get(sr.getSubject().getId()))
                        .observation(appreciations.get(sr.getSubject().getId()))
                        .build();
                reportCardDetailRepository.save(detail);
            }

            log.info("Bulletin généré: enrollment={} pourcentage={} rang={} mention={}",
                    enrollment.getId(), global.getPourcentage(), rang, mention);

            responses.add(toResponse(reportCard));
        }

        try {
            if (!period.isVerrouille()) {
                periodClosureService.verrouillerPeriode(period.getId());
            }
        } catch (IllegalStateException ex) {
            log.info("Période déjà verrouillée, verrouillage automatique ignoré pour la génération du bulletin : {}", ex.getMessage());
        } catch (SecurityException ex) {
            log.info("L'utilisateur n'a pas la permission de verrouiller la période automatiquement : {}", ex.getMessage());
        }

        periodClosureService.verrouillerAnneeSiComplete(period.getId());

        return responses;
    }

    @Transactional(readOnly = true)
    public ReportCardResponse getReportCard(Long id) {
        return toResponse(findById(id));
    }

    @Transactional(readOnly = true)
    public List<ReportCardResponse> getByEnrollment(Long enrollmentId) {
        return reportCardRepository.findByEnrollmentId(enrollmentId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReportCardResponse> getByTerm(Long termId) {
        return reportCardRepository.findByPeriodId(termId).stream()
                .map(this::toResponse)
                .toList();
    }

        @Transactional
        public ReportCardResponse generateForEnrollment(Long enrollmentId, Long periodId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Inscription non trouvée avec l'ID: " + enrollmentId));
        Period period = periodRepository.findById(periodId)
            .orElseThrow(() -> new ResourceNotFoundException("Période non trouvée avec l'ID: " + periodId));

        // permission check (reuses existing assertion for classroom-level access)
        assertCanGenerateBulletins(enrollment.getClassroom());

        // --- Calcul des résultats pour l'inscription ---
        List<SubjectResult> subjectResults = calculator.computeSubjectResults(enrollment, period);
        BulletinCalculatorService.GlobalResult global = calculator.computeGlobalResult(subjectResults);

        // Pour calculer les rangs, charge les inscriptions de la classe et calcule en mémoire
        List<Enrollment> enrollments = enrollmentRepository.findByClassroomId(enrollment.getClassroom().getId());
        Map<Long, List<SubjectResult>> resultsByEnrollment = calculator.computeClassSubjectResults(enrollment.getClassroom(), period, enrollments);
        Map<Long, BigDecimal> percentagesByEnrollment = enrollments.stream()
            .collect(Collectors.toMap(Enrollment::getId, e -> calculator.computeGlobalResult(resultsByEnrollment.getOrDefault(e.getId(), List.of())).getPourcentage()));
        List<Long> ranking = percentagesByEnrollment.entrySet().stream()
            .sorted(Map.Entry.<Long, BigDecimal>comparingByValue().reversed())
            .map(Map.Entry::getKey)
            .toList();
        Map<Long, Map<Long, Integer>> subjectRanksByEnrollment = computeSubjectRanks(enrollments, resultsByEnrollment);

        int rang = ranking.indexOf(enrollment.getId()) + 1;
        String mention = resolveMention(global.getPourcentage());
        String decision = global.getPourcentage().compareTo(decisionAdmis) >= 0 ? "ADMIS" : "ECHEC";

        // Supprime les bulletins existants pour cette inscription+période
        reportCardRepository.findByEnrollmentId(enrollment.getId()).stream()
            .filter(rc -> rc.getPeriod() != null && rc.getPeriod().getId().equals(period.getId()))
            .forEach(rc -> {
                reportCardDetailRepository.findByReportCardId(rc.getId()).forEach(reportCardDetailRepository::delete);
                rc.setDeletedAt(java.time.LocalDateTime.now());
                reportCardRepository.save(rc);
            });

        long absences = attendanceRepository.countByStudentIdAndPeriodIdAndRetardFalseAndAbsenceTrue(enrollment.getStudent().getId(), period.getId());
        long retards = attendanceRepository.countByStudentIdAndPeriodIdAndRetardTrueAndAbsenceFalse(enrollment.getStudent().getId(), period.getId());
        Discipline discipline = disciplineRepository.findByStudentIdAndPeriodId(enrollment.getStudent().getId(), period.getId());

        ReportCard reportCard = ReportCard.builder()
            .enrollment(enrollment)
            .period(period)
            .pourcentage(global.getPourcentage())
            .totalPoints(global.getTotalPoints())
            .maximumPoints(global.getMaximumPoints())
            .rang(rang)
            .mention(mention)
            .decision(decision)
            .totalAbsences((int) absences)
            .totalRetards((int) retards)
            .conduite(discipline != null ? discipline.getConduite() : null)
            .application(discipline != null ? discipline.getApplication() : null)
            .build();
        reportCard = reportCardRepository.save(reportCard);

        Map<Long, Integer> subjectRanks = subjectRanksByEnrollment.getOrDefault(enrollment.getId(), Map.of());
        for (SubjectResult sr : subjectResults) {
            ReportCardDetail detail = ReportCardDetail.builder()
                .reportCard(reportCard)
                .subject(sr.getSubject())
                .coefficient(sr.getCoefficient())
                .moyenne(sr.getMoyenne())
                .points(sr.getPoints())
                .maximum(sr.getMaximum())
                .pourcentage(sr.getPourcentage())
                .rangMatiere(subjectRanks.get(sr.getSubject().getId()))
                .observation(null)
                .build();
            reportCardDetailRepository.save(detail);
        }

        return toResponse(reportCard);
        }

    @Transactional(readOnly = true)
    public Page<ReportCardResponse> getAccessibleReportCards(Pageable pageable) {
        if (securityUtils.isSuperAdmin()) {
            return reportCardRepository.findAll(pageable)
                    .map(this::toResponse);
        }
        return reportCardRepository.findBySchoolId(securityUtils.getCurrentSchoolId(), pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<ReportCardResponse> getAccessibleReportCards() {
        if (securityUtils.isSuperAdmin()) {
            return reportCardRepository.findAll().stream()
                    .map(this::toResponse)
                    .toList();
        }
        return reportCardRepository.findBySchoolId(securityUtils.getCurrentSchoolId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public List<AcademicYearReportCardResponse> generateAcademicYearBulletins(Long academicYearId, Long classroomId) {
        AcademicYear academicYear = academicYearRepository.findById(academicYearId)
                .orElseThrow(() -> new ResourceNotFoundException("Année scolaire non trouvée avec l'ID: " + academicYearId));
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new ResourceNotFoundException("Classe non trouvée avec l'ID: " + classroomId));

        // Vérifier les permissions pour générer les bulletins annuels
        securityUtils.assertPermission("BULLETIN_ANNUEL_GENERER"); // Nouvelle permission
        securityUtils.assertSchoolAccess(academicYear.getSchool() != null ? academicYear.getSchool().getId() : null);
        securityUtils.assertSchoolAccess(classroom.getAcademicYear() != null && classroom.getAcademicYear().getSchool() != null ? classroom.getAcademicYear().getSchool().getId() : null);

        List<Enrollment> enrollments = enrollmentRepository.findByClassroomId(classroom.getId());
        List<AcademicYearReportCardResponse> responses = new java.util.ArrayList<>();

        // Pour chaque élève, calculer le bulletin annuel
        for (Enrollment enrollment : enrollments) {
            // Calcul des résultats par matière pour l'année entière
            List<SubjectResult> annualSubjectResults = calculator.computeAcademicYearSubjectResults(enrollment, academicYear);
            BulletinCalculatorService.GlobalResult annualGlobalResult = calculator.computeGlobalResult(annualSubjectResults);

            // Calcul du rang général annuel
            // Ceci est une version simplifiée. Pour un rang précis, il faudrait calculer les pourcentages annuels
            // de tous les élèves de la classe et les trier.
            // Pour l'exemple, nous allons juste prendre un rang temporaire ou le recalculer plus tard.
            // TODO: Implémenter le calcul du rang annuel global de manière plus robuste.
            int annualRang = 1; // Placeholder
            String annualMention = resolveMention(annualGlobalResult.getPourcentage());
            String annualDecision = annualGlobalResult.getPourcentage().compareTo(decisionAdmis) >= 0 ? "ADMIS" : "ECHEC";

            // Agrégation des absences et retards sur l'année
            long totalAbsences = enrollmentRepository.findByStudentId(enrollment.getStudent().getId()).stream()
                    .flatMap(e -> attendanceRepository.findByStudentId(e.getStudent().getId()).stream())
                    .filter(a -> a.getPeriod() != null && a.getPeriod().getTrimester() != null && a.getPeriod().getTrimester().getAcademicYear().getId().equals(academicYear.getId()))
                    .filter(Attendance::isAbsence)
                    .count();
            long totalRetards = enrollmentRepository.findByStudentId(enrollment.getStudent().getId()).stream()
                    .flatMap(e -> attendanceRepository.findByStudentId(e.getStudent().getId()).stream())
                    .filter(a -> a.getPeriod() != null && a.getPeriod().getTrimester() != null && a.getPeriod().getTrimester().getAcademicYear().getId().equals(academicYear.getId()))
                    .filter(Attendance::isRetard)
                    .count();

            // Agrégation de la conduite et de l'application (simplifié pour l'exemple, pourrait être plus complexe)
            // Par exemple, prendre la dernière valeur ou la plus fréquente.
            String conduite = "Satisfaisante"; // Placeholder
            String application = "Bonne"; // Placeholder

            // Supprimer l'ancien bulletin annuel s'il existe
            academicYearReportCardRepository.findByEnrollmentIdAndAcademicYearId(enrollment.getId(), academicYear.getId())
                    .stream().findFirst().ifPresent(ar -> {
                        academicYearReportCardDetailRepository.findByAcademicYearReportCardId(ar.getId())
                                .forEach(academicYearReportCardDetailRepository::delete);
                        ar.setDeletedAt(java.time.LocalDateTime.now());
                        academicYearReportCardRepository.save(ar);
                    });

            AcademicYearReportCard academicYearReportCard = AcademicYearReportCard.builder()
                    .enrollment(enrollment)
                    .academicYear(academicYear)
                    .pourcentage(annualGlobalResult.getPourcentage())
                    .totalPoints(annualGlobalResult.getTotalPoints())
                    .maximumPoints(annualGlobalResult.getMaximumPoints())
                    .rang(annualRang)
                    .mention(annualMention)
                    .decision(annualDecision)
                    .totalAbsences((int) totalAbsences)
                    .totalRetards((int) totalRetards)
                    .conduite(conduite)
                    .application(application)
                    .dateGeneration(LocalDateTime.now())
                    .statut(AcademicYearReportCard.Statut.BROUILLON.name())
                    .build();
            academicYearReportCard = academicYearReportCardRepository.save(academicYearReportCard);

            for (SubjectResult sr : annualSubjectResults) { // Correction: Utiliser annualSubjectResults
                Integer rangMatiereAnnuel = calculator.computeAcademicYearSubjectRank(enrollment, sr.getSubject(), academicYear);

                List<Trimester> trimesters = trimesterRepository.findByAcademicYearId(academicYear.getId());
                trimesters.sort(Comparator.comparing(t -> t.getOrdre() != null ? t.getOrdre() : Integer.MAX_VALUE));
                BigDecimal t1 = BigDecimal.ZERO;
                BigDecimal t2 = BigDecimal.ZERO;
                BigDecimal t3 = BigDecimal.ZERO;
                BigDecimal exam = BigDecimal.ZERO;

                for (Trimester trimester : trimesters) {
                    List<SubjectResult> trimesterResults = calculator.computeSubjectResultsForTrimester(enrollment, trimester);
                    BigDecimal moyenne = trimesterResults.stream()
                            .filter(tr -> tr.getSubject().getId().equals(sr.getSubject().getId()))
                            .findFirst()
                            .map(SubjectResult::getMoyenne)
                            .orElse(BigDecimal.ZERO);
                    switch (trimester.getOrdre() != null ? trimester.getOrdre() : 0) {
                        case 1 -> t1 = moyenne;
                        case 2 -> t2 = moyenne;
                        case 3 -> t3 = moyenne;
                        default -> {}
                    }
                }

                List<SubjectResult> examResults = calculator.computeSubjectResultsForExams(enrollment, academicYear);
                exam = examResults.stream()
                        .filter(er -> er.getSubject().getId().equals(sr.getSubject().getId()))
                        .findFirst()
                        .map(SubjectResult::getMoyenne)
                        .orElse(BigDecimal.ZERO);

                AcademicYearReportCardDetail detail = AcademicYearReportCardDetail.builder()
                        .academicYearReportCard(academicYearReportCard)
                        .subject(sr.getSubject())
                        .coefficient(sr.getCoefficient())
                        .moyenne(sr.getMoyenne())
                        .points(sr.getPoints())
                        .maximum(sr.getMaximum())
                        .pourcentage(sr.getPourcentage())
                        .rangMatiere(rangMatiereAnnuel)
                        .observation(sr.getObservation())
                        .moyenneT1(t1)
                        .moyenneT2(t2)
                        .moyenneT3(t3)
                        .moyenneExamen(exam)
                        .build();
                academicYearReportCardDetailRepository.save(detail);
            }

            log.info("Bulletin annuel généré: enrollment={} academicYear={} pourcentage={} rang={} mention={}",
                    enrollment.getId(), academicYear.getId(), annualGlobalResult.getPourcentage(), annualRang, annualMention);

            responses.add(toAcademicYearReportCardResponse(academicYearReportCard));
        }

        return responses;
    }

    private AcademicYearReportCardResponse toAcademicYearReportCardResponse(AcademicYearReportCard reportCard) {
        return academicYearReportCardMapper.toResponse(reportCard);
    }

    private ReportCard findById(Long id) {
        ReportCard reportCard = reportCardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bulletin non trouvé avec l'ID: " + id));
        securityUtils.assertSchoolAccess(reportCard.getSchoolId());
        return reportCard;
    }

    /**
     * Résout la mention (libellé français) selon les seuils configurés.
     * Source de vérité unique : l'enum Mention (système congolais).
     * Le libellé est stocké tel quel en BDD, donc aucun mapping supplémentaire à l'affichage.
     */
    private String resolveMention(BigDecimal pourcentage) {
        return com.bulletin.entity.enums.Mention.from(
                pourcentage, mentionExcellent, mentionTresBien, mentionBien, mentionSatisfaction
        ).getLibelle();
    }

    private ReportCardResponse toResponse(ReportCard reportCard) {
        ReportCardResponse response = reportCardMapper.toResponse(reportCard);
        if (response.getPourcentage() != null) {
            response.setMoyenne(response.getPourcentage().divide(new BigDecimal("5"), 2, RoundingMode.HALF_UP));
        }
        if (reportCard.getPeriod() != null && reportCard.getPeriod().getTrimester() != null) {
            response.setTrimesterNom(reportCard.getPeriod().getTrimester().getNom());
        }
        if (reportCard.getEnrollment() != null && reportCard.getEnrollment().getStudent() != null) {
            com.bulletin.entity.Student student = reportCard.getEnrollment().getStudent();
            String fullName = student.getNom();
            if (student.getPostnom() != null && !student.getPostnom().isEmpty()) {
                fullName += " " + student.getPostnom();
            }
            if (student.getPrenom() != null && !student.getPrenom().isEmpty()) {
                fullName += " " + student.getPrenom();
            }
            response.setStudentNom(fullName);
            response.setEleveNomComplet(fullName);
            response.setElevePrenom(student.getPrenom());
            response.setEleveNom(student.getNom());
        }
        // La mention est déjà stockée en libellé français en BDD (enum Mention) : pas de mapping.
        response.setMention(reportCard.getMention());
        List<ReportCardDetailResponse> details = reportCardDetailRepository.findByReportCardId(reportCard.getId()).stream()
                .map(d -> ReportCardDetailResponse.builder()
                        .id(d.getId())
                        .subjectId(d.getSubject() != null ? d.getSubject().getId() : null)
                        .subjectNom(d.getSubject() != null ? d.getSubject().getNom() : null)
                        .subjectCode(d.getSubject() != null ? d.getSubject().getCode() : null)
                        .coefficient(d.getCoefficient())
                        .moyenne(d.getMoyenne())
                        .rangMatiere(d.getRangMatiere())
                        .points(d.getPoints())
                        .maximum(d.getMaximum())
                        .pourcentage(d.getPourcentage())
                        .appreciation(d.getObservation())
                        .build())
                .sorted((a, b) -> {
                    String nomA = a.getSubjectNom() == null ? "" : a.getSubjectNom();
                    String nomB = b.getSubjectNom() == null ? "" : b.getSubjectNom();
                    return nomA.compareTo(nomB);
                })
                .toList();
        response.setDetails(details);
        return response;
    }

    /**
     * Calcule les rangs par matière pour chaque élève, EN MÉMOIRE (aucune requête).
     *
     * Pour chaque matière, on trie les moyennes de tous les élèves (ordre décroissant),
     * puis on détermine la position de chaque élève.
     *
     * @return map enrollmentId -> (subjectId -> rang)
     */
    private Map<Long, Map<Long, Integer>> computeSubjectRanks(
            List<Enrollment> enrollments,
            Map<Long, List<SubjectResult>> resultsByEnrollment) {

        // subjectId -> liste triée (desc) des moyennes de la classe
        Map<Long, List<BigDecimal>> averagesBySubject = new HashMap<>();
        for (List<SubjectResult> results : resultsByEnrollment.values()) {
            for (SubjectResult sr : results) {
                if (sr.getSubject() == null || sr.getMoyenne() == null) {
                    continue;
                }
                averagesBySubject.computeIfAbsent(sr.getSubject().getId(), k -> new java.util.ArrayList<>())
                        .add(sr.getMoyenne());
            }
        }
        averagesBySubject.values().forEach(list -> list.sort(Comparator.reverseOrder()));

        // enrollmentId -> (subjectId -> rang)
        Map<Long, Map<Long, Integer>> ranksByEnrollment = new HashMap<>();
        for (Enrollment enrollment : enrollments) {
            Map<Long, Integer> ranks = new HashMap<>();
            List<SubjectResult> results = resultsByEnrollment.getOrDefault(enrollment.getId(), List.of());
            for (SubjectResult sr : results) {
                if (sr.getSubject() == null || sr.getMoyenne() == null) {
                    continue;
                }
                List<BigDecimal> averages = averagesBySubject.getOrDefault(sr.getSubject().getId(), List.of());
                int rang = averages.indexOf(sr.getMoyenne()) + 1;
                ranks.put(sr.getSubject().getId(), rang > 0 ? rang : null);
            }
            ranksByEnrollment.put(enrollment.getId(), ranks);
        }
        return ranksByEnrollment;
    }

    public List<ReportCardResponse> getMyReportCards(Long trimestreId) {
        Long currentUserId = securityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new SecurityException("Authentification requise");
        }

        List<UserStudent> links = userStudentRepository.findByUserId(currentUserId);
        if (links.isEmpty()) {
            return List.of();
        }

        List<Long> studentIds = links.stream()
                .map(us -> us.getStudent().getId())
                .toList();

        List<Enrollment> enrollments = enrollmentRepository.findByStudentIdIn(studentIds);
        if (enrollments.isEmpty()) {
            return List.of();
        }

        List<ReportCard> reportCards = reportCardRepository.findByEnrollmentIn(enrollments);

        if (trimestreId != null) {
            reportCards = reportCards.stream()
                    .filter(rc -> rc.getPeriod() != null && rc.getPeriod().getTrimester() != null && rc.getPeriod().getTrimester().getId().equals(trimestreId))
                    .toList();
        }

        return reportCards.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<AcademicYearReportCardResponse> getAccessibleAcademicYearReportCards(Pageable pageable) {
        if (securityUtils.isSuperAdmin()) {
            return academicYearReportCardRepository.findAll(pageable)
                    .map(this::toAcademicYearReportCardResponse);
        }
        return academicYearReportCardRepository.findBySchoolId(securityUtils.getCurrentSchoolId(), pageable)
                .map(this::toAcademicYearReportCardResponse);
    }

    @Transactional(readOnly = true)
    public AcademicYearReportCardResponse getAcademicYearReportCard(Long id) {
        AcademicYearReportCard reportCard = academicYearReportCardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bulletin annuel non trouvé avec l'ID: " + id));
        Long schoolId = reportCard.getSchoolId();
        securityUtils.assertSchoolAccess(schoolId);
        return toAcademicYearReportCardResponse(reportCard);
    }

    @Transactional(readOnly = true)
    public List<AcademicYearReportCardResponse> getAcademicYearReportCardsByEnrollment(Long enrollmentId) {
        return academicYearReportCardRepository.findByEnrollmentId(enrollmentId).stream()
                .map(this::toAcademicYearReportCardResponse)
                .toList();
    }

    public byte[] generateAcademicYearPdf(Long id) {
        AcademicYearReportCard reportCard = academicYearReportCardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bulletin annuel non trouvé avec l'ID: " + id));
        securityUtils.assertSchoolAccess(reportCard.getSchoolId());
        bulletinPdfService.generateAcademicYearPdf(id);
        try {
            return bulletinPdfService.loadPdf(id);
        } catch (Exception e) {
            throw new RuntimeException("Échec du chargement du PDF annuel", e);
        }
    }

    private void assertCanGenerateBulletins(Classroom classroom) {
        // Permission granulaire BULLETIN_GENERER (ou direction, rétrocompatible)
        if (securityUtils.hasPermission("BULLETIN_GENERER") || securityUtils.isDirection()) {
            return;
        }
        Long currentUserId = securityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new SecurityException("Authentification requise");
        }

        // classroom.getTitulaireId() référence un Teacher.id (pas un User.id).
        // On vérifie que le compte User connecté est lié, via UserTeacher, au Teacher titulaire.
        Long titulaireTeacherId = classroom.getTitulaireId();
        if (titulaireTeacherId == null) {
            throw new SecurityException("Accès refusé : aucun titulaire n'est défini pour cette classe");
        }

        // Requête ciblée (au lieu de charger toute la table user_teachers en mémoire)
        boolean owns = userTeacherRepository.existsByUser_IdAndTeacher_Id(currentUserId, titulaireTeacherId);
        if (!owns) {
            throw new SecurityException("Accès refusé : seuls la direction ou le titulaire de la classe peuvent générer les bulletins");
        }
    }
}
