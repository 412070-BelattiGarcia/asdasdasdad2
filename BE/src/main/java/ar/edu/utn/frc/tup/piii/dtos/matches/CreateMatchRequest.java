package ar.edu.utn.frc.tup.piii.dtos.matches;

public record CreateMatchRequest(
        String playerName,
        String deckId
) {
}
