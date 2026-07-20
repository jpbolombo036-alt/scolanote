package com.bulletin.controller.bulletin;

import com.bulletin.dto.bulletin.ReportCardActionRequest;
import com.bulletin.dto.bulletin.ReportCardWorkflowResponse;
import com.bulletin.service.ReportCardWorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bulletins/workflow")
@RequiredArgsConstructor
@Tag(name = "Workflow bulletins", description = "Validation et signature des bulletins (préfet/directeur)")
public class ReportCardWorkflowController {

    private final ReportCardWorkflowService reportCardWorkflowService;

    @PostMapping("/{id}/validate-prefet")
    @Operation(summary = "Valider par le préfet", description = "Passe le bulletin en VALIDE_PREFET (réservé au préfet/direction)")
    public ResponseEntity<ReportCardWorkflowResponse> validateByPrefet(@PathVariable Long id) {
        return ResponseEntity.ok(reportCardWorkflowService.validateByPrefet(id));
    }

    @PostMapping("/{id}/validate-directeur")
    @Operation(summary = "Valider par le directeur", description = "Passe le bulletin en VALIDE_DIRECTEUR (réservé au directeur)")
    public ResponseEntity<ReportCardWorkflowResponse> validateByDirecteur(@PathVariable Long id, @Valid @RequestBody(required = false) ReportCardActionRequest request) {
        return ResponseEntity.ok(reportCardWorkflowService.validateByDirecteur(id, request));
    }

    @PostMapping("/{id}/sign")
    @Operation(summary = "Signer le bulletin", description = "Signe électroniquement le bulletin (réservé au directeur)")
    public ResponseEntity<ReportCardWorkflowResponse> sign(@PathVariable Long id, @Valid @RequestBody(required = false) ReportCardActionRequest request) {
        return ResponseEntity.ok(reportCardWorkflowService.sign(id, request));
    }

    @PostMapping("/{id}/publish")
    @Operation(summary = "Publier le bulletin", description = "Publie le bulletin après signature (réservé à la direction)")
    public ResponseEntity<ReportCardWorkflowResponse> publish(@PathVariable Long id) {
        return ResponseEntity.ok(reportCardWorkflowService.publish(id));
    }
}
