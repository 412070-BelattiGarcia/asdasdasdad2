# Frontend State Contract

## Goal

Define frontend responsibilities.

Frontend is presentation and interaction only.

## Frontend location

```
features/match/
shared/models/
core/api/
core/websocket/
```

## Frontend may store

- publicState: MatchPublicView
- privateState: PlayerPrivateView
- events: string[]
- connectionStatus: "CONNECTED" | "DISCONNECTED" | "RECONNECTING"
- selectedCardInstanceId: string | null
- selectedTargetInstanceId: string | null
- lastError: ApiError | null

## Frontend must not store as truth

- calculated damage
- victory decision
- legal move decision
- opponent hidden card identities
- deck order

## Match page model example

```json
{
  "publicState": {
    "matchId": "9a747f90-b50e-49df-9d8a-456c9796aa11",
    "status": "ACTIVE",
    "phase": "MAIN",
    "currentPlayerId": "player-1"
  },
  "privateState": {
    "playerId": "player-1",
    "hand": []
  },
  "events": [],
  "connectionStatus": "CONNECTED",
  "selectedCardInstanceId": null,
  "selectedTargetInstanceId": null,
  "lastError": null
}
```

## Action panel rules

Buttons are enabled based on server state.

Frontend may use simple checks for UX, but backend remains authoritative.

Example: Attach Energy button may be disabled if:
- not player's turn
- phase is not MAIN
- hasAttachedEnergy is true

Even if frontend enables a wrong button, backend must reject invalid action.

## GameAction dispatch example

`dispatchAttachEnergy(handIndex: number, targetPokemonInstanceId: string): void`

must send:
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

## Required match UI components

MVP:
- match-page
- board
- player-area
- opponent-area
- active-pokemon-slot
- bench-zone
- hand-zone
- action-panel
- game-log

Later:
- prize-zone
- discard-zone
- deck-zone
- stadium-zone
- drag-drop-targets

## Visual privacy

Opponent hand is exposed only as a count in the public state view. Never render opponent hand card names unless a specific rule reveals them.

```json
{
  "handCount": 6
}
```

Never render opponent hand card names unless a specific rule reveals them.