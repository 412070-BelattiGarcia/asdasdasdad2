package ar.edu.utn.frc.tup.piii.engine.action;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameActionTypeTest {

    @Test
    void shouldContainCHOOSE_KO_REPLACEMENT() {
        assertNotNull(GameActionType.valueOf("CHOOSE_KO_REPLACEMENT"));
    }
}
