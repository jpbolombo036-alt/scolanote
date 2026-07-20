package com.bulletin.controller.curriculum;

import com.bulletin.dto.curriculum.CurriculumSubjectRequest;
import com.bulletin.dto.curriculum.CurriculumSubjectResponse;
import com.bulletin.service.CurriculumSubjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matieres-programme")
@RequiredArgsConstructor
@Tag(name = "Matières de programme", description = "Coefficients et matières par programme")
public class CurriculumSubjectController {

    private final CurriculumSubjectService curriculumSubjectService;

    @PostMapping
    @Operation(summary = "Ajouter une matière au programme", description = "Ajoute une matière avec coefficient à un programme")
    public ResponseEntity<CurriculumSubjectResponse> createCurriculumSubject(@Valid @RequestBody CurriculumSubjectRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(curriculumSubjectService.createCurriculumSubject(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Matière de programme par ID", description = "Retourne une matière de programme par son ID")
    public ResponseEntity<CurriculumSubjectResponse> getCurriculumSubject(@PathVariable Long id) {
        return ResponseEntity.ok(curriculumSubjectService.getCurriculumSubject(id));
    }

    @GetMapping
    @Operation(summary = "Liste des matières de programme", description = "Retourne toutes les matières de programme")
    public ResponseEntity<List<CurriculumSubjectResponse>> getAllCurriculumSubjects() {
        return ResponseEntity.ok(curriculumSubjectService.getAllCurriculumSubjects());
    }

    @GetMapping("/programme/{programmeId}")
    @Operation(summary = "Matières par programme", description = "Retourne les matières d'un programme (avec coefficients)")
    public ResponseEntity<List<CurriculumSubjectResponse>> getByCurriculum(@PathVariable Long programmeId) {
        return ResponseEntity.ok(curriculumSubjectService.getByCurriculum(programmeId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier une matière de programme", description = "Modifie coefficient/ordre/obligatoire")
    public ResponseEntity<CurriculumSubjectResponse> updateCurriculumSubject(@PathVariable Long id, @Valid @RequestBody CurriculumSubjectRequest request) {
        return ResponseEntity.ok(curriculumSubjectService.updateCurriculumSubject(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une matière de programme", description = "Retire une matière du programme")
    public ResponseEntity<Void> deleteCurriculumSubject(@PathVariable Long id) {
        curriculumSubjectService.deleteCurriculumSubject(id);
        return ResponseEntity.noContent().build();
    }
}
