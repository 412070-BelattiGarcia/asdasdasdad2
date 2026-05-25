package ar.edu.utn.frc.tup.piii.dtos.matches;

import java.util.List;

public record GameActionResponse(
        boolean success,
        String clientRequestId,
        Object publicState,
        Object privateState,
        List<GameEventDto> events,
        ErrorDto error
) {
    public record GameEventDto(String type, String message, Object payload) {}
    public record ErrorDto(String code, String message, Object details) {}
}
