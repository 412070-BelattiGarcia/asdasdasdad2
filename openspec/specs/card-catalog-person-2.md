# AI Proposal Spec: card-catalog-person-2

## Spec name

card-catalog-person-2

## Purpose

Define the work for the deck management role within the card catalog and deck module, focusing on deck validation, deck CRUD, and the engine deck-loading adapter.

This change must follow the division defined in `/divisionCatalogo.md` and respect the rules in `/openspec/config.yaml`.

## Mandatory context files

OpenSpec MUST read and obey:

- `/openspec/config.yaml`
- `/docs/contracts_ai/00-contract-index.md`
- `/docs/contracts_ai/01-project-scope-contract.md`
- `/docs/contracts_ai/02-project-structure-contract.md`
- `/docs/contracts_ai/04-card-model-contract.md`
- `/docs/contracts_ai/05-deck-contract.md`
- `/docs/contracts_ai/06-game-state-contract.md`
- `/docs/contracts_ai/07-setup-flow-contract.md`
- `/docs/contracts_ai/08-game-action-contract.md`
- `/docs/contracts_ai/09-rule-validation-contract.md`
- `/docs/contracts_ai/10-attack-pipeline-contract.md`
- `/docs/contracts_ai/11-status-effects-contract.md`
- `/docs/contracts_ai/13-rest-api-contract.md`
- `/docs/contracts_ai/14-websocket-contract.md`
- `/docs/contracts_ai/15-frontend-state-contract.md`
- `/docs/contracts_ai/16-test-scenarios-contract.md`
- `/divisionCatalogo.md`
- `/openspec/specs/engine-persona1-contracts-and-gameengine.md`

## Architecture constraints

- Backend is the source of truth for deck validation and deck loading.
- Frontend only consumes server state and does not enforce deck rules.
- `DeckLoadAdapter` must stay isolated from Spring/JPA details except for repository access through the adapter implementation.
- No postponed features should be implemented: auth, JWT, ranking, chat, animations, multiple expansions, Mega Evolution.

## Package root

```
ar.edu.utn.frc.tup.piii
```

## Scope — classes to implement or verify

All live under `BE/src/main/java/ar/edu/utn/frc/tup/piii/`.

### services/decks/

| Class | Current State | Action |
|-------|--------------|--------|
| `DeckService` | Existing stub | Complete |
| `DeckValidator` | Existing stub | Complete |
| `SeedDeckService` | Existing stub | Complete only for dev/testing |

### controllers/decks/

| Class | Current State | Action |
|-------|--------------|--------|
| `DeckController` | Existing stub | Complete |

### mappers/

| Class | Current State | Action |
|-------|--------------|--------|
| `DeckMapper` | Does not exist | Create |

### engine/ports/impl/

| Class | Current State | Action |
|-------|--------------|--------|
| `DeckLoadAdapter` | Does not exist | Create |

## Requirements

### Requirement 1 — Deck validation must be reusable

The system MUST validate decks with the same logic from REST and from the engine.

The shared logic MUST live in `services/decks/DeckValidator` and be consumed by both `DeckService` and `DeckLoadAdapter`.

#### Scenarios

- Given a deck with exactly 60 cards, at most 4 copies per `cardId`, and at least one Basic Pokémon, When validation runs, Then the result must be valid.
- Given a deck with fewer than 60 cards, When validation runs, Then the result must indicate the deck is invalid.
- Given a deck with more than 4 copies of the same card, When validation runs, Then the result must indicate a duplicate-copy violation.

### Requirement 2 — DeckService must manage deck CRUD

The system MUST expose deck creation, retrieval, update, deletion, and player deck listing logic.

#### Scenarios

- Given a valid `CreateDeckRequest`, When `createDeck` executes, Then the deck must be persisted.
- Given an existing `deckId`, When `getDeck` executes, Then it must return the corresponding deck.
- Given an invalid `UpdateDeckRequest`, When `updateDeck` executes, Then it must reject the operation.
- Given a `playerId`, When `listDecksByPlayer` executes, Then it must return only that player's decks.

### Requirement 3 — DeckController must expose deck endpoints

The system MUST expose REST endpoints for deck CRUD and validation.

#### Scenarios

- Given a `POST /api/decks` request, When the payload is valid, Then the controller must create the deck.
- Given a `GET /api/decks/{id}` request, When the deck exists, Then the controller must return its details.
- Given a `POST /api/decks/{id}/validate` request, When the deck exists, Then the controller must return validation results.

### Requirement 4 — DeckLoadAdapter must load decks for the engine

The system MUST implement `DeckLoadPort.loadDeck(UUID deckId)` through `DeckLoadAdapter`.

#### Scenarios

- Given an existing `deckId`, When the engine requests the deck, Then the adapter must return the `Deck` domain object.
- Given an invalid deck persisted by mistake, When `DeckLoadAdapter` loads it, Then it must fail with a controlled validation error and not hand it to the engine.
- Given a missing `deckId`, When `loadDeck` executes, Then the adapter must fail with a controlled load error.

### Requirement 5 — SeedDeckService is optional for dev/testing

The system SHOULD allow seed decks only for development or testing.

#### Scenarios

- Given the `dev` profile is active, When the application starts, Then `SeedDeckService` may preload decks.
- Given a non-dev profile, When the application starts, Then seed loading must not run.

## Explicit non-goals

Do not implement in this change:

- External card catalog logic.
- Synchronization with the Pokémon TCG API.
- `CardLookupAdapter`.
- Engine, `GameEngine`, or action handlers.
- Authentication, JWT, ranking, chat, or animations.
- New expansions or rules outside the MVP.

## Verification requirements

The change MUST end with:

1. `mvn compile` inside `BE/` without errors.
2. `mvn test` inside `BE/` without failures.
3. Verification that `DeckLoadAdapter` implements `DeckLoadPort` and keeps deck loading decoupled from the internal engine.

## Implementation targets

The implementation generated from this spec should cover:

- `services/decks/DeckService`
- `services/decks/DeckValidator`
- `services/decks/SeedDeckService`
- `controllers/decks/DeckController`
- `mappers/DeckMapper`
- `engine/ports/impl/DeckLoadAdapter`
