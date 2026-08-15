package com.bulletin.service;

import org.springframework.dao.DataIntegrityViolationException;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Ordonnance le calcul/persistance serveur d'un ordre avec retry sur conflit d'unicité.
 *
 * <p>Chaque tentative passe par {@link AutoOrdreService#attemptOnce} invoquée sur le bean
 * proxy Spring — ce qui garantit une transaction isolée ({@code REQUIRES_NEW}) par
 * tentative : le rollback d'une tentative échouée nettoie le contexte de persistance de
 * l'entité ayant échoué, et le {@code max} est recalculé (nouvelle transaction) afin de
 * tenir compte d'une éventuelle ligne concurremment insérée.</p>
 *
 * <p>Retry maximal : 2 tentatives. Au-delà, la contrainte d'unicité de la base fait
 * échouer la requête (le {@code DataIntegrityViolationException} est relégué).</p>
 */
public final class AutoOrdreRetry {

    private AutoOrdreRetry() {
    }

    /**
     * @param maxOrdreSupplier  recompute du max du scope (nouvelle transaction par tentative)
     * @param attempt           construit l'entité, la persiste et renvoie la réponse DTO —
     *                          ré-exécutée à chaque tentative (entité fraîche chaque fois)
     */
    public static <T> T retry(AutoOrdreService autoOrdre,
                              Supplier<Integer> maxOrdreSupplier,
                              Function<Integer, T> attempt) {
        int attempts = 0;
        while (true) {
            try {
                return autoOrdre.attemptOnce(maxOrdreSupplier, attempt);
            } catch (DataIntegrityViolationException ex) {
                if (++attempts >= 2) {
                    throw ex;
                }
            }
        }
    }
}
