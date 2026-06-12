package ar.edu.utn.frc.tup.piii.engine.attack;

import ar.edu.utn.frc.tup.piii.engine.EngineContext;

public class AttackChainBuilder {

    public static AttackStep.AttackStepResult executeChain(AttackStep firstStep, EngineContext ctx, AttackContext attackCtx) {
        AttackStep current = firstStep;
        while (current != null) {
            AttackStep.AttackStepResult result = current.execute(ctx, attackCtx);
            if (result != AttackStep.AttackStepResult.CONTINUE) {
                return result;
            }
            current = current.getNext();
        }
        return AttackStep.AttackStepResult.CONTINUE;
    }
}
