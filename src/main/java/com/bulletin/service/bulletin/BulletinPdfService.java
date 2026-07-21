package com.bulletin.service.bulletin;

import com.bulletin.dto.bulletin.ReportCardDetailResponse;
import com.bulletin.entity.ReportCard;
import com.bulletin.repository.ReportCardDetailRepository;
import com.bulletin.repository.ReportCardRepository;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BulletinPdfService {

    private final ReportCardRepository reportCardRepository;
    private final ReportCardDetailRepository reportCardDetailRepository;

    @Value("${app.upload.dir:/tmp/uploads}")
    private String uploadDir;

    public String generatePdf(Long reportCardId) {
        ReportCard reportCard = reportCardRepository.findById(reportCardId)
                .orElseThrow(() -> new IllegalArgumentException("Bulletin non trouvé: " + reportCardId));

        List<ReportCardDetailResponse> details = reportCardDetailRepository.findByReportCardId(reportCardId).stream()
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

        try {
            Path dir = Path.of(uploadDir, "bulletins");
            Files.createDirectories(dir);
            Path file = dir.resolve("bulletin-" + reportCardId + ".pdf");

            Document document = new Document(PageSize.A4, 20, 20, 20, 20);
            PdfWriter.getInstance(document, new FileOutputStream(file.toFile()));
            document.open();

            addHeader(document, reportCard);
            addStudentInfo(document, reportCard);
            addAttendanceAndDiscipline(document, reportCard);
            addGradesTable(document, details);
            addSummary(document, reportCard);
            addSignatures(document);

            document.close();

            String relativePath = "uploads/bulletins/bulletin-" + reportCardId + ".pdf";
            reportCard.setPdfUrl(relativePath);
            reportCardRepository.save(reportCard);

            log.info("PDF généré: {}", file.toAbsolutePath());
            return relativePath;
        } catch (Exception e) {
            log.error("Erreur lors de la génération du PDF", e);
            throw new RuntimeException("Échec de la génération du PDF", e);
        }
    }

    private void addHeader(Document document, ReportCard reportCard) throws Exception {
        Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD);
        Font subtitleFont = new Font(Font.HELVETICA, 12, Font.NORMAL);
        Font smallFont = new Font(Font.HELVETICA, 10, Font.NORMAL);

        Paragraph title = new Paragraph("BULLETIN DE NOTES", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        document.add(new Paragraph(" "));

        String schoolName = reportCard.getEnrollment() != null
                && reportCard.getEnrollment().getClassroom() != null
                && reportCard.getEnrollment().getClassroom().getAcademicYear() != null
                && reportCard.getEnrollment().getClassroom().getAcademicYear().getSchool() != null
                ? reportCard.getEnrollment().getClassroom().getAcademicYear().getSchool().getNom()
                : "ÉCOLE";
        Paragraph school = new Paragraph(schoolName, subtitleFont);
        school.setAlignment(Element.ALIGN_CENTER);
        document.add(school);

        String year = reportCard.getEnrollment() != null
                && reportCard.getEnrollment().getClassroom() != null
                && reportCard.getEnrollment().getClassroom().getAcademicYear() != null
                ? reportCard.getEnrollment().getClassroom().getAcademicYear().getLibelle()
                : "Année scolaire: -";
        Paragraph yearParagraph = new Paragraph(year, subtitleFont);
        yearParagraph.setAlignment(Element.ALIGN_CENTER);
        document.add(yearParagraph);

        document.add(new Paragraph(" "));
    }

    private void addStudentInfo(Document document, ReportCard reportCard) throws Exception {
        String studentName = reportCard.getEnrollment() != null && reportCard.getEnrollment().getStudent() != null
                ? reportCard.getEnrollment().getStudent().getNom() + " " +
                  (reportCard.getEnrollment().getStudent().getPostnom() != null ?
                   reportCard.getEnrollment().getStudent().getPostnom() : "")
                : "-";
        String matricule = reportCard.getEnrollment() != null && reportCard.getEnrollment().getStudent() != null
                ? reportCard.getEnrollment().getStudent().getMatricule() : "-";
        String classroom = reportCard.getEnrollment() != null && reportCard.getEnrollment().getClassroom() != null
                ? reportCard.getEnrollment().getClassroom().getNom() : "-";
        String period = reportCard.getPeriod() != null ? reportCard.getPeriod().getNom() : "-";

        addField(document, "Élève", studentName);
        addField(document, "Matricule", matricule);
        addField(document, "Classe", classroom);
        addField(document, "Période", period);
        addField(document, "Date", java.time.LocalDate.now().toString());
        document.add(new Paragraph(" "));
    }

    private void addAttendanceAndDiscipline(Document document, ReportCard reportCard) throws Exception {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100f);

        String attendance = String.format("Absences: %d | Retards: %d",
            reportCard.getTotalAbsences() != null ? reportCard.getTotalAbsences() : 0,
            reportCard.getTotalRetards() != null ? reportCard.getTotalRetards() : 0);

        String discipline = String.format("Conduite: %s | Application: %s",
            reportCard.getConduite() != null ? reportCard.getConduite() : "-",
            reportCard.getApplication() != null ? reportCard.getApplication() : "-");

        table.addCell("ASSIDUITÉ");
        table.addCell("DISCIPLINE");
        table.addCell(attendance);
        table.addCell(discipline);

        document.add(table);
        document.add(new Paragraph(" "));
    }

    private void addGradesTable(Document document, List<ReportCardDetailResponse> details) throws Exception {
        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100f);

        table.addCell("Matière");
        table.addCell("Coeff");
        table.addCell("Notes");
        table.addCell("Moyenne");
        table.addCell("Rang");
        table.addCell("Appréciation");
        table.addCell("%");

        for (ReportCardDetailResponse detail : details) {
            table.addCell(detail.getSubjectNom() != null ? detail.getSubjectNom() : "-");
            table.addCell(detail.getCoefficient() != null ? String.valueOf(detail.getCoefficient()) : "-");
            table.addCell("");
            table.addCell(detail.getMoyenne() != null ? detail.getMoyenne().toString() : "-");
            table.addCell(detail.getRangMatiere() != null ? String.valueOf(detail.getRangMatiere()) : "-");
            table.addCell(detail.getAppreciation() != null ? detail.getAppreciation() : "-");
            table.addCell(detail.getPourcentage() != null ? detail.getPourcentage().toString() : "-");
        }

        document.add(table);
        document.add(new Paragraph(" "));
    }

    private void addSummary(Document document, ReportCard reportCard) throws Exception {
        document.add(new Paragraph(" "));

        String total = String.format("Total: %s / %s = %s%%",
            reportCard.getTotalPoints() != null ? reportCard.getTotalPoints() : "0",
            reportCard.getMaximumPoints() != null ? reportCard.getMaximumPoints() : "0",
            reportCard.getPourcentage() != null ? reportCard.getPourcentage() : "0");

        document.add(new Paragraph(total));

        String rang = reportCard.getRang() != null ?
            String.format("Rang: %d", reportCard.getRang()) : "Rang: -";
        document.add(new Paragraph(rang));

        if (reportCard.getMention() != null) {
            document.add(new Paragraph("Mention: " + reportCard.getMention()));
        }

        if (reportCard.getDecision() != null) {
            document.add(new Paragraph("Décision: " + reportCard.getDecision()));
        }

        document.add(new Paragraph(" "));

        String titulaire = reportCard.getEnrollment() != null
                && reportCard.getEnrollment().getClassroom() != null
                && reportCard.getEnrollment().getClassroom().getTitulaireId() != null
                ? "Titulaire: " + reportCard.getEnrollment().getClassroom().getTitulaireId()
                : null;
        if (titulaire != null) {
            document.add(new Paragraph(titulaire));
        }
    }

    private void addSignatures(Document document) throws Exception {
        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100f);

        table.addCell("Enseignant\n\n\nSignature");
        table.addCell("Directeur\n\n\nSignature");
        table.addCell("Parent/Tuteur\n\n\nSignature");

        document.add(table);
        document.add(new Paragraph(" "));
        document.add(new Paragraph("[CACHET DE L'ÉCOLE]"));
    }

    private void addField(Document document, String label, String value) throws Exception {
        document.add(new Paragraph(label + ": " + value));
    }
}
