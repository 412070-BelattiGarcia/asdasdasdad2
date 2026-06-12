package ar.edu.utn.frc.tup.piii.engine.attack;

import ar.edu.utn.frc.tup.piii.engine.EngineContext;
import ar.edu.utn.frc.tup.piii.engine.event.GameEvent;
import ar.edu.utn.frc.tup.piii.engine.event.GameEventType;
import ar.edu.utn.frc.tup.piii.engine.model.PokemonInPlay;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttackEffectRegistry {

    private final Map<AttackEffectType, AttackEffectResolver> resolvers = new HashMap<>();

    public void registerResolver(AttackEffectResolver resolver) {
        resolvers.put(resolver.getType(), resolver);
    }

    public void resolve(EngineContext ctx, PokemonInPlay attacker, PokemonInPlay defender, List<AttackEffect> effects, Map<String, Object> payload) {
        if (effects == null) return;
        for (AttackEffect effect : effects) {
            AttackEffectResolver resolver = resolvers.get(effect.getType());
            if (resolver == null) continue;
            resolver.resolve(ctx, attacker, defender, effect, payload);
            ctx.addEvent(new GameEvent(
                    GameEventType.ATTACK_EFFECT_RESOLVED.name(),
                    ctx.getState().getMatchId(),
                    ctx.getState().getTurnNumber(),
                    Instant.now(),
                    "Attack effect resolved: " + effect.getType().name(),
                    Map.of(
                            "effectType", effect.getType().name(),
                            "attackName", payload != null ? payload.getOrDefault("attackName", "") : "",
                            "result", "resolved"
                    )
            ));
        }
    }
}
