package ar.edu.utn.frc.tup.piii.engine.event;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameEventTypeTest {

    @Test
    void shouldContainKO_REPLACEMENT_REQUIRED() {
        assertNotNull(GameEventType.valueOf("KO_REPLACEMENT_REQUIRED"));
    }

    @Test
    void shouldContainKO_REPLACEMENT_DONE() {
        assertNotNull(GameEventType.valueOf("KO_REPLACEMENT_DONE"));
    }

    @Test
    void shouldContainSUDDEN_DEATH_STARTED() {
        assertNotNull(GameEventType.valueOf("SUDDEN_DEATH_STARTED"));
    }

    @Test
    void shouldContainCONFUSION_SELF_HIT() {
        assertNotNull(GameEventType.valueOf("CONFUSION_SELF_HIT"));
    }
}
