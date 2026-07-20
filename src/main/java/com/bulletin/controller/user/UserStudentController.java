package com.bulletin.controller.user;

import com.bulletin.dto.user.UserStudentRequest;
import com.bulletin.dto.user.UserStudentResponse;
import com.bulletin.service.UserStudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/utilisateurs-eleves")
@RequiredArgsConstructor
@Tag(name = "Liens user-élève", description = "Association comptes ↔ élèves")
public class UserStudentController {

    private final UserStudentService userStudentService;

    @PostMapping
    @Operation(summary = "Associer un élève", description = "Associe un élève à un compte")
    public ResponseEntity<UserStudentResponse> createUserStudent(@Valid @RequestBody UserStudentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userStudentService.createUserStudent(request));
    }

    @GetMapping
    @Operation(summary = "Liste des liens", description = "Retourne tous les liens user-élève")
    public ResponseEntity<List<UserStudentResponse>> getAllUserStudents() {
        return ResponseEntity.ok(userStudentService.getAllUserStudents());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Retirer une association", description = "Retire l'association user-élève")
    public ResponseEntity<Void> deleteUserStudent(@PathVariable Long id) {
        userStudentService.deleteUserStudent(id);
        return ResponseEntity.noContent().build();
    }
}
