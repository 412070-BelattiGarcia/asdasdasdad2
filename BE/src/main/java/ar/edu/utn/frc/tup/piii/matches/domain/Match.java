package ar.edu.utn.frc.tup.piii.matches.domain;

import java.time.Instant;
import java.util.UUID;

public class Match {
    private UUID id;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;

    public UUID getId() { return id; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
