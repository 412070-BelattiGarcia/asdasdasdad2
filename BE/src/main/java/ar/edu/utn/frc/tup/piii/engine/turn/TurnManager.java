package ar.edu.utn.frc.tup.piii.engine.turn;

import ar.edu.utn.frc.tup.piii.engine.EngineContext;
import ar.edu.utn.frc.tup.piii.engine.event.GameEvent;
import ar.edu.utn.frc.tup.piii.engine.event.GameEventType;
import ar.edu.utn.frc.tup.piii.engine.model.GameState;
import ar.edu.utn.frc.tup.piii.engine.model.PlayerState;
import ar.edu.utn.frc.tup.piii.engine.model.TurnFlags;
import ar.edu.utn.frc.tup.piii.engine.ports.RandomizerPort;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TurnManager {

    private final RandomizerPort randomizerPort;

    public TurnManager(RandomizerPort randomizerPort) {
        this.randomizerPort = randomizerPort;
    }

    public void startTurn(EngineContext ctx) {
        GameState state = ctx.getState();
        PlayerState currentPlayer = findPlayerState(state, state.getCurrentPlayerId());

        resetTurnFlags(state.getTurnFlags());
        state.setPendingPrizeOwnerPlayerId(null);

        if (currentPlayer.getDeck() == null || currentPlayer.getDeck().isEmpty()) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("playerId", currentPlayer.getPlayerId().toString());
            ctx.addEvent(new GameEvent(
                    GameEventType.STATE_UPDATED.name(),
                    state.getMatchId(),
                    state.getTurnNumber(),
                    Instant.now(),
                    "El jugador " + currentPlayer.getPlayerId() + " no puede robar: el deck está vacío.",
                    payload
            ));
        }
    }

    public void endTurn(EngineContext ctx) {
        GameState state = ctx.getState();

        state.setPhase(TurnPhase.BETWEEN_TURNS);

        UUID nextPlayerId = findOtherPlayerId(state, state.getCurrentPlayerId());

        Map<String, Object> payload = new HashMap<>();
        payload.put("phase", "DRAW");
        payload.put("turnNumber", state.getTurnNumber() + 1);
        payload.put("nextPlayerId", nextPlayerId.toString());

        ctx.addEvent(new GameEvent(
                GameEventType.PHASE_CHANGED.name(),
                state.getMatchId(),
                state.getTurnNumber(),
                Instant.now(),
                "El turno " + state.getTurnNumber() + " del jugador " + state.getCurrentPlayerId() + " ha terminado.",
                payload
        ));

        state.setCurrentPlayerId(nextPlayerId);
        state.setTurnNumber(state.getTurnNumber() + 1);
        state.setPhase(TurnPhase.DRAW);
    }

    private PlayerState findPlayerState(GameState state, UUID playerId) {
        for (PlayerState player : state.getPlayers()) {
            if (player.getPlayerId().equals(playerId)) {
                return player;
            }
        }
        throw new IllegalStateException("Player not found: " + playerId);
    }

    private UUID findOtherPlayerId(GameState state, UUID currentPlayerId) {
        for (PlayerState player : state.getPlayers()) {
            if (!player.getPlayerId().equals(currentPlayerId)) {
                return player.getPlayerId();
            }
        }
        throw new IllegalStateException("No other player found for currentPlayerId: " + currentPlayerId);
    }

    private void resetTurnFlags(TurnFlags flags) {
        flags.setHasDrawnForTurn(false);
        flags.setHasAttachedEnergy(false);
        flags.setHasRetreated(false);
        flags.setHasPlayedSupporter(false);
        flags.setHasPlayedStadium(false);
        flags.setHasAttacked(false);
    }
}
