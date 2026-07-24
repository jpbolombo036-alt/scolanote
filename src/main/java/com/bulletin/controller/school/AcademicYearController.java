package com.bulletin.controller.school;

import com.bulletin.dto.school.AcademicYearRequest;
import com.bulletin.dto.school.AcademicYearResponse;
import com.bulletin.security.SecurityUtils;
import com.bulletin.service.AcademicYearService;
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
@RequestMapping("/api/annees-academiques")
@RequiredArgsConstructor
@Tag(name = "Années scolaires", description = "Gestion des années scolaires")
public class AcademicYearController {

    private final AcademicYearService academicYearService;
    private final SecurityUtils securityUtils;

    @PostMapping
    @Operation(summary = "Créer une année scolaire", description = "Crée une nouvelle année scolaire (direction uniquement)")
    public ResponseEntity<AcademicYearResponse> createAcademicYear(@Valid @RequestBody AcademicYearRequest request) {
        if (!securityUtils.isDirection()) {
            throw new SecurityException("Accès refusé : seul la direction peut créer une année scolaire");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(academicYearService.createAcademicYear(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Année scolaire par ID", description = "Retourne une année scolaire par son ID")
    public ResponseEntity<AcademicYearResponse> getAcademicYear(@PathVariable Long id) {
        return ResponseEntity.ok(academicYearService.getAcademicYear(id));
    }

    @GetMapping
    @Operation(summary = "Liste des années scolaires", description = "Retourne les années scolaires accessibles à l'utilisateur connecté")
    public ResponseEntity<Page<AcademicYearResponse>> getAccessibleAcademicYears(Pageable pageable) {
        return ResponseEntity.ok(academicYearService.getAccessibleAcademicYears(pageable));
    }

    @GetMapping("/all")
    @Operation(summary = "Liste complète des années scolaires", description = "Retourne toutes les années scolaires (SUPER_ADMIN uniquement)")
    public ResponseEntity<List<AcademicYearResponse>> getAllAcademicYearsUnpaginated() {
        return ResponseEntity.ok(academicYearService.getAccessibleAcademicYears());
    }

    @GetMapping("/ecole/{ecoleId}")
    @Operation(summary = "Années par école", description = "Retourne les années scolaires d'une école")
    public ResponseEntity<List<AcademicYearResponse>> getBySchool(@PathVariable Long ecoleId) {
        return ResponseEntity.ok(academicYearService.getAcademicYearsBySchool(ecoleId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier une année scolaire", description = "Modifie une année scolaire (direction uniquement)")
    public ResponseEntity<AcademicYearResponse> updateAcademicYear(@PathVariable Long id, @Valid @RequestBody AcademicYearRequest request) {
        if (!securityUtils.isDirection()) {
            throw new SecurityException("Accès refusé : seul la direction peut modifier une année scolaire");
        }
        return ResponseEntity.ok(academicYearService.updateAcademicYear(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une année scolaire", description = "Supprime une année scolaire (direction uniquement)")
    public ResponseEntity<Void> deleteAcademicYear(@PathVariable Long id) {
        if (!securityUtils.isDirection()) {
            throw new SecurityException("Accès refusé : seul la direction peut supprimer une année scolaire");
        }
        academicYearService.deleteAcademicYear(id);
        return ResponseEntity.noContent().build();
    }
}
