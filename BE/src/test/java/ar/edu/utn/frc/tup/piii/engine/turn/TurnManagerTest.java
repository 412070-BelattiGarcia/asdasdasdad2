package ar.edu.utn.frc.tup.piii.engine.turn;

import ar.edu.utn.frc.tup.piii.engine.EngineContext;
import ar.edu.utn.frc.tup.piii.engine.model.GameState;
import ar.edu.utn.frc.tup.piii.engine.model.PlayerState;
import ar.edu.utn.frc.tup.piii.engine.model.TurnFlags;
import ar.edu.utn.frc.tup.piii.engine.ports.RandomizerPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TurnManagerTest {

    @Mock
    private RandomizerPort randomizer;
    @Mock
    private EngineContext ctx;
    @Mock
    private GameState state;
    @Mock
    private PlayerState currentPlayer;

    private TurnManager turnManager;
    private UUID playerId;
    private UUID anotherPlayerId;

    @BeforeEach
    void setUp() {
        turnManager = new TurnManager(randomizer);
        playerId = UUID.randomUUID();
        anotherPlayerId = UUID.randomUUID();
    }

    @Test
    void shouldResetPendingPrizeOwnerPlayerIdOnStartTurn() {
        TurnFlags flags = new TurnFlags();

        when(ctx.getState()).thenReturn(state);
        when(state.getCurrentPlayerId()).thenReturn(playerId);
        when(state.getPlayers()).thenReturn(new PlayerState[]{
                createPlayer(playerId),
                createPlayer(anotherPlayerId)
        });
        when(state.getTurnFlags()).thenReturn(flags);
        state.setPendingPrizeOwnerPlayerId(playerId);

        turnManager.startTurn(ctx);

        assertNull(state.getPendingPrizeOwnerPlayerId());
    }

    private PlayerState createPlayer(UUID id) {
        PlayerState p = new PlayerState();
        p.setPlayerId(id);
        return p;
    }
}
