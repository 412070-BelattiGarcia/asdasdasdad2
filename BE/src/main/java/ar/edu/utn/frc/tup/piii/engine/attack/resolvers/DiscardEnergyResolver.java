package ar.edu.utn.frc.tup.piii.engine.attack.resolvers;

import ar.edu.utn.frc.tup.piii.engine.EngineContext;
import ar.edu.utn.frc.tup.piii.engine.attack.AttackEffect;
import ar.edu.utn.frc.tup.piii.engine.attack.AttackEffectResolver;
import ar.edu.utn.frc.tup.piii.engine.attack.AttackEffectType;
import ar.edu.utn.frc.tup.piii.engine.event.GameEvent;
import ar.edu.utn.frc.tup.piii.engine.event.GameEventType;
import ar.edu.utn.frc.tup.piii.engine.model.CardInstance;
import ar.edu.utn.frc.tup.piii.engine.model.PlayerState;
import ar.edu.utn.frc.tup.piii.engine.model.PokemonInPlay;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DiscardEnergyResolver implements AttackEffectResolver {

    @Override
    public void resolve(EngineContext ctx, PokemonInPlay attacker, PokemonInPlay defender, AttackEffect effect, Map<String, Object> payload) {
        Object countObj = effect.getParams().get("count");
        if (!(countObj instanceof Number count)) return;

        List<String> targetEnergyIds = (List<String>) payload.get("targetEnergies");
        if (targetEnergyIds == null || targetEnergyIds.isEmpty()) return;

        List<CardInstance> discarded = new ArrayList<>();
        List<CardInstance> defenderEnergies = defender.getAttachedEnergies();
        if (defenderEnergies == null) return;

        int toDiscard = Math.min(count.intValue(), targetEnergyIds.size());
        for (String idStr : targetEnergyIds.subList(0, toDiscard)) {
            UUID id = UUID.fromString(idStr);
            defenderEnergies.removeIf(ci -> {
                if (ci.getInstanceId().equals(id)) {
                    discarded.add(ci);
                    return true;
                }
                return false;
            });
        }

        PlayerState opponent = findOpponent(ctx, attacker);
        if (opponent != null) {
            opponent.getDiscard().addAll(discarded);
        }

        List<String> discardedIds = discarded.stream()
                .map(ci -> ci.getInstanceId().toString())
                .toList();
        ctx.addEvent(new GameEvent(
                GameEventType.ENERGY_DISCARDED.name(),
                ctx.getState().getMatchId(),
                ctx.getState().getTurnNumber(),
                Instant.now(),
                "Discarded " + discarded.size() + " energy cards from defending Pokemon.",
                Map.of(
                        "targetPokemonInstanceId", defender.getInstanceId().toString(),
                        "discardedEnergies", discardedIds,
                        "count", discarded.size()
                )
        ));
    }

    private PlayerState findOpponent(EngineContext ctx, PokemonInPlay attacker) {
        for (PlayerState ps : ctx.getState().getPlayers()) {
            if (ps.getActivePokemon() != null && !ps.getActivePokemon().getInstanceId().equals(attacker.getInstanceId())) {
                return ps;
            }
            if (ps.getBench() != null) {
                boolean hasAsBench = ps.getBench().stream()
                        .anyMatch(p -> p.getInstanceId().equals(attacker.getInstanceId()));
                if (hasAsBench) continue;
            }
            if (ps.getActivePokemon() != null && !ps.getActivePokemon().getInstanceId().equals(attacker.getInstanceId())) {
                return ps;
            }
        }
        return null;
    }

    @Override
    public AttackEffectType getType() {
        return AttackEffectType.DISCARD_ENERGY;
    }
}
