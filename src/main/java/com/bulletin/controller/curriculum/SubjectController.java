package com.bulletin.controller.curriculum;

import com.bulletin.dto.curriculum.SubjectRequest;
import com.bulletin.dto.curriculum.SubjectResponse;
import com.bulletin.service.SubjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matieres")
@RequiredArgsConstructor
@Tag(name = "Matières", description = "Gestion des matières")
public class SubjectController {

    private final SubjectService subjectService;

    @PostMapping
    @Operation(summary = "Créer une matière", description = "Crée une nouvelle matière")
    public ResponseEntity<SubjectResponse> createSubject(@Valid @RequestBody SubjectRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(subjectService.createSubject(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Matière par ID", description = "Retourne une matière par son ID")
    public ResponseEntity<SubjectResponse> getSubject(@PathVariable Long id) {
        return ResponseEntity.ok(subjectService.getSubject(id));
    }

    @GetMapping
    @Operation(summary = "Liste des matières", description = "Retourne toutes les matières")
    public ResponseEntity<List<SubjectResponse>> getAllSubjects() {
        return ResponseEntity.ok(subjectService.getAllSubjects());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier une matière", description = "Modifie une matière")
    public ResponseEntity<SubjectResponse> updateSubject(@PathVariable Long id, @Valid @RequestBody SubjectRequest request) {
        return ResponseEntity.ok(subjectService.updateSubject(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une matière", description = "Supprime une matière")
    public ResponseEntity<Void> deleteSubject(@PathVariable Long id) {
        subjectService.deleteSubject(id);
        return ResponseEntity.noContent().build();
    }
}
