package com.bulletin.controller.user;

import com.bulletin.dto.user.UserTeacherRequest;
import com.bulletin.dto.user.UserTeacherResponse;
import com.bulletin.security.SecurityUtils;
import com.bulletin.service.UserTeacherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user-teachers")
@RequiredArgsConstructor
@Tag(name = "Liens user-professeur", description = "Association comptes ↔ professeurs")
public class UserTeacherController {

    private final UserTeacherService userTeacherService;
    private final SecurityUtils securityUtils;

    @PostMapping
    @Operation(summary = "Associer un professeur", description = "Associe un professeur à un compte")
    public ResponseEntity<UserTeacherResponse> createUserTeacher(@Valid @RequestBody UserTeacherRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userTeacherService.createUserTeacher(request));
    }

    @GetMapping
    @Operation(summary = "Liste des liens", description = "Retourne les liens user-professeur accessibles")
    public ResponseEntity<List<UserTeacherResponse>> getAccessibleUserTeachers() {
        return ResponseEntity.ok(userTeacherService.getAccessibleUserTeachers());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Retirer une association", description = "Retire l'association user-professeur")
    public ResponseEntity<Void> deleteUserTeacher(@PathVariable Long id) {
        userTeacherService.deleteUserTeacher(id);
        return ResponseEntity.noContent().build();
    }
}
