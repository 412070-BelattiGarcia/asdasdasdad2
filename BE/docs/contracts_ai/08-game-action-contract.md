# Game Action Contract

## Goal

Define the canonical action input/output format.

All game mutations must enter through:

```
GameEngine.applyAction(matchId, playerId, action)
```

## Backend location

```
engine/action/
matches/api/GameActionController.java
```

## Frontend location

```
shared/models/game-action.models.ts
features/match/services/game-action-dispatcher.service.ts
```

## GameActionRequest

```json
{
  "type": "ATTACH_ENERGY",
  "playerId": "player-1",
  "payload": {
    "energyCardInstanceId": "card-instance-502",
    "targetPokemonInstanceId": "card-instance-100"
  },
  "clientRequestId": "client-req-001"
}
```

## GameActionResponse success

```json
{
  "success": true,
  "clientRequestId": "client-req-001",
  "publicState": {},
  "privateState": {},
  "events": [
    {
      "type": "ENERGY_ATTACHED",
      "message": "Santi attached Fire Energy to Slugma."
    }
  ],
  "error": null
}
```

## GameActionResponse error

```json
{
  "success": false,
  "clientRequestId": "client-req-001",
  "publicState": null,
  "privateState": null,
  "events": [],
  "error": {
    "code": "ENERGY_ALREADY_ATTACHED",
    "message": "No puedes unir más de 1 Energía por turno.",
    "details": {
      "phase": "MAIN",
      "hasAttachedEnergy": true
    }
  }
}
```

## Action: PUT_BASIC_ON_BENCH

```json
{
  "type": "PUT_BASIC_ON_BENCH",
  "playerId": "player-1",
  "payload": {
    "cardInstanceId": "card-instance-501"
  },
  "clientRequestId": "client-req-002"
}
```

## Action: ATTACH_ENERGY

```json
{
  "type": "ATTACH_ENERGY",
  "playerId": "player-1",
  "payload": {
    "energyCardInstanceId": "card-instance-502",
    "targetPokemonInstanceId": "card-instance-100"
  },
  "clientRequestId": "client-req-003"
}
```

## Action: DECLARE_ATTACK

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
  "clientRequestId": "client-req-004"
}
```

## Action: RETREAT_ACTIVE

```json
{
  "type": "RETREAT_ACTIVE",
  "playerId": "player-1",
  "payload": {
    "newActivePokemonInstanceId": "card-instance-110",
    "energyCardInstanceIdsToDiscard": [
      "card-instance-201"
    ]
  },
  "clientRequestId": "client-req-005"
}
```

## Action: CHOOSE_KNOCKOUT_REPLACEMENT

```json
{
  "type": "CHOOSE_KNOCKOUT_REPLACEMENT",
  "playerId": "player-2",
  "payload": {
    "newActivePokemonInstanceId": "card-instance-330"
  },
  "clientRequestId": "client-req-006"
}
```

## Action rules

- playerId must match the authenticated/guest session.
- Only the current player can act unless resolving a pending decision.
- Every valid action must:
  - validate rules
  - mutate state
  - persist state
  - append immutable log entry
  - publish WebSocket events