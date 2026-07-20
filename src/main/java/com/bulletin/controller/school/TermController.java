package com.bulletin.controller.school;

import com.bulletin.dto.school.TermRequest;
import com.bulletin.dto.school.TermResponse;
import com.bulletin.service.TermService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trimestres")
@RequiredArgsConstructor
@Tag(name = "Trimestres", description = "Gestion des périodes/trimestres")
public class TermController {

    private final TermService termService;

    @PostMapping
    @Operation(summary = "Créer un trimestre", description = "Crée une nouvelle période")
    public ResponseEntity<TermResponse> createTerm(@Valid @RequestBody TermRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(termService.createTerm(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Trimestre par ID", description = "Retourne un trimestre par son ID")
    public ResponseEntity<TermResponse> getTerm(@PathVariable Long id) {
        return ResponseEntity.ok(termService.getTerm(id));
    }

    @GetMapping
    @Operation(summary = "Liste des trimestres", description = "Retourne tous les trimestres")
    public ResponseEntity<List<TermResponse>> getAllTerms() {
        return ResponseEntity.ok(termService.getAllTerms());
    }

    @GetMapping("/annee-academique/{anneeAcademiqueId}")
    @Operation(summary = "Trimestres par année", description = "Retourne les trimestres d'une année scolaire")
    public ResponseEntity<List<TermResponse>> getByAcademicYear(@PathVariable Long anneeAcademiqueId) {
        return ResponseEntity.ok(termService.getTermsByAcademicYear(anneeAcademiqueId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un trimestre", description = "Modifie un trimestre")
    public ResponseEntity<TermResponse> updateTerm(@PathVariable Long id, @Valid @RequestBody TermRequest request) {
        return ResponseEntity.ok(termService.updateTerm(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un trimestre", description = "Supprime un trimestre")
    public ResponseEntity<Void> deleteTerm(@PathVariable Long id) {
        termService.deleteTerm(id);
        return ResponseEntity.noContent().build();
    }
}
