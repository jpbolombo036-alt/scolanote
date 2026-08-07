package com.bulletin.controller.bulletin;

import com.bulletin.dto.bulletin.BulletinGenerateRequest;
import com.bulletin.dto.bulletin.ReportCardResponse;
import com.bulletin.security.SecurityUtils;
import com.bulletin.service.ReportCardService;
import com.bulletin.service.bulletin.BulletinPdfService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final SecurityUtils securityUtils;

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

    @PostMapping("/inscription/{inscriptionId}/generer")
    @Operation(summary = "Générer le bulletin d'une inscription", description = "Génère le bulletin calculé pour une inscription (élève) et une période. Body: { \"periodId\": 123 }")
    public ResponseEntity<ReportCardResponse> generateForEnrollment(@PathVariable Long inscriptionId, @RequestBody java.util.Map<String, Long> body) {
        Long periodId = body == null ? null : body.get("periodId");
        if (periodId == null) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.BAD_REQUEST).build();
        }
        ReportCardResponse response = reportCardService.generateForEnrollment(inscriptionId, periodId);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(response);
    }

    @GetMapping("/trimestre/{trimestreId}")
    @Operation(summary = "Bulletins par trimestre", description = "Retourne tous les bulletins d'un trimestre")
    public ResponseEntity<List<ReportCardResponse>> getByTerm(@PathVariable Long trimestreId) {
        return ResponseEntity.ok(reportCardService.getByTerm(trimestreId));
    }

    @GetMapping
    @Operation(summary = "Tous les bulletins", description = "Retourne les bulletins accessibles à l'utilisateur connecté")
    public ResponseEntity<Page<ReportCardResponse>> getAccessibleReportCards(Pageable pageable) {
        return ResponseEntity.ok(reportCardService.getAccessibleReportCards(pageable));
    }

    @GetMapping("/unpaginated")
    @Operation(summary = "Liste complète des bulletins", description = "Retourne tous les bulletins sans pagination")
    public ResponseEntity<List<ReportCardResponse>> getAllReportCardsUnpaginated() {
        return ResponseEntity.ok(reportCardService.getAccessibleReportCards());
    }

    @PostMapping("/{id}/pdf")
    @Operation(summary = "G�n�rer le PDF", description = "G�n�re le PDF du bulletin et le retourne en t�l�chargement")
    public ResponseEntity<byte[]> generatePdf(@PathVariable Long id) {
        try {
            // Régénère le PDF si absent du stockage (S3 ou local).
            // Sur Railway le filesystem local est éphémère : si S3 n'est pas configuré,
            // le PDF est simplement régénéré à la demande.
            if (!bulletinPdfService.pdfExists(id)) {
                bulletinPdfService.generatePdf(id);
            }
            byte[] pdfBytes = bulletinPdfService.loadPdf(id);
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

    @GetMapping("/mes-bulletins")
    @Operation(summary = "Mes bulletins", description = "Retourne les bulletins de l'utilisateur connect� (�l�ve ou parent), optionnellement filtr�s par trimestre")
    public ResponseEntity<List<ReportCardResponse>> getMyReportCards(@RequestParam(required = false) Long trimestreId) {
        return ResponseEntity.ok(reportCardService.getMyReportCards(trimestreId));
    }
}
