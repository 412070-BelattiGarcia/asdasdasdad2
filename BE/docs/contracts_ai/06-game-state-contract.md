# Game State Contract

## Goal

Define the canonical game state.

Backend owns the complete state.

Frontend receives only sanitized state.

## Backend location

```
engine/model/
persistence/ (serialized as JSON column in MatchEntity)
```

## Frontend location

```
shared/models/game-state.models.ts
features/match/
```

## GameState (private backend model)

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

## PlayerState (private backend model)

```
playerId: UUID
side: PlayerSide
deck: CardInstance[]
hand: CardInstance[]
prizes: CardInstance[]
discard: CardInstance[]
activePokemon: PokemonInPlay | null
bench: PokemonInPlay[]
mulliganCount: int
```

## PokemonInPlay

```
instanceId: UUID
cardDefinitionId: string
ownerPlayerId: UUID
enteredTurnNumber: int
evolvedThisTurn: boolean
damageCounters: int
specialConditions: SpecialCondition[]
attachedEnergies: CardInstance[]
toolCardInstanceId: UUID | null
```

Energies attached to a Pokémon are stored as a `List<CardInstance>` directly in `PokemonInPlay.attachedEnergies`. No separate `AttachedCard` class exists.

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

## Public view (projected from GameState)

The `GameActionResponse.publicState` is a projection — not a separate class. It exposes only:
- matchId, status, phase, turnNumber, currentPlayerId
- For each player: side, prizes (IDs only), activePokemon (instanceId, cardDefinitionId, damageCounters, specialConditions, attachedEnergyCount), bench (same projection), mulliganCount
- deckCount, handCount, discardCount for each player (counts only)

No separate `PublicGameState` or `PrivatePlayerState` classes exist. The view is built inline by `MatchQueryService` or the controller layer.

## Public view JSON example

```json
{
  "matchId": "9a747f90-b50e-49df-9d8a-456c9796aa11",
  "status": "ACTIVE",
  "phase": "MAIN",
  "turnNumber": 3,
  "currentPlayerId": "player-1",
  "players": [
    {
      "playerId": "player-1",
      "side": "PLAYER_ONE",
      "prizes": ["ci-20", "ci-21", "ci-22", "ci-23", "ci-24", "ci-25"],
      "prizesRemaining": 6,
      "activePokemon": {
        "instanceId": "ci-30",
        "cardDefinitionId": "xy1-10",
        "damageCounters": 20,
        "specialConditions": [],
        "attachedEnergyCount": 1
      },
      "bench": [],
      "deckCount": 35,
      "handCount": 6,
      "discardCount": 0,
      "mulliganCount": 0
    },
    {
      "playerId": "player-2",
      "side": "PLAYER_TWO",
      "prizes": ["ci-50", "ci-51", "ci-52", "ci-53", "ci-54", "ci-55"],
      "prizesRemaining": 6,
      "activePokemon": {
        "instanceId": "ci-60",
        "cardDefinitionId": "xy1-7",
        "damageCounters": 0,
        "specialConditions": ["POISONED"],
        "attachedEnergyCount": 0
      },
      "bench": [],
      "deckCount": 35,
      "handCount": 6,
      "discardCount": 0,
      "mulliganCount": 0
    }
  ]
}
```

## Private view (projected per player)

Returned alongside public state for the requesting player only. Contains:

```
playerId: UUID
hand: { instanceId, cardDefinitionId, name, supertype }[]
deckCount: int
discardCount: int
prizes: { slot: int, known: boolean, card: CardDetail | null }[]
```

## Privacy rules

Never send to the opponent:
- hand card identities
- deck card identities
- deck order
- prize card identities
- unrevealed setup Pokémon before reveal

The opponent may receive only counts and public board information.
