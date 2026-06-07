package ar.edu.utn.frc.tup.piii.services.matches;

import ar.edu.utn.frc.tup.piii.dtos.matches.*;
import ar.edu.utn.frc.tup.piii.engine.GameEngine;
import ar.edu.utn.frc.tup.piii.engine.action.ActionResult;
import ar.edu.utn.frc.tup.piii.engine.action.GameAction;
import ar.edu.utn.frc.tup.piii.engine.action.GameActionType;
import ar.edu.utn.frc.tup.piii.engine.action.GameError;
import ar.edu.utn.frc.tup.piii.engine.event.GameEvent;
import ar.edu.utn.frc.tup.piii.engine.model.GameState;
import ar.edu.utn.frc.tup.piii.engine.model.PrivatePlayerState;
import ar.edu.utn.frc.tup.piii.engine.ports.DeckLoadPort;
import ar.edu.utn.frc.tup.piii.engine.ports.StatePersisterPort;
import ar.edu.utn.frc.tup.piii.engine.setup.SetupManager;
import ar.edu.utn.frc.tup.piii.exceptions.ConflictException;
import ar.edu.utn.frc.tup.piii.exceptions.NotFoundException;
import ar.edu.utn.frc.tup.piii.exceptions.ValidationException;
import ar.edu.utn.frc.tup.piii.mappers.matches.MatchMapper;
import ar.edu.utn.frc.tup.piii.repositories.entities.MatchEntity;
import ar.edu.utn.frc.tup.piii.repositories.entities.MatchPlayerEntity;
import ar.edu.utn.frc.tup.piii.repositories.jpa.MatchJpaRepository;
import ar.edu.utn.frc.tup.piii.repositories.jpa.MatchPlayerJpaRepository;
import ar.edu.utn.frc.tup.piii.repositories.jpa.PlayerJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Service
public class MatchApplicationService {

    private final GameEngine gameEngine;
    private final SetupManager setupManager;
    private final StatePersisterPort statePersisterPort;
    private final DeckLoadPort deckLoadPort;
    private final MatchMapper matchMapper;
    private final MatchQueryService matchQueryService;
    private final MatchJpaRepository matchJpaRepository;
    private final MatchPlayerJpaRepository matchPlayerJpaRepository;

    private final PlayerJpaRepository playerJpaRepository;
    private final ConcurrentHashMap<UUID, ReentrantLock> matchLocks = new ConcurrentHashMap<>();
    private static final long LOCK_TIMEOUT_SECONDS = 10;

    public MatchApplicationService(GameEngine gameEngine,
                                    SetupManager setupManager,
                                    StatePersisterPort statePersisterPort,
                                    DeckLoadPort deckLoadPort,
                                    MatchMapper matchMapper,
                                    MatchQueryService matchQueryService,
                                    MatchJpaRepository matchJpaRepository,
                                    MatchPlayerJpaRepository matchPlayerJpaRepository,
                                    PlayerJpaRepository playerJpaRepository) {
        this.gameEngine = gameEngine;
        this.setupManager = setupManager;
        this.statePersisterPort = statePersisterPort;
        this.deckLoadPort = deckLoadPort;
        this.matchMapper = matchMapper;
        this.matchQueryService = matchQueryService;
        this.matchJpaRepository = matchJpaRepository;
        this.matchPlayerJpaRepository = matchPlayerJpaRepository;
        this.playerJpaRepository = playerJpaRepository;
    }

    @Transactional
    public MatchResponse createMatch(CreateMatchRequest request) {
        if (request.getPlayer1Id() == null) {
            throw new ValidationException("player1Id is required");
        }
        UUID player1Id = UUID.fromString(request.getPlayer1Id());
        String player1Kind = resolvePlayerKind(player1Id);

        UUID player2Id = null;
        if (request.getPlayer2Id() != null) {
            player2Id = UUID.fromString(request.getPlayer2Id());
            resolvePlayerKind(player2Id);
        } else if (request.getPlayer2Name() != null) {
            throw new ValidationException("player2Id is required when player2Name is provided");
        }

        MatchEntity match = new MatchEntity();
        match.setStatus("WAITING");
        match.setTurnNumber(0);
        match = matchJpaRepository.save(match);

        MatchPlayerEntity player1 = new MatchPlayerEntity();
        player1.setMatch(match);
        player1.setPlayerId(player1Id);
        player1.setPlayerKind(player1Kind);
        player1.setSide("PLAYER_ONE");
        player1.setDeckId(UUID.fromString(request.getPlayer1DeckId()));
        player1.setDisplayName(request.getPlayer1Name());
        matchPlayerJpaRepository.save(player1);

        List<MatchPlayerEntity> players = new ArrayList<>(List.of(player1));

        if (player2Id != null) {
            String player2Kind = resolvePlayerKind(player2Id);
            MatchPlayerEntity player2 = new MatchPlayerEntity();
            player2.setMatch(match);
            player2.setPlayerId(player2Id);
            player2.setPlayerKind(player2Kind);
            player2.setSide("PLAYER_TWO");
            player2.setDeckId(UUID.fromString(request.getPlayer2DeckId()));
            player2.setDisplayName(request.getPlayer2Name());
            matchPlayerJpaRepository.save(player2);
            players.add(player2);

            UUID deck1Id = UUID.fromString(request.getPlayer1DeckId());
            UUID deck2Id = UUID.fromString(request.getPlayer2DeckId());

            deckLoadPort.loadDeck(deck1Id);
            deckLoadPort.loadDeck(deck2Id);

            GameState gameState = setupManager.setup(match.getId(), player1Id, player2Id, deck1Id, deck2Id);
            statePersisterPort.saveState(match.getId(), gameState);

            match.setStatus("ACTIVE");
            match.setCurrentPlayerId(gameState.getCurrentPlayerId());
            match.setFirstPlayerId(gameState.getFirstPlayerId());
            match.setTurnNumber(gameState.getTurnNumber());
            match = matchJpaRepository.save(match);
        }

        return matchMapper.toMatchResponse(match, players);
    }

    @Transactional
    public MatchResponse joinMatch(UUID matchId, JoinMatchRequest request) {
        MatchEntity match = matchJpaRepository.findById(matchId)
                .orElseThrow(() -> new NotFoundException("Match not found: " + matchId));

        if (!"WAITING".equals(match.getStatus())) {
            throw new ValidationException("Match is not waiting for players");
        }

        if (request.playerId() == null) {
            throw new ValidationException("playerId is required");
        }
        UUID player2Id = UUID.fromString(request.playerId());
        String player2Kind = resolvePlayerKind(player2Id);

        List<MatchPlayerEntity> existingPlayers = matchPlayerJpaRepository.findByMatch_Id(matchId);
        MatchPlayerEntity player1 = existingPlayers.stream()
                .filter(p -> "PLAYER_ONE".equals(p.getSide()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Match has no player 1"));

        MatchPlayerEntity player2 = new MatchPlayerEntity();
        player2.setMatch(match);
        player2.setPlayerId(player2Id);
        player2.setPlayerKind(player2Kind);
        player2.setSide("PLAYER_TWO");
        player2.setDeckId(UUID.fromString(request.deckId()));
        player2.setDisplayName(request.playerName());
        matchPlayerJpaRepository.save(player2);

        UUID deck1Id = player1.getDeckId();
        UUID deck2Id = player2.getDeckId();

        deckLoadPort.loadDeck(deck1Id);
        deckLoadPort.loadDeck(deck2Id);

        GameState gameState = setupManager.setup(matchId, player1.getPlayerId(), player2Id, deck1Id, deck2Id);
        statePersisterPort.saveState(matchId, gameState);

        match.setStatus("ACTIVE");
        match.setCurrentPlayerId(gameState.getCurrentPlayerId());
        match.setFirstPlayerId(gameState.getFirstPlayerId());
        match.setTurnNumber(gameState.getTurnNumber());
        match = matchJpaRepository.save(match);

        List<MatchPlayerEntity> allPlayers = new ArrayList<>(existingPlayers);
        allPlayers.add(player2);

        return matchMapper.toMatchResponse(match, allPlayers);
    }

    public GameActionResponse executeAction(UUID matchId, GameActionRequest request) {
        ReentrantLock lock = matchLocks.computeIfAbsent(matchId, k -> new ReentrantLock(true));
        try {
            if (!lock.tryLock(LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                throw new ConflictException("Match " + matchId + " is busy. Try again.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ConflictException("Request interrupted for match " + matchId);
        }

        try {
            GameAction action = new GameAction();
            action.setType(GameActionType.valueOf(request.type()));
            action.setPlayerId(UUID.fromString(request.playerId()));
            action.setClientRequestId(request.clientRequestId());
            if (request.payload() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> payload = (Map<String, Object>) request.payload();
                action.setPayload(payload);
            }

            UUID playerUuid = UUID.fromString(request.playerId());
            ActionResult result = gameEngine.applyAction(matchId, playerUuid, action);

            if (result.getPublicState() == null) {
                GameState state = gameEngine.loadState(matchId);
                if (state != null) {
                    result.setPublicState(matchQueryService.buildPublicState(state));
                    result.setPrivateState(matchQueryService.buildPrivateState(state, playerUuid));
                }
            }

            List<GameActionResponse.GameEventDto> eventDtos = result.getEvents() != null
                    ? result.getEvents().stream()
                    .map(e -> new GameActionResponse.GameEventDto(
                            e.getType(),
                            e.getMessage(),
                            e.getPayload()))
                    .collect(Collectors.toList())
                    : List.of();

            GameActionResponse.ErrorDto errorDto = null;
            if (result.getError() != null) {
                errorDto = new GameActionResponse.ErrorDto(
                        result.getError().getCode(),
                        result.getError().getMessage(),
                        result.getError().getDetails()
                );
            }

            return new GameActionResponse(
                    result.isSuccess(),
                    result.getClientRequestId(),
                    result.getPublicState(),
                    result.getPrivateState(),
                    eventDtos,
                    errorDto
            );
        } finally {
            lock.unlock();
        }
    }

    public PrivatePlayerState getPrivateState(UUID matchId, UUID playerId) {
        GameState state = gameEngine.loadState(matchId);
        if (state == null) return null;
        return matchQueryService.buildPrivateState(state, playerId);
    }

    public List<UUID> getPlayerIds(UUID matchId) {
        return matchPlayerJpaRepository.findByMatch_Id(matchId)
                .stream()
                .map(MatchPlayerEntity::getPlayerId)
                .collect(Collectors.toList());
    }

    private String resolvePlayerKind(UUID playerId) {
        if (playerJpaRepository.existsById(playerId)) {
            return "PLAYER";
        }
        throw new ValidationException("Player not found: " + playerId);
    }

    public MatchStateResponse getMatchState(UUID matchId, UUID playerId) {
        GameState state = gameEngine.loadState(matchId);
        if (state == null) {
            throw new NotFoundException("Match state not found: " + matchId);
        }

        var publicState = matchQueryService.buildPublicState(state);
        var privateState = matchQueryService.buildPrivateState(state, playerId);

        return new MatchStateResponse(
                matchId.toString(),
                publicState,
                privateState
        );
    }
}