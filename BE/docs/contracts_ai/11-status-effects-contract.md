# Status Effects Contract

## Goal

Define special condition rules.

The TPI requires all five special conditions, their incompatibilities, and between-turn processing order.

## Backend location

```
engine/model/PokemonInPlay.java (specialConditions field)
engine/attack/AttackResolver.java (status logic as private helper methods)
```

No separate `StatusEffectManager` class exists in V1. Between-turn status processing (poison, burn, sleep, paralysis) is handled by private methods within `AttackResolver`, invoked by `TurnManager` during the `BETWEEN_TURNS` phase.

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

## Event strings for status effects

Events are returned as plain strings in `GameActionResponse.events[]`:

- `"Froakie took 10 damage from poison."`
- `"Slugma took 20 damage from burn (tails)."`
- `"Froakie woke up."`
- `"Slugma is no longer paralyzed."`

No typed event objects with structured payloads exist in V1.

## Frontend display

Frontend may display:
- ASLEEP: rotate card left
- CONFUSED: rotate card upside down
- PARALYZED: rotate card right
- BURNED: marker
- POISONED: marker

Frontend display must not affect rules.