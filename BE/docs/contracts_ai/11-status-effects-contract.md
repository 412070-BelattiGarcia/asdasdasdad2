# Status Effects Contract

## Goal

Define special condition rules.

The TPI requires all five special conditions, their incompatibilities, and between-turn processing order.

## Backend location

```
engine/status/StatusEffectManager.java
engine/model/PokemonInPlay.java
```

## Frontend location

```
features/match/components/active-pokemon-slot/
features/match/components/bench-zone/
shared/models/game-state.models.ts
```

## Special conditions

- ASLEEP
- BURNED
- CONFUSED
- PARALYZED
- POISONED

## Exclusive rotation conditions

Only one of these may be active at the same time:
- ASLEEP
- CONFUSED
- PARALYZED

The newest one replaces the previous one.

## Marker conditions

These may coexist:
- BURNED
- POISONED

A Pokémon may be BURNED + POISONED + PARALYZED at the same time.

## Between-turns order

Fixed order:
1. POISONED
2. BURNED
3. ASLEEP
4. PARALYZED
5. Abilities / other between-turn effects
6. Knockout check

## Removal

All special conditions are removed when the Pokémon:
- retreats to Bench
- evolves

## Apply condition event

```json
{
  "type": "SPECIAL_CONDITION_APPLIED",
  "matchId": "9a747f90-b50e-49df-9d8a-456c9796aa11",
  "turnNumber": 3,
  "payload": {
    "pokemonInstanceId": "card-instance-300",
    "condition": "POISONED",
    "currentConditions": ["POISONED"]
  }
}
```

## Poison between-turn event

```json
{
  "type": "DAMAGE_APPLIED",
  "matchId": "9a747f90-b50e-49df-9d8a-456c9796aa11",
  "turnNumber": 3,
  "payload": {
    "source": "POISONED",
    "targetPokemonInstanceId": "card-instance-300",
    "damageCountersAdded": 1,
    "defenderTotalDamageCounters": 7
  }
}
```

## Burn between-turn event

```json
{
  "type": "DAMAGE_APPLIED",
  "matchId": "9a747f90-b50e-49df-9d8a-456c9796aa11",
  "turnNumber": 3,
  "payload": {
    "source": "BURNED",
    "coinFlip": "TAILS",
    "targetPokemonInstanceId": "card-instance-300",
    "damageCountersAdded": 2,
    "defenderTotalDamageCounters": 8
  }
}
```

## Asleep between-turn event

```json
{
  "type": "SPECIAL_CONDITION_REMOVED",
  "matchId": "9a747f90-b50e-49df-9d8a-456c9796aa11",
  "payload": {
    "pokemonInstanceId": "card-instance-300",
    "condition": "ASLEEP",
    "reason": "COIN_FLIP_HEADS"
  }
}
```

## Paralysis removal event

```json
{
  "type": "SPECIAL_CONDITION_REMOVED",
  "matchId": "9a747f90-b50e-49df-9d8a-456c9796aa11",
  "payload": {
    "pokemonInstanceId": "card-instance-300",
    "condition": "PARALYZED",
    "reason": "END_OF_OWNER_TURN"
  }
}
```

## Frontend display

Frontend may display:
- ASLEEP: rotate card left
- CONFUSED: rotate card upside down
- PARALYZED: rotate card right
- BURNED: marker
- POISONED: marker

Frontend display must not affect rules.