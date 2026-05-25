# REST API Contract

## Goal

Define REST endpoints and JSON formats.

## Backend location

```
matches/api/
cards/api/
decks/api/
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
- `GET /api/decks/seed`
- `GET /api/decks/{deckId}`
- `POST /api/decks/validate`

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
    "energyCardInstanceId": "card-instance-502",
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
  "events": [],
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

## GET /api/decks/seed

Response:
```json
{
  "items": [
    {
      "id": "seed-fire-deck",
      "name": "Seed Fire Deck",
      "valid": true,
      "totalCards": 60
    },
    {
      "id": "seed-water-deck",
      "name": "Seed Water Deck",
      "valid": true,
      "totalCards": 60
    }
  ]
}
```

## POST /api/decks/validate

Request:
```json
{
  "cards": [
    {
      "cardId": "xy1-10",
      "quantity": 4
    }
  ]
}
```

Response:
```json
{
  "valid": false,
  "errors": [
    {
      "code": "DECK_SIZE_INVALID",
      "message": "El mazo debe tener exactamente 60 cartas.",
      "details": {
        "currentSize": 4,
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