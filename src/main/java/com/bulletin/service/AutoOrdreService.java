package com.bulletin.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Service dédié au calcul serveur de l'ordre (max + 1) et à la persistance atomique
 * d'une entité ordonnée.
 *
 * <p>Le prochain ordre vaut {@code MAX(ordre) + 1} (ou {@code 1} s'il n'y en a pas).
 * La persistance et le mapping de réponse s'effectuent dans une transaction isolée
 * ({@code REQUIRES_NEW}) afin que, en cas de conflit d'unicité (concurrence), le
 * rollback de la tentative échouée nettoie le contexte de persistance et que le
 * {@code max} soit re-interrogé dans une nouvelle transaction.</p>
 */
@Service
public class AutoOrdreService {

    /**
     * @return le prochain ordre disponible : {@code max + 1}, ou {@code 1} si {@code max} est null.
     *         MAX() ignore les valeurs NULL, donc un scope sans enregistrement renvoie 1.
     */
    public static Integer nextOrdre(Integer max) {
        return (max == null) ? 1 : max + 1;
    }

    /**
     * Calcule le prochain ordre dans le scope (via {@code maxOrdreSupplier}) puis exécute
     * une tentative de persistance ({@code attempt}).
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public <T> T attemptOnce(Supplier<Integer> maxOrdreSupplier, Function<Integer, T> attempt) {
        Integer max = maxOrdreSupplier.get();
        Integer ordre = nextOrdre(max);
        return attempt.apply(ordre);
    }
}
