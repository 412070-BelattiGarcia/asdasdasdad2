package ar.edu.utn.frc.tup.piii.engine.handlers;

import ar.edu.utn.frc.tup.piii.engine.EngineContext;
import ar.edu.utn.frc.tup.piii.engine.action.GameAction;
import ar.edu.utn.frc.tup.piii.engine.action.GameActionType;
import ar.edu.utn.frc.tup.piii.engine.model.GameState;
import ar.edu.utn.frc.tup.piii.engine.model.PlayerState;
import ar.edu.utn.frc.tup.piii.engine.model.PokemonInPlay;
import ar.edu.utn.frc.tup.piii.engine.turn.TurnManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChooseKOReplacementHandlerTest {

    @Mock
    private EngineContext ctx;
    @Mock
    private GameState state;
    @Mock
    private PlayerState player;
    @Mock
    private TurnManager turnManager;

    private ChooseKOReplacementHandler handler;
    private UUID playerId;
    private PokemonInPlay benched;

    @BeforeEach
    void setUp() {
        handler = new ChooseKOReplacementHandler(turnManager);
        playerId = UUID.randomUUID();
        benched = new PokemonInPlay();
        benched.setInstanceId(UUID.randomUUID());
        benched.setCardDefinitionId("pkm-bench");
    }

    @Test
    void shouldMoveSelectedPokemonFromBenchToActive() {
        List<PokemonInPlay> bench = new ArrayList<>(List.of(benched));

        when(ctx.getState()).thenReturn(state);
        when(ctx.getPlayer(playerId)).thenReturn(player);
        when(state.isPendingKOReplacement()).thenReturn(true);
        when(state.getKnockedOutPlayerId()).thenReturn(playerId);
        when(player.getBench()).thenReturn(bench);

        GameAction action = new GameAction();
        action.setType(GameActionType.CHOOSE_KO_REPLACEMENT);
        action.setPlayerId(playerId);
        action.setPayload(Map.of("benchPokemonInstanceId", benched.getInstanceId().toString()));

        handler.handle(ctx, action);

        assertTrue(bench.isEmpty());
        verify(player).setActivePokemon(benched);
        verify(state).setPendingKOReplacement(false);
        verify(state).setKnockedOutPlayerId(null);
        verify(turnManager).advancePhase(state);
    }

    @Test
    void shouldReturnEarlyWhenNoPendingKO() {
        when(ctx.getState()).thenReturn(state);
        when(state.isPendingKOReplacement()).thenReturn(false);

        handler.handle(ctx, new GameAction());

        verifyNoInteractions(player);
        verify(turnManager, never()).advancePhase(any());
    }

    @Test
    void shouldReturnEarlyWhenNotAffectedPlayer() {
        when(ctx.getState()).thenReturn(state);
        when(state.isPendingKOReplacement()).thenReturn(true);
        when(state.getKnockedOutPlayerId()).thenReturn(UUID.randomUUID());

        GameAction action = new GameAction();
        action.setPlayerId(playerId);

        handler.handle(ctx, action);

        verifyNoInteractions(player);
        verify(turnManager, never()).advancePhase(any());
    }

    @Test
    void shouldSetErrorWhenBenchPokemonNotFound() {
        PokemonInPlay other = new PokemonInPlay();
        other.setInstanceId(UUID.randomUUID());
        List<PokemonInPlay> bench = new ArrayList<>(List.of(benched));

        when(ctx.getState()).thenReturn(state);
        when(ctx.getPlayer(playerId)).thenReturn(player);
        when(state.isPendingKOReplacement()).thenReturn(true);
        when(state.getKnockedOutPlayerId()).thenReturn(playerId);
        when(player.getBench()).thenReturn(bench);

        GameAction action = new GameAction();
        action.setType(GameActionType.CHOOSE_KO_REPLACEMENT);
        action.setPlayerId(playerId);
        action.setPayload(Map.of("benchPokemonInstanceId", other.getInstanceId().toString()));

        handler.handle(ctx, action);

        verify(ctx).setError(any());
        verify(state, never()).setPendingKOReplacement(anyBoolean());
        verify(turnManager, never()).advancePhase(any());
    }

    @Test
    void shouldSetErrorWhenBenchPokemonIdMissing() {
        when(ctx.getState()).thenReturn(state);
        when(ctx.getPlayer(playerId)).thenReturn(player);
        when(state.isPendingKOReplacement()).thenReturn(true);
        when(state.getKnockedOutPlayerId()).thenReturn(playerId);

        GameAction action = new GameAction();
        action.setType(GameActionType.CHOOSE_KO_REPLACEMENT);
        action.setPlayerId(playerId);
        action.setPayload(new HashMap<>());

        handler.handle(ctx, action);

        verify(ctx).setError(any());
        verify(state, never()).setPendingKOReplacement(anyBoolean());
    }
}
