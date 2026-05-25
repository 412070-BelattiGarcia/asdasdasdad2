# Project Structure Contract

## Rule

This file defines the canonical folder and package structure.

OpenCode must not create alternative package names, duplicated DTO folders, or parallel architectures.

## Backend root package

```
com.pokemontcg
```

## Backend structure

```
backend/
src/main/java/com/pokemontcg/
  PokemonTcgApplication.java
  config/
    CorsConfig.java
    WebSocketConfig.java
    OpenApiConfig.java
  common/
    error/
      ApiErrorResponse.java
      GlobalExceptionHandler.java
      DomainException.java
      ValidationException.java
      NotFoundException.java
    ids/
      MatchId.java
      PlayerId.java
      CardId.java
      CardInstanceId.java
      DeckId.java
  cards/
    api/
      CardController.java
      dto/
        CardSummaryResponse.java
        CardDetailResponse.java
        CardSearchRequest.java
    application/
      CardCatalogService.java
      CardCacheSyncService.java
    domain/
      CardDefinition.java
      PokemonCardDefinition.java
      EnergyCardDefinition.java
      TrainerCardDefinition.java
      AttackDefinition.java
      EnergyCost.java
      WeaknessDefinition.java
      ResistanceDefinition.java
    infrastructure/
      CardEntity.java
      CardJpaRepository.java
      PokemonTcgApiClient.java
      CardMapper.java
  decks/
    api/
      DeckController.java
      dto/
        DeckResponse.java
        DeckCardResponse.java
        CreateDeckRequest.java
        UpdateDeckRequest.java
        DeckValidationResponse.java
    application/
      DeckService.java
      DeckValidator.java
      SeedDeckService.java
    domain/
      Deck.java
      DeckCard.java
      DeckValidationResult.java
      DeckValidationError.java
    infrastructure/
      DeckEntity.java
      DeckCardEntity.java
      DeckJpaRepository.java
      DeckCardJpaRepository.java
      DeckMapper.java
  matches/
    api/
      MatchController.java
      GameActionController.java
      dto/
        CreateMatchRequest.java
        JoinMatchRequest.java
        MatchResponse.java
        GameActionRequest.java
        GameActionResponse.java
    application/
      MatchApplicationService.java
      MatchQueryService.java
    domain/
      Match.java
      MatchStatus.java
    infrastructure/
      MatchEntity.java
      MatchStateEntity.java
      MatchLogEntity.java
      MatchJpaRepository.java
      MatchStateJpaRepository.java
      MatchLogJpaRepository.java
      MatchMapper.java
    websocket/
      MatchWebSocketPublisher.java
      MatchWebSocketController.java
  engine/
    GameEngine.java
    model/
      GameState.java
      PlayerState.java
      BoardState.java
      PokemonInPlay.java
      AttachedCard.java
      TurnFlags.java
      PublicGameState.java
      PrivatePlayerState.java
    action/
      GameAction.java
      GameActionType.java
      ActionResult.java
      GameError.java
    event/
      GameEvent.java
      GameEventType.java
    setup/
      SetupManager.java
      MulliganService.java
    turn/
      TurnManager.java
      TurnPhase.java
    rules/
      RuleValidator.java
    attack/
      AttackResolver.java
      AttackStep.java
      DamageCalculator.java
      EnergyRequirementValidator.java
    status/
      StatusEffectManager.java
    victory/
      VictoryConditionChecker.java
      FinishReason.java
    ports/
      CardLookupPort.java
      RandomizerPort.java
      StatePersisterPort.java
      EventPublisherPort.java
```

## Frontend structure

```
frontend/
src/app/
  core/
    api/
      api-client.service.ts
      match-api.service.ts
      card-api.service.ts
      deck-api.service.ts
    websocket/
      match-socket.service.ts
    interceptors/
    error/
  shared/
    models/
      card.models.ts
      deck.models.ts
      game-state.models.ts
      game-action.models.ts
      game-event.models.ts
      api-error.models.ts
    components/
      card-image/
      loading/
      error-message/
  features/
    lobby/
      lobby-page.component.ts
      lobby-page.component.html
      lobby-page.component.css
    match/
      pages/
        match-page.component.ts
        match-page.component.html
        match-page.component.css
      components/
        board/
        player-area/
        opponent-area/
        active-pokemon-slot/
        bench-zone/
        hand-zone/
        prize-zone/
        discard-zone/
        action-panel/
        game-log/
      services/
        match-facade.service.ts
        game-action-dispatcher.service.ts
    decks/
      pages/
        deck-list-page.component.ts
        deck-builder-page.component.ts
      components/
        card-search/
        deck-card-list/
        deck-validation-panel/
      services/
        deck-builder-facade.service.ts
    auth/
  README.md
```

## Dependency rules

### Backend

- api may depend on application
- application may depend on domain, engine, and infrastructure ports
- infrastructure may depend on database/JPA/external APIs
- engine must not depend on:
  - Spring annotations
  - JPA entities
  - REST controllers
  - WebSocket classes
  - repositories
  - database classes

### Forbidden inside engine

Do not use:
- @RestController
- @Service
- @Repository
- @Entity
- @Autowired

The engine must be Java-oriented, testable and isolated.

## Frontend rule

Frontend never decides game rules.

Frontend can:
- render state
- send GameActionRequest
- show available buttons
- display errors
- subscribe to WebSocket events

Frontend cannot:
- calculate official damage
- decide victory
- mutate match state locally
- reveal opponent hidden data