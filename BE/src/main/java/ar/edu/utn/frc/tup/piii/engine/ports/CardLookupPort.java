package ar.edu.utn.frc.tup.piii.engine.ports;

import ar.edu.utn.frc.tup.piii.cards.domain.CardDefinition;

public interface CardLookupPort {
    CardDefinition getCardById(String cardId);
}
