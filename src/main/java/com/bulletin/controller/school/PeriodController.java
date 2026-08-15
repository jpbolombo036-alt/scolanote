package com.bulletin.controller.school;

import com.bulletin.dto.school.PeriodRequest;
import com.bulletin.dto.school.PeriodResponse;
import com.bulletin.security.SecurityUtils;
import com.bulletin.service.PeriodService;
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
@RequestMapping("/api/periodes")
@RequiredArgsConstructor
@Tag(name = "Périodes", description = "Gestion des périodes")
public class PeriodController {
    private final PeriodService periodService;
    private final SecurityUtils securityUtils;

    @PostMapping
    @Operation(summary = "Créer une période", description = "Crée une période (direction uniquement). L'ordre est généré automatiquement serveur (max+1, scope trimestre+école) ; la valeur fournie est ignorée.")
    public ResponseEntity<PeriodResponse> createPeriod(@Valid @RequestBody PeriodRequest request) {
        securityUtils.assertPermission("PERIODE_GERER");
        return ResponseEntity.status(HttpStatus.CREATED).body(periodService.createPeriod(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Période par ID")
    public ResponseEntity<PeriodResponse> getPeriod(@PathVariable Long id) {
        return ResponseEntity.ok(periodService.getPeriod(id));
    }

    @GetMapping
    @Operation(summary = "Liste des périodes", description = "Retourne les périodes accessibles à l'utilisateur connecté")
    public ResponseEntity<Page<PeriodResponse>> getAccessiblePeriods(Pageable pageable) {
        return ResponseEntity.ok(periodService.getAccessiblePeriods(pageable));
    }

    @GetMapping("/all")
    @Operation(summary = "Liste complète des périodes", description = "Retourne toutes les périodes (SUPER_ADMIN uniquement)")
    public ResponseEntity<List<PeriodResponse>> getAllPeriodsUnpaginated() {
        return ResponseEntity.ok(periodService.getAccessiblePeriods());
    }

    @GetMapping("/trimestre/{trimestreId}")
    @Operation(summary = "Périodes par trimestre")
    public ResponseEntity<List<PeriodResponse>> getByTrimester(@PathVariable Long trimestreId) {
        return ResponseEntity.ok(periodService.getPeriodsByTrimester(trimestreId));
    }

    @GetMapping("/verrouillees")
    @Operation(summary = "Périodes verrouillées")
    public ResponseEntity<List<PeriodResponse>> getVerrouillees() {
        return ResponseEntity.ok(periodService.getVerrouillees());
    }

    @GetMapping("/ouvertes")
    @Operation(summary = "Périodes ouvertes")
    public ResponseEntity<List<PeriodResponse>> getOuvertes() {
        return ResponseEntity.ok(periodService.getOuvertes());
    }

    // NOTE: GET /{id}/valider est géré par PeriodClosureController (responsabilité clôture).
    // L'endpoint en double ici causait un "Ambiguous mapping" qui empêchait le démarrage.

    @PutMapping("/{id}")
    @Operation(summary = "Modifier une période", description = "Modifie une période. L'ordre est immuable (ignoré du request).")
    public ResponseEntity<PeriodResponse> updatePeriod(@PathVariable Long id, @Valid @RequestBody PeriodRequest request) {
        return ResponseEntity.ok(periodService.updatePeriod(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une période")
    public ResponseEntity<Void> deletePeriod(@PathVariable Long id) {
        periodService.deletePeriod(id);
        return ResponseEntity.noContent().build();
    }
}
