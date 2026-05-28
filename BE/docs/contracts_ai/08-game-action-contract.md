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
controllers/matches/GameActionController.java
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
    "handIndex": 2,
    "targetPokemonInstanceId": "card-instance-100"
  },
  "clientRequestId": "client-req-001"
}
```

Payload fields reference cards by `handIndex` (position in hand) rather than `cardInstanceId`, since the client knows its hand order but not the server's internal instance IDs for unrevealed cards.

## GameActionResponse success

```json
{
  "success": true,
  "clientRequestId": "client-req-001",
  "publicState": {},
  "privateState": {},
  "events": [
    "Santi attached Fire Energy to Slugma."
  ],
  "error": null
}
```

- `events` is a `List<String>` of human-readable descriptions of what happened.
- No `GameEvent` objects, no `GameEventType` enum. Events are inline plain strings.

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
    "handIndex": 0
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
    "handIndex": 2,
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
    "attackIndex": 0,
    "targetPokemonInstanceId": "card-instance-300"
  },
  "clientRequestId": "client-req-004"
}
```

The attacker is implicitly the Active Pokémon of the requesting player.

## Action: RETREAT_ACTIVE

```json
{
  "type": "RETREAT_ACTIVE",
  "playerId": "player-1",
  "payload": {
    "benchIndex": 0
  },
  "clientRequestId": "client-req-005"
}
```

- `benchIndex`: position on bench (0-4) for the Pokémon to become Active.
- Energy discard for retreat cost is automatic: the backend discards the first N attached Energies that satisfy the cost from the retreating Active Pokémon. The frontend only specifies the target bench slot.

## Action: EVOLVE_POKEMON

```json
{
  "type": "EVOLVE_POKEMON",
  "playerId": "player-1",
  "payload": {
    "handIndex": 2,
    "targetPokemonInstanceId": "card-instance-100"
  },
  "clientRequestId": "client-req-006"
}
```

## Action: PLAY_TRAINER

```json
{
  "type": "PLAY_TRAINER",
  "playerId": "player-1",
  "payload": {
    "handIndex": 4
  },
  "clientRequestId": "client-req-007"
}
```

## Action: END_TURN

```json
{
  "type": "END_TURN",
  "playerId": "player-1",
  "payload": {},
  "clientRequestId": "client-req-008"
}
```

## Action rules

- playerId must match the authenticated/guest session.
- Only the current player can act.
- Every valid action must:
  - validate rules
  - mutate state
  - persist state
  - publish WebSocket events
- Handlers return `void`; they mutate `GameState` directly via `EngineContext`.
