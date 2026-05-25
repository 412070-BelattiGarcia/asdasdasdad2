# Setup Flow Contract

## Goal

Define match creation and initial setup.

## Backend location

```
engine/setup/
engine/GameEngine.java
matches/application/
```

## Frontend location

```
features/lobby/
features/match/
```

## Match creation flow

1. Player 1 creates a match
2. Match status is WAITING
3. Player 2 joins
4. Match status changes to SETUP
5. Both decks are loaded from seed or selected decks
6. Backend shuffles both decks
7. Each player draws 7 cards
8. Mulligan is resolved
9. Each player chooses Active Pokémon
10. Each player may put up to 5 Basic Pokémon on Bench
11. Backend creates 6 Prize cards per player
12. Coin flip chooses first player
13. Both sides reveal Active/Bench
14. Match status changes to ACTIVE
15. First turn begins

The TPI requires initial 7-card hands, mulligan, Active/Bench setup, six Prize cards and first-player coin flip.

## MVP simplification

For first playable MVP, setup may be automatic:
- choose first Basic Pokémon in hand as Active
- put up to 5 additional Basic Pokémon on Bench
- create 6 Prize cards
- randomly choose first player

Manual setup can be added later.

## CreateMatchRequest

```json
{
  "playerName": "Santi",
  "deckId": "seed-fire-deck"
}
```

## CreateMatchResponse

```json
{
  "matchId": "9a747f90-b50e-49df-9d8a-456c9796aa11",
  "playerId": "player-1",
  "status": "WAITING",
  "side": "PLAYER_ONE"
}
```

## JoinMatchRequest

```json
{
  "playerName": "Lucas",
  "deckId": "seed-water-deck"
}
```

## JoinMatchResponse

```json
{
  "matchId": "9a747f90-b50e-49df-9d8a-456c9796aa11",
  "playerId": "player-2",
  "status": "SETUP",
  "side": "PLAYER_TWO"
}
```

## Setup completed event

```json
{
  "type": "SETUP_COMPLETED",
  "matchId": "9a747f90-b50e-49df-9d8a-456c9796aa11",
  "turnNumber": 1,
  "currentPlayerId": "player-1",
  "publicState": {
    "status": "ACTIVE",
    "phase": "DRAW"
  }
}
```

## Mulligan event

```json
{
  "type": "MULLIGAN_DECLARED",
  "matchId": "9a747f90-b50e-49df-9d8a-456c9796aa11",
  "playerId": "player-2",
  "mulliganCount": 1,
  "opponentMayDrawCards": 1
}
```

## Setup invariants

- Each player must have exactly 1 Active Pokémon before ACTIVE status
- Bench size must be between 0 and 5
- Prize count must be 6 unless sudden death
- Hand, deck, prize and discard zones must contain unique card instance IDs