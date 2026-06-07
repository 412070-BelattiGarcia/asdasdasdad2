package ar.edu.utn.frc.tup.piii.engine.handlers;

import ar.edu.utn.frc.tup.piii.cards.domain.CardDefinition;
import ar.edu.utn.frc.tup.piii.cards.domain.PokemonCardDefinition;
import ar.edu.utn.frc.tup.piii.engine.EngineContext;
import ar.edu.utn.frc.tup.piii.engine.action.GameAction;
import ar.edu.utn.frc.tup.piii.engine.event.GameEvent;
import ar.edu.utn.frc.tup.piii.engine.event.GameEventType;
import ar.edu.utn.frc.tup.piii.engine.model.CardInstance;
import ar.edu.utn.frc.tup.piii.engine.model.PokemonInPlay;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PutBasicOnBenchHandler implements GameHandler {
    public void handle(EngineContext ctx, GameAction action) {
        Integer handIndex = action.getPayloadInt("handIndex");
        if (handIndex == null) return;
        var player = ctx.getPlayer(action.getPlayerId());

        if (handIndex < 0 || handIndex >= player.getHand().size()) return;
        CardInstance card = player.getHand().get(handIndex);

        CardDefinition def = ctx.getCardLookup().getCardById(card.getCardDefinitionId());
        if (!(def instanceof PokemonCardDefinition pokemonCardDef)) return;

        if (!"BASIC".equals(pokemonCardDef.getStage())) return;

        if (player.getBench().size() >= 5) return;

        player.getHand().remove(handIndex);

        PokemonInPlay pkm = new PokemonInPlay();
        pkm.setInstanceId(card.getInstanceId());
        pkm.setCardDefinitionId(card.getCardDefinitionId());
        pkm.setOwnerPlayerId(player.getPlayerId());
        pkm.setDamageCounters(0);
        pkm.setSpecialConditions(new ArrayList<>());
        pkm.setAttachedEnergies(new ArrayList<>());
        pkm.setEvolvedThisTurn(false);
        pkm.setEnteredTurnNumber(ctx.getState().getTurnNumber());
        player.getBench().add(pkm);

        Map<String, Object> eventPayload = new HashMap<>();
        eventPayload.put("cardDefinitionId", card.getCardDefinitionId());
        eventPayload.put("handIndex", handIndex);
        eventPayload.put("instanceId", card.getInstanceId().toString());
        ctx.addEvent(new GameEvent(GameEventType.POKEMON_PLACED_ON_BENCH.name(),
                ctx.getState().getMatchId(),
                ctx.getState().getTurnNumber(),
                Instant.now(),
                "Placed on bench",
                eventPayload
        ));
    }
}
