package ar.edu.utn.frc.tup.piii.dtos.cards;

public record CardSummaryResponse(
        String id,
        String name,
        String supertype,
        String setCode,
        String number,
        String imageSmallUrl
) {
}
