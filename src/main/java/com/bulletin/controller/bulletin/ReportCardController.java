package com.bulletin.controller.bulletin;

import com.bulletin.dto.bulletin.BulletinGenerateRequest;
import com.bulletin.dto.bulletin.ReportCardResponse;
import com.bulletin.service.ReportCardService;
import com.bulletin.service.bulletin.BulletinPdfService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bulletins")
@RequiredArgsConstructor
@Tag(name = "Bulletins", description = "Génération et consultation des bulletins (calcul automatique)")
public class ReportCardController {

    private final ReportCardService reportCardService;
    private final BulletinPdfService bulletinPdfService;

    @PostMapping("/generer")
    @Operation(summary = "Générer les bulletins", description = "Génère les bulletins calculés automatiquement pour toute une classe et un trimestre")
    public ResponseEntity<List<ReportCardResponse>> generateBulletins(@Valid @RequestBody BulletinGenerateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reportCardService.generateBulletins(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Bulletin par ID", description = "Retourne un bulletin avec ses détails")
    public ResponseEntity<ReportCardResponse> getReportCard(@PathVariable Long id) {
        return ResponseEntity.ok(reportCardService.getReportCard(id));
    }

    @GetMapping("/inscription/{inscriptionId}")
    @Operation(summary = "Bulletins par inscription", description = "Retourne les bulletins d'un élève (par inscription)")
    public ResponseEntity<List<ReportCardResponse>> getByEnrollment(@PathVariable Long inscriptionId) {
        return ResponseEntity.ok(reportCardService.getByEnrollment(inscriptionId));
    }

    @GetMapping("/trimestre/{trimestreId}")
    @Operation(summary = "Bulletins par trimestre", description = "Retourne tous les bulletins d'un trimestre")
    public ResponseEntity<List<ReportCardResponse>> getByTerm(@PathVariable Long trimestreId) {
        return ResponseEntity.ok(reportCardService.getByTerm(trimestreId));
    }

    @GetMapping
    @Operation(summary = "Tous les bulletins", description = "Retourne tous les bulletins")
    public ResponseEntity<List<ReportCardResponse>> getAllReportCards() {
        return ResponseEntity.ok(reportCardService.getAllReportCards());
    }

    @PostMapping("/{id}/pdf")
    @Operation(summary = "Générer le PDF", description = "Génère le PDF du bulletin et le retourne en téléchargement")
    public ResponseEntity<byte[]> generatePdf(@PathVariable Long id) {
        try {
            java.nio.file.Path pdfPath = bulletinPdfService.getPdfPath(id);
            if (!java.nio.file.Files.exists(pdfPath)) {
                bulletinPdfService.generatePdf(id);
                pdfPath = bulletinPdfService.getPdfPath(id);
            }
            byte[] pdfBytes = java.nio.file.Files.readAllBytes(pdfPath);
            String filename = "bulletin-" + id + ".pdf";
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_PDF);
            headers.setContentDisposition(org.springframework.http.ContentDisposition.attachment().filename(filename).build());
            headers.setContentLength(pdfBytes.length);
            return ResponseEntity.ok().headers(headers).body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
