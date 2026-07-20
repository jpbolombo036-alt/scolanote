package com.bulletin.service;

import com.bulletin.dto.bulletin.BulletinGenerateRequest;
import com.bulletin.dto.bulletin.ReportCardDetailResponse;
import com.bulletin.dto.bulletin.ReportCardResponse;
import com.bulletin.entity.*;
import com.bulletin.exception.ResourceNotFoundException;
import com.bulletin.mapper.ReportCardMapper;
import com.bulletin.repository.*;
import com.bulletin.repository.UserTeacherRepository;
import com.bulletin.security.SecurityUtils;
import com.bulletin.service.bulletin.BulletinCalculatorService;
import com.bulletin.service.bulletin.SubjectResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportCardService {

    private final ReportCardRepository reportCardRepository;
    private final ReportCardDetailRepository reportCardDetailRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ClassroomRepository classroomRepository;
    private final TermRepository termRepository;
    private final ReportCardMapper reportCardMapper;
    private final BulletinCalculatorService calculator;
    private final SecurityUtils securityUtils;
    private final UserTeacherRepository userTeacherRepository;

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

    /**
     * Génère (ou régénère) les bulletins de toute une classe pour un trimestre.
     * Calcul automatique des moyennes, points, pourcentage, rang, mention et décision.
     */
    @Transactional
    public List<ReportCardResponse> generateBulletins(BulletinGenerateRequest request) {
        Classroom classroom = classroomRepository.findById(request.getClassroomId())
                .orElseThrow(() -> new ResourceNotFoundException("Classe non trouvée avec l'ID: " + request.getClassroomId()));
        Term term = termRepository.findById(request.getTermId())
                .orElseThrow(() -> new ResourceNotFoundException("Trimestre non trouvé avec l'ID: " + request.getTermId()));

        assertCanGenerateBulletins(classroom);

        List<Enrollment> enrollments = enrollmentRepository.findByClassroomId(classroom.getId());

        // Calcul des pourcentages par élève pour le classement
        Map<Long, BigDecimal> percentagesByEnrollment = enrollments.stream()
                .collect(Collectors.toMap(
                        Enrollment::getId,
                        e -> calculator.computeGlobalResult(calculator.computeSubjectResults(e, term)).getPourcentage()
                ));

        List<Long> ranking = percentagesByEnrollment.entrySet().stream()
                .sorted(Map.Entry.<Long, BigDecimal>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .toList();

        List<ReportCardResponse> responses = new java.util.ArrayList<>();

        for (Enrollment enrollment : enrollments) {
            List<SubjectResult> subjectResults = calculator.computeSubjectResults(enrollment, term);
            BulletinCalculatorService.GlobalResult global = calculator.computeGlobalResult(subjectResults);

            int rang = ranking.indexOf(enrollment.getId()) + 1;
            String mention = resolveMention(global.getPourcentage());
            String decision = global.getPourcentage().compareTo(decisionAdmis) >= 0 ? "ADMIS" : "ECHEC";

            // Suppression des bulletins existants pour cette inscription + trimestre
            reportCardRepository.findByEnrollmentId(enrollment.getId()).stream()
                    .filter(rc -> rc.getTerm() != null && rc.getTerm().getId().equals(term.getId()))
                    .forEach(rc -> {
                        reportCardDetailRepository.findByReportCardId(rc.getId())
                                .forEach(reportCardDetailRepository::delete);
                        rc.setDeletedAt(java.time.LocalDateTime.now());
                        reportCardRepository.save(rc);
                    });

            ReportCard reportCard = ReportCard.builder()
                    .enrollment(enrollment)
                    .term(term)
                    .pourcentage(global.getPourcentage())
                    .totalPoints(global.getTotalPoints())
                    .maximumPoints(global.getMaximumPoints())
                    .rang(rang)
                    .mention(mention)
                    .decision(decision)
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
                        .build();
                reportCardDetailRepository.save(detail);
            }

            log.info("Bulletin généré: enrollment={} pourcentage={} rang={} mention={}",
                    enrollment.getId(), global.getPourcentage(), rang, mention);

            responses.add(toResponse(reportCard));
        }

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
        return reportCardRepository.findByTermId(termId).stream()
                .map(this::toResponse)
                .toList();
    }

    private ReportCard findById(Long id) {
        return reportCardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bulletin non trouvé avec l'ID: " + id));
    }

    private String resolveMention(BigDecimal pourcentage) {
        if (pourcentage.compareTo(mentionExcellent) >= 0) return "EXCELLENT";
        if (pourcentage.compareTo(mentionTresBien) >= 0) return "TRES BIEN";
        if (pourcentage.compareTo(mentionBien) >= 0) return "BIEN";
        if (pourcentage.compareTo(mentionSatisfaction) >= 0) return "SATISFACTION";
        return "ECHEC";
    }

    private ReportCardResponse toResponse(ReportCard reportCard) {
        ReportCardResponse response = reportCardMapper.toResponse(reportCard);
        List<ReportCardDetailResponse> details = reportCardDetailRepository.findByReportCardId(reportCard.getId()).stream()
                .map(d -> ReportCardDetailResponse.builder()
                        .id(d.getId())
                        .subjectId(d.getSubject() != null ? d.getSubject().getId() : null)
                        .subjectNom(d.getSubject() != null ? d.getSubject().getNom() : null)
                        .subjectCode(d.getSubject() != null ? d.getSubject().getCode() : null)
                        .coefficient(d.getCoefficient())
                        .moyenne(d.getMoyenne())
                        .points(d.getPoints())
                        .maximum(d.getMaximum())
                        .pourcentage(d.getPourcentage())
                        .observation(d.getObservation())
                        .build())
                .sorted(Comparator.comparing(d -> d.getSubjectNom() == null ? "" : d.getSubjectNom()))
                .toList();
        response.setDetails(details);
        return response;
    }

    /**
     * Section 7 : la génération de bulletins est réservée à la direction
     * (SUPER_ADMIN/ADMIN/DIRECTEUR/PREFET) ou au titulaire de la classe.
     */
    private void assertCanGenerateBulletins(Classroom classroom) {
        if (securityUtils.isDirection()) {
            return;
        }
        Long currentUserId = securityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new SecurityException("Authentification requise");
        }
        if (classroom.getTitulaireId() != null && classroom.getTitulaireId().equals(currentUserId)) {
            return;
        }
        boolean owns = userTeacherRepository.findAll().stream()
                .anyMatch(ut -> ut.getUser() != null && ut.getUser().getId().equals(currentUserId)
                        && classroom.getTitulaireId() != null
                        && ut.getTeacher() != null
                        && ut.getTeacher().getId().equals(classroom.getTitulaireId()));
        if (!owns) {
            throw new SecurityException("Accès refusé : seuls la direction ou le titulaire de la classe peuvent générer les bulletins");
        }
    }
}
