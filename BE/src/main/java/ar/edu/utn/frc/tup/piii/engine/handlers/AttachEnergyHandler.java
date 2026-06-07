package ar.edu.utn.frc.tup.piii.engine.handlers;

import ar.edu.utn.frc.tup.piii.cards.domain.CardDefinition;
import ar.edu.utn.frc.tup.piii.cards.domain.EnergyCardDefinition;
import ar.edu.utn.frc.tup.piii.engine.EngineContext;
import ar.edu.utn.frc.tup.piii.engine.action.GameAction;
import ar.edu.utn.frc.tup.piii.engine.event.GameEvent;
import ar.edu.utn.frc.tup.piii.engine.event.GameEventType;
import ar.edu.utn.frc.tup.piii.engine.model.CardInstance;
import ar.edu.utn.frc.tup.piii.engine.model.PokemonInPlay;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class AttachEnergyHandler implements GameHandler {
    public void handle(EngineContext ctx, GameAction action) {
        Map<String, Object> payload = action.getPayload();
        Integer handIndex = action.getPayloadInt("handIndex");
        if (handIndex == null) return;
        String targetIdStr = action.getPayloadString("targetPokemonInstanceId");
        if (targetIdStr == null) return;
        UUID targetPokemonInstanceId = UUID.fromString(targetIdStr);
        var player = ctx.getPlayer(action.getPlayerId());

        if (ctx.getState().getTurnFlags().hasAttachedEnergy()) return;

        if (handIndex < 0 || handIndex >= player.getHand().size()) return;
        CardInstance card = player.getHand().get(handIndex);

        CardDefinition def = ctx.getCardLookup().getCardById(card.getCardDefinitionId());
        if (!(def instanceof EnergyCardDefinition)) return;

        PokemonInPlay target = HandlerHelper.findPokemon(player, targetPokemonInstanceId);
        if (target == null) return;

        player.getHand().remove(handIndex);
        target.getAttachedEnergies().add(card);

        ctx.getState().getTurnFlags().setHasAttachedEnergy(true);

        ctx.addEvent(new GameEvent(
                GameEventType.ENERGY_ATTACHED.name(),
                ctx.getState().getMatchId(),
                ctx.getState().getTurnNumber(),
                Instant.now(),
                "Energy attached",
                payload
        ));
    }
}
