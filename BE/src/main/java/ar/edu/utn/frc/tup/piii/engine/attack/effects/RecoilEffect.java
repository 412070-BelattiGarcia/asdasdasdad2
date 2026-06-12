package ar.edu.utn.frc.tup.piii.engine.attack.effects;

import ar.edu.utn.frc.tup.piii.cards.domain.PokemonCardDefinition;
import ar.edu.utn.frc.tup.piii.engine.EngineContext;
import ar.edu.utn.frc.tup.piii.engine.attack.AttackContext;
import ar.edu.utn.frc.tup.piii.engine.event.GameEvent;
import ar.edu.utn.frc.tup.piii.engine.event.GameEventType;

import java.time.Instant;
import java.util.Map;

public class RecoilEffect implements PostDamageEffect {

    private final int damageCountersToSelf;

    public RecoilEffect(int damageCountersToSelf) {
        this.damageCountersToSelf = damageCountersToSelf;
    }

    @Override
    public void apply(EngineContext ctx, AttackContext attackCtx) {
        var attacker = attackCtx.getAttacker();
        attacker.setDamageCounters(attacker.getDamageCounters() + damageCountersToSelf);

        ctx.addEvent(new GameEvent(
                GameEventType.DAMAGE_APPLIED.name(),
                ctx.getState().getMatchId(),
                ctx.getState().getTurnNumber(),
                Instant.now(),
                "Recoil: attacker took " + (damageCountersToSelf * 10) + " damage.",
                Map.of(
                        "attackerPokemonInstanceId", attacker.getInstanceId().toString(),
                        "selfDamageCounters", damageCountersToSelf
                )
        ));
    }

    @Override
    public EffectTiming getTiming() {
        return EffectTiming.AFTER_DAMAGE;
    }
}
