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

    @PostMapping("/generate")
    @Operation(summary = "Générer les bulletins", description = "Génère les bulletins calculés automatiquement pour toute une classe et un trimestre")
    public ResponseEntity<List<ReportCardResponse>> generateBulletins(@Valid @RequestBody BulletinGenerateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reportCardService.generateBulletins(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Bulletin par ID", description = "Retourne un bulletin avec ses détails")
    public ResponseEntity<ReportCardResponse> getReportCard(@PathVariable Long id) {
        return ResponseEntity.ok(reportCardService.getReportCard(id));
    }

    @GetMapping("/enrollment/{enrollmentId}")
    @Operation(summary = "Bulletins par inscription", description = "Retourne les bulletins d'un élève (par inscription)")
    public ResponseEntity<List<ReportCardResponse>> getByEnrollment(@PathVariable Long enrollmentId) {
        return ResponseEntity.ok(reportCardService.getByEnrollment(enrollmentId));
    }

    @GetMapping("/term/{termId}")
    @Operation(summary = "Bulletins par trimestre", description = "Retourne tous les bulletins d'un trimestre")
    public ResponseEntity<List<ReportCardResponse>> getByTerm(@PathVariable Long termId) {
        return ResponseEntity.ok(reportCardService.getByTerm(termId));
    }

    @PostMapping("/{id}/pdf")
    @Operation(summary = "Générer le PDF", description = "Génère le PDF du bulletin et met à jour le pdf_url")
    public ResponseEntity<String> generatePdf(@PathVariable Long id) {
        String pdfUrl = bulletinPdfService.generatePdf(id);
        return ResponseEntity.ok(pdfUrl);
    }
}
