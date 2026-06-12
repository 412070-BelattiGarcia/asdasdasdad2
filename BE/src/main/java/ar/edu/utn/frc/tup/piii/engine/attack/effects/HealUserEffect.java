package ar.edu.utn.frc.tup.piii.engine.attack.effects;

import ar.edu.utn.frc.tup.piii.engine.EngineContext;
import ar.edu.utn.frc.tup.piii.engine.attack.AttackContext;
import ar.edu.utn.frc.tup.piii.engine.event.GameEvent;
import ar.edu.utn.frc.tup.piii.engine.event.GameEventType;

import java.time.Instant;
import java.util.Map;

public class HealUserEffect implements PostDamageEffect {

    private final int damageCountersHealed;

    public HealUserEffect(int damageCountersHealed) {
        this.damageCountersHealed = damageCountersHealed;
    }

    @Override
    public void apply(EngineContext ctx, AttackContext attackCtx) {
        var attacker = attackCtx.getAttacker();
        int currentDamage = attacker.getDamageCounters();
        int newDamage = Math.max(0, currentDamage - damageCountersHealed);
        attacker.setDamageCounters(newDamage);

        ctx.addEvent(new GameEvent(
                GameEventType.STATE_UPDATED.name(),
                ctx.getState().getMatchId(),
                ctx.getState().getTurnNumber(),
                Instant.now(),
                "Attacker healed " + (damageCountersHealed * 10) + " damage.",
                Map.of(
                        "attackerPokemonInstanceId", attacker.getInstanceId().toString(),
                        "healedCounters", damageCountersHealed
                )
        ));
    }

    @Override
    public EffectTiming getTiming() {
        return EffectTiming.AFTER_DAMAGE;
    }
}
