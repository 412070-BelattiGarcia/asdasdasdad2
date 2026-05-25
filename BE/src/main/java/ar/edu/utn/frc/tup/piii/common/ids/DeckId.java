package ar.edu.utn.frc.tup.piii.common.ids;

import java.util.UUID;

public record DeckId(UUID value) {
    public static DeckId create() {
        return new DeckId(UUID.randomUUID());
    }

    public static DeckId of(String value) {
        return new DeckId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
