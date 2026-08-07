package com.bulletin.service.bulletin;

import com.bulletin.entity.Enrollment;
import com.bulletin.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import com.bulletin.service.ReportCardService; // Importez ReportCardService
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class BulletinService {

    // Note: Ces dépendances sont des suppositions basées sur votre modèle.
    // Adaptez-les à vos repositories existants.
    private final ReportCardService reportCardService; // Injecter ReportCardService
    private final EnrollmentRepository enrollmentRepository;

    /**
     * Lance la génération des bulletins pour une classe et une période en arrière-plan.
     * L'annotation @Async garantit que cette méthode s'exécute dans un thread séparé.
     *
     * @param classroomId L'ID de la classe pour laquelle générer les bulletins.
     * @param periodId L'ID de la période concernée.
     * @param triggeredByUserId L'ID de l'utilisateur qui a initié la génération (pour l'audit).
     * @return un CompletableFuture qui signale la fin de la tâche.
     */
    @Async
    public CompletableFuture<Void> generateBulletinsForClassAsync(Long classroomId, Long periodId, Long triggeredByUserId) {
        log.info("Lancement de la génération asynchrone des bulletins pour la classe {} et la période {}.", classroomId, periodId);

        // 1. Récupérer les inscriptions pour la classe.
        // List<Enrollment> enrollments = enrollmentRepository.findByClassroomId(classroomId);
        // log.info("Trouvé {} élèves à traiter pour la classe {}.", enrollments.size(), classroomId);

        // 2. Boucler sur chaque inscription.
        // for (Enrollment enrollment : enrollments) {
        //   try {
        //     // 3. Appeler votre logique de calcul et de création de bulletin pour un seul élève.
        //     // Cette méthode synchrone contient toute votre logique métier.
        //     generateAndSaveReportCardForStudent(enrollment, periodId, triggeredByUserId);
        //     log.debug("Bulletin généré avec succès pour l'élève {}.", enrollment.getStudent().getId());
        //   } catch (Exception e) {
        //     // On journalise l'erreur pour cet élève sans arrêter le processus global.
        //     log.error("Échec de la génération du bulletin pour l'élève ID {}: {}", enrollment.getStudent().getId(), e.getMessage(), e);
        //   }
        // }

        log.info("Processus de génération asynchrone terminé pour la classe {}.", classroomId);

        // La méthode doit retourner un CompletableFuture.
        // `completedFuture(null)` indique que la tâche est terminée sans valeur de retour.
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Lance la génération des bulletins annuels pour une classe et une année scolaire en arrière-plan.
     */
    @Async
    public CompletableFuture<Void> generateAcademicYearBulletinsForClassAsync(Long academicYearId, Long classroomId, Long triggeredByUserId) {
        log.info("Lancement de la génération asynchrone des bulletins annuels pour l'année scolaire {} et la classe {}.", academicYearId, classroomId);

        // Appeler la logique de génération annuelle du ReportCardService
        reportCardService.generateAcademicYearBulletins(academicYearId, classroomId);

        log.info("Processus de génération asynchrone des bulletins annuels terminé pour l'année scolaire {}.", academicYearId);

        return CompletableFuture.completedFuture(null);
    }

    // ... Vos autres méthodes de service, y compris la logique synchrone pour un seul bulletin.
}