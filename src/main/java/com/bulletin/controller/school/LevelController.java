package com.bulletin.controller.school;

import com.bulletin.dto.school.LevelRequest;
import com.bulletin.dto.school.LevelResponse;
import com.bulletin.service.LevelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/levels")
@RequiredArgsConstructor
@Tag(name = "Niveaux", description = "Gestion des niveaux (Maternelle, Primaire, Secondaire)")
public class LevelController {

    private final LevelService levelService;

    @PostMapping
    @Operation(summary = "Créer un niveau", description = "Crée un nouveau niveau")
    public ResponseEntity<LevelResponse> createLevel(@Valid @RequestBody LevelRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(levelService.createLevel(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Niveau par ID", description = "Retourne un niveau par son ID")
    public ResponseEntity<LevelResponse> getLevel(@PathVariable Long id) {
        return ResponseEntity.ok(levelService.getLevel(id));
    }

    @GetMapping
    @Operation(summary = "Liste des niveaux", description = "Retourne tous les niveaux")
    public ResponseEntity<List<LevelResponse>> getAllLevels() {
        return ResponseEntity.ok(levelService.getAllLevels());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un niveau", description = "Modifie un niveau")
    public ResponseEntity<LevelResponse> updateLevel(@PathVariable Long id, @Valid @RequestBody LevelRequest request) {
        return ResponseEntity.ok(levelService.updateLevel(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un niveau", description = "Supprime un niveau")
    public ResponseEntity<Void> deleteLevel(@PathVariable Long id) {
        levelService.deleteLevel(id);
        return ResponseEntity.noContent().build();
    }
}
