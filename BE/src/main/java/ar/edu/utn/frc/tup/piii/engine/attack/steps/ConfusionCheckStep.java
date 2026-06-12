package ar.edu.utn.frc.tup.piii.engine.attack.steps;

import ar.edu.utn.frc.tup.piii.engine.EngineContext;
import ar.edu.utn.frc.tup.piii.engine.attack.AbstractAttackStep;
import ar.edu.utn.frc.tup.piii.engine.attack.AttackContext;
import ar.edu.utn.frc.tup.piii.engine.attack.StatusEffectManager;

public class ConfusionCheckStep extends AbstractAttackStep {

    @Override
    public AttackStepResult execute(EngineContext ctx, AttackContext attackCtx) {
        if (StatusEffectManager.isConfused(attackCtx.getAttacker()) && ctx.getRandomizer().nextInt(2) == 0) {
            int selfDmgCounters = 3;
            attackCtx.setConfusedSelfHit(true);
            attackCtx.setSelfDamageCounters(selfDmgCounters);
            return AttackStepResult.STOP_CHAIN_END_TURN;
        }
        return proceed(ctx, attackCtx);
    }
}
