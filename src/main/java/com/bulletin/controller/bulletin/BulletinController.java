package com.bulletin.controller.bulletin;

import com.bulletin.security.SecurityUtils;
import com.bulletin.service.ReportCardService;
import com.bulletin.service.bulletin.BulletinService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/bulletins")
@RequiredArgsConstructor
@Tag(name = "Bulletins", description = "Gestion des bulletins scolaires")
public class BulletinController {

    private final ReportCardService reportCardService; // Injecter ReportCardService pour la génération annuelle
    private final BulletinService bulletinService;
    private final SecurityUtils securityUtils;

    @PostMapping("/generer-async")
    @Operation(summary = "Lancer la génération asynchrone des bulletins", description = "Lance la génération des bulletins pour une classe et une période en arrière-plan. La réponse est immédiate.")
    public ResponseEntity<?> generateBulletinsAsync(@Valid @RequestBody GenerationRequest request) {
        // Vérification des permissions (ex: BULLETIN_GENERER)
        securityUtils.assertPermission("BULLETIN_GENERER");

        // Récupération de l'ID de l'utilisateur qui a lancé l'action pour l'audit
        Long userId = securityUtils.getCurrentUserId();

        // Appel de la méthode asynchrone. L'appel retourne immédiatement.
        bulletinService.generateBulletinsForClassAsync(request.getClassroomId(), request.getPeriodId(), userId);

        // Retour d'une réponse 202 Accepted pour informer le client que la tâche a été acceptée
        return ResponseEntity.accepted().body(Map.of("message", "La génération des bulletins a été lancée en arrière-plan."));
    }

    @PostMapping("/generer-annuel-async")
    @Operation(summary = "Lancer la génération asynchrone des bulletins annuels", description = "Lance la génération des bulletins annuels pour une classe et une année scolaire en arrière-plan. La réponse est immédiate.")
    public ResponseEntity<?> generateAcademicYearBulletinsAsync(@Valid @RequestBody AcademicYearGenerationRequest request) {
        // Vérification des permissions (ex: BULLETIN_ANNUEL_GENERER)
        securityUtils.assertPermission("BULLETIN_ANNUEL_GENERER");

        Long userId = securityUtils.getCurrentUserId();

        // Appel de la méthode asynchrone pour la génération annuelle
        // Note: La méthode generateAcademicYearBulletins dans ReportCardService n'est pas encore asynchrone.
        // Il faudrait l'envelopper dans une méthode @Async dans BulletinService ou un nouveau service.
        // Pour l'instant, nous appelons directement la méthode synchrone pour l'exemple.
        // reportCardService.generateAcademicYearBulletins(request.getAcademicYearId(), request.getClassroomId());
        // Une implémentation plus robuste utiliserait un service asynchrone dédié.
        bulletinService.generateAcademicYearBulletinsForClassAsync(request.getAcademicYearId(), request.getClassroomId(), userId);

        return ResponseEntity.accepted().body(Map.of("message", "La génération des bulletins annuels a été lancée en arrière-plan."));
    }

    // DTO interne pour la requête de génération.
    // Pour un projet plus grand, ce DTO devrait être dans son propre fichier dans le package dto.
    @Data
    static class GenerationRequest {
        @NotNull(message = "L'ID de la classe est requis.")
        private Long classroomId;
        @NotNull(message = "L'ID de la période est requis.")
        private Long periodId;
    }

    @Data
    static class AcademicYearGenerationRequest {
        @NotNull(message = "L'ID de l'année scolaire est requis.")
        private Long academicYearId;
        @NotNull(message = "L'ID de la classe est requis.")
        private Long classroomId;
    }
}