package com.bulletin.controller.school;

import com.bulletin.dto.school.SchoolRequest;
import com.bulletin.dto.school.SchoolResponse;
import com.bulletin.security.SecurityUtils;
import com.bulletin.service.SchoolService;
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
@RequestMapping("/api/ecoles")
@RequiredArgsConstructor
@Tag(name = "Écoles", description = "Gestion des établissements scolaires")
public class SchoolController {

    private final SchoolService schoolService;
    private final SecurityUtils securityUtils;

    @PostMapping
    @Operation(summary = "Créer une école", description = "Crée un nouvel établissement scolaire (SUPER_ADMIN uniquement)")
    public ResponseEntity<SchoolResponse> createSchool(@Valid @RequestBody SchoolRequest request) {
        if (!securityUtils.isSuperAdmin()) {
            throw new SecurityException("Accès refusé : seul SUPER_ADMIN peut créer une école");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(schoolService.createSchool(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "École par ID", description = "Retourne une école par son ID")
    public ResponseEntity<SchoolResponse> getSchool(@PathVariable Long id) {
        return ResponseEntity.ok(schoolService.getSchool(id));
    }

    @GetMapping
    @Operation(summary = "Liste des écoles", description = "Retourne toutes les écoles (avec pagination optionnelle)")
    public ResponseEntity<Page<SchoolResponse>> getAllSchools(Pageable pageable) {
        return ResponseEntity.ok(schoolService.getAllSchools(pageable));
    }

    @GetMapping("/all")
    @Operation(summary = "Liste complète des écoles", description = "Retourne toutes les écoles sans pagination")
    public ResponseEntity<List<SchoolResponse>> getAllSchoolsUnpaginated() {
        return ResponseEntity.ok(schoolService.getAllSchools());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier une école", description = "Modifie une école existante (SUPER_ADMIN uniquement)")
    public ResponseEntity<SchoolResponse> updateSchool(@PathVariable Long id, @Valid @RequestBody SchoolRequest request) {
        if (!securityUtils.isSuperAdmin()) {
            throw new SecurityException("Accès refusé : seul SUPER_ADMIN peut modifier une école");
        }
        return ResponseEntity.ok(schoolService.updateSchool(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une école", description = "Supprime une école (SUPER_ADMIN uniquement)")
    public ResponseEntity<Void> deleteSchool(@PathVariable Long id) {
        if (!securityUtils.isSuperAdmin()) {
            throw new SecurityException("Accès refusé : seul SUPER_ADMIN peut supprimer une école");
        }
        schoolService.deleteSchool(id);
        return ResponseEntity.noContent().build();
    }
}
