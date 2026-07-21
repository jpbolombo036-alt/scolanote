package com.bulletin.controller.school;

import com.bulletin.dto.school.PeriodRequest;
import com.bulletin.dto.school.PeriodResponse;
import com.bulletin.dto.school.PeriodValidationResponse;
import com.bulletin.security.SecurityUtils;
import com.bulletin.service.PeriodClosureService;
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
    private final PeriodClosureService periodClosureService;
    private final SecurityUtils securityUtils;

    @PostMapping
    @Operation(summary = "Créer une période", description = "Crée une période (direction uniquement)")
    public ResponseEntity<PeriodResponse> createPeriod(@Valid @RequestBody PeriodRequest request) {
        if (!securityUtils.isDirection()) {
            throw new SecurityException("Accès refusé : seul la direction peut créer une période");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(periodService.createPeriod(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Période par ID")
    public ResponseEntity<PeriodResponse> getPeriod(@PathVariable Long id) {
        return ResponseEntity.ok(periodService.getPeriod(id));
    }

    @GetMapping
    @Operation(summary = "Liste des périodes", description = "Retourne toutes les périodes (avec pagination optionnelle)")
    public ResponseEntity<Page<PeriodResponse>> getAllPeriods(Pageable pageable) {
        return ResponseEntity.ok(periodService.getAllPeriods(pageable));
    }

    @GetMapping("/all")
    @Operation(summary = "Liste complète des périodes")
    public ResponseEntity<List<PeriodResponse>> getAllPeriodsUnpaginated() {
        return ResponseEntity.ok(periodService.getAllPeriods());
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

    @GetMapping("/{id}/valider")
    @Operation(summary = "Valider qu'une période peut être verrouillée")
    public ResponseEntity<PeriodValidationResponse> validateClosure(@PathVariable Long id) {
        return ResponseEntity.ok(periodClosureService.validatePeriodCanBeLocked(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier une période")
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
