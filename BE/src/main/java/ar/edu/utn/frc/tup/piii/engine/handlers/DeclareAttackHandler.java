package ar.edu.utn.frc.tup.piii.engine.handlers;

import ar.edu.utn.frc.tup.piii.cards.domain.PokemonCardDefinition;
import ar.edu.utn.frc.tup.piii.engine.EngineContext;
import ar.edu.utn.frc.tup.piii.engine.MatchStatus;
import ar.edu.utn.frc.tup.piii.engine.action.GameAction;
import ar.edu.utn.frc.tup.piii.engine.action.GameError;
import ar.edu.utn.frc.tup.piii.engine.attack.AttackResolver;
import ar.edu.utn.frc.tup.piii.engine.event.GameEvent;
import ar.edu.utn.frc.tup.piii.engine.event.GameEventType;
import ar.edu.utn.frc.tup.piii.engine.model.CardInstance;
import ar.edu.utn.frc.tup.piii.engine.model.GameState;
import ar.edu.utn.frc.tup.piii.engine.model.PokemonInPlay;
import ar.edu.utn.frc.tup.piii.engine.victory.VictoryConditionChecker;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DeclareAttackHandler implements GameHandler {
    public void handle(EngineContext ctx, GameAction action) {
        GameState state = ctx.getState();
        var player = ctx.getPlayer(action.getPlayerId());
        var opponent = ctx.getOpponent(action.getPlayerId());

        if (state.getTurnFlags().hasAttacked()) return;

        Integer attackIndex = action.getPayloadInt("attackIndex");
        if (attackIndex == null) return;
        String targetIdStr = action.getPayloadString("targetPokemonInstanceId");
        if (targetIdStr == null) return;
        UUID targetPokemonInstanceId = UUID.fromString(targetIdStr);
        PokemonInPlay attacker = player.getActivePokemon();
        PokemonInPlay defender = HandlerHelper.findPokemon(opponent, targetPokemonInstanceId);

        if (attacker == null || defender == null) return;
        AttackResolver.AttackResolutionResult result =
                AttackResolver.resolve(attacker, defender, ctx.getCardLookup(), attackIndex);

        if (!result.energyValid()) {
            ctx.setError(new GameError("INSUFFICIENT_ENERGY", "The attacking Pokemon does not have enough energy."));
            return;
        }

        int newDamage = defender.getDamageCounters() + result.damageCalc().damageCountersAdded();
        defender.setDamageCounters(newDamage);
        PokemonCardDefinition defenderDef =
                (PokemonCardDefinition) ctx.getCardLookup().getCardById(defender.getCardDefinitionId());
        boolean isKO = newDamage * 10 >= defenderDef.getHp();

        Map<String, Object> dmgPayload = new HashMap<>();
        dmgPayload.put("attackerPokemonInstanceId", attacker.getInstanceId().toString());
        dmgPayload.put("defenderPokemonInstanceId", targetPokemonInstanceId.toString());
        dmgPayload.put("finalDamage", result.damageCalc().finalDamage());
        dmgPayload.put("damageCountersAdded", result.damageCalc().damageCountersAdded());
        dmgPayload.put("weaknessApplied", result.damageCalc().weaknessApplied());
        dmgPayload.put("resistanceApplied", result.damageCalc().resistanceApplied());

        ctx.addEvent(new GameEvent(
                GameEventType.DAMAGE_APPLIED.name(),
                state.getMatchId(),
                state.getTurnNumber(),
                Instant.now(),
                "Damage applied.",
                dmgPayload
        ));
        if (isKO) {
            state.setPendingPrizeOwnerPlayerId(player.getPlayerId());

            if (defender.getAttachedEnergies() != null && !defender.getAttachedEnergies().isEmpty()) {
                opponent.getDiscard().addAll(defender.getAttachedEnergies());
                defender.getAttachedEnergies().clear();
            }

            opponent.getDiscard().add(new CardInstance(defender.getInstanceId(), defender.getCardDefinitionId()));
            boolean isActive = opponent.getActivePokemon() != null
                    && opponent.getActivePokemon().getInstanceId().equals(defender.getInstanceId());
            if (isActive) {
                opponent.setActivePokemon(null);
            } else if (opponent.getBench() != null) {
                opponent.getBench().removeIf(p -> p.getInstanceId().equals(defender.getInstanceId()));
            }
            Map<String, Object> koPayload = new HashMap<>();
            koPayload.put("knockedOutPokemonInstanceId", targetPokemonInstanceId.toString());
            koPayload.put("ownerPlayerId", opponent.getPlayerId().toString());

            ctx.addEvent(new GameEvent(
                    GameEventType.KNOCKOUT_OCCURRED.name(),
                    state.getMatchId(),
                    state.getTurnNumber(),
                    Instant.now(),
                    "Knockout occurred.",
                    koPayload
            ));
            boolean hasBench = opponent.getBench() != null && !opponent.getBench().isEmpty();
            if (!hasBench) {
                VictoryConditionChecker.VictoryCheckResult victoryResult =
                        VictoryConditionChecker.check(state, player.getPlayerId());
                if (victoryResult.finished()) {
                    state.setWinnerPlayerId(victoryResult.winnerPlayerId());
                    state.setFinishReason(victoryResult.reason());
                    state.setStatus(MatchStatus.FINISHED);
                }
                ctx.addEvent(new GameEvent(
                        GameEventType.VICTORY_DECIDED.name(),
                        state.getMatchId(),
                        state.getTurnNumber(),
                        Instant.now(),
                        "Victory decided.",
                        Map.of("winnerPlayerId", player.getPlayerId().toString())
                ));
            }
        }
        state.getTurnFlags().setHasAttacked(true);

        ctx.addEvent(new GameEvent(
                GameEventType.ATTACK_DECLARED.name(),
                state.getMatchId(),
                state.getTurnNumber(),
                Instant.now(),
                "Attack declared.",
                Map.of("attackIndex", attackIndex)
        ));
    }
}
