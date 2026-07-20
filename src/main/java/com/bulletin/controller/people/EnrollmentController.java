package com.bulletin.controller.people;

import com.bulletin.dto.people.EnrollmentRequest;
import com.bulletin.dto.people.EnrollmentResponse;
import com.bulletin.service.EnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inscriptions")
@RequiredArgsConstructor
@Tag(name = "Inscriptions", description = "Gestion des inscriptions des élèves par classe")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping
    @Operation(summary = "Créer une inscription", description = "Inscrit un élève dans une classe")
    public ResponseEntity<EnrollmentResponse> createEnrollment(@Valid @RequestBody EnrollmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(enrollmentService.createEnrollment(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Inscription par ID", description = "Retourne une inscription par son ID")
    public ResponseEntity<EnrollmentResponse> getEnrollment(@PathVariable Long id) {
        return ResponseEntity.ok(enrollmentService.getEnrollment(id));
    }

    @GetMapping
    @Operation(summary = "Liste des inscriptions", description = "Retourne toutes les inscriptions")
    public ResponseEntity<List<EnrollmentResponse>> getAllEnrollments() {
        return ResponseEntity.ok(enrollmentService.getAllEnrollments());
    }

    @GetMapping("/eleve/{eleveId}")
    @Operation(summary = "Inscriptions par élève", description = "Retourne les inscriptions d'un élève")
    public ResponseEntity<List<EnrollmentResponse>> getByStudent(@PathVariable Long eleveId) {
        return ResponseEntity.ok(enrollmentService.getEnrollmentsByStudent(eleveId));
    }

    @GetMapping("/salle/{salleId}")
    @Operation(summary = "Inscriptions par classe", description = "Retourne les inscriptions d'une classe")
    public ResponseEntity<List<EnrollmentResponse>> getByClassroom(@PathVariable Long salleId) {
        return ResponseEntity.ok(enrollmentService.getEnrollmentsByClassroom(salleId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier une inscription", description = "Modifie une inscription")
    public ResponseEntity<EnrollmentResponse> updateEnrollment(@PathVariable Long id, @Valid @RequestBody EnrollmentRequest request) {
        return ResponseEntity.ok(enrollmentService.updateEnrollment(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une inscription", description = "Supprime une inscription")
    public ResponseEntity<Void> deleteEnrollment(@PathVariable Long id) {
        enrollmentService.deleteEnrollment(id);
        return ResponseEntity.noContent().build();
    }
}
