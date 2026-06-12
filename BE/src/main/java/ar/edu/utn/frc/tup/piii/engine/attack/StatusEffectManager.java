package ar.edu.utn.frc.tup.piii.engine.attack;

import ar.edu.utn.frc.tup.piii.cards.domain.PokemonCardDefinition;
import ar.edu.utn.frc.tup.piii.engine.SpecialCondition;
import ar.edu.utn.frc.tup.piii.engine.event.GameEvent;
import ar.edu.utn.frc.tup.piii.engine.event.GameEventType;
import ar.edu.utn.frc.tup.piii.engine.model.CardInstance;
import ar.edu.utn.frc.tup.piii.engine.model.GameState;
import ar.edu.utn.frc.tup.piii.engine.model.PlayerState;
import ar.edu.utn.frc.tup.piii.engine.model.PokemonInPlay;
import ar.edu.utn.frc.tup.piii.engine.ports.CardLookupPort;
import ar.edu.utn.frc.tup.piii.engine.ports.RandomizerPort;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StatusEffectManager {

    public static void applyCondition(PokemonInPlay pkm, SpecialCondition newCondition) {
        if (pkm.getSpecialConditions() == null) {
            pkm.setSpecialConditions(new ArrayList<>());
        }
        if (newCondition == SpecialCondition.ASLEEP ||
                newCondition == SpecialCondition.CONFUSED ||
                newCondition == SpecialCondition.PARALYZED) {
            pkm.getSpecialConditions().remove(SpecialCondition.ASLEEP);
            pkm.getSpecialConditions().remove(SpecialCondition.CONFUSED);
            pkm.getSpecialConditions().remove(SpecialCondition.PARALYZED);
        }
        if (!pkm.getSpecialConditions().contains(newCondition)) {
            pkm.getSpecialConditions().add(newCondition);
        }
    }

    public static List<GameEvent> processBetweenTurnStatuses(GameState state, RandomizerPort randomizer, CardLookupPort cardLookup) {
        List<GameEvent> events = new ArrayList<>();

        for (PlayerState player : state.getPlayers()) {
            PokemonInPlay active = player.getActivePokemon();
            if (active == null || active.getSpecialConditions() == null) continue;

            List<SpecialCondition> conditions = new ArrayList<>(active.getSpecialConditions());

            for (SpecialCondition sc : conditions) {
                switch (sc) {
                    case POISONED:
                        active.setDamageCounters(active.getDamageCounters() + 1);
                        events.add(new GameEvent(
                                GameEventType.DAMAGE_APPLIED.name(),
                                state.getMatchId(),
                                state.getTurnNumber(),
                                Instant.now(),
                                "Poisened Pokemon took 1 damage counter.",
                                null
                        ));
                        checkKoBetweenTurns(active, cardLookup, state, player, events);
                        break;

                    case BURNED:
                        if (randomizer.nextInt(2) == 0) {
                            active.getSpecialConditions().remove(SpecialCondition.BURNED);
                            events.add(new GameEvent(
                                    GameEventType.STATE_UPDATED.name(),
                                    state.getMatchId(),
                                    state.getTurnNumber(),
                                    Instant.now(),
                                    "Burned Pokemon recovered.",
                                    null
                            ));
                        } else {
                            active.setDamageCounters(active.getDamageCounters() + 2);
                            events.add(new GameEvent(
                                    GameEventType.DAMAGE_APPLIED.name(),
                                    state.getMatchId(),
                                    state.getTurnNumber(),
                                    Instant.now(),
                                    "Burned Pokemon took 2 damage counters.",
                                    null
                            ));
                            checkKoBetweenTurns(active, cardLookup, state, player, events);
                        }
                        break;

                    case ASLEEP:
                        if (randomizer.nextInt(2) == 0) {
                            active.getSpecialConditions().remove(SpecialCondition.ASLEEP);
                            events.add(new GameEvent(
                                    GameEventType.STATE_UPDATED.name(),
                                    state.getMatchId(),
                                    state.getTurnNumber(),
                                    Instant.now(),
                                    "Sleeping Pokemon woke up.",
                                    null
                            ));
                        }
                        break;

                    case PARALYZED:
                        active.getSpecialConditions().remove(SpecialCondition.PARALYZED);
                        events.add(new GameEvent(
                                GameEventType.STATE_UPDATED.name(),
                                state.getMatchId(),
                                state.getTurnNumber(),
                                Instant.now(),
                                "Paralyzed Pokemon recovered between turns.",
                                null
                        ));
                        break;

                    default:
                        break;
                }
            }
        }

        return events;
    }

    public static void checkKoBetweenTurns(
            PokemonInPlay pkm, CardLookupPort cardLookup,
            GameState state, PlayerState owner, List<GameEvent> events) {
        PokemonCardDefinition pkmDef = (PokemonCardDefinition) cardLookup.getCardById(pkm.getCardDefinitionId());
        if (pkmDef == null) return;
        if (pkm.getDamageCounters() * 10 < pkmDef.getHp()) return;

        if (pkm.getAttachedEnergies() != null) {
            owner.getDiscard().addAll(pkm.getAttachedEnergies());
            pkm.getAttachedEnergies().clear();
        }
        owner.getDiscard().add(new CardInstance(pkm.getInstanceId(), pkm.getCardDefinitionId()));

        PlayerState opponent = null;
        for (PlayerState p : state.getPlayers()) {
            if (!p.getPlayerId().equals(owner.getPlayerId())) {
                opponent = p;
                break;
            }
        }

        boolean isActive = owner.getActivePokemon() != null
                && owner.getActivePokemon().getInstanceId().equals(pkm.getInstanceId());
        if (isActive) {
            owner.setActivePokemon(null);
            if (owner.getBench() != null && !owner.getBench().isEmpty()) {
                owner.setActivePokemon(owner.getBench().remove(0));
            }
        } else if (owner.getBench() != null) {
            owner.getBench().removeIf(p -> p.getInstanceId().equals(pkm.getInstanceId()));
        }

        if (opponent != null) {
            state.setPendingPrizeOwnerPlayerId(opponent.getPlayerId());
            state.setPendingPrizeCount(state.getPendingPrizeCount() + 1);
        }

        events.add(new GameEvent(
                GameEventType.KNOCKOUT_OCCURRED.name(),
                state.getMatchId(), state.getTurnNumber(), Instant.now(),
                "Knockout by special condition.",
                Map.of(
                        "knockedOutPokemonInstanceId", pkm.getInstanceId().toString(),
                        "ownerPlayerId", owner.getPlayerId().toString()
                )
        ));
    }

    public static void clearConditionsOnEvolveOrRetreat(PokemonInPlay pokemon) {
        if (pokemon.getSpecialConditions() != null) {
            pokemon.getSpecialConditions().clear();
        }
    }

    public static void clearConditionsOnBench(PlayerState player) {
        if (player.getBench() == null) return;
        for (PokemonInPlay pkm : player.getBench()) {
            clearConditionsOnEvolveOrRetreat(pkm);
        }
    }

    public static boolean isConfused(PokemonInPlay pokemon) {
        return pokemon.getSpecialConditions() != null
                && pokemon.getSpecialConditions().contains(SpecialCondition.CONFUSED);
    }
}
