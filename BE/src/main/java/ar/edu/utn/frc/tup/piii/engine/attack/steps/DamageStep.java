package ar.edu.utn.frc.tup.piii.engine.attack.steps;

import ar.edu.utn.frc.tup.piii.engine.EngineContext;
import ar.edu.utn.frc.tup.piii.engine.attack.AbstractAttackStep;
import ar.edu.utn.frc.tup.piii.engine.attack.AttackContext;
import ar.edu.utn.frc.tup.piii.engine.attack.DamageCalculator;

public class DamageStep extends AbstractAttackStep {

    @Override
    public AttackStepResult execute(EngineContext ctx, AttackContext attackCtx) {
        var dmgResult = DamageCalculator.calculate(
                attackCtx.getAttacker(),
                attackCtx.getDefender(),
                ctx.getCardLookup(),
                attackCtx.getAttackIndex(),
                attackCtx.getDamageModifiers()
        );
        attackCtx.setDamageCalc(dmgResult);

        int newDamage = attackCtx.getDefender().getDamageCounters() + dmgResult.damageCountersAdded();
        attackCtx.getDefender().setDamageCounters(newDamage);

        return proceed(ctx, attackCtx);
    }
}
