package com.bulletin.controller.grade;

import com.bulletin.dto.grade.AssessmentTypeRequest;
import com.bulletin.dto.grade.AssessmentTypeResponse;
import com.bulletin.service.AssessmentTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/types-evaluations")
@RequiredArgsConstructor
@Tag(name = "Types d'évaluation", description = "Interrogation, TP, Composition, Examen...")
public class AssessmentTypeController {

    private final AssessmentTypeService assessmentTypeService;

    @PostMapping
    @Operation(summary = "Créer un type d'évaluation", description = "Crée un type d'évaluation")
    public ResponseEntity<AssessmentTypeResponse> createAssessmentType(@Valid @RequestBody AssessmentTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(assessmentTypeService.createAssessmentType(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Type par ID", description = "Retourne un type d'évaluation par son ID")
    public ResponseEntity<AssessmentTypeResponse> getAssessmentType(@PathVariable Long id) {
        return ResponseEntity.ok(assessmentTypeService.getAssessmentType(id));
    }

    @GetMapping
    @Operation(summary = "Liste des types", description = "Retourne tous les types d'évaluation")
    public ResponseEntity<List<AssessmentTypeResponse>> getAllAssessmentTypes() {
        return ResponseEntity.ok(assessmentTypeService.getAllAssessmentTypes());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un type", description = "Modifie un type d'évaluation")
    public ResponseEntity<AssessmentTypeResponse> updateAssessmentType(@PathVariable Long id, @Valid @RequestBody AssessmentTypeRequest request) {
        return ResponseEntity.ok(assessmentTypeService.updateAssessmentType(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un type", description = "Supprime un type d'évaluation")
    public ResponseEntity<Void> deleteAssessmentType(@PathVariable Long id) {
        assessmentTypeService.deleteAssessmentType(id);
        return ResponseEntity.noContent().build();
    }
}
