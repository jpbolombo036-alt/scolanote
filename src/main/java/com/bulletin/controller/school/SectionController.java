package com.bulletin.controller.school;

import com.bulletin.dto.school.SectionRequest;
import com.bulletin.dto.school.SectionResponse;
import com.bulletin.service.SectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sections")
@RequiredArgsConstructor
@Tag(name = "Sections", description = "Gestion des sections (Humanités, Technique, Professionnelle)")
public class SectionController {

    private final SectionService sectionService;

    @PostMapping
    @Operation(summary = "Créer une section", description = "Crée une nouvelle section")
    public ResponseEntity<SectionResponse> createSection(@Valid @RequestBody SectionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sectionService.createSection(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Section par ID", description = "Retourne une section par son ID")
    public ResponseEntity<SectionResponse> getSection(@PathVariable Long id) {
        return ResponseEntity.ok(sectionService.getSection(id));
    }

    @GetMapping
    @Operation(summary = "Liste des sections", description = "Retourne toutes les sections")
    public ResponseEntity<List<SectionResponse>> getAllSections() {
        return ResponseEntity.ok(sectionService.getAllSections());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier une section", description = "Modifie une section")
    public ResponseEntity<SectionResponse> updateSection(@PathVariable Long id, @Valid @RequestBody SectionRequest request) {
        return ResponseEntity.ok(sectionService.updateSection(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une section", description = "Supprime une section")
    public ResponseEntity<Void> deleteSection(@PathVariable Long id) {
        sectionService.deleteSection(id);
        return ResponseEntity.noContent().build();
    }
}
