# Enums Contract

## Rule

These enum names and values are canonical.
Do not translate them.
Do not create aliases.
Do not create duplicated enum types.

## Backend location

```
ar.edu.utn.frc.tup.piii.engine/
ar.edu.utn.frc.tup.piii.cards/domain/
ar.edu.utn.frc.tup.piii.decks/domain/
ar.edu.utn.frc.tup.piii.matches/domain/
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

## EnergyCardType

- BASIC
- SPECIAL

## TrainerSubtype

- ITEM
- SUPPORTER
- STADIUM
- ACE_SPEC

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
- SUPPORTER

## SpecialCondition

- ASLEEP
- BURNED
- CONFUSED
- PARALYZED
- POISONED

## GameActionType

- PUT_BASIC_ON_BENCH
- ATTACH_ENERGY
- EVOLVE_POKEMON
- PLAY_TRAINER
- RETREAT_ACTIVE
- DECLARE_ATTACK
- END_TURN

## Event strings (inline in GameActionResponse)

Events are plain descriptive strings returned in `GameActionResponse.events[]`. No `GameEventType` enum exists in V1.

Examples:
- "Santi attached Fire Energy to Slugma."
- "Slugma dealt 30 damage to Froakie."
- "Froakie was Knocked Out."
- "Santi took 1 Prize card."

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