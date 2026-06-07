package ar.edu.utn.frc.tup.piii.engine.handlers;

import ar.edu.utn.frc.tup.piii.engine.EngineContext;
import ar.edu.utn.frc.tup.piii.engine.action.GameAction;
import ar.edu.utn.frc.tup.piii.engine.action.GameActionType;
import ar.edu.utn.frc.tup.piii.engine.model.CardInstance;
import ar.edu.utn.frc.tup.piii.engine.model.GameState;
import ar.edu.utn.frc.tup.piii.engine.model.PlayerState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TakePrizeCardHandlerTest {

    @Mock
    private EngineContext ctx;
    @Mock
    private GameState state;
    @Mock
    private PlayerState player;

    private TakePrizeCardHandler handler;
    private UUID playerId;
    private UUID otherPlayerId;
    private List<CardInstance> prizes;
    private List<CardInstance> hand;

    @BeforeEach
    void setUp() {
        handler = new TakePrizeCardHandler();
        playerId = UUID.randomUUID();
        otherPlayerId = UUID.randomUUID();
        prizes = new ArrayList<>(List.of(new CardInstance(UUID.randomUUID(), "card-1")));
        hand = new ArrayList<>();
    }

    private GameAction createAction(UUID pid, int prizeSlot) {
        GameAction action = new GameAction();
        action.setType(GameActionType.TAKE_PRIZE_CARD);
        action.setPlayerId(pid);
        action.setPayload(Map.of("prizeSlot", prizeSlot));
        action.setClientRequestId("req-1");
        return action;
    }

    @Test
    void shouldTakePrizeWhenPlayerOwnsKO() {
        PlayerState p1 = new PlayerState();
        p1.setPlayerId(playerId);
        p1.setPrizes(new ArrayList<>());
        p1.setHand(new ArrayList<>());
        p1.setBench(new ArrayList<>());
        PlayerState p2 = new PlayerState();
        p2.setPlayerId(otherPlayerId);
        p2.setPrizes(new ArrayList<>());
        p2.setHand(new ArrayList<>());
        p2.setBench(new ArrayList<>());

        when(ctx.getState()).thenReturn(state);
        when(ctx.getPlayer(playerId)).thenReturn(player);
        when(state.getPendingPrizeOwnerPlayerId()).thenReturn(playerId);
        when(state.getPlayers()).thenReturn(new PlayerState[]{p1, p2});
        when(player.getPrizes()).thenReturn(prizes);
        when(player.getHand()).thenReturn(hand);
        when(player.getPlayerId()).thenReturn(playerId);

        handler.handle(ctx, createAction(playerId, 0));

        assertTrue(prizes.isEmpty());
        assertEquals(1, hand.size());
        assertEquals("card-1", hand.get(0).getCardDefinitionId());
        verify(state).setPendingPrizeOwnerPlayerId(null);
    }

    @Test
    void shouldRejectWhenPlayerDoesNotOwnKO() {
        when(ctx.getState()).thenReturn(state);
        when(ctx.getPlayer(otherPlayerId)).thenReturn(player);
        when(state.getPendingPrizeOwnerPlayerId()).thenReturn(playerId);

        handler.handle(ctx, createAction(otherPlayerId, 0));

        assertEquals(1, prizes.size());
    }

    @Test
    void shouldRejectWhenNoPendingKO() {
        when(ctx.getState()).thenReturn(state);
        when(ctx.getPlayer(playerId)).thenReturn(player);
        when(state.getPendingPrizeOwnerPlayerId()).thenReturn(null);

        handler.handle(ctx, createAction(playerId, 0));

        assertEquals(1, prizes.size());
    }
}
