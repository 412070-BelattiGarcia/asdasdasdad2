package ar.edu.utn.frc.tup.piii.engine.rules;

import ar.edu.utn.frc.tup.piii.cards.domain.*;
import ar.edu.utn.frc.tup.piii.engine.EngineContext;
import ar.edu.utn.frc.tup.piii.engine.SpecialCondition;
import ar.edu.utn.frc.tup.piii.engine.action.GameAction;
import ar.edu.utn.frc.tup.piii.engine.action.GameActionType;
import ar.edu.utn.frc.tup.piii.engine.handlers.HandlerHelper;
import ar.edu.utn.frc.tup.piii.engine.model.*;
import ar.edu.utn.frc.tup.piii.engine.turn.TurnPhase;
import ar.edu.utn.frc.tup.piii.engine.ports.CardLookupPort;

import java.util.stream.Collectors;

import java.util.*;

public class RuleValidator {

    private final CardLookupPort cardLookup;

    public RuleValidator(CardLookupPort cardLookup) {
        this.cardLookup = cardLookup;
    }

    public boolean validate(EngineContext ctx, GameAction action) {
        GameActionType type = action.getType();
        return switch (type) {
            case ATTACH_ENERGY -> validateAttachEnergy(ctx, action);
            case PUT_BASIC_ON_BENCH -> validatePutBasicOnBench(ctx, action);
            case EVOLVE_POKEMON -> validateEvolve(ctx, action);
            case PLAY_TRAINER -> validatePlayTrainer(ctx, action);
            case RETREAT_ACTIVE -> validateRetreat(ctx, action);
            case DECLARE_ATTACK -> validateAttack(ctx, action);
            case END_TURN -> validateEndTurn(ctx, action);
            case DRAW_CARD -> validateDrawCard(ctx, action);
            case TAKE_PRIZE_CARD -> validateTakePrizeCard(ctx, action);
            case CHOOSE_KNOCKOUT_REPLACEMENT -> validateChooseKnockoutReplacement(ctx, action);
        };
    }

    private boolean validateAttachEnergy(EngineContext ctx, GameAction action) {
        GameState state = ctx.getState();
        if (state.getPhase() != TurnPhase.MAIN) return false;

        PlayerState player = ctx.getPlayer(action.getPlayerId());
        if (state.getTurnFlags().hasAttachedEnergy()) return false;

        Integer handIndex = action.getPayloadInt("handIndex");
        if (handIndex == null || handIndex < 0 || handIndex >= player.getHand().size()) return false;

        CardInstance card = player.getHand().get(handIndex);
        CardDefinition def = cardLookup.getCardById(card.getCardDefinitionId());
        if (!(def instanceof EnergyCardDefinition)) return false;

        String targetIdStr = action.getPayloadString("targetPokemonInstanceId");
        if (targetIdStr == null) return false;
        PokemonInPlay target = HandlerHelper.findPokemon(player, UUID.fromString(targetIdStr));
        return target != null;
    }

    private boolean validatePutBasicOnBench(EngineContext ctx, GameAction action) {
        GameState state = ctx.getState();
        if (state.getPhase() != TurnPhase.MAIN) return false;

        PlayerState player = ctx.getPlayer(action.getPlayerId());
        Integer handIndex = action.getPayloadInt("handIndex");
        if (handIndex == null || handIndex < 0 || handIndex >= player.getHand().size()) return false;

        CardInstance card = player.getHand().get(handIndex);
        CardDefinition def = cardLookup.getCardById(card.getCardDefinitionId());
        if (!(def instanceof PokemonCardDefinition pokemonDef)) return false;
        if (!"BASIC".equals(pokemonDef.getStage())) return false;

        return player.getBench().size() < 5;
    }

    private boolean validateEvolve(EngineContext ctx, GameAction action) {
        GameState state = ctx.getState();
        if (state.getPhase() != TurnPhase.MAIN) return false;

        PlayerState player = ctx.getPlayer(action.getPlayerId());
        Integer handIndex = action.getPayloadInt("handIndex");
        if (handIndex == null || handIndex < 0 || handIndex >= player.getHand().size()) return false;

        CardInstance evolutionCard = player.getHand().get(handIndex);
        CardDefinition evolutionDef = cardLookup.getCardById(evolutionCard.getCardDefinitionId());
        if (!(evolutionDef instanceof PokemonCardDefinition evoDef)) return false;
        if ("BASIC".equals(evoDef.getStage())) return false;

        String targetIdStr = action.getPayloadString("targetPokemonInstanceId");
        if (targetIdStr == null) return false;
        UUID targetId = UUID.fromString(targetIdStr);
        PokemonInPlay target = HandlerHelper.findPokemon(player, targetId);
        if (target == null) return false;
        if (target.getEnteredTurnNumber() == state.getTurnNumber()) return false;
        if (target.isEvolvedThisTurn()) return false;

        CardDefinition targetDef = cardLookup.getCardById(target.getCardDefinitionId());
        if (!(targetDef instanceof PokemonCardDefinition targetPkmDef)) return false;

        if (!evoDef.getEvolvesFrom().equalsIgnoreCase(targetPkmDef.getName())) return false;

        String targetStage = targetPkmDef.getStage();
        String evolutionStage = evoDef.getStage();
        boolean validProgression =
                ("BASIC".equalsIgnoreCase(targetStage) && "STAGE_1".equalsIgnoreCase(evolutionStage)) ||
                ("STAGE_1".equalsIgnoreCase(targetStage) && "STAGE_2".equalsIgnoreCase(evolutionStage));
        if (!validProgression) return false;

        boolean onBench = player.getBench().stream()
                .anyMatch(p -> p.getInstanceId().equals(targetId));
        boolean isActive = player.getActivePokemon() != null
                && player.getActivePokemon().getInstanceId().equals(targetId);
        return onBench || isActive;
    }

    private boolean validatePlayTrainer(EngineContext ctx, GameAction action) {
        GameState state = ctx.getState();
        if (state.getPhase() != TurnPhase.MAIN) return false;

        PlayerState player = ctx.getPlayer(action.getPlayerId());
        Integer handIndex = action.getPayloadInt("handIndex");
        if (handIndex == null || handIndex < 0 || handIndex >= player.getHand().size()) return false;

        CardInstance card = player.getHand().get(handIndex);
        CardDefinition def = cardLookup.getCardById(card.getCardDefinitionId());
        if (!(def instanceof TrainerCardDefinition trainerDef)) return false;

        if (trainerDef.getTrainerSubtype() == TrainerSubtype.SUPPORTER
                && state.getTurnFlags().hasPlayedSupporter()) return false;

        if (trainerDef.getTrainerSubtype() == TrainerSubtype.STADIUM
                && state.getTurnFlags().hasPlayedStadium()) return false;

        return true;
    }

    private boolean validateRetreat(EngineContext ctx, GameAction action) {
        GameState state = ctx.getState();
        if (state.getPhase() != TurnPhase.MAIN) return false;

        if (state.getTurnFlags().hasRetreated()) return false;

        PlayerState player = ctx.getPlayer(action.getPlayerId());
        if (player.getBench().isEmpty()) return false;

        PokemonInPlay active = player.getActivePokemon();
        if (active == null) return false;

        CardDefinition activeDef = cardLookup.getCardById(active.getCardDefinitionId());
        if (activeDef instanceof PokemonCardDefinition pkmDef) {
            List<EnergyType> retreatCost = pkmDef.getRetreatCost();
            int requiredEnergies = retreatCost != null ? retreatCost.size() : 0;
            int attachedEnergies = active.getAttachedEnergies() != null ? active.getAttachedEnergies().size() : 0;
            if (attachedEnergies < requiredEnergies) return false;

            List<String> rawDiscard = (List<String>) action.getPayload().get("energyCardInstanceIdsToDiscard");
            if (rawDiscard == null || rawDiscard.size() < requiredEnergies) return false;

            List<UUID> toDiscard = rawDiscard.stream().map(UUID::fromString).toList();
            List<EnergyType> discardedTypes = active.getAttachedEnergies().stream()
                    .filter(ci -> toDiscard.contains(ci.getInstanceId()))
                    .map(ci -> cardLookup.getCardById(ci.getCardDefinitionId()))
                    .filter(def -> def instanceof EnergyCardDefinition)
                    .flatMap(def -> ((EnergyCardDefinition) def).getProvides().stream())
                    .collect(Collectors.toList());

            List<EnergyType> remainingCost = new ArrayList<>(retreatCost);
            for (EnergyType discarded : discardedTypes) {
                if (discarded == EnergyType.COLORLESS) {
                    if (!remainingCost.isEmpty()) {
                        remainingCost.remove(0);
                    }
                } else {
                    remainingCost.remove(discarded);
                }
            }
            if (!remainingCost.isEmpty()) return false;
        }

        List<SpecialCondition> conditions = active.getSpecialConditions();
        if (conditions != null) {
            if (conditions.contains(SpecialCondition.ASLEEP)) return false;
            if (conditions.contains(SpecialCondition.PARALYZED)) return false;
        }

        return true;
    }

    private boolean validateAttack(EngineContext ctx, GameAction action) {
        GameState state = ctx.getState();
        if (state.getPhase() != TurnPhase.MAIN) return false;

        if (state.getTurnNumber() == 1
                && state.getCurrentPlayerId().equals(state.getFirstPlayerId())) return false;

        if (state.getTurnFlags().hasAttacked()) return false;

        PlayerState player = ctx.getPlayer(action.getPlayerId());
        PokemonInPlay active = player.getActivePokemon();
        if (active == null) return false;

        CardDefinition activeDef = cardLookup.getCardById(active.getCardDefinitionId());
        if (activeDef instanceof PokemonCardDefinition pkmDef) {
            Integer attackIndex = action.getPayloadInt("attackIndex");
            if (attackIndex == null) return false;
            boolean validIndex = pkmDef.getAttacks() != null
                    && pkmDef.getAttacks().stream().anyMatch(a -> a.getIndex() == attackIndex);
            if (!validIndex) return false;
        }

        List<SpecialCondition> conditions = active.getSpecialConditions();
        if (conditions != null) {
            if (conditions.contains(SpecialCondition.ASLEEP)) return false;
            if (conditions.contains(SpecialCondition.PARALYZED)) return false;
        }

        return true;
    }

    private boolean validateEndTurn(EngineContext ctx, GameAction action) {
        TurnPhase phase = ctx.getState().getPhase();
        return phase == TurnPhase.DRAW || phase == TurnPhase.MAIN;
    }

    private boolean validateDrawCard(EngineContext ctx, GameAction action) {
        if (ctx.getState().getPhase() != TurnPhase.DRAW) return false;
        if (ctx.getState().getTurnFlags() != null && ctx.getState().getTurnFlags().hasDrawnForTurn()) return false;
        return true;
    }

    private boolean validateTakePrizeCard(EngineContext ctx, GameAction action) {
        UUID pendingOwner = ctx.getState().getPendingPrizeOwnerPlayerId();
        return pendingOwner != null && pendingOwner.equals(action.getPlayerId());
    }

    private boolean validateChooseKnockoutReplacement(EngineContext ctx, GameAction action) {
        PlayerState opponent = ctx.getOpponent(action.getPlayerId());
        if (opponent == null) return false;
        return opponent.getActivePokemon() == null && opponent.getBench() != null && !opponent.getBench().isEmpty();
    }
}
