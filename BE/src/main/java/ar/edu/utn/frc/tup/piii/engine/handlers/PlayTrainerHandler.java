package ar.edu.utn.frc.tup.piii.engine.handlers;

import ar.edu.utn.frc.tup.piii.cards.domain.CardDefinition;
import ar.edu.utn.frc.tup.piii.cards.domain.TrainerCardDefinition;
import ar.edu.utn.frc.tup.piii.cards.domain.TrainerSubtype;
import ar.edu.utn.frc.tup.piii.engine.EngineContext;
import ar.edu.utn.frc.tup.piii.engine.action.GameAction;
import ar.edu.utn.frc.tup.piii.engine.event.GameEvent;
import ar.edu.utn.frc.tup.piii.engine.event.GameEventType;
import ar.edu.utn.frc.tup.piii.engine.model.CardInstance;
import ar.edu.utn.frc.tup.piii.engine.model.GameState;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/*
 * MVP: Trainer effects are not implemented.
 * Only validates Supporter limit (1 per turn) and
 * moves the card from hand to discard pile.
 * Complex trainer logic (draw, heal, search) is future scope.
 */

public class PlayTrainerHandler implements GameHandler {
    public void handle(EngineContext ctx, GameAction action) {
        GameState state = ctx.getState();
        var player = ctx.getPlayer(action.getPlayerId());
        Integer handIndex = action.getPayloadInt("handIndex");
        if (handIndex == null) return;

        if (handIndex < 0 || handIndex >= player.getHand().size()) return;
        CardInstance card = player.getHand().get(handIndex);

        CardDefinition def = ctx.getCardLookup().getCardById(card.getCardDefinitionId());
        if (!(def instanceof TrainerCardDefinition trainerDef)) return;

            if (trainerDef.getTrainerSubtype() == TrainerSubtype.SUPPORTER) {
            if (state.getTurnFlags().hasPlayedSupporter()) return;
            state.getTurnFlags().setHasPlayedSupporter(true);
        }

        if (trainerDef.getTrainerSubtype() == TrainerSubtype.STADIUM) {
            if (state.getTurnFlags().hasPlayedStadium()) return;
            state.getTurnFlags().setHasPlayedStadium(true);
        }

        player.getHand().remove(handIndex);
        player.getDiscard().add(card);

        Map<String, Object> eventPayload = new HashMap<>();
        eventPayload.put("cardInstanceId", card.getInstanceId().toString());
        eventPayload.put("trainerSubtype", trainerDef.getTrainerSubtype());

        ctx.addEvent(new GameEvent(
                GameEventType.TRAINER_PLAYED.name(),
                state.getMatchId(),
                state.getTurnNumber(),
                Instant.now(),
                "Trainer card played.",
                eventPayload
        ));
    }
}
