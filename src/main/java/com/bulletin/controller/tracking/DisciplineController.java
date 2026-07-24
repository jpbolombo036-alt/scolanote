package com.bulletin.controller.tracking;

import com.bulletin.dto.tracking.DisciplineRequest;
import com.bulletin.dto.tracking.DisciplineResponse;
import com.bulletin.security.SecurityUtils;
import com.bulletin.service.DisciplineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/disciplines")
@RequiredArgsConstructor
@Tag(name = "Discipline", description = "Suivi de la conduite et de l'application")
public class DisciplineController {

    private final DisciplineService disciplineService;
    private final SecurityUtils securityUtils;

    @PostMapping
    @Operation(summary = "Créer une fiche de discipline", description = "Enregistre la conduite/application d'un élève pour un trimestre")
    public ResponseEntity<DisciplineResponse> createDiscipline(@Valid @RequestBody DisciplineRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(disciplineService.createDiscipline(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Discipline par ID", description = "Retourne une fiche de discipline par son ID")
    public ResponseEntity<DisciplineResponse> getDiscipline(@PathVariable Long id) {
        return ResponseEntity.ok(disciplineService.getDiscipline(id));
    }

    @GetMapping
    @Operation(summary = "Liste des fiches", description = "Retourne les fiches de discipline accessibles à l'utilisateur connecté")
    public ResponseEntity<List<DisciplineResponse>> getAccessibleDisciplines() {
        return ResponseEntity.ok(disciplineService.getAccessibleDisciplines());
    }

    @GetMapping("/eleve/{eleveId}")
    @Operation(summary = "Discipline par élève", description = "Retourne les fiches de discipline d'un élève")
    public ResponseEntity<List<DisciplineResponse>> getByStudent(@PathVariable Long eleveId) {
        return ResponseEntity.ok(disciplineService.getByStudent(eleveId));
    }

    @GetMapping("/trimestre/{trimestreId}")
    @Operation(summary = "Discipline par trimestre", description = "Retourne les fiches de discipline d'un trimestre")
    public ResponseEntity<List<DisciplineResponse>> getByTerm(@PathVariable Long trimestreId) {
        return ResponseEntity.ok(disciplineService.getByTerm(trimestreId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier une fiche", description = "Modifie une fiche de discipline")
    public ResponseEntity<DisciplineResponse> updateDiscipline(@PathVariable Long id, @Valid @RequestBody DisciplineRequest request) {
        return ResponseEntity.ok(disciplineService.updateDiscipline(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une fiche", description = "Supprime une fiche de discipline")
    public ResponseEntity<Void> deleteDiscipline(@PathVariable Long id) {
        disciplineService.deleteDiscipline(id);
        return ResponseEntity.noContent().build();
    }
}
