package ar.edu.utn.frc.tup.piii.engine.trainer.resolvers;

import ar.edu.utn.frc.tup.piii.cards.domain.TrainerCardDefinition;
import ar.edu.utn.frc.tup.piii.engine.EngineContext;
import ar.edu.utn.frc.tup.piii.engine.event.GameEvent;
import ar.edu.utn.frc.tup.piii.engine.event.GameEventType;
import ar.edu.utn.frc.tup.piii.engine.model.CardInstance;
import ar.edu.utn.frc.tup.piii.engine.model.PlayerState;
import ar.edu.utn.frc.tup.piii.engine.trainer.EffectType;
import ar.edu.utn.frc.tup.piii.engine.trainer.TrainerEffectResolver;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class StadiumPlayResolver implements TrainerEffectResolver {

    @Override
    public void resolve(EngineContext ctx, PlayerState player, TrainerCardDefinition card, Map<String, Object> payload) {
        var state = ctx.getState();

        if (state.getStadiumCardInstanceId() != null) {
            Map<String, Object> removePayload = new HashMap<>();
            removePayload.put("stadiumCardInstanceId", state.getStadiumCardInstanceId().toString());
            removePayload.put("reason", "replaced");

            ctx.addEvent(new GameEvent(
                    GameEventType.STADIUM_REMOVED.name(),
                    state.getMatchId(),
                    state.getTurnNumber(),
                    Instant.now(),
                    "Stadium replaced.",
                    removePayload
            ));
        }

        Integer handIndex = (Integer) payload.get("handIndex");
        if (handIndex == null || handIndex < 0 || handIndex >= player.getHand().size()) return;

        CardInstance stadiumCard = player.getHand().get(handIndex);
        state.setStadiumCardInstanceId(stadiumCard.getInstanceId());

        player.getHand().remove(handIndex.intValue());

        Map<String, Object> eventPayload = new HashMap<>();
        eventPayload.put("stadiumCardInstanceId", stadiumCard.getInstanceId().toString());
        eventPayload.put("playerId", player.getPlayerId().toString());
        eventPayload.put("cardDefinitionId", stadiumCard.getCardDefinitionId());

        ctx.addEvent(new GameEvent(
                GameEventType.STADIUM_PLAYED.name(),
                state.getMatchId(),
                state.getTurnNumber(),
                Instant.now(),
                "Stadium played.",
                eventPayload
        ));
    }

    @Override
    public EffectType getType() {
        return EffectType.STADIUM_PLAY;
    }
}
