package ar.edu.utn.frc.tup.piii.engine.setup;

import ar.edu.utn.frc.tup.piii.cards.domain.CardDefinition;
import ar.edu.utn.frc.tup.piii.cards.domain.PokemonCardDefinition;
import ar.edu.utn.frc.tup.piii.decks.domain.Deck;
import ar.edu.utn.frc.tup.piii.engine.PlayerSide;
import ar.edu.utn.frc.tup.piii.engine.model.CardInstance;
import ar.edu.utn.frc.tup.piii.engine.model.GameState;
import ar.edu.utn.frc.tup.piii.engine.model.PlayerState;
import ar.edu.utn.frc.tup.piii.engine.model.PokemonInPlay;
import ar.edu.utn.frc.tup.piii.engine.model.TurnFlags;
import ar.edu.utn.frc.tup.piii.engine.event.GameEvent;
import ar.edu.utn.frc.tup.piii.engine.event.GameEventType;
import ar.edu.utn.frc.tup.piii.engine.ports.CardLookupPort;
import ar.edu.utn.frc.tup.piii.engine.ports.DeckLoadPort;
import ar.edu.utn.frc.tup.piii.engine.ports.EventPublisherPort;
import ar.edu.utn.frc.tup.piii.engine.ports.RandomizerPort;
import ar.edu.utn.frc.tup.piii.engine.turn.TurnPhase;
import ar.edu.utn.frc.tup.piii.engine.MatchStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SetupManager {

    private final DeckLoadPort deckLoadPort;
    private final CardLookupPort cardLookupPort;
    private final RandomizerPort randomizerPort;
    private final EventPublisherPort eventPublisher;

    public SetupManager(DeckLoadPort deckLoadPort, CardLookupPort cardLookupPort, RandomizerPort randomizerPort, EventPublisherPort eventPublisher) {
        this.deckLoadPort = deckLoadPort;
        this.cardLookupPort = cardLookupPort;
        this.randomizerPort = randomizerPort;
        this.eventPublisher = eventPublisher;
    }

    public GameState setup(UUID matchId, UUID playerOneId, UUID playerTwoId, UUID deckOneId, UUID deckTwoId) {
        Deck deck1 = deckLoadPort.loadDeck(deckOneId);
        Deck deck2 = deckLoadPort.loadDeck(deckTwoId);

        List<CardInstance> deck1Cards = expandDeck(deck1);
        List<CardInstance> deck2Cards = expandDeck(deck2);

        deck1Cards = shuffleDeck(deck1Cards);
        deck2Cards = shuffleDeck(deck2Cards);

        PlayerState playerOneState = createPlayerState(playerOneId, PlayerSide.PLAYER_ONE);
        PlayerState playerTwoState = createPlayerState(playerTwoId, PlayerSide.PLAYER_TWO);

        dealInitialHand(playerOneState, deck1Cards);
        dealInitialHand(playerTwoState, deck2Cards);

        resolveMulligan(playerOneState, deck1Cards, matchId);
        resolveMulligan(playerTwoState, deck2Cards, matchId);

        drawExtraCards(playerOneState, deck1Cards, playerTwoState.getMulliganCount());
        drawExtraCards(playerTwoState, deck2Cards, playerOneState.getMulliganCount());

        selectActivePokemon(playerOneState, playerOneId);
        selectActivePokemon(playerTwoState, playerTwoId);

        fillBenchWithBasics(playerOneState, playerOneId);
        fillBenchWithBasics(playerTwoState, playerTwoId);

        assignPrizes(playerOneState, deck1Cards);
        assignPrizes(playerTwoState, deck2Cards);

        playerOneState.setDeck(new ArrayList<>(deck1Cards));
        playerTwoState.setDeck(new ArrayList<>(deck2Cards));

        if (playerOneState.getPrizes().size() != 6 || playerTwoState.getPrizes().size() != 6) {
            throw new IllegalStateException("Both players must have exactly 6 prizes");
        }

        int coinFlip = randomizerPort.nextInt(2);
        UUID firstPlayerId = coinFlip == 0 ? playerOneId : playerTwoId;

        GameState gameState = new GameState();
        gameState.setMatchId(matchId);
        gameState.setStatus(MatchStatus.ACTIVE);
        gameState.setPhase(TurnPhase.DRAW);
        gameState.setTurnNumber(1);
        gameState.setCurrentPlayerId(firstPlayerId);
        gameState.setFirstPlayerId(firstPlayerId);
        gameState.setPlayers(new PlayerState[]{playerOneState, playerTwoState});

        TurnFlags turnFlags = new TurnFlags();
        gameState.setTurnFlags(turnFlags);

        Instant now = Instant.now();
        gameState.setCreatedAt(now);
        gameState.setUpdatedAt(now);

        return gameState;
    }

    private PlayerState createPlayerState(UUID playerId, PlayerSide side) {
        PlayerState state = new PlayerState();
        state.setPlayerId(playerId);
        state.setSide(side);
        state.setDiscard(new ArrayList<>());
        return state;
    }

    private List<CardInstance> expandDeck(Deck deck) {
        List<CardInstance> result = new ArrayList<>();
        for (var deckCard : deck.getCards()) {
            for (int i = 0; i < deckCard.getQuantity(); i++) {
                result.add(new CardInstance(UUID.randomUUID(), deckCard.getCardId()));
            }
        }
        return result;
    }

    private List<CardInstance> shuffleDeck(List<CardInstance> deck) {
        List<CardInstance> mutableCopy = new ArrayList<>(deck);
        randomizerPort.shuffle(mutableCopy);
        return mutableCopy;
    }

    private void dealInitialHand(PlayerState playerState, List<CardInstance> deckCards) {
        List<CardInstance> hand = new ArrayList<>(deckCards.subList(0, 7));
        deckCards.subList(0, 7).clear();
        playerState.setHand(hand);
    }

    private void resolveMulligan(PlayerState playerState, List<CardInstance> deckCards, UUID matchId) {
        while (!hasBasicPokemon(playerState.getHand())) {
            List<String> revealedCardIds = playerState.getHand().stream()
                    .map(CardInstance::getCardDefinitionId)
                    .toList();
            GameEvent revealEvent = new GameEvent(
                    GameEventType.MULLIGAN_REVEALED.name(),
                    matchId,
                    0,
                    Instant.now(),
                    "Mano revelada por mulligan",
                    Map.of("playerId", playerState.getPlayerId().toString(), "revealedCardIds", revealedCardIds)
            );
            eventPublisher.publishEvents(matchId, List.of(revealEvent));

            deckCards.addAll(playerState.getHand());
            playerState.getHand().clear();

            List<CardInstance> shuffled = shuffleDeck(deckCards);
            deckCards.clear();
            deckCards.addAll(shuffled);

            List<CardInstance> hand = new ArrayList<>(deckCards.subList(0, 7));
            deckCards.subList(0, 7).clear();
            playerState.setHand(hand);

            playerState.setMulliganCount(playerState.getMulliganCount() + 1);
        }
    }

    private boolean hasBasicPokemon(List<CardInstance> hand) {
        for (CardInstance card : hand) {
            CardDefinition def = cardLookupPort.getCardById(card.getCardDefinitionId());
            if (def == null) {
                throw new IllegalStateException("Card definition not found: " + card.getCardDefinitionId());
            }
            if (def instanceof PokemonCardDefinition pkmn && "BASIC".equals(pkmn.getStage())) {
                return true;
            }
        }
        return false;
    }

    private void drawExtraCards(PlayerState playerState, List<CardInstance> deckCards, int extraCount) {
        for (int i = 0; i < extraCount; i++) {
            if (!deckCards.isEmpty()) {
                playerState.getHand().add(deckCards.remove(0));
            }
        }
    }

    private void selectActivePokemon(PlayerState playerState, UUID playerId) {
        List<CardInstance> hand = playerState.getHand();
        for (int i = 0; i < hand.size(); i++) {
            CardInstance card = hand.get(i);
            CardDefinition def = cardLookupPort.getCardById(card.getCardDefinitionId());
            if (def == null) {
                throw new IllegalStateException("Card definition not found: " + card.getCardDefinitionId());
            }
            if (def instanceof PokemonCardDefinition pkmn && "BASIC".equals(pkmn.getStage())) {
                CardInstance removed = hand.remove(i);

                PokemonInPlay active = new PokemonInPlay();
                active.setInstanceId(removed.getInstanceId());
                active.setCardDefinitionId(removed.getCardDefinitionId());
                active.setOwnerPlayerId(playerId);
                active.setEnteredTurnNumber(0);
                active.setEvolvedThisTurn(false);
                active.setDamageCounters(0);
                active.setSpecialConditions(new ArrayList<>());
                active.setAttachedEnergies(new ArrayList<>());

                playerState.setActivePokemon(active);
                return;
            }
        }
        throw new IllegalStateException("No Basic Pokemon found for active selection");
    }

    private void fillBenchWithBasics(PlayerState playerState, UUID playerId) {
        List<PokemonInPlay> bench = new ArrayList<>();
        List<CardInstance> hand = playerState.getHand();
        for (int i = hand.size() - 1; i >= 0 && bench.size() < 5; i--) {
            CardInstance card = hand.get(i);
            CardDefinition def = cardLookupPort.getCardById(card.getCardDefinitionId());
            if (def instanceof PokemonCardDefinition pkmn && "BASIC".equals(pkmn.getStage())) {
                PokemonInPlay pkm = new PokemonInPlay();
                pkm.setInstanceId(card.getInstanceId());
                pkm.setCardDefinitionId(card.getCardDefinitionId());
                pkm.setOwnerPlayerId(playerId);
                pkm.setEnteredTurnNumber(0);
                pkm.setEvolvedThisTurn(false);
                pkm.setDamageCounters(0);
                pkm.setSpecialConditions(new ArrayList<>());
                pkm.setAttachedEnergies(new ArrayList<>());
                bench.add(pkm);
                hand.remove(i);
            }
        }
        playerState.setBench(bench);
    }

    private void assignPrizes(PlayerState playerState, List<CardInstance> deckCards) {
        List<CardInstance> prizes = new ArrayList<>(deckCards.subList(0, 6));
        deckCards.subList(0, 6).clear();
        playerState.setPrizes(prizes);
    }
}
