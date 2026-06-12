package ar.edu.utn.frc.tup.piii.engine.attack.resolvers;

import ar.edu.utn.frc.tup.piii.engine.EngineContext;
import ar.edu.utn.frc.tup.piii.engine.SpecialCondition;
import ar.edu.utn.frc.tup.piii.engine.ability.hooks.SweetVeilHook;
import ar.edu.utn.frc.tup.piii.engine.attack.AttackEffect;
import ar.edu.utn.frc.tup.piii.engine.attack.AttackEffectResolver;
import ar.edu.utn.frc.tup.piii.engine.attack.AttackEffectType;
import ar.edu.utn.frc.tup.piii.engine.attack.StatusEffectManager;
import ar.edu.utn.frc.tup.piii.engine.event.GameEvent;
import ar.edu.utn.frc.tup.piii.engine.event.GameEventType;
import ar.edu.utn.frc.tup.piii.engine.model.PlayerState;
import ar.edu.utn.frc.tup.piii.engine.model.PokemonInPlay;

import java.time.Instant;
import java.util.Map;

public class ApplySpecialConditionResolver implements AttackEffectResolver {

    @Override
    public void resolve(EngineContext ctx, PokemonInPlay attacker, PokemonInPlay defender, AttackEffect effect, Map<String, Object> payload) {
        String conditionStr = (String) effect.getParams().get("condition");
        if (conditionStr == null) return;

        SpecialCondition condition;
        try {
            condition = SpecialCondition.valueOf(conditionStr);
        } catch (IllegalArgumentException e) {
            return;
        }

        PlayerState defenderOwner = ctx.getPlayer(defender.getOwnerPlayerId());
        if (SweetVeilHook.isImmune(defender, defenderOwner, ctx.getCardLookup())) {
            ctx.addEvent(new GameEvent(
                    GameEventType.STATUS_APPLIED.name(),
                    ctx.getState().getMatchId(),
                    ctx.getState().getTurnNumber(),
                    Instant.now(),
                    "Sweet Veil blocked " + conditionStr + ".",
                    Map.of(
                            "targetPokemonInstanceId", defender.getInstanceId().toString(),
                            "condition", conditionStr,
                            "blocked", true
                    )
            ));
            return;
        }

        StatusEffectManager.applyCondition(defender, condition);

        String attackName = (String) payload.get("attackName");
        ctx.addEvent(new GameEvent(
                GameEventType.STATUS_APPLIED.name(),
                ctx.getState().getMatchId(),
                ctx.getState().getTurnNumber(),
                Instant.now(),
                "Applied " + conditionStr + " to defending Pokemon.",
                Map.of(
                        "targetPokemonInstanceId", defender.getInstanceId().toString(),
                        "condition", conditionStr,
                        "sourceAttackName", attackName != null ? attackName : ""
                )
        ));
    }

    @Override
    public AttackEffectType getType() {
        return AttackEffectType.APPLY_SPECIAL_CONDITION;
    }
}
