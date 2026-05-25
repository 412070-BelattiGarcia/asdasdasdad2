# WebSocket Contract

## Goal

Define WebSocket topics and payloads.

The TPI requires real-time bidirectional communication, state sync after valid actions, event notifications and reconnection support.

## Backend location

```
matches/websocket/
engine/ports/EventPublisherPort.java
```

## Frontend location

```
core/websocket/match-socket.service.ts
features/match/services/match-facade.service.ts
```

## Topics

Public match events:
- `/topic/matches/{matchId}/events`

Private player state:
- `/user/queue/matches/{matchId}/private-state`

Optional client action destination:
- `/app/matches/{matchId}/actions`

For MVP, REST actions plus WebSocket updates are enough.

## Public GameEvent payload

```json
{
  "type": "DAMAGE_APPLIED",
  "matchId": "9a747f90-b50e-49df-9d8a-456c9796aa11",
  "turnNumber": 3,
  "createdAt": "2026-05-06T15:30:00Z",
  "message": "Slugma dealt 20 damage to Froakie.",
  "payload": {
    "attackerPokemonInstanceId": "card-instance-100",
    "defenderPokemonInstanceId": "card-instance-300",
    "finalDamage": 20,
    "damageCountersAdded": 2
  }
}
```

## Public state update event

```json
{
  "type": "STATE_UPDATED",
  "matchId": "9a747f90-b50e-49df-9d8a-456c9796aa11",
  "publicState": {}
}
```

## Private state update event

```json
{
  "matchId": "9a747f90-b50e-49df-9d8a-456c9796aa11",
  "playerId": "player-1",
  "privateState": {
    "hand": [
      {
        "instanceId": "card-instance-501",
        "cardId": "xy1-10",
        "name": "Slugma"
      }
    ]
  }
}
```

## Reconnection flow

When frontend reconnects:

1. Reconnect WebSocket
2. Call: `GET /api/matches/{matchId}/state?playerId={playerId}`
3. Replace local state with server state
4. Continue listening to events

## Privacy rules

Public WebSocket events must not include:
- opponent hand identities
- deck order
- unrevealed prize identities
- private selections

Private messages must be sent only to the owning player.