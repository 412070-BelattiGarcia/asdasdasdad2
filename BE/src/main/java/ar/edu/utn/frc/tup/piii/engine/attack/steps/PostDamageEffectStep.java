package ar.edu.utn.frc.tup.piii.engine.attack.steps;

import ar.edu.utn.frc.tup.piii.cards.domain.PokemonCardDefinition;
import ar.edu.utn.frc.tup.piii.engine.EngineContext;
import ar.edu.utn.frc.tup.piii.engine.SpecialCondition;
import ar.edu.utn.frc.tup.piii.engine.ability.hooks.SpikyShieldHook;
import ar.edu.utn.frc.tup.piii.engine.attack.AbstractAttackStep;
import ar.edu.utn.frc.tup.piii.engine.attack.AttackContext;
import ar.edu.utn.frc.tup.piii.engine.attack.AttackEffectType;
import ar.edu.utn.frc.tup.piii.engine.attack.effects.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PostDamageEffectStep extends AbstractAttackStep {

    @Override
    public AttackStepResult execute(EngineContext ctx, AttackContext attackCtx) {
        SpikyShieldHook.afterDamageTaken(attackCtx.getDefender(), attackCtx.getAttacker(), ctx);

        var cardDef = ctx.getCardLookup().getCardById(attackCtx.getAttacker().getCardDefinitionId());
        if (cardDef instanceof PokemonCardDefinition pDef
                && pDef.getAttacks() != null
                && attackCtx.getAttackIndex() >= 0
                && attackCtx.getAttackIndex() < pDef.getAttacks().size()) {

            var attackDef = pDef.getAttacks().get(attackCtx.getAttackIndex());
            List<PostDamageEffect> effects = buildEffects(attackDef.getEffects());
            for (PostDamageEffect effect : effects) {
                effect.apply(ctx, attackCtx);
            }
        }

        return proceed(ctx, attackCtx);
    }

    private List<PostDamageEffect> buildEffects(List<ar.edu.utn.frc.tup.piii.engine.attack.AttackEffect> attackEffects) {
        List<PostDamageEffect> effects = new ArrayList<>();
        if (attackEffects == null) return effects;

        for (var ae : attackEffects) {
            Map<String, Object> params = ae.getParams() != null ? ae.getParams() : Map.of();
            switch (ae.getType()) {
                case APPLY_SPECIAL_CONDITION:
                    String conditionStr = (String) params.get("condition");
                    if (conditionStr != null) {
                        try {
                            SpecialCondition sc = SpecialCondition.valueOf(conditionStr);
                            effects.add(new ApplyConditionEffect(sc));
                        } catch (IllegalArgumentException ignored) {}
                    }
                    break;
                case HEAL_USER:
                    int healCounters = parseIntParam(params.get("count"), 3);
                    effects.add(new HealUserEffect(healCounters));
                    break;
                case DISCARD_ENERGY:
                    int discardCount = parseIntParam(params.get("count"), 1);
                    effects.add(new DiscardEnergyEffect(discardCount));
                    break;
                case SWITCH_AFTER_DAMAGE:
                    effects.add(new SwitchDefenderEffect());
                    break;
                default:
                    break;
            }
        }
        return effects;
    }

    private int parseIntParam(Object value, int defaultVal) {
        if (value instanceof Number n) return n.intValue();
        if (value instanceof String s) {
            try { return Integer.parseInt(s); } catch (NumberFormatException e) { return defaultVal; }
        }
        return defaultVal;
    }
}
