# REST API Contract

## Goal

Define REST endpoints and JSON formats.

## Backend location

```
controllers/matches/
controllers/cards/
controllers/decks/
```

## Frontend location

```
core/api/
```

## General error format

```json
{
  "timestamp": "2026-05-06T15:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "code": "INSUFFICIENT_ENERGY",
  "message": "No puedes atacar: te falta 1 Energía para usar este ataque.",
  "path": "/api/matches/9a747f90-b50e-49df-9d8a-456c9796aa11/actions",
  "details": {
    "required": ["FIRE", "COLORLESS"],
    "attached": ["FIRE"]
  }
}
```

## MVP endpoints

- `POST /api/matches`
- `POST /api/matches/{matchId}/join`
- `GET /api/matches/{matchId}/state?playerId={playerId}`
- `POST /api/matches/{matchId}/actions`
- `GET /api/cards`
- `GET /api/cards/{cardId}`
- `POST /api/cards/sync`
- `POST /api/decks`
- `GET /api/decks/{deckId}`
- `PUT /api/decks/{deckId}`
- `DELETE /api/decks/{deckId}`
- `GET /api/decks?playerId={id}`
- `POST /api/decks/{deckId}/validate`

## POST /api/matches

Request:
```json
{
  "playerName": "Santi",
  "deckId": "seed-fire-deck"
}
```

Response:
```json
{
  "matchId": "9a747f90-b50e-49df-9d8a-456c9796aa11",
  "playerId": "player-1",
  "side": "PLAYER_ONE",
  "status": "WAITING"
}
```

## POST /api/matches/{matchId}/join

Request:
```json
{
  "playerName": "Lucas",
  "deckId": "seed-water-deck"
}
```

Response:
```json
{
  "matchId": "9a747f90-b50e-49df-9d8a-456c9796aa11",
  "playerId": "player-2",
  "side": "PLAYER_TWO",
  "status": "SETUP"
}
```

## GET /api/matches/{matchId}/state

Response:
```json
{
  "publicState": {},
  "privateState": {}
}
```

## POST /api/matches/{matchId}/actions

Request:
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

Response:
```json
{
  "success": true,
  "clientRequestId": "client-req-003",
  "publicState": {},
  "privateState": {},
  "events": [
    "Santi attached Fire Energy to Slugma."
  ],
  "error": null
}
```

## GET /api/cards

Request query example:
```
/api/cards?query=slugma&setCode=xy1&supertype=POKEMON&page=0&size=20
```

Response:
```json
{
  "items": [
    {
      "id": "xy1-10",
      "name": "Slugma",
      "supertype": "POKEMON",
      "setCode": "xy1",
      "number": "10",
      "imageSmallUrl": "https://example/slugma.png"
    }
  ],
  "page": 0,
  "size": 20,
  "totalItems": 1
}
```

## POST /api/cards/sync

Triggers manual card cache synchronization. No request body.

Response:
```json
{
  "success": true,
  "message": "Sync completed.",
  "newCards": 147,
  "updatedCards": 0
}
```

## GET /api/decks/{deckId}

Response:
```json
{
  "id": "seed-fire-deck",
  "name": "Seed Fire Deck",
  "ownerPlayerId": null,
  "source": "SEED",
  "totalCards": 60,
  "valid": true,
  "cards": [
    {
      "cardId": "xy1-10",
      "name": "Slugma",
      "quantity": 4,
      "supertype": "POKEMON",
      "isBasicEnergy": false
    }
  ]
}
```

## GET /api/decks?playerId={id}

Response:
```json
{
  "items": [
    {
      "id": "seed-fire-deck",
      "name": "Seed Fire Deck",
      "valid": true,
      "totalCards": 60
    }
  ]
}
```

## POST /api/decks

Request:
```json
{
  "name": "My Fire Deck",
  "playerId": "player-1",
  "cards": [
    { "cardId": "xy1-10", "quantity": 4 },
    { "cardId": "energy-fire-basic", "quantity": 18 }
  ]
}
```

Response:
```json
{
  "id": "deck-uuid",
  "name": "My Fire Deck",
  "valid": true,
  "totalCards": 60
}
```

## PUT /api/decks/{deckId}

Same body format as POST. Replaces the entire deck.

## DELETE /api/decks/{deckId}

Response: `204 No Content`

## POST /api/decks/{deckId}/validate

Response:
```json
{
  "valid": false,
  "errors": [
    {
      "code": "DECK_SIZE_INVALID",
      "message": "El mazo debe tener exactamente 60 cartas.",
      "details": {
        "currentSize": 55,
        "requiredSize": 60
      }
    }
  ]
}
```

## Forbidden MVP endpoints

Do not implement before MVP:
- `/api/auth/login`
- `/api/auth/register`
- `/api/ranking`
- `/api/chat`
