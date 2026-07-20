package com.bulletin.service.bulletin;

import com.bulletin.dto.bulletin.ReportCardDetailResponse;
import com.bulletin.entity.ReportCard;
import com.bulletin.repository.ReportCardDetailRepository;
import com.bulletin.repository.ReportCardRepository;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
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
                        .points(d.getPoints())
                        .maximum(d.getMaximum())
                        .pourcentage(d.getPourcentage())
                        .observation(d.getObservation())
                        .build())
                .toList();

        try {
            Path dir = Path.of(uploadDir, "bulletins");
            Files.createDirectories(dir);
            Path file = dir.resolve("bulletin-" + reportCardId + ".pdf");

            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(file.toFile()));
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            document.add(new Paragraph("Bulletin de notes", titleFont));
            document.add(new Paragraph(" "));

            addField(document, "Élève", reportCard.getEnrollment() != null && reportCard.getEnrollment().getStudent() != null
                    ? reportCard.getEnrollment().getStudent().getNom() : "-");
            addField(document, "Classe", reportCard.getEnrollment() != null && reportCard.getEnrollment().getClassroom() != null
                    ? reportCard.getEnrollment().getClassroom().getNom() : "-");
            addField(document, "Trimestre", reportCard.getTerm() != null ? reportCard.getTerm().getNom() : "-");
            addField(document, "Pourcentage", reportCard.getPourcentage() != null ? reportCard.getPourcentage() + "%" : "-");
            addField(document, "Rang", reportCard.getRang() != null ? String.valueOf(reportCard.getRang()) : "-");
            addField(document, "Mention", reportCard.getMention());
            addField(document, "Décision", reportCard.getDecision());
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100f);
            table.addCell("Matière");
            table.addCell("Coeff");
            table.addCell("Moyenne");
            table.addCell("Points");
            table.addCell("Max");
            table.addCell("%");

            for (ReportCardDetailResponse detail : details) {
                table.addCell(detail.getSubjectNom() != null ? detail.getSubjectNom() : "-");
                table.addCell(detail.getCoefficient() != null ? String.valueOf(detail.getCoefficient()) : "-");
                table.addCell(detail.getMoyenne() != null ? detail.getMoyenne().toString() : "-");
                table.addCell(detail.getPoints() != null ? detail.getPoints().toString() : "-");
                table.addCell(detail.getMaximum() != null ? detail.getMaximum().toString() : "-");
                table.addCell(detail.getPourcentage() != null ? detail.getPourcentage().toString() : "-");
            }

            document.add(table);
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

    private void addField(Document document, String label, String value) throws Exception {
        document.add(new Paragraph(label + ": " + value));
    }
}
