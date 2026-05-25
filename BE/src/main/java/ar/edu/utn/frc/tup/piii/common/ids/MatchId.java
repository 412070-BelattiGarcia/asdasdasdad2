package ar.edu.utn.frc.tup.piii.common.ids;

import java.util.UUID;

public record MatchId(UUID value) {
    public static MatchId create() {
        return new MatchId(UUID.randomUUID());
    }

    public static MatchId of(String value) {
        return new MatchId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
