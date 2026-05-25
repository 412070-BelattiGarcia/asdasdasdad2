# Game State Contract

## Goal

Define the canonical game state.

Backend owns the complete state.

Frontend receives only sanitized state.

## Backend location

```
engine/model/
matches/infrastructure/MatchStateEntity.java
```

## Frontend location

```
shared/models/game-state.models.ts
features/match/
```

## GameState private backend model

```
matchId: UUID
status: MatchStatus
phase: TurnPhase
turnNumber: int
currentPlayerId: UUID
firstPlayerId: UUID
players: PlayerState[2]
stadiumCardInstanceId: UUID | null
turnFlags: TurnFlags
pendingDecision: PendingDecision | null
winnerPlayerId: UUID | null
finishReason: FinishReason | null
createdAt: Instant
updatedAt: Instant
```

## PlayerState private backend model

```
playerId: UUID
side: PlayerSide
deck: CardInstanceId[]
hand: CardInstanceId[]
prizes: CardInstanceId[]
discard: CardInstanceId[]
activePokemon: PokemonInPlay | null
bench: PokemonInPlay[]
mulliganCount: int
```

## PokemonInPlay

```
instanceId: UUID
cardId: string
ownerPlayerId: UUID
enteredTurnNumber: int
evolvedThisTurn: boolean
damageCounters: int
specialConditions: SpecialCondition[]
attachedCards: AttachedCard[]
toolCardInstanceId: UUID | null
```

## AttachedCard

```
instanceId: UUID
cardId: string
attachedAs: "ENERGY" | "TOOL" | "EVOLUTION"
```

## TurnFlags

```
hasDrawnForTurn: boolean
hasAttachedEnergy: boolean
hasRetreated: boolean
hasPlayedSupporter: boolean
hasPlayedStadium: boolean
hasAttacked: boolean
```

## PendingDecision

```
type: DECISION_TYPE
requestingPlayerId: UUID
choices: CHOICE[]
timeoutSeconds: int
```

## PublicGameState

```json
{
  "matchId": "uuid",
  "status": "ACTIVE",
  "phase": "MAIN",
  "turnNumber": 3,
  "currentPlayerId": "player-1",
  "firstPlayerId": "player-1",
  "players": [
    {
      "playerId": "player-1",
      "side": "PLAYER_ONE",
      "prizes": ["ci-20", "ci-21", "ci-22", "ci-23", "ci-24", "ci-25"],
      "activePokemon": {
        "instanceId": "ci-30",
        "cardId": "xy1-10",
        "damageCounters": 20,
        "specialConditions": [],
        "attachedCards": ["ci-40"]
      },
      "bench": []
    },
    {
      "playerId": "player-2",
      "side": "PLAYER_TWO",
      "prizes": ["ci-50", "ci-51", "ci-52", "ci-53", "ci-54", "ci-55"],
      "activePokemon": {
        "instanceId": "ci-60",
        "cardId": "xy1-7",
        "damageCounters": 0,
        "specialConditions": ["POISONED"],
        "attachedCards": []
      },
      "bench": []
    }
  ]
}
```

## PrivatePlayerState

```json
{
  "playerId": "player-1",
  "hand": [
    {
      "instanceId": "ci-100",
      "cardId": "xy1-10",
      "name": "Slugma",
      "supertype": "POKEMON"
    }
  ],
  "deckCount": 35,
  "discardCount": 3,
  "prizes": [
    {
      "slot": 0,
      "known": false,
      "card": null
    }
  ]
}
```

## Privacy rules

Never send to the opponent:
- hand card identities
- deck card identities
- deck order
- prize card identities
- unrevealed setup Pokémon before reveal

The opponent may receive only counts and public board information.