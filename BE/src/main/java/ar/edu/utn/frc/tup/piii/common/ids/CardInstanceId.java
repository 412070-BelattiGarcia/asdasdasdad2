package ar.edu.utn.frc.tup.piii.common.ids;

import java.util.UUID;

public record CardInstanceId(UUID value) {
    public static CardInstanceId create() {
        return new CardInstanceId(UUID.randomUUID());
    }

    public static CardInstanceId of(String value) {
        return new CardInstanceId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
