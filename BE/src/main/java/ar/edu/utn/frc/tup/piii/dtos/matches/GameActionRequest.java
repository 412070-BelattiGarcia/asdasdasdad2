package ar.edu.utn.frc.tup.piii.dtos.matches;

public record GameActionRequest(
        String type,
        String playerId,
        Object payload,
        String clientRequestId
) {
}
