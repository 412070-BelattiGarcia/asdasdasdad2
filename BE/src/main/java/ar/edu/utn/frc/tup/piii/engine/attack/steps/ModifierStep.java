package ar.edu.utn.frc.tup.piii.engine.attack.steps;

import ar.edu.utn.frc.tup.piii.engine.EngineContext;
import ar.edu.utn.frc.tup.piii.engine.attack.AbstractAttackStep;
import ar.edu.utn.frc.tup.piii.engine.attack.AttackContext;

public class ModifierStep extends AbstractAttackStep {

    @Override
    public AttackStepResult execute(EngineContext ctx, AttackContext attackCtx) {
        return proceed(ctx, attackCtx);
    }
}
