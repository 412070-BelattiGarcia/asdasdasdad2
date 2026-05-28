## 1. Backend structure cleanup

- [x] 1.1 Replace package root `com.pokemontcg` with `ar.edu.utn.frc.tup.piii` in the contract
- [x] 1.2 Rename main class from `PokemonTcgApplication.java` to `Application.java`
- [x] 1.3 Replace nested module structure (cards/api/, cards/application/, etc.) with flat type-based structure (controllers/, services/, dtos/, repositories/, engine/)
- [x] 1.4 Add `configs/` with actual files: `GameEngineConfig.java`, `MappersConfig.java`, `SpringDocConfig.java`, `WebSocketConfig.java`
- [x] 1.5 Replace `common/error/` with flat `exceptions/` package containing `DomainException.java`, `NotFoundException.java`, `ValidationException.java`
- [x] 1.6 Replace `common/ids/` with `common/ids/` keeping actual IDs: `CardId.java`, `CardInstanceId.java`, `DeckId.java`, `MatchId.java`, `PlayerId.java`
- [x] 1.7 Move DTOs from `cards/api/dto/`, `decks/api/dto/`, `matches/api/dto/` to flat `dtos/` by feature: `dtos/cards/`, `dtos/common/`, `dtos/decks/`, `dtos/matches/`
- [x] 1.8 Add `advice/GlobalExceptionHandler.java` and `ErrorApi.java` in `dtos/common/`
- [x] 1.9 Add `clients/PokemonTcgApiClient.java`
- [x] 1.10 Add `controllers/PingController.java`
- [x] 1.11 Add `mappers/decks/DeckMapper.java` and `mappers/matches/MatchMapper.java`
- [x] 1.12 Add full `repositories/entities/` with all actual entity files
- [x] 1.13 Add full `repositories/jpa/` with all actual JPA repositories
- [x] 1.14 Add all `services/` files grouped by feature: `cards/`, `decks/`, `matches/`
- [x] 1.15 Add `websocket/MatchWebSocketController.java` and `websocket/MatchWebSocketPublisher.java`
- [x] 1.16 Update engine structure: add `PlayerSide.java`, `SpecialCondition.java`, and `ports/impl/` with adapter implementations
- [x] 1.17 Keep and verify dependency rules (api → services → domain/engine, engine isolated from Spring/JPA)

## 2. Frontend structure cleanup

- [x] 2.1 Remove `.component` suffix from all component filenames (e.g., `card-catalog-page.ts` not `card-catalog-page.component.ts`)
- [x] 2.2 Add `routes.ts` files for each feature: `cards/routes.ts`, `decks/routes.ts`, `lobby/routes.ts`, `match/routes.ts`
- [x] 2.3 Remove `auth/` feature (does not exist)
- [x] 2.4 Remove all non-existent sub-component directories inside match: `board/`, `player-area/`, `opponent-area/`, `active-pokemon-slot/`, `bench-zone/`, `hand-zone/`, `prize-zone/`, `discard-zone/`, `action-panel/`, `game-log/`
- [x] 2.5 Remove `shared/components/` and its children `card-image/`, `loading/`, `error-message/`
- [x] 2.6 Remove `core/interceptors/` and `core/error/`
- [x] 2.7 Add actual page files: `card-catalog-page.ts`, `deck-list-page.ts`, `deck-builder-page.ts`, `lobby-page.ts`, `match-page.ts`
- [x] 2.8 Add actual service files: `deck-builder-facade.service.ts`, `match-facade.service.ts`, `game-action-dispatcher.service.ts`
- [x] 2.9 Add actual model files under `shared/models/`: `card.models.ts`, `deck.models.ts`, `game-state.models.ts`, `game-action.models.ts`, `api-error.models.ts`

## 3. Verify

- [x] 3.1 Read the final contract and confirm every listed file exists in the codebase
- [x] 3.2 Confirm every real file in both BE and FE has a corresponding entry in the contract
- [x] 3.3 Confirm dependency rules section is still accurate
- [x] 3.4 Confirm frontend rules section is still accurate
