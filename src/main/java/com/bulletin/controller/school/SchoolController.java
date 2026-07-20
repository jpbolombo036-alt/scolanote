package com.bulletin.controller.school;

import com.bulletin.dto.school.SchoolRequest;
import com.bulletin.dto.school.SchoolResponse;
import com.bulletin.service.SchoolService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schools")
@RequiredArgsConstructor
@Tag(name = "Écoles", description = "Gestion des établissements scolaires")
public class SchoolController {

    private final SchoolService schoolService;

    @PostMapping
    @Operation(summary = "Créer une école", description = "Crée un nouvel établissement scolaire")
    public ResponseEntity<SchoolResponse> createSchool(@Valid @RequestBody SchoolRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(schoolService.createSchool(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "École par ID", description = "Retourne une école par son ID")
    public ResponseEntity<SchoolResponse> getSchool(@PathVariable Long id) {
        return ResponseEntity.ok(schoolService.getSchool(id));
    }

    @GetMapping
    @Operation(summary = "Liste des écoles", description = "Retourne toutes les écoles")
    public ResponseEntity<List<SchoolResponse>> getAllSchools() {
        return ResponseEntity.ok(schoolService.getAllSchools());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier une école", description = "Modifie une école existante")
    public ResponseEntity<SchoolResponse> updateSchool(@PathVariable Long id, @Valid @RequestBody SchoolRequest request) {
        return ResponseEntity.ok(schoolService.updateSchool(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une école", description = "Supprime une école")
    public ResponseEntity<Void> deleteSchool(@PathVariable Long id) {
        schoolService.deleteSchool(id);
        return ResponseEntity.noContent().build();
    }
}
