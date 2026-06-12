package ar.edu.utn.frc.tup.piii.engine.attack.effects;

import ar.edu.utn.frc.tup.piii.engine.EngineContext;
import ar.edu.utn.frc.tup.piii.engine.attack.AttackContext;
import ar.edu.utn.frc.tup.piii.engine.event.GameEvent;
import ar.edu.utn.frc.tup.piii.engine.event.GameEventType;
import ar.edu.utn.frc.tup.piii.engine.model.PlayerState;

import java.time.Instant;
import java.util.Map;

public class SwitchDefenderEffect implements PostDamageEffect {

    @Override
    public void apply(EngineContext ctx, AttackContext attackCtx) {
        var defender = attackCtx.getDefender();
        PlayerState owner = ctx.getPlayer(defender.getOwnerPlayerId());
        if (owner.getBench() == null || owner.getBench().isEmpty()) return;

        var replacement = owner.getBench().remove(0);
        owner.getBench().add(defender);
        owner.setActivePokemon(replacement);

        ctx.addEvent(new GameEvent(
                GameEventType.STATE_UPDATED.name(),
                ctx.getState().getMatchId(),
                ctx.getState().getTurnNumber(),
                Instant.now(),
                "Defender switched with bench Pokemon.",
                Map.of(
                        "oldActiveInstanceId", defender.getInstanceId().toString(),
                        "newActiveInstanceId", replacement.getInstanceId().toString()
                )
        ));
    }

    @Override
    public EffectTiming getTiming() {
        return EffectTiming.AFTER_DAMAGE;
    }
}
