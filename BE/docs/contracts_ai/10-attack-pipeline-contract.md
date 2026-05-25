# Attack Pipeline Contract

## Goal

Define the exact attack resolution order.

The TPI explicitly evaluates attack sequence, damage calculation, weakness, resistance, modifiers, knockout and prize taking.

## Backend location

```
engine/attack/
engine/status/
engine/victory/
```

## Frontend location

```
features/match/components/action-panel/
features/match/components/game-log/
```

## Attack order

The attack resolution order is fixed:

1. Announce attack
2. Validate Energy requirement
3. If attacker is CONFUSED, flip coin
4. Resolve required selections
5. Resolve attack prerequisites
6. Apply effects that modify or cancel the attack
7. Calculate damage:
   - base damage
   - attacker modifiers
   - defender weakness
   - defender resistance
   - defender modifiers
   - minimum 0
8. Place damage counters
9. Apply post-damage effects:
   - special conditions
   - energy discards
   - bench damage
   - healing
10. Check knockouts
11. Take Prize cards
12. Ask for Active replacement if required
13. Check victory
14. End turn

## Damage formula for MVP

```
damage = baseDamage
damage += attackerModifiers
damage *= weaknessMultiplier
damage += resistanceValue
damage += defenderModifiers
damage = max(damage, 0)
damageCounters = damage / 10
```

Resistance is usually negative, for example -20.

## DeclareAttack request

```json
{
  "type": "DECLARE_ATTACK",
  "playerId": "player-1",
  "payload": {
    "attackerPokemonInstanceId": "card-instance-100",
    "attackIndex": 0,
    "targetPokemonInstanceId": "card-instance-300",
    "declaredSelections": []
  },
  "clientRequestId": "client-req-attack-001"
}
```

## DamageApplied event

```json
{
  "type": "DAMAGE_APPLIED",
  "matchId": "9a747f90-b50e-49df-9d8a-456c9796aa11",
  "turnNumber": 3,
  "sourcePlayerId": "player-1",
  "targetPlayerId": "player-2",
  "payload": {
    "attackerPokemonInstanceId": "card-instance-100",
    "defenderPokemonInstanceId": "card-instance-300",
    "attackName": "Poison Powder",
    "baseDamage": 60,
    "weaknessApplied": false,
    "resistanceApplied": false,
    "finalDamage": 60,
    "damageCountersAdded": 6,
    "defenderTotalDamageCounters": 6
  }
}
```

## Knockout event

```json
{
  "type": "KNOCKOUT_OCCURRED",
  "matchId": "9a747f90-b50e-49df-9d8a-456c9796aa11",
  "turnNumber": 3,
  "payload": {
    "knockedOutPokemonInstanceId": "card-instance-300",
    "ownerPlayerId": "player-2",
    "prizesToTake": 1,
    "discardedCardInstanceIds": [
      "card-instance-300",
      "card-instance-331"
    ]
  }
}
```

## PrizeTaken event

```json
{
  "type": "PRIZE_TAKEN",
  "matchId": "9a747f90-b50e-49df-9d8a-456c9796aa11",
  "turnNumber": 3,
  "payload": {
    "playerId": "player-1",
    "prizeCountTaken": 1,
    "remainingPrizeCount": 5
  }
}
```

## Confusion failed attack event

```json
{
  "type": "ATTACK_DECLARED",
  "matchId": "9a747f90-b50e-49df-9d8a-456c9796aa11",
  "payload": {
    "attackName": "Tackle",
    "confusionCheck": "TAILS",
    "attackFailed": true,
    "selfDamageCountersAdded": 3
  }
}
```

## MVP supported attack types

MVP may support only:
- fixed damage
- fixed damage + poison
- fixed damage + burn
- fixed damage + asleep
- fixed damage + paralysis
- fixed damage + confusion
- no-damage status attack

Unsupported complex attacks must be marked as UNIMPLEMENTED_EFFECT.