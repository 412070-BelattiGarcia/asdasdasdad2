# WebSocket Contract

## Goal

Define WebSocket topics and payloads.

The TPI requires real-time bidirectional communication, state sync after valid actions, event notifications and reconnection support.

## Backend location

```
websocket/MatchWebSocketController.java
websocket/MatchWebSocketPublisher.java
```

No `EventPublisherPort` exists in V1. WebSocket publishing is called directly from `MatchApplicationService` or `GameEngine.applyAction()` via the `MatchWebSocketPublisher` Spring component.

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
        "cardDefinitionId": "xy1-10",
        "name": "Slugma"
      }
    ]
  }
}
```

## Event messages

WebSocket events carry the same `events: string[]` as the REST response. No typed GameEvent objects.
```json
{
  "type": "STATE_UPDATED",
  "matchId": "9a747f90-b50e-49df-9d8a-456c9796aa11",
  "events": ["Slugma dealt 20 damage to Froakie."],
  "publicState": {},
  "privateState": {}
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