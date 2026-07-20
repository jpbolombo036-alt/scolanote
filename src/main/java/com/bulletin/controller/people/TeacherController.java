package com.bulletin.controller.people;

import com.bulletin.dto.people.TeacherRequest;
import com.bulletin.dto.people.TeacherResponse;
import com.bulletin.service.TeacherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teachers")
@RequiredArgsConstructor
@Tag(name = "Professeurs", description = "Gestion des professeurs")
public class TeacherController {

    private final TeacherService teacherService;

    @PostMapping
    @Operation(summary = "Créer un professeur", description = "Crée un nouveau professeur")
    public ResponseEntity<TeacherResponse> createTeacher(@Valid @RequestBody TeacherRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(teacherService.createTeacher(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Professeur par ID", description = "Retourne un professeur par son ID")
    public ResponseEntity<TeacherResponse> getTeacher(@PathVariable Long id) {
        return ResponseEntity.ok(teacherService.getTeacher(id));
    }

    @GetMapping
    @Operation(summary = "Liste des professeurs", description = "Retourne tous les professeurs")
    public ResponseEntity<List<TeacherResponse>> getAllTeachers() {
        return ResponseEntity.ok(teacherService.getAllTeachers());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un professeur", description = "Modifie un professeur")
    public ResponseEntity<TeacherResponse> updateTeacher(@PathVariable Long id, @Valid @RequestBody TeacherRequest request) {
        return ResponseEntity.ok(teacherService.updateTeacher(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un professeur", description = "Supprime un professeur")
    public ResponseEntity<Void> deleteTeacher(@PathVariable Long id) {
        teacherService.deleteTeacher(id);
        return ResponseEntity.noContent().build();
    }
}
