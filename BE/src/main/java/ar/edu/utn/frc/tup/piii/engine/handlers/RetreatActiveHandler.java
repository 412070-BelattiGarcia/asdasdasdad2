package ar.edu.utn.frc.tup.piii.engine.handlers;

import ar.edu.utn.frc.tup.piii.cards.domain.EnergyType;
import ar.edu.utn.frc.tup.piii.cards.domain.PokemonCardDefinition;
import ar.edu.utn.frc.tup.piii.engine.EngineContext;
import ar.edu.utn.frc.tup.piii.engine.action.GameAction;
import ar.edu.utn.frc.tup.piii.engine.attack.StatusEffectManager;
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
        var attached = active.getAttachedEnergies();

        // Determine which energy instance IDs to discard
        final List<UUID> toDiscard;
        if (requiredEnergies > 0) {
            @SuppressWarnings("unchecked")
            List<String> rawDiscard = (List<String>) payload.get("energyCardInstanceIdsToDiscard");
            if (rawDiscard != null && !rawDiscard.isEmpty()) {
                toDiscard = rawDiscard.stream().map(UUID::fromString).toList();
            } else if (attached != null) {
                // Auto-select first N attached energies
                toDiscard = attached.stream()
                        .limit(requiredEnergies)
                        .map(CardInstance::getInstanceId)
                        .toList();
            } else {
                toDiscard = List.of();
            }
            if (toDiscard.size() < requiredEnergies) return;
        } else {
            toDiscard = List.of();
        }

        if (attached != null) {
            List<CardInstance> toDiscardInstances = attached.stream()
                    .filter(ci -> toDiscard.contains(ci.getInstanceId()))
                    .toList();
            player.getDiscard().addAll(toDiscardInstances);
            attached.removeAll(toDiscardInstances);
            for (CardInstance e : toDiscardInstances) {
                ctx.addEvent(new GameEvent(
                        GameEventType.ENERGY_DISCARDED.name(),
                        state.getMatchId(),
                        state.getTurnNumber(),
                        Instant.now(),
                        "Energy discarded for retreat cost.",
                        Map.of(
                                "pokemonInstanceId", active.getInstanceId().toString(),
                                "energyInstanceId", e.getInstanceId().toString(),
                                "reason", "RETREAT_COST"
                        )
                ));
            }
        }

        StatusEffectManager.clearConditionsOnEvolveOrRetreat(active);

        player.getBench().remove(benchIndex.intValue());
        active.setEnteredTurnNumber(state.getTurnNumber());
        player.getBench().add(active);
        player.setActivePokemon(selected);
        state.getTurnFlags().setHasRetreated(true);

        active.setSpecialConditions(new ArrayList<>());

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
