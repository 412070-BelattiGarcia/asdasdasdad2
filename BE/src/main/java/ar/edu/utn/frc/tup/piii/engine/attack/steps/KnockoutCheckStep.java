package ar.edu.utn.frc.tup.piii.engine.attack.steps;

import ar.edu.utn.frc.tup.piii.cards.domain.CardDefinition;
import ar.edu.utn.frc.tup.piii.cards.domain.PokemonCardDefinition;
import ar.edu.utn.frc.tup.piii.engine.EngineContext;
import ar.edu.utn.frc.tup.piii.engine.MatchStatus;
import ar.edu.utn.frc.tup.piii.engine.ability.hooks.DestinyBurstHook;
import ar.edu.utn.frc.tup.piii.engine.attack.AbstractAttackStep;
import ar.edu.utn.frc.tup.piii.engine.attack.AttackContext;
import ar.edu.utn.frc.tup.piii.engine.event.GameEvent;
import ar.edu.utn.frc.tup.piii.engine.event.GameEventType;
import ar.edu.utn.frc.tup.piii.engine.model.CardInstance;
import ar.edu.utn.frc.tup.piii.engine.model.GameState;
import ar.edu.utn.frc.tup.piii.engine.model.PlayerState;
import ar.edu.utn.frc.tup.piii.engine.model.PokemonInPlay;
import ar.edu.utn.frc.tup.piii.engine.victory.FinishReason;
import ar.edu.utn.frc.tup.piii.engine.victory.VictoryConditionChecker;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class KnockoutCheckStep extends AbstractAttackStep {

    @Override
    public AttackStepResult execute(EngineContext ctx, AttackContext attackCtx) {
        GameState state = ctx.getState();
        var player = ctx.getPlayer(attackCtx.getDefender().getOwnerPlayerId());
        var opponent = ctx.getOpponent(attackCtx.getDefender().getOwnerPlayerId());
        PokemonInPlay defender = attackCtx.getDefender();

        if (opponent == null) {
            return proceed(ctx, attackCtx);
        }

        CardDefinition rawDef = ctx.getCardLookup().getCardById(defender.getCardDefinitionId());
        if (!(rawDef instanceof PokemonCardDefinition defenderDef)) {
            return proceed(ctx, attackCtx);
        }

        boolean isKO = defender.getDamageCounters() * 10 >= defenderDef.getHp();

        if (!isKO) {
            return proceed(ctx, attackCtx);
        }

        attackCtx.setKnockoutOccurred(true);
        state.setPendingPrizeOwnerPlayerId(opponent.getPlayerId());
        state.setPendingPrizeCount(defenderDef.isEx() ? 2 : 1);

        if (defender.getAttachedEnergies() != null && !defender.getAttachedEnergies().isEmpty()) {
            player.getDiscard().addAll(defender.getAttachedEnergies());
            defender.getAttachedEnergies().clear();
        }

        if (defender.getToolCardInstanceId() != null) {
            player.getDiscard().add(new CardInstance(defender.getToolCardInstanceId(), "TOOL"));
            defender.setToolCardInstanceId(null);
        }

        player.getDiscard().add(new CardInstance(defender.getInstanceId(), defender.getCardDefinitionId()));

        boolean isActive = player.getActivePokemon() != null
                && player.getActivePokemon().getInstanceId().equals(defender.getInstanceId());
        if (isActive) {
            player.setActivePokemon(null);
            if (player.getBench() != null && !player.getBench().isEmpty()) {
                state.setPendingKOReplacement(true);
                state.setKnockedOutPlayerId(player.getPlayerId());
                List<String> candidates = player.getBench().stream()
                        .map(p -> p.getInstanceId().toString())
                        .toList();
                ctx.addEvent(new GameEvent(
                        GameEventType.KO_REPLACEMENT_REQUIRED.name(), state.getMatchId(),
                        state.getTurnNumber(), Instant.now(),
                        "KO replacement required.",
                        Map.of("knockedOutPlayerId", player.getPlayerId().toString(),
                                "candidates", candidates)
                ));
            } else {
                VictoryConditionChecker.VictoryCheckResult vr =
                        VictoryConditionChecker.check(state, opponent.getPlayerId());
                if (vr.finished()) {
                    if (vr.winnerPlayerId() != null) {
                        state.setWinnerPlayerId(vr.winnerPlayerId());
                        state.setFinishReason(vr.reason());
                        state.setStatus(MatchStatus.FINISHED);
                    } else if (vr.suddenDeath()) {
                        state.setSuddenDeath(true);
                        state.setStatus(MatchStatus.FINISHED);
                        state.setFinishReason(FinishReason.SUDDEN_DEATH);
                    }
                }
            }
        } else if (player.getBench() != null) {
            player.getBench().removeIf(p -> p.getInstanceId().equals(defender.getInstanceId()));
        }

        Map<String, Object> koPayload = new HashMap<>();
        koPayload.put("knockedOutPokemonInstanceId", defender.getInstanceId().toString());
        koPayload.put("ownerPlayerId", player.getPlayerId().toString());

        ctx.addEvent(new GameEvent(
                GameEventType.KNOCKOUT_OCCURRED.name(),
                state.getMatchId(),
                state.getTurnNumber(),
                Instant.now(),
                "Knockout occurred.",
                koPayload
        ));

        DestinyBurstHook.onKnockout(defender, attackCtx.getAttacker(), ctx);

        return proceed(ctx, attackCtx);
    }
}
