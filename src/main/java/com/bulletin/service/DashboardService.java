package com.bulletin.service;

import com.bulletin.dto.bulletin.ReportCardResponse;
import com.bulletin.dto.dashboard.DashboardResponse;
import com.bulletin.entity.ReportCard;
import com.bulletin.repository.ReportCardRepository;
import com.bulletin.repository.StudentRepository;
import com.bulletin.repository.ClassroomRepository;
import com.bulletin.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final ReportCardRepository reportCardRepository;
    private final StudentRepository studentRepository;
    private final ClassroomRepository classroomRepository;
    private final SecurityUtils securityUtils;

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {
        Long schoolId = securityUtils.getCurrentSchoolId();

        long studentCount = schoolId == null
                ? studentRepository.count()
                : studentRepository.countBySchoolId(schoolId);

        long classroomCount = schoolId == null
                ? classroomRepository.count()
                : classroomRepository.countBySchoolId(schoolId);

        List<ReportCard> allCards = schoolId == null
                ? reportCardRepository.findAll()
                : reportCardRepository.findBySchoolId(schoolId);

        long bulletinCount = allCards.size();

        BigDecimal average = allCards.isEmpty()
                ? null
                : allCards.stream()
                .map(ReportCard::getPourcentage)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(bulletinCount), 2, RoundingMode.HALF_UP);

        List<DashboardResponse.BulletinItem> recent = allCards.stream()
                .sorted((a, b) -> {
                    if (a.getDateGeneration() == null && b.getDateGeneration() == null) return 0;
                    if (a.getDateGeneration() == null) return 1;
                    if (b.getDateGeneration() == null) return -1;
                    return b.getDateGeneration().compareTo(a.getDateGeneration());
                })
                .limit(8)
                .map(this::toBulletinItem)
                .toList();

        DashboardResponse.MentionDistribution distribution = computeMentions(allCards);

        // Construire dynamiquement les activités à partir des dernières entités
        List<DashboardResponse.ActivityItem> activities = new ArrayList<>();

        // Activités basées sur les derniers bulletins
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.FRENCH);
        for (int i = 0; i < Math.min(3, recent.size()); i++) {
            DashboardResponse.BulletinItem b = recent.get(i);
            String time = b.getDate() != null ? b.getDate() : null;
            activities.add(DashboardResponse.ActivityItem.builder()
                    .text("Bulletin de " + (b.getStudent() == null ? "un élève" : b.getStudent()))
                    .time(time)
                    .type("bulletin")
                    .build());
        }

        // Nouveaux élèves récents
        var recentStudents = studentRepository.findAll(PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "createdAt"))).getContent();
        for (var s : recentStudents) {
            String time = s.getCreatedAt() != null ? s.getCreatedAt().format(dtf) : null;
            String name = (s.getNom() == null ? "Élève" : s.getNom()) + (s.getPrenom() != null ? " " + s.getPrenom() : "");
            activities.add(DashboardResponse.ActivityItem.builder()
                    .text("Nouvel élève ajouté : " + name)
                    .time(time)
                    .type("student")
                    .build());
        }

        // Nouvelles classes récentes
        var recentClasses = classroomRepository.findAll(PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "createdAt"))).getContent();
        for (var c : recentClasses) {
            String time = c.getCreatedAt() != null ? c.getCreatedAt().format(dtf) : null;
            String nom = c.getNom() != null ? c.getNom() : "Classe";
            activities.add(DashboardResponse.ActivityItem.builder()
                    .text("Classe créée : " + nom)
                    .time(time)
                    .type("classroom")
                    .build());
        }

        return DashboardResponse.builder()
                .stats(DashboardResponse.Stats.builder()
                        .students(studentCount)
                        .classrooms(classroomCount)
                        .reportCards(bulletinCount)
                        .average(average)
                        .build())
                .recentBulletins(recent)
                .mentions(distribution)
                .activities(activities)
                .build();
    }

    private DashboardResponse.BulletinItem toBulletinItem(ReportCard reportCard) {
        String fullName = reportCard.getEnrollment() != null
                && reportCard.getEnrollment().getStudent() != null
                ? buildStudentName(reportCard.getEnrollment().getStudent())
                : "Élève";

        String classe = reportCard.getEnrollment() != null
                && reportCard.getEnrollment().getClassroom() != null
                ? reportCard.getEnrollment().getClassroom().getNom()
                : null;

        String trimestre = reportCard.getPeriod() != null
                && reportCard.getPeriod().getTrimester() != null
                ? reportCard.getPeriod().getTrimester().getNom()
                : (reportCard.getPeriod() != null ? reportCard.getPeriod().getNom() : null);

        BigDecimal moyenne = reportCard.getPourcentage() != null
                ? reportCard.getPourcentage().divide(new BigDecimal("5"), 2, RoundingMode.HALF_UP)
                : null;

                String mention = reportCard.getMention();
                if (mention != null) {
                        mention = switch (mention) {
                                case "EXCELLENT", "Excellent" -> "Excellent";
                                case "TRES BIEN", "Très Bien" -> "Très Bien";
                                case "BIEN", "Bien" -> "Bien";
                                case "SATISFACTION", "Passable" -> "Passable";
                                case "ECHEC", "Insuffisant" -> "Insuffisant";
                                default -> mention;
                        };
                }

        String date = reportCard.getDateGeneration() != null
                ? reportCard.getDateGeneration().format(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.FRENCH))
                : null;

        return DashboardResponse.BulletinItem.builder()
                .id(reportCard.getId())
                .student(fullName)
                .classe(classe)
                .trimestre(trimestre)
                .moyenne(moyenne)
                .mention(mention)
                .date(date)
                .build();
    }

    private DashboardResponse.MentionDistribution computeMentions(List<ReportCard> cards) {
        long tresBien = 0;
        long bien = 0;
        long assezBien = 0;
        long passable = 0;
        long insuffisant = 0;

        for (ReportCard card : cards) {
            String mention = card.getMention();
            if (mention == null) continue;
            switch (mention) {
                                case "Excellent", "Très Bien", "EXCELLENT" -> tresBien++;
                case "Bien", "TRES BIEN" -> bien++;
                case "Assez Bien", "BIEN" -> assezBien++;
                case "Passable", "SATISFACTION" -> passable++;
                case "Insuffisant", "ECHEC" -> insuffisant++;
                default -> {}
            }
        }

        return DashboardResponse.MentionDistribution.builder()
                .tresBien(tresBien)
                .bien(bien)
                .assezBien(assezBien)
                .passable(passable)
                .insuffisant(insuffisant)
                .total((long) cards.size())
                .build();
    }

    private String buildStudentName(com.bulletin.entity.Student student) {
        StringBuilder sb = new StringBuilder(student.getNom() != null ? student.getNom() : "");
        if (student.getPostnom() != null && !student.getPostnom().isEmpty()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(student.getPostnom());
        }
        if (student.getPrenom() != null && !student.getPrenom().isEmpty()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(student.getPrenom());
        }
        return sb.toString();
    }
}
