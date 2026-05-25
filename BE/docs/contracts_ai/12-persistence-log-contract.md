# Persistence and Log Contract

## Goal

Define what must be persisted after every relevant action.

The persisted state must be sufficient to reconstruct the full match.

## Backend location

```
matches/infrastructure/
engine/ports/StatePersisterPort.java
```

## State persistence rule

After every valid action, persist:
- match status
- full board state
- both hands
- both decks with order
- both discard piles
- both Prize zones
- Active and Bench Pokémon
- attached cards
- damage counters
- special conditions
- turn flags
- pending decisions
- immutable log entry

The TPI requires persistent reconstruction of the full match state, including hands, ordered decks, prizes, damage, conditions and turn flags.

## MatchStateEntity minimum fields

```
id: UUID
matchId: UUID
version: long
serializedStateJson: text/jsonb
createdAt: Instant
```

## MatchLogEntity minimum fields

```
id: UUID
matchId: UUID
turnNumber: int
playerId: UUID | null
eventType: GameEventType
actionType: GameActionType | null
result: string
payloadJson: text/jsonb
createdAt: Instant
```

## Persisted state JSON example

```json
{
  "matchId": "9a747f90-b50e-49df-9d8a-456c9796aa11",
  "version": 12,
  "status": "ACTIVE",
  "phase": "MAIN",
  "turnNumber": 3,
  "currentPlayerId": "player-1",
  "players": [
    {
      "playerId": "player-1",
      "deck": ["ci-1", "ci-2", "ci-3"],
      "hand": ["ci-10", "ci-11"],
      "prizes": ["ci-20", "ci-21", "ci-22", "ci-23", "ci-24", "ci-25"],
      "discard": [],
      "activePokemon": {
        "instanceId": "ci-30",
        "cardId": "xy1-10",
        "damageCounters": 2,
        "specialConditions": [],
        "attachedCards": ["ci-40"]
      },
      "bench": []
    }
  ],
  "turnFlags": {
    "hasDrawnForTurn": true,
    "hasAttachedEnergy": false,
    "hasRetreated": false,
    "hasPlayedSupporter": false,
    "hasPlayedStadium": false,
    "hasAttacked": false
  }
}
```

## Log entry example

```json
{
  "matchId": "9a747f90-b50e-49df-9d8a-456c9796aa11",
  "turnNumber": 3,
  "playerId": "player-1",
  "actionType": "ATTACH_ENERGY",
  "eventType": "ENERGY_ATTACHED",
  "result": "SUCCESS",
  "payload": {
    "energyCardInstanceId": "card-instance-502",
    "targetPokemonInstanceId": "card-instance-100"
  },
  "createdAt": "2026-05-06T15:30:00Z"
}
```

## Immutability rule

Log entries must never be updated or deleted by game logic.

If a correction is needed, append a new event.