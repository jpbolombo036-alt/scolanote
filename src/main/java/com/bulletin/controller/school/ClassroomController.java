package com.bulletin.controller.school;

import com.bulletin.dto.school.ClassroomRequest;
import com.bulletin.dto.school.ClassroomResponse;
import com.bulletin.security.SecurityUtils;
import com.bulletin.service.ClassroomService;
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
@RequestMapping("/api/salles")
@RequiredArgsConstructor
@Tag(name = "Classes", description = "Gestion des classes")
public class ClassroomController {

    private final ClassroomService classroomService;
    private final SecurityUtils securityUtils;

    @PostMapping
    @Operation(summary = "Créer une classe", description = "Crée une nouvelle classe (direction uniquement)")
    public ResponseEntity<ClassroomResponse> createClassroom(@Valid @RequestBody ClassroomRequest request) {
        if (!securityUtils.isDirection()) {
            throw new SecurityException("Accès refusé : seul la direction peut créer une classe");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(classroomService.createClassroom(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Classe par ID", description = "Retourne une classe par son ID")
    public ResponseEntity<ClassroomResponse> getClassroom(@PathVariable Long id) {
        return ResponseEntity.ok(classroomService.getClassroom(id));
    }

    @GetMapping
    @Operation(summary = "Liste des classes", description = "Retourne les classes accessibles à l'utilisateur connecté")
    public ResponseEntity<Page<ClassroomResponse>> getAccessibleClassrooms(Pageable pageable) {
        return ResponseEntity.ok(classroomService.getAccessibleClassrooms(pageable));
    }

    @GetMapping("/all")
    @Operation(summary = "Liste complète des classes", description = "Retourne toutes les classes (SUPER_ADMIN uniquement)")
    public ResponseEntity<List<ClassroomResponse>> getAllClassroomsUnpaginated() {
        return ResponseEntity.ok(classroomService.getAccessibleClassrooms());
    }

    @GetMapping("/annee-academique/{anneeAcademiqueId}")
    @Operation(summary = "Classes par année", description = "Retourne les classes d'une année scolaire")
    public ResponseEntity<List<ClassroomResponse>> getByAcademicYear(@PathVariable Long anneeAcademiqueId) {
        return ResponseEntity.ok(classroomService.getClassroomsByAcademicYear(anneeAcademiqueId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier une classe", description = "Modifie une classe (direction uniquement)")
    public ResponseEntity<ClassroomResponse> updateClassroom(@PathVariable Long id, @Valid @RequestBody ClassroomRequest request) {
        if (!securityUtils.isDirection()) {
            throw new SecurityException("Accès refusé : seul la direction peut modifier une classe");
        }
        return ResponseEntity.ok(classroomService.updateClassroom(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une classe", description = "Supprime une classe (direction uniquement)")
    public ResponseEntity<Void> deleteClassroom(@PathVariable Long id) {
        if (!securityUtils.isDirection()) {
            throw new SecurityException("Accès refusé : seul la direction peut supprimer une classe");
        }
        classroomService.deleteClassroom(id);
        return ResponseEntity.noContent().build();
    }
}
