package ar.edu.utn.frc.tup.piii.services.decks;

import ar.edu.utn.frc.tup.piii.decks.domain.Deck;
import ar.edu.utn.frc.tup.piii.decks.domain.DeckCard;
import ar.edu.utn.frc.tup.piii.decks.domain.DeckValidationResult;
import ar.edu.utn.frc.tup.piii.dtos.decks.*;
import ar.edu.utn.frc.tup.piii.exceptions.NotFoundException;
import ar.edu.utn.frc.tup.piii.exceptions.ValidationException;
import ar.edu.utn.frc.tup.piii.mappers.decks.DeckMapper;
import ar.edu.utn.frc.tup.piii.repositories.entities.DeckEntity;
import ar.edu.utn.frc.tup.piii.repositories.jpa.DeckJpaRepository;
import ar.edu.utn.frc.tup.piii.repositories.jpa.PlayerJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DeckService {

    private final DeckJpaRepository deckJpaRepository;
    private final DeckValidator deckValidator;
    private final DeckMapper deckMapper;
    private final PlayerJpaRepository playerJpaRepository;

    public DeckService(DeckJpaRepository deckJpaRepository, DeckValidator deckValidator,
                       DeckMapper deckMapper, PlayerJpaRepository playerJpaRepository) {
        this.deckJpaRepository = deckJpaRepository;
        this.deckValidator = deckValidator;
        this.deckMapper = deckMapper;
        this.playerJpaRepository = playerJpaRepository;
    }

    @Transactional
    public DeckResponse createDeck(CreateDeckRequest request) {
        if (request.playerId() != null) {
            UUID playerId = UUID.fromString(request.playerId());
            if (!playerJpaRepository.existsById(playerId)) {
                throw new ValidationException("Player not found: " + playerId);
            }
        }
        DeckEntity entity = deckMapper.toEntity(request);
        if (request.playerId() != null) {
            entity.setOwnerPlayer(playerJpaRepository.getReferenceById(UUID.fromString(request.playerId())));
        }
        DeckValidationResult validation = deckValidator.validate(
                entity.getCards().stream().map(deckMapper::toDomainCard).collect(Collectors.toList()));
        entity.setValid(validation.isValid());
        entity = deckJpaRepository.save(entity);
        return deckMapper.toResponse(entity, validation);
    }

    public DeckResponse getDeck(UUID deckId) {
        DeckEntity entity = findEntity(deckId);
        DeckValidationResult validation = deckValidator.validate(
                entity.getCards().stream().map(deckMapper::toDomainCard).collect(Collectors.toList()));
        return deckMapper.toResponse(entity, validation);
    }

    @Transactional
    public DeckResponse updateDeck(UUID deckId, UpdateDeckRequest request) {
        DeckEntity entity = findEntity(deckId);
        deckMapper.updateEntity(entity, request);
        DeckValidationResult validation = deckValidator.validate(
                entity.getCards().stream().map(deckMapper::toDomainCard).collect(Collectors.toList()));
        entity.setValid(validation.isValid());
        entity = deckJpaRepository.save(entity);
        return deckMapper.toResponse(entity, validation);
    }

    @Transactional
    public void deleteDeck(UUID deckId) {
        DeckEntity entity = findEntity(deckId);
        deckJpaRepository.delete(entity);
    }

    public List<DeckResponse> listDecksByPlayer(UUID playerId) {
        return deckJpaRepository.findByOwnerPlayerId(playerId).stream()
                .map(entity -> {
                    DeckValidationResult validation = deckValidator.validate(
                            entity.getCards().stream().map(deckMapper::toDomainCard).collect(Collectors.toList()));
                    return deckMapper.toResponse(entity, validation);
                })
                .collect(Collectors.toList());
    }

    public DeckValidationResponse validateDeck(UUID deckId) {
        DeckEntity entity = findEntity(deckId);
        DeckValidationResult validation = deckValidator.validate(
                entity.getCards().stream().map(deckMapper::toDomainCard).collect(Collectors.toList()));
        return deckMapper.toValidationResponse(validation);
    }

    public DeckValidationResponse validateCards(ValidateDeckRequest request) {
        List<DeckCard> deckCards = request.cards().stream()
                .map(c -> {
                    DeckCard card = new DeckCard();
                    card.setCardId(c.cardId());
                    card.setQuantity(c.quantity());
                    return card;
                })
                .collect(Collectors.toList());
        DeckValidationResult validation = deckValidator.validate(deckCards);
        return deckMapper.toValidationResponse(validation);
    }

    public Deck loadDeckDomain(UUID deckId) {
        DeckEntity entity = findEntity(deckId);
        return deckMapper.toDomain(entity);
    }

    private DeckEntity findEntity(UUID deckId) {
        return deckJpaRepository.findById(deckId)
                .orElseThrow(() -> new NotFoundException("Deck not found: " + deckId));
    }
}