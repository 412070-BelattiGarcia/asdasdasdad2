package ar.edu.utn.frc.tup.piii.engine.handlers;

import ar.edu.utn.frc.tup.piii.cards.domain.CardDefinition;
import ar.edu.utn.frc.tup.piii.cards.domain.PokemonCardDefinition;
import ar.edu.utn.frc.tup.piii.engine.EngineContext;
import ar.edu.utn.frc.tup.piii.engine.MatchStatus;
import ar.edu.utn.frc.tup.piii.engine.action.GameAction;
import ar.edu.utn.frc.tup.piii.engine.action.GameError;
import ar.edu.utn.frc.tup.piii.engine.attack.AbstractAttackStep;
import ar.edu.utn.frc.tup.piii.engine.attack.AttackChainBuilder;
import ar.edu.utn.frc.tup.piii.engine.attack.AttackContext;
import ar.edu.utn.frc.tup.piii.engine.attack.AttackEffectRegistry;
import ar.edu.utn.frc.tup.piii.engine.attack.AttackStep;
import ar.edu.utn.frc.tup.piii.engine.attack.steps.ConfusionCheckStep;
import ar.edu.utn.frc.tup.piii.engine.attack.steps.DamageStep;
import ar.edu.utn.frc.tup.piii.engine.attack.steps.EnergyCheckStep;
import ar.edu.utn.frc.tup.piii.engine.attack.steps.KnockoutCheckStep;
import ar.edu.utn.frc.tup.piii.engine.attack.steps.ModifierStep;
import ar.edu.utn.frc.tup.piii.engine.attack.steps.PostDamageEffectStep;
import ar.edu.utn.frc.tup.piii.engine.attack.steps.PrerequisiteStep;
import ar.edu.utn.frc.tup.piii.engine.attack.steps.TargetSelectionStep;
import ar.edu.utn.frc.tup.piii.engine.event.GameEvent;
import ar.edu.utn.frc.tup.piii.engine.event.GameEventType;
import ar.edu.utn.frc.tup.piii.engine.model.PokemonInPlay;
import ar.edu.utn.frc.tup.piii.engine.turn.TurnManager;
import ar.edu.utn.frc.tup.piii.engine.victory.FinishReason;
import ar.edu.utn.frc.tup.piii.engine.victory.VictoryConditionChecker;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DeclareAttackHandler implements GameHandler {

    private final TurnManager turnManager;
    private final AttackEffectRegistry attackEffectRegistry;
    private final AttackStep attackChain;

    public DeclareAttackHandler(TurnManager turnManager, AttackEffectRegistry attackEffectRegistry) {
        this.turnManager = turnManager;
        this.attackEffectRegistry = attackEffectRegistry;
        this.attackChain = AbstractAttackStep.buildChain(
                new EnergyCheckStep(),
                new ConfusionCheckStep(),
                new TargetSelectionStep(),
                new PrerequisiteStep(),
                new ModifierStep(),
                new DamageStep(),
                new PostDamageEffectStep(),
                new KnockoutCheckStep()
        );
    }

    public void handle(EngineContext ctx, GameAction action) {
        var state = ctx.getState();
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

        List<UUID> discardEnergyIds = null;
        if (action.getPayload().containsKey("energyCardInstanceIdsToDiscard")) {
            @SuppressWarnings("unchecked")
            List<String> rawIds = (List<String>) action.getPayload().get("energyCardInstanceIdsToDiscard");
            discardEnergyIds = rawIds.stream().map(UUID::fromString).toList();
        }

        AttackContext attackCtx = new AttackContext(
                attacker, defender, attackIndex,
                state.getTurnFlags().getDamageModifiers(), targetPokemonInstanceId
        );
        attackCtx.setDiscardEnergyInstanceIds(discardEnergyIds);

        AttackStep.AttackStepResult chainResult = AttackChainBuilder.executeChain(attackChain, ctx, attackCtx);

        if (chainResult == AttackStep.AttackStepResult.STOP_CHAIN && attackCtx.getErrorMessage() != null) {
            if ("INSUFFICIENT_ENERGY".equals(attackCtx.getErrorMessage())) {
                ctx.setError(new GameError("INSUFFICIENT_ENERGY", "The attacking Pokemon does not have enough energy."));
            }
            return;
        }

        if (attackCtx.isConfusedSelfHit()) {
            handleConfusionSelfHit(ctx, player, attacker, opponent);
            if (state.isPendingKOReplacement()) {
                state.getTurnFlags().setHasAttacked(true);
                return;
            }
            state.getTurnFlags().setHasAttacked(true);
            turnManager.endTurn(ctx);
            return;
        }

        if (attackCtx.getDamageCalc() != null) {
            Map<String, Object> dmgPayload = new HashMap<>();
            dmgPayload.put("attackerPokemonInstanceId", attacker.getInstanceId().toString());
            dmgPayload.put("defenderPokemonInstanceId", targetPokemonInstanceId.toString());
            dmgPayload.put("finalDamage", attackCtx.getDamageCalc().finalDamage());
            dmgPayload.put("damageCountersAdded", attackCtx.getDamageCalc().damageCountersAdded());
            dmgPayload.put("weaknessApplied", attackCtx.getDamageCalc().weaknessApplied());
            dmgPayload.put("resistanceApplied", attackCtx.getDamageCalc().resistanceApplied());

            ctx.addEvent(new GameEvent(
                    GameEventType.DAMAGE_APPLIED.name(),
                    state.getMatchId(),
                    state.getTurnNumber(),
                    Instant.now(),
                    "Damage applied.",
                    dmgPayload
            ));
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

        if (state.isPendingKOReplacement()) {
            return;
        }

        CardDefinition attackerCardDef = ctx.getCardLookup().getCardById(attacker.getCardDefinitionId());
        if (attackerCardDef instanceof PokemonCardDefinition pDef
                && pDef.getAttacks() != null && attackIndex >= 0 && attackIndex < pDef.getAttacks().size()) {
            var attackDef = pDef.getAttacks().get(attackIndex);
            Map<String, Object> effectPayload = new HashMap<>();
            effectPayload.put("attackName", attackDef.getName());
            attackEffectRegistry.resolve(ctx, attacker, defender, attackDef.getEffects(), effectPayload);
        }

        if (state.getStatus() == MatchStatus.FINISHED) return;

        turnManager.endTurn(ctx);
    }

    private void handleConfusionSelfHit(EngineContext ctx,
                                         ar.edu.utn.frc.tup.piii.engine.model.PlayerState player,
                                         PokemonInPlay attacker,
                                         ar.edu.utn.frc.tup.piii.engine.model.PlayerState opponent) {
        var state = ctx.getState();

        ctx.addEvent(new GameEvent(
                GameEventType.CONFUSION_SELF_HIT.name(),
                state.getMatchId(), state.getTurnNumber(), Instant.now(),
                "Confused Pokemon hit itself.",
                Map.of("attackerPokemonInstanceId", attacker.getInstanceId().toString(),
                        "selfDamageCounters", 3)
        ));
        ctx.addEvent(new GameEvent(
                GameEventType.DAMAGE_APPLIED.name(),
                state.getMatchId(), state.getTurnNumber(), Instant.now(),
                "Self-inflicted damage from confusion.",
                Map.of("attackerPokemonInstanceId", attacker.getInstanceId().toString(),
                        "damageCountersAdded", 3)
        ));

        if (isPokemonKOd(attacker, ctx)) {
            handleKODuringConfusion(ctx, player, attacker, opponent);
        }
    }

    private boolean isPokemonKOd(PokemonInPlay pokemon, EngineContext ctx) {
        CardDefinition def = ctx.getCardLookup().getCardById(pokemon.getCardDefinitionId());
        int hp = (def instanceof PokemonCardDefinition p) ? p.getHp() : 0;
        return pokemon.getDamageCounters() * 10 >= hp;
    }

    private void handleKODuringConfusion(EngineContext ctx,
                                          ar.edu.utn.frc.tup.piii.engine.model.PlayerState player,
                                          PokemonInPlay attacker,
                                          ar.edu.utn.frc.tup.piii.engine.model.PlayerState opponent) {
        var state = ctx.getState();

        if (attacker.getAttachedEnergies() != null) {
            for (var e : attacker.getAttachedEnergies()) {
                ctx.addEvent(new GameEvent(
                        GameEventType.ENERGY_DISCARDED.name(),
                        state.getMatchId(),
                        state.getTurnNumber(),
                        Instant.now(),
                        "Energy discarded during confusion KO.",
                        Map.of(
                                "pokemonInstanceId", attacker.getInstanceId().toString(),
                                "energyInstanceId", e.getInstanceId().toString(),
                                "reason", "CONFUSION_KO"
                        )
                ));
            }
            player.getDiscard().addAll(attacker.getAttachedEnergies());
            attacker.getAttachedEnergies().clear();
        }

        player.getDiscard().add(
                new ar.edu.utn.frc.tup.piii.engine.model.CardInstance(attacker.getInstanceId(), attacker.getCardDefinitionId()));

        if (player.getActivePokemon() != null
                && player.getActivePokemon().getInstanceId().equals(attacker.getInstanceId())) {
            player.setActivePokemon(null);
            if (player.getBench() != null && !player.getBench().isEmpty()) {
                state.setPendingKOReplacement(true);
                state.setKnockedOutPlayerId(player.getPlayerId());
                java.util.List<String> candidates = player.getBench().stream()
                        .map(p -> p.getInstanceId().toString())
                        .toList();
                ctx.addEvent(new GameEvent(
                        GameEventType.KO_REPLACEMENT_REQUIRED.name(), state.getMatchId(),
                        state.getTurnNumber(), Instant.now(),
                        "KO replacement required after confusion self-hit.",
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
        }
    }
}
