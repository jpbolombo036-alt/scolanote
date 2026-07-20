package com.bulletin.controller.curriculum;

import com.bulletin.dto.curriculum.CurriculumRequest;
import com.bulletin.dto.curriculum.CurriculumResponse;
import com.bulletin.service.CurriculumService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/programmes")
@RequiredArgsConstructor
@Tag(name = "Programmes", description = "Gestion des programmes officiels")
public class CurriculumController {

    private final CurriculumService curriculumService;

    @PostMapping
    @Operation(summary = "Créer un programme", description = "Crée un nouveau programme")
    public ResponseEntity<CurriculumResponse> createCurriculum(@Valid @RequestBody CurriculumRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(curriculumService.createCurriculum(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Programme par ID", description = "Retourne un programme par son ID")
    public ResponseEntity<CurriculumResponse> getCurriculum(@PathVariable Long id) {
        return ResponseEntity.ok(curriculumService.getCurriculum(id));
    }

    @GetMapping
    @Operation(summary = "Liste des programmes", description = "Retourne tous les programmes")
    public ResponseEntity<List<CurriculumResponse>> getAllCurricula() {
        return ResponseEntity.ok(curriculumService.getAllCurricula());
    }

    @GetMapping("/niveau/{niveauId}")
    @Operation(summary = "Programmes par niveau", description = "Retourne les programmes d'un niveau")
    public ResponseEntity<List<CurriculumResponse>> getByLevel(@PathVariable Long niveauId) {
        return ResponseEntity.ok(curriculumService.getCurriculaByLevel(niveauId));
    }

    @GetMapping("/section/{sectionId}")
    @Operation(summary = "Programmes par section", description = "Retourne les programmes d'une section")
    public ResponseEntity<List<CurriculumResponse>> getBySection(@PathVariable Long sectionId) {
        return ResponseEntity.ok(curriculumService.getCurriculaBySection(sectionId));
    }

    @GetMapping("/option/{optionId}")
    @Operation(summary = "Programmes par option", description = "Retourne les programmes d'une option")
    public ResponseEntity<List<CurriculumResponse>> getByOption(@PathVariable Long optionId) {
        return ResponseEntity.ok(curriculumService.getCurriculaByOption(optionId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un programme", description = "Modifie un programme")
    public ResponseEntity<CurriculumResponse> updateCurriculum(@PathVariable Long id, @Valid @RequestBody CurriculumRequest request) {
        return ResponseEntity.ok(curriculumService.updateCurriculum(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un programme", description = "Supprime un programme")
    public ResponseEntity<Void> deleteCurriculum(@PathVariable Long id) {
        curriculumService.deleteCurriculum(id);
        return ResponseEntity.noContent().build();
    }
}
