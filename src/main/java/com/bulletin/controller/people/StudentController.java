package com.bulletin.controller.people;

import com.bulletin.dto.people.StudentRequest;
import com.bulletin.dto.people.StudentResponse;
import com.bulletin.security.SecurityUtils;
import com.bulletin.service.StudentService;
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
@RequestMapping("/api/eleves")
@RequiredArgsConstructor
@Tag(name = "Élèves", description = "Gestion des élèves")
public class StudentController {

    private final StudentService studentService;
    private final SecurityUtils securityUtils;

    @PostMapping
    @Operation(summary = "Créer un élève", description = "Crée un nouvel élève (direction uniquement)")
    public ResponseEntity<StudentResponse> createStudent(@Valid @RequestBody StudentRequest request) {
        if (!securityUtils.isDirection()) {
            throw new SecurityException("Accès refusé : seul la direction peut créer un élève");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(studentService.createStudent(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Élève par ID", description = "Retourne un élève par son ID")
    public ResponseEntity<StudentResponse> getStudent(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.getStudent(id));
    }

    @GetMapping
    @Operation(summary = "Liste des élèves", description = "Retourne les élèves accessibles à l'utilisateur connecté")
    public ResponseEntity<Page<StudentResponse>> getAccessibleStudents(Pageable pageable) {
        return ResponseEntity.ok(studentService.getAccessibleStudents(pageable));
    }

    @GetMapping("/all")
    @Operation(summary = "Liste complète des élèves", description = "Retourne tous les élèves (SUPER_ADMIN uniquement)")
    public ResponseEntity<List<StudentResponse>> getAllStudentsUnpaginated() {
        return ResponseEntity.ok(studentService.getAccessibleStudents());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un élève", description = "Modifie un élève (direction uniquement)")
    public ResponseEntity<StudentResponse> updateStudent(@PathVariable Long id, @Valid @RequestBody StudentRequest request) {
        if (!securityUtils.isDirection()) {
            throw new SecurityException("Accès refusé : seul la direction peut modifier un élève");
        }
        return ResponseEntity.ok(studentService.updateStudent(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un élève", description = "Supprime un élève (direction uniquement)")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        if (!securityUtils.isDirection()) {
            throw new SecurityException("Accès refusé : seul la direction peut supprimer un élève");
        }
        studentService.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }
}
