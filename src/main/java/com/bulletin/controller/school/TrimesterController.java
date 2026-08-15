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
    @Operation(summary = "Créer un trimestre", description = "Crée un trimestre (direction uniquement). L'ordre est généré automatiquement serveur (max+1, scope année+école) ; la valeur fournie est ignorée.")
    public ResponseEntity<TrimesterResponse> createTrimester(@Valid @RequestBody TrimesterRequest request) {
        securityUtils.assertPermission("TRIMESTRE_GERER");
        return ResponseEntity.status(HttpStatus.CREATED).body(trimesterService.createTrimester(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Trimestre par ID")
    public ResponseEntity<TrimesterResponse> getTrimester(@PathVariable Long id) {
        return ResponseEntity.ok(trimesterService.getTrimester(id));
    }

    @GetMapping
    @Operation(summary = "Liste des trimestres", description = "Retourne les trimestres accessibles à l'utilisateur connecté")
    public ResponseEntity<Page<TrimesterResponse>> getAccessibleTrimesters(Pageable pageable) {
        return ResponseEntity.ok(trimesterService.getAccessibleTrimesters(pageable));
    }

    @GetMapping("/all")
    @Operation(summary = "Liste complète des trimestres", description = "Retourne tous les trimestres (SUPER_ADMIN uniquement)")
    public ResponseEntity<List<TrimesterResponse>> getAllTrimestersUnpaginated() {
        return ResponseEntity.ok(trimesterService.getAccessibleTrimesters());
    }

    @GetMapping("/annee-academique/{anneeAcademiqueId}")
    @Operation(summary = "Trimestres par année scolaire")
    public ResponseEntity<List<TrimesterResponse>> getByAcademicYear(@PathVariable Long anneeAcademiqueId) {
        return ResponseEntity.ok(trimesterService.getTrimestersByAcademicYear(anneeAcademiqueId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un trimestre", description = "Modifie un trimestre (direction uniquement). L'ordre est immuable (ignoré du request).")
    public ResponseEntity<TrimesterResponse> updateTrimester(@PathVariable Long id, @Valid @RequestBody TrimesterRequest request) {
        securityUtils.assertPermission("TRIMESTRE_GERER");
        return ResponseEntity.ok(trimesterService.updateTrimester(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un trimestre", description = "Supprime un trimestre (direction uniquement)")
    public ResponseEntity<Void> deleteTrimester(@PathVariable Long id) {
        securityUtils.assertPermission("TRIMESTRE_GERER");
        trimesterService.deleteTrimester(id);
        return ResponseEntity.noContent().build();
    }
}
