package ar.edu.utn.frc.tup.piii.engine.rules;

import ar.edu.utn.frc.tup.piii.cards.domain.*;
import ar.edu.utn.frc.tup.piii.engine.EngineContext;
import ar.edu.utn.frc.tup.piii.engine.ErrorCode;
import ar.edu.utn.frc.tup.piii.engine.MatchStatus;
import ar.edu.utn.frc.tup.piii.engine.SpecialCondition;
import ar.edu.utn.frc.tup.piii.engine.ability.hooks.ForestsCurseHook;
import ar.edu.utn.frc.tup.piii.engine.action.GameAction;
import ar.edu.utn.frc.tup.piii.engine.action.GameActionType;
import ar.edu.utn.frc.tup.piii.engine.action.GameError;
import ar.edu.utn.frc.tup.piii.engine.handlers.HandlerHelper;
import ar.edu.utn.frc.tup.piii.engine.model.*;
import ar.edu.utn.frc.tup.piii.engine.trainer.TrainerEffectRegistry;
import ar.edu.utn.frc.tup.piii.engine.turn.TurnPhase;
import ar.edu.utn.frc.tup.piii.engine.attack.EnergyRequirementValidator;
import ar.edu.utn.frc.tup.piii.engine.ports.CardLookupPort;

import java.util.stream.Collectors;

import java.util.*;

public class RuleValidator {
    private final CardLookupPort cardLookup;
    private final TrainerEffectRegistry effectRegistry;

    public RuleValidator(CardLookupPort cardLookup) {
        this.cardLookup = cardLookup;
        this.effectRegistry = null;
    }

    public RuleValidator(CardLookupPort cardLookup, TrainerEffectRegistry effectRegistry) {
        this.cardLookup = cardLookup;
        this.effectRegistry = effectRegistry;
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
            case ATTACH_TOOL -> validateAttachTool(ctx, action);
            case USE_ABILITY -> validateUseAbility(ctx, action);
            case CHOOSE_KO_REPLACEMENT -> validateKOReplacement(ctx, action);
            case SETUP_PLACE_ACTIVE -> validateSetupPlaceActive(ctx, action);
            case SETUP_PLACE_BENCH -> validateSetupPlaceBench(ctx, action);
            case SETUP_REMOVE_ACTIVE -> validateSetupRemoveActive(ctx, action);
            case SETUP_REMOVE_BENCH -> validateSetupRemoveBench(ctx, action);
            case CONFIRM_SETUP -> validateConfirmSetup(ctx, action);
            case RESOLVE_MULLIGAN_DRAW -> validateResolveMulliganDraw(ctx, action);
            case RESOLVE_INITIAL_MULLIGAN -> true;
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
        if (state.getPhase() != TurnPhase.MAIN && state.getPhase() != TurnPhase.DRAW) return false;

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

        // No evolution on the player's first turn
        if (!state.hasPlayerCompletedFirstTurn(action.getPlayerId())) return false;

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

        if (trainerDef.getTrainerSubtype() == TrainerSubtype.ITEM
                && ForestsCurseHook.isItemBlocked(player, state, cardLookup)) return false;

        if (trainerDef.getTrainerSubtype() == TrainerSubtype.SUPPORTER
                && state.getTurnFlags().hasPlayedSupporter()) return false;

        if (trainerDef.getTrainerSubtype() == TrainerSubtype.STADIUM
                && state.getTurnFlags().hasPlayedStadium()) return false;

        if (trainerDef.getEffectCode() != null && effectRegistry != null
                && !effectRegistry.isEffectCodeKnown(trainerDef.getEffectCode())) {
            ctx.setError(new GameError(ErrorCode.UNKNOWN_EFFECT_CODE.name(),
                    "Unknown trainer effect code: " + trainerDef.getEffectCode()));
            return false;
        }

        if (trainerDef.getEffectCode() != null && effectRegistry != null
                && effectRegistry.isEffectCodeKnown(trainerDef.getEffectCode())) {
            var effectType = effectRegistry.getEffectType(trainerDef.getEffectCode());
            if (effectType != null) {
                List<String> requiredKeys = effectRegistry.getRequiredTargetKeys(effectType);
                for (String key : requiredKeys) {
                    if (action.getPayload() == null || !action.getPayload().containsKey(key)) {
                        ctx.setError(new GameError(ErrorCode.MISSING_TARGET.name(),
                                "Missing required target: " + key));
                        return false;
                    }
                }
            }
        }

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

            // Auto-select first N attached energies if not specified
            List<CardInstance> attached = active.getAttachedEnergies() != null ? active.getAttachedEnergies() : List.of();
            List<UUID> toDiscard;
            @SuppressWarnings("unchecked")
            List<String> rawDiscard = (List<String>) action.getPayload().get("energyCardInstanceIdsToDiscard");
            if (rawDiscard == null || rawDiscard.isEmpty()) {
                toDiscard = attached.stream().limit(requiredEnergies).map(CardInstance::getInstanceId).toList();
            } else {
                toDiscard = rawDiscard.stream().map(UUID::fromString).toList();
                if (toDiscard.size() < requiredEnergies) return false;
            }

            List<EnergyType> discardedTypes = attached.stream()
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
                    int idx = remainingCost.indexOf(discarded);
                    if (idx >= 0) {
                        remainingCost.remove(idx);
                    } else {
                        // Specific type didn't match → try COLORLESS (any type covers it)
                        idx = remainingCost.indexOf(EnergyType.COLORLESS);
                        if (idx >= 0) {
                            remainingCost.remove(idx);
                        }
                    }
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

        // No attack on the player's first turn
        if (!state.hasPlayerCompletedFirstTurn(action.getPlayerId())) return false;

        if (state.getTurnFlags().hasAttacked()) return false;

        PlayerState player = ctx.getPlayer(action.getPlayerId());
        PokemonInPlay active = player.getActivePokemon();
        if (active == null) return false;

        Integer attackIndex = action.getPayloadInt("attackIndex");
        if (attackIndex == null) return false;

        CardDefinition activeDef = cardLookup.getCardById(active.getCardDefinitionId());
        if (activeDef instanceof PokemonCardDefinition pkmDef) {
            boolean validIndex = pkmDef.getAttacks() != null
                    && pkmDef.getAttacks().stream().anyMatch(a -> a.getIndex() == attackIndex);
            if (!validIndex) return false;
        }

        // Check energy requirements
        if (!EnergyRequirementValidator.checkEnergyRequirements(active, cardLookup, attackIndex)) {
            ctx.setError(new GameError(ErrorCode.INSUFFICIENT_ENERGY.name(),
                    "El Pokémon activo no tiene suficiente energía para este ataque."));
            return false;
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

    private boolean validateAttachTool(EngineContext ctx, GameAction action) {
        GameState state = ctx.getState();
        if (state.getPhase() != TurnPhase.MAIN) return false;

        PlayerState player = ctx.getPlayer(action.getPlayerId());
        Integer handIndex = action.getPayloadInt("handIndex");
        if (handIndex == null || handIndex < 0 || handIndex >= player.getHand().size()) return false;

        CardInstance card = player.getHand().get(handIndex);
        CardDefinition def = cardLookup.getCardById(card.getCardDefinitionId());
        if (!(def instanceof TrainerCardDefinition trainerDef)) return false;
        if (trainerDef.getTrainerSubtype() != TrainerSubtype.POKEMON_TOOL
                && trainerDef.getTrainerSubtype() != TrainerSubtype.ITEM) return false;

        String targetIdStr = action.getPayloadString("targetPokemonInstanceId");
        if (targetIdStr == null) return false;

        PokemonInPlay target = HandlerHelper.findPokemon(player, UUID.fromString(targetIdStr));
        if (target == null) {
            ctx.setError(new GameError(ErrorCode.INVALID_TARGET.name(), "Invalid target Pokemon."));
            return false;
        }

        if (target.getToolCardInstanceId() != null) {
            ctx.setError(new GameError(ErrorCode.TOOL_ALREADY_EQUIPPED.name(), "Target Pokemon already has a tool equipped."));
            return false;
        }

        return true;
    }

    private boolean validateKOReplacement(EngineContext ctx, GameAction action) {
        GameState state = ctx.getState();
        if (!state.isPendingKOReplacement()) return false;
        if (!state.getKnockedOutPlayerId().equals(action.getPlayerId())) return false;

        String benchPkmIdStr = action.getPayloadString("benchPokemonInstanceId");
        if (benchPkmIdStr == null) return false;

        PlayerState player = ctx.getPlayer(action.getPlayerId());
        UUID benchPkmId = UUID.fromString(benchPkmIdStr);
        return player.getBench().stream()
                .anyMatch(p -> p.getInstanceId().equals(benchPkmId));
    }

    private boolean validateUseAbility(EngineContext ctx, GameAction action) {
        GameState state = ctx.getState();
        if (state.getPhase() != TurnPhase.MAIN) return false;

        PlayerState player = ctx.getPlayer(action.getPlayerId());

        String pokemonInstanceIdStr = action.getPayloadString("pokemonInstanceId");
        String abilityName = action.getPayloadString("abilityName");
        if (pokemonInstanceIdStr == null || abilityName == null) return false;

        UUID pokemonInstanceId = UUID.fromString(pokemonInstanceIdStr);
        PokemonInPlay pokemon = HandlerHelper.findPokemon(player, pokemonInstanceId);
        if (pokemon == null) return false;

        CardDefinition cardDef = cardLookup.getCardById(pokemon.getCardDefinitionId());
        if (!(cardDef instanceof PokemonCardDefinition pokemonDef)) return false;

        boolean hasAbility = pokemonDef.getAbilities() != null
                && pokemonDef.getAbilities().stream().anyMatch(a -> a.getName().equals(abilityName));
        if (!hasAbility) return false;

        if (pokemon.getAbilitiesUsedThisTurn().contains(abilityName)) return false;

        if (pokemon.getSpecialConditions().contains(SpecialCondition.ASLEEP)
                || pokemon.getSpecialConditions().contains(SpecialCondition.PARALYZED)) return false;

        return true;
    }

    private boolean validateSetupPlaceActive(EngineContext ctx, GameAction action) {
        PlayerState player = ctx.getPlayer(action.getPlayerId());
        if (player == null) return false;
        if (player.getActivePokemon() != null) return false;
        String cardInstanceIdStr = action.getPayloadString("cardInstanceId");
        if (cardInstanceIdStr == null) return false;
        UUID cardInstanceId = UUID.fromString(cardInstanceIdStr);
        Optional<CardInstance> cardOpt = player.getHand().stream()
                .filter(c -> c.getInstanceId().equals(cardInstanceId))
                .findFirst();
        if (cardOpt.isEmpty()) return false;
        CardDefinition def = cardLookup.getCardById(cardOpt.get().getCardDefinitionId());
        if (!(def instanceof PokemonCardDefinition pkmnDef)) return false;
        return "BASIC".equalsIgnoreCase(pkmnDef.getStage()) || pkmnDef.getStage() == null;
    }

    private boolean validateSetupPlaceBench(EngineContext ctx, GameAction action) {
        PlayerState player = ctx.getPlayer(action.getPlayerId());
        if (player == null) return false;
        if (player.getBench().size() >= 5) return false;
        String cardInstanceIdStr = action.getPayloadString("cardInstanceId");
        if (cardInstanceIdStr == null) return false;
        UUID cardInstanceId = UUID.fromString(cardInstanceIdStr);
        Optional<CardInstance> cardOpt = player.getHand().stream()
                .filter(c -> c.getInstanceId().equals(cardInstanceId))
                .findFirst();
        if (cardOpt.isEmpty()) return false;
        CardDefinition def = cardLookup.getCardById(cardOpt.get().getCardDefinitionId());
        if (!(def instanceof PokemonCardDefinition pkmnDef)) return false;
        return "BASIC".equalsIgnoreCase(pkmnDef.getStage()) || pkmnDef.getStage() == null;
    }

    private boolean validateSetupRemoveActive(EngineContext ctx, GameAction action) {
        PlayerState player = ctx.getPlayer(action.getPlayerId());
        if (player == null) return false;
        return player.getActivePokemon() != null;
    }

    private boolean validateSetupRemoveBench(EngineContext ctx, GameAction action) {
        PlayerState player = ctx.getPlayer(action.getPlayerId());
        if (player == null) return false;
        String cardInstanceIdStr = action.getPayloadString("cardInstanceId");
        if (cardInstanceIdStr == null) return false;
        UUID cardInstanceId = UUID.fromString(cardInstanceIdStr);
        return player.getBench().stream().anyMatch(p -> p.getInstanceId().equals(cardInstanceId));
    }

    private boolean validateResolveMulliganDraw(EngineContext ctx, GameAction action) {
        GameState state = ctx.getState();
        if (state.getStatus() != MatchStatus.SETUP) {
            ctx.setError(new GameError("WRONG_STATUS", "Solo se puede resolver mulligan durante SETUP"));
            return false;
        }
        if (!state.isMulliganDrawPending()) {
            ctx.setError(new GameError("NOT_PENDING", "No hay decisión de mulligan pendiente"));
            return false;
        }
        if (!state.hasPendingMulliganDraw(action.getPlayerId())) {
            ctx.setError(new GameError("ALREADY_RESOLVED", "Ya resolviste tu decisión de mulligan"));
            return false;
        }
        return true;
    }

    private boolean validateConfirmSetup(EngineContext ctx, GameAction action) {
        PlayerState player = ctx.getPlayer(action.getPlayerId());
        if (player == null) return false;
        if (player.isSetupConfirmed()) return false;

        GameState state = ctx.getState();
        if (state.isMulliganDrawPending() && state.hasPendingMulliganDraw(action.getPlayerId())) {
            ctx.setError(new GameError("MULLIGAN_DRAW_PENDING",
                "Debes decidir sobre el mulligan del oponente antes de confirmar tu setup"));
            return false;
        }

        return player.getActivePokemon() != null;
    }
}
