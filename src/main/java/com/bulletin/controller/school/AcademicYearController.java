package com.bulletin.controller.school;

import com.bulletin.dto.school.AcademicYearRequest;
import com.bulletin.dto.school.AcademicYearResponse;
import com.bulletin.service.AcademicYearService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    @PostMapping
    @Operation(summary = "Créer une année scolaire", description = "Crée une nouvelle année scolaire")
    public ResponseEntity<AcademicYearResponse> createAcademicYear(@Valid @RequestBody AcademicYearRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(academicYearService.createAcademicYear(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Année scolaire par ID", description = "Retourne une année scolaire par son ID")
    public ResponseEntity<AcademicYearResponse> getAcademicYear(@PathVariable Long id) {
        return ResponseEntity.ok(academicYearService.getAcademicYear(id));
    }

    @GetMapping
    @Operation(summary = "Liste des années scolaires", description = "Retourne toutes les années scolaires")
    public ResponseEntity<List<AcademicYearResponse>> getAllAcademicYears() {
        return ResponseEntity.ok(academicYearService.getAllAcademicYears());
    }

    @GetMapping("/ecole/{ecoleId}")
    @Operation(summary = "Années par école", description = "Retourne les années scolaires d'une école")
    public ResponseEntity<List<AcademicYearResponse>> getBySchool(@PathVariable Long ecoleId) {
        return ResponseEntity.ok(academicYearService.getAcademicYearsBySchool(ecoleId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier une année scolaire", description = "Modifie une année scolaire")
    public ResponseEntity<AcademicYearResponse> updateAcademicYear(@PathVariable Long id, @Valid @RequestBody AcademicYearRequest request) {
        return ResponseEntity.ok(academicYearService.updateAcademicYear(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une année scolaire", description = "Supprime une année scolaire")
    public ResponseEntity<Void> deleteAcademicYear(@PathVariable Long id) {
        academicYearService.deleteAcademicYear(id);
        return ResponseEntity.noContent().build();
    }
}
