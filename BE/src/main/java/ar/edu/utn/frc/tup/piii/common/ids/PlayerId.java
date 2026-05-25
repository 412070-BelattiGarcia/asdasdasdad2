package ar.edu.utn.frc.tup.piii.common.ids;

import java.util.UUID;

public record PlayerId(UUID value) {
    public static PlayerId create() {
        return new PlayerId(UUID.randomUUID());
    }

    public static PlayerId of(String value) {
        return new PlayerId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
