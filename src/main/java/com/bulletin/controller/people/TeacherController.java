package com.bulletin.controller.people;

import com.bulletin.dto.people.TeacherRequest;
import com.bulletin.dto.people.TeacherResponse;
import com.bulletin.security.SecurityUtils;
import com.bulletin.service.TeacherService;
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
@RequestMapping("/api/enseignants")
@RequiredArgsConstructor
@Tag(name = "Professeurs", description = "Gestion des professeurs")
public class TeacherController {

    private final TeacherService teacherService;
    private final SecurityUtils securityUtils;

    @PostMapping
    @Operation(summary = "Créer un professeur", description = "Crée un nouveau professeur (direction uniquement)")
    public ResponseEntity<TeacherResponse> createTeacher(@Valid @RequestBody TeacherRequest request) {
        if (!securityUtils.isDirection()) {
            throw new SecurityException("Accès refusé : seul la direction peut créer un professeur");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(teacherService.createTeacher(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Professeur par ID", description = "Retourne un professeur par son ID")
    public ResponseEntity<TeacherResponse> getTeacher(@PathVariable Long id) {
        return ResponseEntity.ok(teacherService.getTeacher(id));
    }

    @GetMapping
    @Operation(summary = "Liste des professeurs", description = "Retourne les professeurs accessibles à l'utilisateur connecté")
    public ResponseEntity<Page<TeacherResponse>> getAccessibleTeachers(Pageable pageable) {
        return ResponseEntity.ok(teacherService.getAccessibleTeachers(pageable));
    }

    @GetMapping("/all")
    @Operation(summary = "Liste complète des professeurs", description = "Retourne tous les professeurs (SUPER_ADMIN uniquement)")
    public ResponseEntity<List<TeacherResponse>> getAllTeachersUnpaginated() {
        return ResponseEntity.ok(teacherService.getAccessibleTeachers());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un professeur", description = "Modifie un professeur (direction uniquement)")
    public ResponseEntity<TeacherResponse> updateTeacher(@PathVariable Long id, @Valid @RequestBody TeacherRequest request) {
        if (!securityUtils.isDirection()) {
            throw new SecurityException("Accès refusé : seul la direction peut modifier un professeur");
        }
        return ResponseEntity.ok(teacherService.updateTeacher(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un professeur", description = "Supprime un professeur (direction uniquement)")
    public ResponseEntity<Void> deleteTeacher(@PathVariable Long id) {
        if (!securityUtils.isDirection()) {
            throw new SecurityException("Accès refusé : seul la direction peut supprimer un professeur");
        }
        teacherService.deleteTeacher(id);
        return ResponseEntity.noContent().build();
    }
}
