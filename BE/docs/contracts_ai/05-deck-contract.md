# Deck Contract

## Goal

Define deck structure, validation and seed deck behavior.

## Backend location

```
decks/
cards/
```

## Frontend location

```
features/decks/
shared/models/deck.models.ts
```

## Deck rules

A valid deck must have:
- exactly 60 cards
- maximum 4 cards with same card name
- Basic Energy cards are exempt from the 4-copy limit
- maximum 1 ACE_SPEC card total
- at least 1 Basic Pokémon

Deck Builder must use set xy1 for the required base version.

## DeckResponse

```json
{
  "id": "deck-fire-seed",
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
    },
    {
      "cardId": "energy-fire-basic",
      "name": "Fire Energy",
      "quantity": 18,
      "supertype": "ENERGY",
      "isBasicEnergy": true
    }
  ],
  "validation": {
    "valid": true,
    "errors": []
  }
}
```

## DeckValidationResponse

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

## Seed deck types

The MVP requires at least two seed decks:
- Fire-type seed deck
- Water-type seed deck

Each seed deck must be pre-validated and include:
- Basic Pokémon (at least 1)
- Stage 1 Pokémon (optional)
- Energy cards
- Trainer cards (optional for MVP)

## Deck entity fields

```
id: UUID
name: string
ownerPlayerId: UUID | null
source: DECK_SOURCE (SEED | CUSTOM)
createdAt: Instant
updatedAt: Instant
```

## Deck card entity fields

```
id: UUID
deckId: UUID
cardId: string
quantity: int
```