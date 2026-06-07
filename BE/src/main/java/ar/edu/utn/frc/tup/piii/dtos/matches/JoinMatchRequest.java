package ar.edu.utn.frc.tup.piii.dtos.matches;

public record JoinMatchRequest(
        String playerName,
        String deckId,
        String playerId
) {}
