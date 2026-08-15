package com.bulletin.service;

import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class AutoOrdreServiceTest {

    private final AutoOrdreService autoOrdre = new AutoOrdreService();

    @Test
    void nextOrdre_returnsOneWhenMaxIsNull() {
        assertEquals(1, AutoOrdreService.nextOrdre(null));
    }

    @Test
    void nextOrdre_returnsMaxPlusOne() {
        assertEquals(2, AutoOrdreService.nextOrdre(1));
        assertEquals(6, AutoOrdreService.nextOrdre(5));
        assertEquals(100, AutoOrdreService.nextOrdre(99));
    }

    @Test
    void attemptOnce_computesNextOrdreAndRunsAttempt() {
        Integer result = autoOrdre.attemptOnce(() -> 5, ordre -> {
            assertEquals(6, ordre);
            return ordre;
        });
        assertEquals(6, result);
    }

    @Test
    void attemptOnce_returnsOneWhenNoRecordYet() {
        Integer result = autoOrdre.attemptOnce(() -> null, Function.identity());
        assertEquals(1, result);
    }
}
