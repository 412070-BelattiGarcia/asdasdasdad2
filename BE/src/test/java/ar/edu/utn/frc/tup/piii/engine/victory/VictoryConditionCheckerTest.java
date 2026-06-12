package ar.edu.utn.frc.tup.piii.engine.victory;

import ar.edu.utn.frc.tup.piii.engine.model.CardInstance;
import ar.edu.utn.frc.tup.piii.engine.model.GameState;
import ar.edu.utn.frc.tup.piii.engine.model.PlayerState;
import ar.edu.utn.frc.tup.piii.engine.model.PokemonInPlay;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class VictoryConditionCheckerTest {

    private GameState state;
    private PlayerState p1;
    private PlayerState p2;
    private UUID p1Id;
    private UUID p2Id;

    @BeforeEach
    void setUp() {
        state = new GameState();
        p1 = new PlayerState();
        p2 = new PlayerState();
        p1Id = UUID.randomUUID();
        p2Id = UUID.randomUUID();
        p1.setPlayerId(p1Id);
        p2.setPlayerId(p2Id);
        p1.setActivePokemon(new PokemonInPlay());
        p2.setActivePokemon(new PokemonInPlay());
        p1.setDeck(new ArrayList<>(List.of(
                new CardInstance(UUID.randomUUID(), "deck-1"),
                new CardInstance(UUID.randomUUID(), "deck-2")
        )));
        p2.setDeck(new ArrayList<>(List.of(
                new CardInstance(UUID.randomUUID(), "deck-1"),
                new CardInstance(UUID.randomUUID(), "deck-2")
        )));
        // Start with both having 6 prizes so the game is active
        p1.setPrizes(new ArrayList<>(List.of(
                new CardInstance(UUID.randomUUID(), "prize-1"),
                new CardInstance(UUID.randomUUID(), "prize-2"),
                new CardInstance(UUID.randomUUID(), "prize-3"),
                new CardInstance(UUID.randomUUID(), "prize-4"),
                new CardInstance(UUID.randomUUID(), "prize-5"),
                new CardInstance(UUID.randomUUID(), "prize-6")
        )));
        p2.setPrizes(new ArrayList<>(List.of(
                new CardInstance(UUID.randomUUID(), "prize-1"),
                new CardInstance(UUID.randomUUID(), "prize-2"),
                new CardInstance(UUID.randomUUID(), "prize-3"),
                new CardInstance(UUID.randomUUID(), "prize-4"),
                new CardInstance(UUID.randomUUID(), "prize-5"),
                new CardInstance(UUID.randomUUID(), "prize-6")
        )));
        state.setPlayers(new PlayerState[]{p1, p2});
    }

    @Test
    void shouldDetectSuddenDeathWhenBothTakeLastPrize() {
        p1.setPrizes(new ArrayList<>());
        p2.setPrizes(new ArrayList<>());

        VictoryConditionChecker.VictoryCheckResult result =
                VictoryConditionChecker.check(state, p1Id);

        assertTrue(result.finished());
        assertTrue(result.suddenDeath());
        assertNull(result.winnerPlayerId());
    }

    @Test
    void shouldDeclareWinnerWhenOnlyOnePlayerTakesLastPrize() {
        p1.setPrizes(new ArrayList<>());
        // p2 keeps 6 prizes

        VictoryConditionChecker.VictoryCheckResult result =
                VictoryConditionChecker.check(state, p1Id);

        assertTrue(result.finished());
        assertFalse(result.suddenDeath());
        assertEquals(p1Id, result.winnerPlayerId());
        assertEquals(FinishReason.PRIZES, result.reason());
    }

    @Test
    void shouldDetectSuddenDeathWhenBothNoPokemon() {
        p1.setActivePokemon(null);
        p1.setBench(new ArrayList<>());
        p2.setActivePokemon(null);
        p2.setBench(new ArrayList<>());

        VictoryConditionChecker.VictoryCheckResult result =
                VictoryConditionChecker.check(state, p1Id);

        assertTrue(result.finished());
        assertTrue(result.suddenDeath());
        assertNull(result.winnerPlayerId());
    }

    @Test
    void shouldDetectSuddenDeathWhenBothDeckOut() {
        p1.setDeck(new ArrayList<>());
        p2.setDeck(new ArrayList<>());

        VictoryConditionChecker.VictoryCheckResult result =
                VictoryConditionChecker.check(state, p1Id);

        assertTrue(result.finished());
        assertTrue(result.suddenDeath());
        assertNull(result.winnerPlayerId());
    }

    @Test
    void shouldNotTriggerWhenGameIsActive() {
        // Both have 6 prizes, active Pokémon, non-empty deck (from setUp) → game is active
        VictoryConditionChecker.VictoryCheckResult result =
                VictoryConditionChecker.check(state, p1Id);

        assertFalse(result.finished());
        assertFalse(result.suddenDeath());
    }
}
