package ar.edu.utn.frc.tup.piii.dtos.decks;

public record DeckCardResponse(
        String cardId,
        String name,
        int quantity,
        String supertype,
        boolean isBasicEnergy
) {
}
