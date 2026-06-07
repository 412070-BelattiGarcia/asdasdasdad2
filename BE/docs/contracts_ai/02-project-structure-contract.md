# Project Structure Contract

## Rule

This file defines the canonical folder and package structure.

OpenCode must not create alternative package names, duplicated DTO folders, or parallel architectures.

## Backend root package

```
ar.edu.utn.frc.tup.piii
```

## Backend structure

```
backend/
src/main/java/ar/edu/utn/frc/tup/piii/
  Application.java
  advice/
    GlobalExceptionHandler.java
  cards/
    domain/
      CardDefinition.java
      CardSupertype.java
      EnergyCardDefinition.java
      EnergyType.java
      PokemonCardDefinition.java
      PokemonStage.java
      TrainerCardDefinition.java
      TrainerType.java
  clients/
    PokemonTcgApiClient.java
  configs/
    GameEngineConfig.java
    MappersConfig.java
    SpringDocConfig.java
    WebSocketConfig.java
  controllers/
    PingController.java
    cards/
      CardController.java
    decks/
      DeckController.java
    matches/
      GameActionController.java
      MatchController.java
  decks/
    domain/
      Deck.java
      DeckCard.java
      DeckValidationError.java
      DeckValidationResult.java
  dtos/
    cards/
      CardDetailResponse.java
      CardSearchRequest.java
      CardSummaryResponse.java
    common/
      ErrorApi.java
    decks/
      CreateDeckRequest.java
      DeckCardResponse.java
      DeckResponse.java
      DeckValidationResponse.java
      UpdateDeckRequest.java
    matches/
      CreateMatchRequest.java
      GameActionRequest.java
      GameActionResponse.java
      JoinMatchRequest.java
      MatchResponse.java
      MatchStateResponse.java
  engine/
    EngineContext.java
    ErrorCode.java
    GameEngine.java
    PlayerSide.java
    SpecialCondition.java
    action/
      ActionResult.java
      GameAction.java
      GameActionPayload.java
      GameActionType.java
      GameError.java
      AttachEnergyPayload.java
      DeclareAttackPayload.java
      PutBasicOnBenchPayload.java
      EvolvePokemonPayload.java
      PlayTrainerPayload.java
      RetreatPayload.java
    attack/
      AttackResolver.java
    handlers/
      ActionHandler.java
      PutBasicOnBenchHandler.java
      AttachEnergyHandler.java
      EvolvePokemonHandler.java
      PlayTrainerHandler.java
      DeclareAttackHandler.java
      RetreatActiveHandler.java
      EndTurnHandler.java
      ChooseNewActiveAfterKnockoutHandler.java
      TakePrizeCardHandler.java
    model/
      CardInstance.java
      GameMetadata.java
      GamePhase.java
      GameState.java
      PlayerState.java
      PokemonInPlay.java
      TurnFlags.java
    ports/
      CardLookupPort.java
      DeckLoadPort.java
      RandomizerPort.java
      StatePersisterPort.java
      impl/
        CardLookupAdapter.java
        DeckLoadAdapter.java
        RandomizerAdapter.java
        StatePersisterAdapter.java
    rules/
      RuleValidator.java
    setup/
      SetupManager.java
    turn/
      TurnManager.java
      TurnPhase.java
    victory/
      FinishReason.java
      VictoryConditionChecker.java
  exceptions/
    DomainException.java
    NotFoundException.java
    ValidationException.java
  mappers/
    cards/
      CardMapper.java
    decks/
      DeckMapper.java
    matches/
      MatchMapper.java
  matches/
    domain/
      Match.java
      MatchStatus.java
  persistence/
    GameStateConverter.java
    MatchEntity.java
    MatchRepository.java
  repositories/
    entities/
      CardAttackEntity.java
      CardEntity.java
      CardResistanceEntity.java
      CardWeaknessEntity.java
      DeckCardEntity.java
      DeckEntity.java
      PlayerEntity.java
      MatchPlayerEntity.java
      UserEntity.java
    jpa/
      CardAttackJpaRepository.java
      CardJpaRepository.java
      CardResistanceJpaRepository.java
      CardWeaknessJpaRepository.java
      DeckCardJpaRepository.java
      DeckJpaRepository.java
      PlayerJpaRepository.java
      MatchPlayerJpaRepository.java
      UserJpaRepository.java
  services/
    cards/
      CardCacheSyncService.java
      CardCatalogService.java
    decks/
      DeckService.java
      DeckValidator.java
      SeedDeckService.java
    matches/
      MatchApplicationService.java
      MatchQueryService.java
  websocket/
    MatchWebSocketController.java
    MatchWebSocketPublisher.java
```

## Frontend structure

```
frontend/
src/app/
  app.config.ts
  app.css
  app.html
  app.routes.ts
  app.spec.ts
  app.ts
  core/
    api/
      api-client.service.ts
      card-api.service.ts
      deck-api.service.ts
      match-api.service.ts
    websocket/
      match-socket.service.ts
  shared/
    models/
      api-error.models.ts
      card.models.ts
      deck.models.ts
      game-action.models.ts
      game-state.models.ts
  features/
    cards/
      routes.ts
      pages/
        card-catalog-page/
          card-catalog-page.ts
    decks/
      routes.ts
      pages/
        deck-list-page/
          deck-list-page.ts
        deck-builder-page/
          deck-builder-page.ts
      services/
        deck-builder-facade.service.ts
    lobby/
      routes.ts
      pages/
        lobby-page/
          lobby-page.ts
    match/
      routes.ts
      pages/
        match-page/
          match-page.ts
      services/
        game-action-dispatcher.service.ts
        match-facade.service.ts
```

## Dependency rules

### Backend

- controllers may depend on services and dtos
- controllers may depend on engine for action types and enums
- services may depend on domain, engine, repositories, mappers, clients, and websocket
- services may depend on dtos for request/response mapping
- repositories may depend on database/JPA/external APIs
- domain (cards/domain, decks/domain, matches/domain) must not depend on:
  - Spring annotations
  - JPA entities
  - REST controllers
  - WebSocket classes
  - repositories
  - database classes
- engine must not depend on:
  - Spring annotations
  - JPA entities
  - REST controllers
  - WebSocket classes
  - repositories
  - database classes
- engine ports/impl may depend on engine ports and infrastructure
- advice depends on exceptions and dtos
- mappers depend on domain and dtos
- websocket depends on engine for event types

### Forbidden in domain and engine

Do not use:
- @RestController
- @Service
- @Repository
- @Entity
- @Autowired

The engine and domain packages must be Java-oriented, testable and isolated.

## Frontend rule

Frontend never decides game rules.

Frontend can:
- render state
- send GameActionRequest
- show available buttons
- display errors
- subscribe to WebSocket events
- lazy load feature routes

Frontend cannot:
- calculate official damage
- decide victory
- mutate match state locally
- reveal opponent hidden data
