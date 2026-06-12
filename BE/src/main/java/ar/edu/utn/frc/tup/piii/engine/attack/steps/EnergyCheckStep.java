package ar.edu.utn.frc.tup.piii.engine.attack.steps;

import ar.edu.utn.frc.tup.piii.engine.EngineContext;
import ar.edu.utn.frc.tup.piii.engine.action.GameError;
import ar.edu.utn.frc.tup.piii.engine.attack.AbstractAttackStep;
import ar.edu.utn.frc.tup.piii.engine.attack.AttackContext;
import ar.edu.utn.frc.tup.piii.engine.attack.EnergyRequirementValidator;

public class EnergyCheckStep extends AbstractAttackStep {

    private final EnergyRequirementValidator energyValidator = new EnergyRequirementValidator();

    @Override
    public AttackStepResult execute(EngineContext ctx, AttackContext attackCtx) {
        if (!energyValidator.checkEnergyRequirements(attackCtx.getAttacker(), ctx.getCardLookup(), attackCtx.getAttackIndex())) {
            attackCtx.setEnergyValid(false);
            attackCtx.setErrorMessage("INSUFFICIENT_ENERGY");
            ctx.setError(new GameError("INSUFFICIENT_ENERGY", "The attacking Pokemon does not have enough energy."));
            return AttackStepResult.STOP_CHAIN;
        }
        attackCtx.setEnergyValid(true);
        return proceed(ctx, attackCtx);
    }
}
