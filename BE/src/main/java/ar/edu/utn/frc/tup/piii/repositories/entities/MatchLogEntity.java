package ar.edu.utn.frc.tup.piii.repositories.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "match_logs")
public class MatchLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private MatchEntity match;

    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "turn_number", nullable = false)
    private Integer turnNumber;

    @Column(name = "player_id")
    private UUID playerId;

    @Column(name = "action_type", length = 80)
    private String actionType;

    @Column(name = "event_type", nullable = false, length = 80)
    private String eventType;

    @Column(name = "result", nullable = false, length = 30)
    private String result;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload = "{}";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
