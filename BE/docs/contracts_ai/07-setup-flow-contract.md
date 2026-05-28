# Setup Flow Contract

## Goal

Define match creation and initial setup.

## Backend location

```
engine/setup/SetupManager.java
services/matches/MatchApplicationService.java
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

## Mulligan resolution

Mulligan logic is internal to `SetupManager.setup()`. Steps:
1. After dealing 7 cards, check if player has at least one Basic Pokémon.
2. If not, reveal hand to opponent, shuffle back into deck, re-deal 7 cards.
3. Opponent may draw +1 card per mulligan declared.
4. Repeat until both players have at least one Basic Pokémon.

No separate `MulliganService` class exists. Mulligan is a private method within `SetupManager`.

The `SetupManager.setup()` method handles:
- deck load via `DeckLoadPort`
- shuffle via `RandomizerPort`
- deal 7 cards
- mulligan resolution
- both sides automatically choose first Basic Pokémon in hand as Active
- auto-fill bench with remaining Basic Pokémon (up to 5)
- create 6 Prize cards per player
- coin flip for first player
- mark setup complete → status becomes ACTIVE

## Setup invariants

- Each player must have exactly 1 Active Pokémon before ACTIVE status
- Bench size must be between 0 and 5
- Prize count must be 6 unless sudden death
- Hand, deck, prize and discard zones must contain unique CardInstance IDs
- No CHOOSE_KNOCKOUT_REPLACEMENT action exists; during setup, Active is chosen automatically