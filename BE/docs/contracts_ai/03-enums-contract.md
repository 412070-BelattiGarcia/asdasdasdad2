# Enums Contract

## Rule

These enum names and values are canonical.
Do not translate them.
Do not create aliases.
Do not create duplicated enum types.

## Backend location

```
backend/src/main/java/com/pokemontcg/engine/
backend/src/main/java/com/pokemontcg/cards/domain/
backend/src/main/java/com/pokemontcg/decks/domain/
backend/src/main/java/com/pokemontcg/matches/domain/
```

## Frontend location

```
frontend/src/app/shared/models/
```

## MatchStatus

- WAITING
- SETUP
- ACTIVE
- FINISHED

## TurnPhase

- DRAW
- MAIN
- ATTACK
- BETWEEN_TURNS

## PlayerSide

- PLAYER_ONE
- PLAYER_TWO

## CardSupertype

- POKEMON
- ENERGY
- TRAINER

## PokemonStage

- BASIC
- STAGE_1
- STAGE_2
- MEGA
- RESTORED

MEGA and RESTORED are not required in MVP gameplay, but may exist in card data.

## EnergyType

- GRASS
- FIRE
- WATER
- LIGHTNING
- PSYCHIC
- FIGHTING
- DARKNESS
- METAL
- FAIRY
- COLORLESS

## TrainerType

- ITEM
- STADIUM
- SUPPORER

## SpecialCondition

- ASLEEP
- BURNED
- CONFUSED
- PARALYZED
- POISONED

## GameActionType

- DRAW_CARD
- PUT_BASIC_ON_BENCH
- ATTACH_ENERGY
- EVOLVE_POKEMON
- PLAY_TRAINER
- RETREAT_ACTIVE
- DECLARE_ATTACK
- END_TURN
- CHOOSE_KNOCKOUT_REPLACEMENT
- USE_ABILITY

## GameEventType

- MATCH_CREATED
- PLAYER_JOINED
- SETUP_COMPLETED
- TURN_STARTED
- PHASE_CHANGED
- CARD_DRAWN
- POKEMON_PLACED_ON_BENCH
- ENERGY_ATTACHED
- POKEMON_EVOLVED
- TRAINER_PLAYED
- RETREAT_EXECUTED
- ATTACK_DECLARED
- DAMAGE_APPLIED
- SPECIAL_CONDITION_APPLIED
- SPECIAL_CONDITION_REMOVED
- KNOCKOUT_OCCURRED
- PRIZE_TAKEN
- VICTORY_DECIDED
- STATE_UPDATED

## FinishReason

- KNOCKOUT
- PRIZES
- DECK_OUT
- CONCEDE

## DeckValidationError

- DECK_SIZE_INVALID
- DUPLICATE_CARDS
- MISSING_BASIC_POKEMON
- MORE_THAN_4_COPIES
- INVALID_DECK_FORMAT