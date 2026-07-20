package com.bulletin.controller.school;

import com.bulletin.dto.school.OptionRequest;
import com.bulletin.dto.school.OptionResponse;
import com.bulletin.service.OptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/options")
@RequiredArgsConstructor
@Tag(name = "Options", description = "Gestion des options (Scientifique, Commerciale, ...)")
public class OptionController {

    private final OptionService optionService;

    @PostMapping
    @Operation(summary = "Créer une option", description = "Crée une nouvelle option")
    public ResponseEntity<OptionResponse> createOption(@Valid @RequestBody OptionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(optionService.createOption(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Option par ID", description = "Retourne une option par son ID")
    public ResponseEntity<OptionResponse> getOption(@PathVariable Long id) {
        return ResponseEntity.ok(optionService.getOption(id));
    }

    @GetMapping
    @Operation(summary = "Liste des options", description = "Retourne toutes les options")
    public ResponseEntity<List<OptionResponse>> getAllOptions() {
        return ResponseEntity.ok(optionService.getAllOptions());
    }

    @GetMapping("/section/{sectionId}")
    @Operation(summary = "Options par section", description = "Retourne les options d'une section")
    public ResponseEntity<List<OptionResponse>> getBySection(@PathVariable Long sectionId) {
        return ResponseEntity.ok(optionService.getOptionsBySection(sectionId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier une option", description = "Modifie une option")
    public ResponseEntity<OptionResponse> updateOption(@PathVariable Long id, @Valid @RequestBody OptionRequest request) {
        return ResponseEntity.ok(optionService.updateOption(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une option", description = "Supprime une option")
    public ResponseEntity<Void> deleteOption(@PathVariable Long id) {
        optionService.deleteOption(id);
        return ResponseEntity.noContent().build();
    }
}
