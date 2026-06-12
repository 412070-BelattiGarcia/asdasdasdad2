package ar.edu.utn.frc.tup.piii.engine.attack.steps;

import ar.edu.utn.frc.tup.piii.engine.EngineContext;
import ar.edu.utn.frc.tup.piii.engine.attack.AbstractAttackStep;
import ar.edu.utn.frc.tup.piii.engine.attack.AttackContext;

public class TargetSelectionStep extends AbstractAttackStep {

    @Override
    public AttackStepResult execute(EngineContext ctx, AttackContext attackCtx) {
        if (attackCtx.getDefender() == null) {
            return AttackStepResult.STOP_CHAIN;
        }
        return proceed(ctx, attackCtx);
    }
}
