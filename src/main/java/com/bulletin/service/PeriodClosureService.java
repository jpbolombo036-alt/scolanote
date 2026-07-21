package com.bulletin.service;

import com.bulletin.entity.AcademicYear;
import com.bulletin.entity.Assessment;
import com.bulletin.entity.Period;
import com.bulletin.entity.Trimester;
import com.bulletin.exception.ResourceNotFoundException;
import com.bulletin.repository.AcademicYearRepository;
import com.bulletin.repository.AssessmentRepository;
import com.bulletin.repository.EnrollmentRepository;
import com.bulletin.repository.GradeRepository;
import com.bulletin.repository.PeriodRepository;
import com.bulletin.repository.TrimesterRepository;
import com.bulletin.security.SecurityUtils;
import com.bulletin.dto.school.PeriodValidationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PeriodClosureService {

    private final PeriodRepository periodRepository;
    private final TrimesterRepository trimesterRepository;
    private final AcademicYearRepository academicYearRepository;
    private final AssessmentRepository assessmentRepository;
    private final GradeRepository gradeRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final SecurityUtils securityUtils;

    @Transactional
    public void verrouillerPeriode(Long periodeId) {
        validatePeriodCanBeLocked(periodeId);

        Period periode = periodRepository.findById(periodeId)
                .orElseThrow(() -> new ResourceNotFoundException("Période non trouvée"));

        if (periode.isVerrouille()) {
            throw new IllegalStateException("Période déjà verrouillée le " + periode.getDateVerrouillage());
        }

        periode.setVerrouille(true);
        periode.setDateVerrouillage(LocalDateTime.now());
        periode.setVerrouillePar(securityUtils.getCurrentUsername());
        periodRepository.save(periode);

        log.info("Période verrouillée: {} par {}", periodeId, periode.getVerrouillePar());
    }

    @Transactional
    public void deverrouillerPeriode(Long periodeId) {
        Period periode = periodRepository.findById(periodeId)
                .orElseThrow(() -> new ResourceNotFoundException("Période non trouvée"));

        if (!periode.isVerrouille()) {
            throw new IllegalStateException("Période non verrouillée");
        }

        periode.setVerrouille(false);
        periode.setDateVerrouillage(null);
        periode.setVerrouillePar(null);
        periodRepository.save(periode);

        log.info("Période déverrouillée: {}", periodeId);
    }

    public void assertPeriodeOuverte(Long periodeId) {
        Period periode = periodRepository.findById(periodeId)
                .orElseThrow(() -> new ResourceNotFoundException("Période non trouvée"));

        if (periode.isVerrouille() && !securityUtils.isDirection()) {
            throw new IllegalStateException("Période verrouillée. Seul la direction peut modifier.");
        }
    }

    public PeriodValidationResponse validatePeriodCanBeLocked(Long periodeId) {
        Period periode = periodRepository.findById(periodeId)
                .orElseThrow(() -> new ResourceNotFoundException("Période non trouvée avec l'ID: " + periodeId));

        if (periode.isVerrouille()) {
            return PeriodValidationResponse.builder()
                    .peutVerrouiller(false)
                    .message("Période déjà verrouillée le " + periode.getDateVerrouillage())
                    .build();
        }

        List<Assessment> evaluations = assessmentRepository.findByPeriodId(periodeId);

        if (evaluations.isEmpty()) {
            return PeriodValidationResponse.builder()
                    .peutVerrouiller(false)
                    .nombreEvaluations(0)
                    .message("Aucune évaluation n'a été créée pour cette période.")
                    .build();
        }

        List<Long> evaluationIds = evaluations.stream()
                .map(com.bulletin.entity.Assessment::getId)
                .toList();

        long totalNotes = gradeRepository.findAll().stream()
                .filter(grade -> evaluationIds.contains(grade.getAssessment().getId()))
                .count();

        long totalAttendus = 0;
        for (Assessment evaluation : evaluations) {
            List<com.bulletin.entity.Enrollment> inscriptions = enrollmentRepository.findByClassroomId(evaluation.getAssignment().getClassroom().getId());
            totalAttendus += inscriptions.size();
        }

        long notesManquantes = totalAttendus - totalNotes;

        if (notesManquantes > 0) {
            return PeriodValidationResponse.builder()
                    .peutVerrouiller(false)
                    .nombreEvaluations(evaluations.size())
                    .nombreNotes(totalNotes)
                    .notesManquantes(notesManquantes)
                    .message(notesManquantes + " note(s) manquante(s). Toutes les notes doivent être encodées avant de verrouiller.")
                    .build();
        }

        return PeriodValidationResponse.builder()
                .peutVerrouiller(true)
                .nombreEvaluations(evaluations.size())
                .nombreNotes(totalNotes)
                .notesManquantes(0)
                .message("La période peut être verrouillée.")
                .build();
    }

    @Transactional
    public void verrouillerAnneeSiComplete(Long periodeId) {
        Period periode = periodRepository.findById(periodeId)
                .orElseThrow(() -> new ResourceNotFoundException("Période non trouvée avec l'ID: " + periodeId));

        if (!periode.isVerrouille()) {
            return;
        }

        Trimester trimester = periode.getTrimester();
        if (trimester == null || trimester.getAcademicYear() == null) {
            return;
        }

        AcademicYear academicYear = trimester.getAcademicYear();
        if (academicYear.isVerrouille()) {
            return;
        }

        List<Trimester> trimestres = trimesterRepository.findByAcademicYearId(academicYear.getId());
        boolean tousTrimestresVerrouilles = trimestres.stream()
                .allMatch(t -> t.getPeriods() != null && t.getPeriods().stream()
                        .allMatch(Period::isVerrouille));

        if (tousTrimestresVerrouilles) {
            academicYear.setVerrouille(true);
            academicYear.setDateVerrouillage(LocalDateTime.now());
            academicYear.setVerrouillePar(securityUtils.getCurrentUsername());
            academicYearRepository.save(academicYear);
            log.info("Année scolaire verrouillée: {} par {}", academicYear.getId(), academicYear.getVerrouillePar());
        }
    }
}
