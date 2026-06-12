package ar.edu.utn.frc.tup.piii.engine.attack.resolvers;

import ar.edu.utn.frc.tup.piii.engine.EngineContext;
import ar.edu.utn.frc.tup.piii.engine.attack.AttackEffect;
import ar.edu.utn.frc.tup.piii.engine.attack.AttackEffectResolver;
import ar.edu.utn.frc.tup.piii.engine.attack.AttackEffectType;
import ar.edu.utn.frc.tup.piii.engine.event.GameEvent;
import ar.edu.utn.frc.tup.piii.engine.event.GameEventType;
import ar.edu.utn.frc.tup.piii.engine.model.PokemonInPlay;

import java.time.Instant;
import java.util.Map;

public class HealUserResolver implements AttackEffectResolver {

    @Override
    public void resolve(EngineContext ctx, PokemonInPlay attacker, PokemonInPlay defender, AttackEffect effect, Map<String, Object> payload) {
        Object dmgObj = effect.getParams().get("damage");
        if (!(dmgObj instanceof Number dmg)) return;

        int healCounters = dmg.intValue();
        int currentDamage = attacker.getDamageCounters();
        int newDamage = Math.max(currentDamage - healCounters, 0);
        attacker.setDamageCounters(newDamage);

        ctx.addEvent(new GameEvent(
                GameEventType.POKEMON_HEALED.name(),
                ctx.getState().getMatchId(),
                ctx.getState().getTurnNumber(),
                Instant.now(),
                "Healed " + (currentDamage - newDamage) + " damage counters from attacking Pokemon.",
                Map.of(
                        "targetPokemonInstanceId", attacker.getInstanceId().toString(),
                        "healedCounters", currentDamage - newDamage
                )
        ));
    }

    @Override
    public AttackEffectType getType() {
        return AttackEffectType.HEAL_USER;
    }
}
