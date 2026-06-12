package ar.edu.utn.frc.tup.piii.engine.attack.resolvers;

import ar.edu.utn.frc.tup.piii.engine.EngineContext;
import ar.edu.utn.frc.tup.piii.engine.attack.AttackEffect;
import ar.edu.utn.frc.tup.piii.engine.attack.AttackEffectResolver;
import ar.edu.utn.frc.tup.piii.engine.attack.AttackEffectType;
import ar.edu.utn.frc.tup.piii.engine.event.GameEvent;
import ar.edu.utn.frc.tup.piii.engine.event.GameEventType;
import ar.edu.utn.frc.tup.piii.engine.model.PlayerState;
import ar.edu.utn.frc.tup.piii.engine.model.PokemonInPlay;

import java.time.Instant;
import java.util.*;

public class DamageBenchResolver implements AttackEffectResolver {

    @Override
    public void resolve(EngineContext ctx, PokemonInPlay attacker, PokemonInPlay defender, AttackEffect effect, Map<String, Object> payload) {
        List<Map<String, Object>> benchTargets = (List<Map<String, Object>>) payload.get("benchTargets");
        if (benchTargets == null || benchTargets.isEmpty()) return;

        PlayerState opponent = findOpponent(ctx, attacker);

        List<Map<String, Object>> targetsResult = new ArrayList<>();
        for (Map<String, Object> target : benchTargets) {
            String instanceIdStr = (String) target.get("instanceId");
            Object dmgCountersObj = target.get("damageCounters");
            if (instanceIdStr == null || !(dmgCountersObj instanceof Number dmgCounters)) continue;

            UUID instanceId = UUID.fromString(instanceIdStr);
            PokemonInPlay benched = findBenchedPokemon(opponent, instanceId);
            if (benched == null) continue;

            benched.setDamageCounters(benched.getDamageCounters() + dmgCounters.intValue());
            targetsResult.add(Map.of(
                    "instanceId", instanceIdStr,
                    "damageCounters", dmgCounters.intValue()
            ));
        }

        if (!targetsResult.isEmpty()) {
            ctx.addEvent(new GameEvent(
                    GameEventType.BENCH_DAMAGE.name(),
                    ctx.getState().getMatchId(),
                    ctx.getState().getTurnNumber(),
                    Instant.now(),
                    "Damage applied to benched Pokemon.",
                    Map.of("targets", targetsResult)
            ));
        }
    }

    private PlayerState findOpponent(EngineContext ctx, PokemonInPlay attacker) {
        for (PlayerState ps : ctx.getState().getPlayers()) {
            boolean isAttacker = (ps.getActivePokemon() != null &&
                    ps.getActivePokemon().getInstanceId().equals(attacker.getInstanceId()));
            if (!isAttacker && ps.getBench() != null) {
                isAttacker = ps.getBench().stream()
                        .anyMatch(p -> p.getInstanceId().equals(attacker.getInstanceId()));
            }
            if (!isAttacker) return ps;
        }
        return null;
    }

    private PokemonInPlay findBenchedPokemon(PlayerState player, UUID instanceId) {
        if (player == null || player.getBench() == null) return null;
        return player.getBench().stream()
                .filter(p -> p.getInstanceId().equals(instanceId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public AttackEffectType getType() {
        return AttackEffectType.DAMAGE_BENCH;
    }
}
