package com.bulletin.service;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class AutoOrdreRetryTest {

    private final AutoOrdreService autoOrdre = new AutoOrdreService();

    @Test
    void retry_succeedsOnFirstAttempt() {
        Integer result = AutoOrdreRetry.retry(autoOrdre, () -> 3, ordre -> ordre + 1);
        assertEquals(5, result);
    }

    @Test
    void retry_recomputesAndRetriesOnceOnConflict() {
        AtomicInteger maxCalls = new AtomicInteger();
        AtomicInteger attemptCalls = new AtomicInteger();

        Integer result = AutoOrdreRetry.retry(autoOrdre,
                () -> {
                    int n = maxCalls.incrementAndGet();
                    return n == 1 ? 5 : 9; // 5 -> ordre 6 (conflit), 9 -> ordre 10 (succès)
                },
                ordre -> {
                    int n = attemptCalls.incrementAndGet();
                    if (n == 1) {
                        throw new DataIntegrityViolationException("conflit simulé");
                    }
                    assertEquals(10, ordre);
                    return ordre;
                });

        assertEquals(10, result);
        assertEquals(2, maxCalls.get());
        assertEquals(2, attemptCalls.get());
    }

    @Test
    void retry_throwsAfterTwoFailedAttempts() {
        DataIntegrityViolationException ex = assertThrows(DataIntegrityViolationException.class,
                () -> AutoOrdreRetry.retry(autoOrdre, () -> 0,
                        ordre -> {
                            throw new DataIntegrityViolationException("toujours conflit");
                        }));
        assertTrue(ex.getMessage().contains("toujours conflit"));
    }
}
