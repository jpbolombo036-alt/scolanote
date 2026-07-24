package com.bulletin.controller.grade;

import com.bulletin.dto.grade.GradeRequest;
import com.bulletin.dto.grade.GradeResponse;
import com.bulletin.security.SecurityUtils;
import com.bulletin.service.GradeService;
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
@RequestMapping("/api/notes")
@RequiredArgsConstructor
@Tag(name = "Notes", description = "Encodage des notes des élèves")
public class GradeController {

    private final GradeService gradeService;
    private final SecurityUtils securityUtils;

    @PostMapping
    @Operation(summary = "Encoder une note", description = "Encode une note pour un élève à une évaluation")
    public ResponseEntity<GradeResponse> createGrade(@Valid @RequestBody GradeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(gradeService.createGrade(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Note par ID", description = "Retourne une note par son ID")
    public ResponseEntity<GradeResponse> getGrade(@PathVariable Long id) {
        return ResponseEntity.ok(gradeService.getGrade(id));
    }

    @GetMapping
    @Operation(summary = "Liste des notes", description = "Retourne les notes accessibles à l'utilisateur connecté")
    public ResponseEntity<Page<GradeResponse>> getAccessibleGrades(Pageable pageable) {
        return ResponseEntity.ok(gradeService.getAccessibleGrades(pageable));
    }

    @GetMapping("/unpaginated")
    @Operation(summary = "Liste complète des notes", description = "Retourne toutes les notes sans pagination")
    public ResponseEntity<List<GradeResponse>> getAllGradesUnpaginated() {
        return ResponseEntity.ok(gradeService.getAccessibleGrades());
    }

    @GetMapping("/evaluation/{evaluationId}")
    @Operation(summary = "Notes par évaluation", description = "Retourne les notes d'une évaluation")
    public ResponseEntity<List<GradeResponse>> getByAssessment(@PathVariable Long evaluationId) {
        return ResponseEntity.ok(gradeService.getByAssessment(evaluationId));
    }

    @GetMapping("/eleve/{eleveId}")
    @Operation(summary = "Notes par élève", description = "Retourne les notes d'un élève")
    public ResponseEntity<List<GradeResponse>> getByStudent(@PathVariable Long eleveId) {
        return ResponseEntity.ok(gradeService.getByStudent(eleveId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier une note", description = "Modifie une note")
    public ResponseEntity<GradeResponse> updateGrade(@PathVariable Long id, @Valid @RequestBody GradeRequest request) {
        return ResponseEntity.ok(gradeService.updateGrade(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une note", description = "Supprime une note")
    public ResponseEntity<Void> deleteGrade(@PathVariable Long id) {
        gradeService.deleteGrade(id);
        return ResponseEntity.noContent().build();
    }
}
