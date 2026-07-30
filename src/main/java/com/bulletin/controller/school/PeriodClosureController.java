package com.bulletin.controller.school;

import com.bulletin.dto.school.PeriodValidationResponse;
import com.bulletin.security.SecurityUtils;
import com.bulletin.service.PeriodClosureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/periodes")
@RequiredArgsConstructor
@Tag(name = "Clôture des périodes", description = "Verrouillage et déverrouillage des périodes")
public class PeriodClosureController {

    private final PeriodClosureService periodClosureService;
    private final SecurityUtils securityUtils;

    @PostMapping("/{id}/verrouiller")
    @Operation(summary = "Verrouiller une période", description = "Verrouille une période pour empêcher les modifications (direction uniquement)")
    public ResponseEntity<Void> verrouiller(@PathVariable Long id) {
        if (!securityUtils.isDirection()) {
            throw new SecurityException("Accès refusé : seul la direction peut verrouiller une période");
        }
        periodClosureService.verrouillerPeriode(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/{id}/deverrouiller")
    @Operation(summary = "Déverrouiller une période", description = "Déverrouille une période pour permettre les modifications (direction uniquement)")
    public ResponseEntity<Void> deverrouiller(@PathVariable Long id) {
        if (!securityUtils.isDirection()) {
            throw new SecurityException("Accès refusé : seul la direction peut déverrouiller une période");
        }
        periodClosureService.deverrouillerPeriode(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/{id}/valider")
    @Operation(summary = "Valider la clôture d'une période", description = "Vérifie si une période peut être verrouillée")
    public ResponseEntity<PeriodValidationResponse> valider(@PathVariable Long id) {
        return ResponseEntity.ok(periodClosureService.validatePeriodCanBeLocked(id));
    }
}
