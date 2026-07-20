package com.bulletin.controller.school;

import com.bulletin.dto.school.ClassroomRequest;
import com.bulletin.dto.school.ClassroomResponse;
import com.bulletin.service.ClassroomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/classrooms")
@RequiredArgsConstructor
@Tag(name = "Classes", description = "Gestion des classes")
public class ClassroomController {

    private final ClassroomService classroomService;

    @PostMapping
    @Operation(summary = "Créer une classe", description = "Crée une nouvelle classe")
    public ResponseEntity<ClassroomResponse> createClassroom(@Valid @RequestBody ClassroomRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(classroomService.createClassroom(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Classe par ID", description = "Retourne une classe par son ID")
    public ResponseEntity<ClassroomResponse> getClassroom(@PathVariable Long id) {
        return ResponseEntity.ok(classroomService.getClassroom(id));
    }

    @GetMapping
    @Operation(summary = "Liste des classes", description = "Retourne toutes les classes")
    public ResponseEntity<List<ClassroomResponse>> getAllClassrooms() {
        return ResponseEntity.ok(classroomService.getAllClassrooms());
    }

    @GetMapping("/academic-year/{academicYearId}")
    @Operation(summary = "Classes par année", description = "Retourne les classes d'une année scolaire")
    public ResponseEntity<List<ClassroomResponse>> getByAcademicYear(@PathVariable Long academicYearId) {
        return ResponseEntity.ok(classroomService.getClassroomsByAcademicYear(academicYearId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier une classe", description = "Modifie une classe")
    public ResponseEntity<ClassroomResponse> updateClassroom(@PathVariable Long id, @Valid @RequestBody ClassroomRequest request) {
        return ResponseEntity.ok(classroomService.updateClassroom(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une classe", description = "Supprime une classe")
    public ResponseEntity<Void> deleteClassroom(@PathVariable Long id) {
        classroomService.deleteClassroom(id);
        return ResponseEntity.noContent().build();
    }
}
