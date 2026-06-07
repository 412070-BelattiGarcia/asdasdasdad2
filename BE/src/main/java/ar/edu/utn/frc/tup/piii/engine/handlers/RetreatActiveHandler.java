package ar.edu.utn.frc.tup.piii.engine.handlers;

import ar.edu.utn.frc.tup.piii.cards.domain.EnergyType;
import ar.edu.utn.frc.tup.piii.cards.domain.PokemonCardDefinition;
import ar.edu.utn.frc.tup.piii.engine.EngineContext;
import ar.edu.utn.frc.tup.piii.engine.action.GameAction;
import ar.edu.utn.frc.tup.piii.engine.event.GameEvent;
import ar.edu.utn.frc.tup.piii.engine.event.GameEventType;
import ar.edu.utn.frc.tup.piii.engine.model.CardInstance;
import ar.edu.utn.frc.tup.piii.engine.model.GameState;
import ar.edu.utn.frc.tup.piii.engine.model.PokemonInPlay;
import java.time.Instant;
import java.util.*;

public class RetreatActiveHandler implements GameHandler {
    public void handle(EngineContext ctx, GameAction action) {
        Map<String, Object> payload = action.getPayload();
        GameState state = ctx.getState();
        var player = ctx.getPlayer(action.getPlayerId());

        if (state.getTurnFlags().hasRetreated()) return;

        PokemonInPlay active = player.getActivePokemon();

        if (active == null) return;

        Integer benchIndex = action.getPayloadInt("benchIndex");
        if (benchIndex == null) return;
        if (benchIndex < 0 || benchIndex >= player.getBench().size()) return;
        PokemonInPlay selected = player.getBench().get(benchIndex);

        PokemonCardDefinition activeDef = (PokemonCardDefinition) ctx.getCardLookup().getCardById(active.getCardDefinitionId());
        List<EnergyType> retreatCost = activeDef.getRetreatCost();

        int requiredEnergies = retreatCost != null ? retreatCost.size() : 0;
        @SuppressWarnings("unchecked")
        List<String> rawDiscard = (List<String>) payload.get("energyCardInstanceIdsToDiscard");
        List<UUID> toDiscard = rawDiscard.stream().map(UUID::fromString).toList();

        if (toDiscard.size() < requiredEnergies) return;

        var attached = active.getAttachedEnergies();
        if (attached != null) {
            List<CardInstance> toDiscardInstances = attached.stream()
                    .filter(ci -> toDiscard.contains(ci.getInstanceId()))
                    .toList();
            player.getDiscard().addAll(toDiscardInstances);
            attached.removeAll(toDiscardInstances);
        }

        player.getBench().remove(benchIndex.intValue());
        active.setEnteredTurnNumber(state.getTurnNumber());
        player.getBench().add(active);
        player.setActivePokemon(selected);
        state.getTurnFlags().setHasRetreated(true);

        Map<String, Object> eventPayload = new HashMap<>();
        eventPayload.put("newActivePokemonInstanceId", selected.getInstanceId().toString());
        eventPayload.put("oldActivePokemonInstanceId", active.getInstanceId().toString());

        ctx.addEvent(new GameEvent(
                GameEventType.RETREAT_EXECUTED.name(),
                state.getMatchId(),
                state.getTurnNumber(),
                Instant.now(),
                "Retreat executed.",
                eventPayload
        ));
    }
}
