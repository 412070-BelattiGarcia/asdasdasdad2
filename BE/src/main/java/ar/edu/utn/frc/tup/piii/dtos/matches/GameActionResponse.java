package ar.edu.utn.frc.tup.piii.dtos.matches;

import java.util.List;

public record GameActionResponse(
        boolean success,
        String clientRequestId,
        Object publicState,
        Object privateState,
        List<String> events,
        ErrorDto error
) {
    public record ErrorDto(String code, String message, Object details) {}
}
