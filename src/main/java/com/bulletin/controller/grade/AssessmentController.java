package com.bulletin.controller.grade;

import com.bulletin.dto.grade.AssessmentRequest;
import com.bulletin.dto.grade.AssessmentResponse;
import com.bulletin.service.AssessmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assessments")
@RequiredArgsConstructor
@Tag(name = "Évaluations", description = "Évaluations créées par les professeurs")
public class AssessmentController {

    private final AssessmentService assessmentService;

    @PostMapping
    @Operation(summary = "Créer une évaluation", description = "Crée une évaluation pour une affectation")
    public ResponseEntity<AssessmentResponse> createAssessment(@Valid @RequestBody AssessmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(assessmentService.createAssessment(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Évaluation par ID", description = "Retourne une évaluation par son ID")
    public ResponseEntity<AssessmentResponse> getAssessment(@PathVariable Long id) {
        return ResponseEntity.ok(assessmentService.getAssessment(id));
    }

    @GetMapping
    @Operation(summary = "Liste des évaluations", description = "Retourne toutes les évaluations")
    public ResponseEntity<List<AssessmentResponse>> getAllAssessments() {
        return ResponseEntity.ok(assessmentService.getAllAssessments());
    }

    @GetMapping("/assignment/{assignmentId}")
    @Operation(summary = "Évaluations par affectation", description = "Retourne les évaluations d'une affectation")
    public ResponseEntity<List<AssessmentResponse>> getByAssignment(@PathVariable Long assignmentId) {
        return ResponseEntity.ok(assessmentService.getByAssignment(assignmentId));
    }

    @GetMapping("/term/{termId}")
    @Operation(summary = "Évaluations par trimestre", description = "Retourne les évaluations d'un trimestre")
    public ResponseEntity<List<AssessmentResponse>> getByTerm(@PathVariable Long termId) {
        return ResponseEntity.ok(assessmentService.getByTerm(termId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier une évaluation", description = "Modifie une évaluation")
    public ResponseEntity<AssessmentResponse> updateAssessment(@PathVariable Long id, @Valid @RequestBody AssessmentRequest request) {
        return ResponseEntity.ok(assessmentService.updateAssessment(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une évaluation", description = "Supprime une évaluation")
    public ResponseEntity<Void> deleteAssessment(@PathVariable Long id) {
        assessmentService.deleteAssessment(id);
        return ResponseEntity.noContent().build();
    }
}
