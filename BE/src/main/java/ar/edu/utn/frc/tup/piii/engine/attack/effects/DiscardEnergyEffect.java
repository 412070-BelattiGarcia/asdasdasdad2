package ar.edu.utn.frc.tup.piii.engine.attack.effects;

import ar.edu.utn.frc.tup.piii.engine.EngineContext;
import ar.edu.utn.frc.tup.piii.engine.attack.AttackContext;
import ar.edu.utn.frc.tup.piii.engine.event.GameEvent;
import ar.edu.utn.frc.tup.piii.engine.event.GameEventType;
import ar.edu.utn.frc.tup.piii.engine.model.CardInstance;
import ar.edu.utn.frc.tup.piii.engine.model.PlayerState;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DiscardEnergyEffect implements PostDamageEffect {

    private final int count;

    public DiscardEnergyEffect(int count) {
        this.count = count;
    }

    @Override
    public void apply(EngineContext ctx, AttackContext attackCtx) {
        var defender = attackCtx.getDefender();
        if (defender.getAttachedEnergies() == null || defender.getAttachedEnergies().isEmpty()) return;

        PlayerState owner = ctx.getPlayer(defender.getOwnerPlayerId());
        List<UUID> specificIds = attackCtx.getDiscardEnergyInstanceIds();

        List<CardInstance> attached = new ArrayList<>(defender.getAttachedEnergies());
        List<CardInstance> toRemove;

        if (specificIds != null && !specificIds.isEmpty()) {
            toRemove = attached.stream()
                    .filter(ci -> specificIds.contains(ci.getInstanceId()))
                    .limit(count)
                    .toList();
        } else {
            toRemove = attached.stream()
                    .limit(count)
                    .toList();
        }

        for (var e : toRemove) {
            defender.getAttachedEnergies().removeIf(ci -> ci.getInstanceId().equals(e.getInstanceId()));
            owner.getDiscard().add(e);
            ctx.addEvent(new GameEvent(
                    GameEventType.ENERGY_DISCARDED.name(),
                    ctx.getState().getMatchId(),
                    ctx.getState().getTurnNumber(),
                    Instant.now(),
                    "Energy discarded from defender.",
                    Map.of(
                            "pokemonInstanceId", defender.getInstanceId().toString(),
                            "energyInstanceId", e.getInstanceId().toString(),
                            "reason", "ATTACK_EFFECT"
                    )
            ));
        }
    }

    @Override
    public EffectTiming getTiming() {
        return EffectTiming.AFTER_DAMAGE;
    }
}
