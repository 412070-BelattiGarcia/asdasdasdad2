package ar.edu.utn.frc.tup.piii.engine.handlers;

import ar.edu.utn.frc.tup.piii.engine.EngineContext;
import ar.edu.utn.frc.tup.piii.engine.MatchStatus;
import ar.edu.utn.frc.tup.piii.engine.action.GameAction;
import ar.edu.utn.frc.tup.piii.engine.event.GameEvent;
import ar.edu.utn.frc.tup.piii.engine.event.GameEventType;
import ar.edu.utn.frc.tup.piii.engine.model.CardInstance;
import ar.edu.utn.frc.tup.piii.engine.model.GameState;
import ar.edu.utn.frc.tup.piii.engine.victory.VictoryConditionChecker;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TakePrizeCardHandler implements GameHandler {
    public void handle(EngineContext ctx, GameAction action) {
        var player = ctx.getPlayer(action.getPlayerId());
        GameState state = ctx.getState();

        UUID pendingOwner = state.getPendingPrizeOwnerPlayerId();
        if (pendingOwner == null || !pendingOwner.equals(action.getPlayerId())) return;

        if (player.getPrizes().isEmpty()) return;

        Integer prizeSlot = action.getPayloadInt("prizeSlot");
        if (prizeSlot == null) prizeSlot = 0;

        if (prizeSlot < 0 || prizeSlot >= player.getPrizes().size()) return;

        CardInstance taken = player.getPrizes().remove(prizeSlot.intValue());
        player.getHand().add(taken);
        state.setPendingPrizeOwnerPlayerId(null);
        VictoryConditionChecker.VictoryCheckResult victoryResult =
                VictoryConditionChecker.check(state, player.getPlayerId());

        if (victoryResult.finished()) {
            state.setWinnerPlayerId(victoryResult.winnerPlayerId());
            state.setFinishReason(victoryResult.reason());
            state.setStatus(MatchStatus.FINISHED);
        }

        Map<String, Object> eventPayload = new HashMap<>();
        eventPayload.put("playerId", player.getPlayerId().toString());
        eventPayload.put("remainingPrizeCount", player.getPrizes().size());
        eventPayload.put("prizeSlot", prizeSlot);
        ctx.addEvent(new GameEvent(
                GameEventType.PRIZE_TAKEN.name(),
                state.getMatchId(),
                state.getTurnNumber(),
                Instant.now(),
                "Prize card taken.",
                eventPayload
        ));
    }
}
