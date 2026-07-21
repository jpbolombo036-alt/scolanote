package com.bulletin.controller.school;

import com.bulletin.dto.school.TrimesterRequest;
import com.bulletin.dto.school.TrimesterResponse;
import com.bulletin.security.SecurityUtils;
import com.bulletin.service.TrimesterService;
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
@RequestMapping("/api/trimestres")
@RequiredArgsConstructor
@Tag(name = "Trimestres", description = "Gestion des trimestres")
public class TrimesterController {
    private final TrimesterService trimesterService;
    private final SecurityUtils securityUtils;

    @PostMapping
    @Operation(summary = "Créer un trimestre", description = "Crée un trimestre (direction uniquement)")
    public ResponseEntity<TrimesterResponse> createTrimester(@Valid @RequestBody TrimesterRequest request) {
        if (!securityUtils.isDirection()) {
            throw new SecurityException("Accès refusé : seul la direction peut créer un trimestre");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(trimesterService.createTrimester(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Trimestre par ID")
    public ResponseEntity<TrimesterResponse> getTrimester(@PathVariable Long id) {
        return ResponseEntity.ok(trimesterService.getTrimester(id));
    }

    @GetMapping
    @Operation(summary = "Liste des trimestres", description = "Retourne tous les trimestres (avec pagination optionnelle)")
    public ResponseEntity<Page<TrimesterResponse>> getAllTrimesters(Pageable pageable) {
        return ResponseEntity.ok(trimesterService.getAllTrimesters(pageable));
    }

    @GetMapping("/all")
    @Operation(summary = "Liste complète des trimestres")
    public ResponseEntity<List<TrimesterResponse>> getAllTrimestersUnpaginated() {
        return ResponseEntity.ok(trimesterService.getAllTrimesters());
    }

    @GetMapping("/annee-academique/{anneeAcademiqueId}")
    @Operation(summary = "Trimestres par année scolaire")
    public ResponseEntity<List<TrimesterResponse>> getByAcademicYear(@PathVariable Long anneeAcademiqueId) {
        return ResponseEntity.ok(trimesterService.getTrimestersByAcademicYear(anneeAcademiqueId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un trimestre", description = "Modifie un trimestre (direction uniquement)")
    public ResponseEntity<TrimesterResponse> updateTrimester(@PathVariable Long id, @Valid @RequestBody TrimesterRequest request) {
        if (!securityUtils.isDirection()) {
            throw new SecurityException("Accès refusé : seul la direction peut modifier un trimestre");
        }
        return ResponseEntity.ok(trimesterService.updateTrimester(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un trimestre", description = "Supprime un trimestre (direction uniquement)")
    public ResponseEntity<Void> deleteTrimester(@PathVariable Long id) {
        if (!securityUtils.isDirection()) {
            throw new SecurityException("Accès refusé : seul la direction peut supprimer un trimestre");
        }
        trimesterService.deleteTrimester(id);
        return ResponseEntity.noContent().build();
    }
}
