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
import ar.edu.utn.frc.tup.piii.engine.model.PlayerState;
import ar.edu.utn.frc.tup.piii.engine.model.PokemonInPlay;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AttachToolHandler implements GameHandler {

    public void handle(EngineContext ctx, GameAction action) {
        GameState state = ctx.getState();
        PlayerState player = ctx.getPlayer(action.getPlayerId());

        Integer handIndex = action.getPayloadInt("handIndex");
        if (handIndex == null) return;
        if (handIndex < 0 || handIndex >= player.getHand().size()) return;

        CardInstance card = player.getHand().get(handIndex);
        CardDefinition def = ctx.getCardLookup().getCardById(card.getCardDefinitionId());
        if (!(def instanceof TrainerCardDefinition trainerDef)) return;
        if (trainerDef.getTrainerSubtype() != TrainerSubtype.ITEM) return;

        String targetIdStr = action.getPayloadString("targetPokemonInstanceId");
        if (targetIdStr == null) return;

        PokemonInPlay target = HandlerHelper.findPokemon(player, UUID.fromString(targetIdStr));
        if (target == null) return;

        if (target.getToolCardInstanceId() != null) return;

        player.getHand().remove(handIndex.intValue());
        target.setToolCardInstanceId(card.getInstanceId());

        Map<String, Object> eventPayload = new HashMap<>();
        eventPayload.put("targetPokemonInstanceId", targetIdStr);
        eventPayload.put("toolCardInstanceId", card.getInstanceId().toString());
        eventPayload.put("cardDefinitionId", card.getCardDefinitionId());

        ctx.addEvent(new GameEvent(
                GameEventType.TOOL_ATTACHED.name(),
                state.getMatchId(),
                state.getTurnNumber(),
                Instant.now(),
                "Pokemon tool attached.",
                eventPayload
        ));
    }
}
