package ar.edu.utn.frc.tup.piii.dtos.matches;

public record MatchResponse(
        String matchId,
        String playerId,
        String side,
        String status
) {
}
