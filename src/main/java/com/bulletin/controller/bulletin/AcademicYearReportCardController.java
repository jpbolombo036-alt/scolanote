package com.bulletin.controller.bulletin;

import com.bulletin.dto.bulletin.AcademicYearReportCardDetailResponse;
import com.bulletin.dto.bulletin.AcademicYearReportCardResponse;
import com.bulletin.security.SecurityUtils;
import com.bulletin.service.ReportCardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bulletins-annuels")
@RequiredArgsConstructor
@Tag(name = "Bulletins annuels", description = "Gestion des bulletins annuels")
public class AcademicYearReportCardController {

    private final ReportCardService reportCardService;
    private final SecurityUtils securityUtils;

    @GetMapping
    @Operation(summary = "Liste des bulletins annuels", description = "Retourne les bulletins annuels accessibles à l'utilisateur connecté")
    public ResponseEntity<Page<AcademicYearReportCardResponse>> getAcademicYearReportCards(Pageable pageable) {
        return ResponseEntity.ok(reportCardService.getAccessibleAcademicYearReportCards(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Bulletin annuel par ID", description = "Retourne un bulletin annuel avec ses détails")
    public ResponseEntity<AcademicYearReportCardResponse> getAcademicYearReportCard(@PathVariable Long id) {
        return ResponseEntity.ok(reportCardService.getAcademicYearReportCard(id));
    }

    @GetMapping("/inscription/{enrollmentId}")
    @Operation(summary = "Bulletins annuels par inscription", description = "Retourne les bulletins annuels d'un élève (par inscription)")
    public ResponseEntity<List<AcademicYearReportCardResponse>> getByEnrollment(@PathVariable Long enrollmentId) {
        return ResponseEntity.ok(reportCardService.getAcademicYearReportCardsByEnrollment(enrollmentId));
    }

    @PostMapping("/{id}/pdf")
    @Operation(summary = "Generer le PDF annuel", description = "Genere le PDF du bulletin annuel et le retourne en telechargement")
    public ResponseEntity<byte[]> generatePdf(@PathVariable Long id) {
        byte[] pdfBytes = reportCardService.generateAcademicYearPdf(id);
        String filename = "bulletin-annuel-" + id + ".pdf";
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_PDF);
        headers.setContentDisposition(org.springframework.http.ContentDisposition.attachment().filename(filename).build());
        headers.setContentLength(pdfBytes.length);
        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }
}
