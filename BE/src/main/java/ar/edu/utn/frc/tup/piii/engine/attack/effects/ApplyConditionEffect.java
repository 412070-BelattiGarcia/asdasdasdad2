package ar.edu.utn.frc.tup.piii.engine.attack.effects;

import ar.edu.utn.frc.tup.piii.cards.domain.PokemonCardDefinition;
import ar.edu.utn.frc.tup.piii.engine.EngineContext;
import ar.edu.utn.frc.tup.piii.engine.SpecialCondition;
import ar.edu.utn.frc.tup.piii.engine.attack.AttackContext;
import ar.edu.utn.frc.tup.piii.engine.attack.StatusEffectManager;
import ar.edu.utn.frc.tup.piii.engine.event.GameEvent;
import ar.edu.utn.frc.tup.piii.engine.event.GameEventType;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class ApplyConditionEffect implements PostDamageEffect {

    private final SpecialCondition condition;

    public ApplyConditionEffect(SpecialCondition condition) {
        this.condition = condition;
    }

    @Override
    public void apply(EngineContext ctx, AttackContext attackCtx) {
        StatusEffectManager.applyCondition(attackCtx.getDefender(), condition);
        ctx.addEvent(new GameEvent(
                GameEventType.STATE_UPDATED.name(),
                ctx.getState().getMatchId(),
                ctx.getState().getTurnNumber(),
                Instant.now(),
                "Special condition applied: " + condition.name(),
                Map.of(
                        "targetPokemonInstanceId", attackCtx.getDefender().getInstanceId().toString(),
                        "condition", condition.name()
                )
        ));
    }

    @Override
    public EffectTiming getTiming() {
        return EffectTiming.AFTER_DAMAGE;
    }
}
