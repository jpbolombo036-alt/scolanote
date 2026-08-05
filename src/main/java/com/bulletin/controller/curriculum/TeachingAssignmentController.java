package com.bulletin.controller.curriculum;

import com.bulletin.dto.curriculum.TeachingAssignmentRequest;
import com.bulletin.dto.curriculum.TeachingAssignmentResponse;
import com.bulletin.security.SecurityUtils;
import com.bulletin.service.TeachingAssignmentService;
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
@RequestMapping("/api/attributions-enseignement")
@RequiredArgsConstructor
@Tag(name = "Affectations", description = "Affectation des professeurs aux matières/classe")
public class TeachingAssignmentController {

    private final TeachingAssignmentService teachingAssignmentService;
    private final SecurityUtils securityUtils;

    @PostMapping
    @Operation(summary = "Créer une affectation", description = "Affecte un professeur à une matière dans une classe (direction)")
    public ResponseEntity<TeachingAssignmentResponse> createTeachingAssignment(@Valid @RequestBody TeachingAssignmentRequest request) {
        securityUtils.assertPermission("AFFECTATION_GERER");
        return ResponseEntity.status(HttpStatus.CREATED).body(teachingAssignmentService.createTeachingAssignment(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Affectation par ID", description = "Retourne une affectation par son ID")
    public ResponseEntity<TeachingAssignmentResponse> getTeachingAssignment(@PathVariable Long id) {
        return ResponseEntity.ok(teachingAssignmentService.getTeachingAssignment(id));
    }

    @GetMapping
    @Operation(summary = "Liste des affectations", description = "Retourne les affectations accessibles à l'utilisateur connecté (paginé)")
    public ResponseEntity<Page<TeachingAssignmentResponse>> getAccessibleTeachingAssignments(Pageable pageable) {
        return ResponseEntity.ok(teachingAssignmentService.getAccessibleTeachingAssignments(pageable));
    }

    @GetMapping("/all")
    @Operation(summary = "Liste complète des affectations", description = "Retourne toutes les affectations sans pagination")
    public ResponseEntity<List<TeachingAssignmentResponse>> getAllTeachingAssignments() {
        return ResponseEntity.ok(teachingAssignmentService.getAccessibleTeachingAssignments());
    }

    @GetMapping("/enseignant/{enseignantId}")
    @Operation(summary = "Affectations par professeur", description = "Retourne les affectations d'un professeur")
    public ResponseEntity<List<TeachingAssignmentResponse>> getByTeacher(@PathVariable Long enseignantId) {
        return ResponseEntity.ok(teachingAssignmentService.getByTeacher(enseignantId));
    }

    @GetMapping("/salle/{salleId}")
    @Operation(summary = "Affectations par classe", description = "Retourne les affectations d'une classe")
    public ResponseEntity<List<TeachingAssignmentResponse>> getByClassroom(@PathVariable Long salleId) {
        return ResponseEntity.ok(teachingAssignmentService.getByClassroom(salleId));
    }

    @GetMapping("/matiere/{matiereId}")
    @Operation(summary = "Affectations par matière", description = "Retourne les affectations d'une matière")
    public ResponseEntity<List<TeachingAssignmentResponse>> getBySubject(@PathVariable Long matiereId) {
        return ResponseEntity.ok(teachingAssignmentService.getBySubject(matiereId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier une affectation", description = "Modifie une affectation (direction)")
    public ResponseEntity<TeachingAssignmentResponse> updateTeachingAssignment(@PathVariable Long id, @Valid @RequestBody TeachingAssignmentRequest request) {
        securityUtils.assertPermission("AFFECTATION_GERER");
        return ResponseEntity.ok(teachingAssignmentService.updateTeachingAssignment(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une affectation", description = "Supprime une affectation (direction)")
    public ResponseEntity<Void> deleteTeachingAssignment(@PathVariable Long id) {
        securityUtils.assertPermission("AFFECTATION_GERER");
        teachingAssignmentService.deleteTeachingAssignment(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Alias « /classe/{classeId} » conforme à la spécification (FRONTEND_SPEC.md)
     * et au client frontend. L'ancien slug /salle/{salleId} est conservé pour la rétrocompatibilité.
     */
    @GetMapping("/classe/{classeId}")
    @Operation(summary = "Affectations par classe", description = "Retourne les affectations d'une classe (slug /classe)")
    public ResponseEntity<List<TeachingAssignmentResponse>> getByClassroomByClasse(@PathVariable Long classeId) {
        return ResponseEntity.ok(teachingAssignmentService.getByClassroom(classeId));
    }
}
