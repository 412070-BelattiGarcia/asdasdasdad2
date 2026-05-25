# Rule Validation Contract

## Goal

Define validation behavior for game actions.

All official game rules must be validated in the backend. Frontend validation is only UX.

## Backend location

```
engine/rules/RuleValidator.java
engine/action/GameError.java
```

## Frontend location

```
features/match/components/action-panel/
shared/models/api-error.models.ts
```

## Validation output

```json
{
  "valid": false,
  "error": {
    "code": "INSUFFICIENT_ENERGY",
    "message": "No puedes atacar: te falta 1 Energía de Fuego para usar este ataque.",
    "details": {
      "attackName": "Flamethrower",
      "required": ["FIRE", "COLORLESS", "COLORLESS"],
      "attached": ["FIRE"]
    }
  }
}
```

## General validations

Every action must validate:
- match exists
- match is in expected status
- player belongs to match
- it is the player's turn, unless pending decision says otherwise
- current phase allows action
- referenced card instances exist
- referenced card instances are in expected zone
- targets are valid
- hidden cards are not exposed

## Phase validations

### DRAW

Allowed:
- DRAW_CARD
- END_TURN only if draw is skipped by first-player-first-turn rule

### MAIN

Allowed:
- PUT_BASIC_ON_BENCH
- ATTACH_ENERGY
- EVOLVE_POKEMON
- PLAY_TRAINER
- RETREAT_ACTIVE
- DECLARE_ATTACK
- END_TURN

### ATTACK

Normally only internal attack resolution should happen.

### BETWEEN_TURNS

Only internal status/effects processing.

## Specific rule validations

### Attach Energy

Reject if:
- not MAIN phase
- card is not Energy
- energy card is not in player's hand
- target is not player's Pokémon in play
- hasAttachedEnergy is true

### Put Basic on Bench

Reject if:
- card is not Pokémon
- card is not BASIC
- card is not in player's hand
- bench already has 5 Pokémon

### Attack

Reject if:
- not player's turn
- not MAIN phase
- first player tries to attack on first turn
- attacker is not player's Active Pokémon
- attack index does not exist
- attacker is ASLEEP or PARALYZED
- attacker has insufficient Energy

### Retreat

Reject if:
- not MAIN phase
- hasRetreated is true
- Active Pokémon is ASLEEP or PARALYZED
- player has no Benched Pokémon
- insufficient Energy cards selected for retreat cost

## Error response format

```json
{
  "code": "NOT_YOUR_TURN",
  "message": "No es tu turno.",
  "details": {
    "currentPlayerId": "player-1",
    "requestingPlayerId": "player-2"
  }
}
```

## UX rule

Messages must be descriptive and actionable.

Avoid generic messages like "Validation error."