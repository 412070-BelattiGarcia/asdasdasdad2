# Persistence and Log Contract

## Goal

Define what must be persisted after every relevant action.

The persisted state must be sufficient to reconstruct the full match.

## Backend location

```
persistence/
engine/ports/StatePersisterPort.java
```

## State persistence rule

After every valid action, persist the complete `GameState` as a JSON column.

No separate log/history/event tables exist in V1. The `GameState` JSON contains everything needed to reconstruct the match.

## MatchEntity

```
package ar.edu.utn.frc.tup.piii.persistence;

@Entity
@Table(name = "matches")
public class MatchEntity {

  @Id
  private UUID id;

  @Column(nullable = false)
  @Convert(converter = GameStateConverter.class)
  private GameState state;

  @Column(nullable = false)
  private String playerOneName;

  @Column(nullable = false)
  private UUID playerOneDeckId;

  private String playerTwoName;
  private UUID playerTwoDeckId;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private MatchStatus status;

  @Column(nullable = false)
  private Instant createdAt;

  private Instant updatedAt;
}
```

## MatchRepository

```
package ar.edu.utn.frc.tup.piii.persistence;

@Repository
public interface MatchRepository extends JpaRepository<MatchEntity, UUID> {
  List<MatchEntity> findByStatus(MatchStatus status);
}
```

## GameStateConverter

```
package ar.edu.utn.frc.tup.piii.persistence;

@Converter
public class GameStateConverter implements AttributeConverter<GameState, String> {

  private static final ObjectMapper mapper = new ObjectMapper();

  @Override
  public String convertToDatabaseColumn(GameState state) {
    return mapper.writeValueAsString(state);
  }

  @Override
  public GameState convertToEntityAttribute(String json) {
    return mapper.readValue(json, GameState.class);
  }
}
```

## Persisted state JSON example

```json
{
  "matchId": "9a747f90-b50e-49df-9d8a-456c9796aa11",
  "status": "ACTIVE",
  "phase": "MAIN",
  "turnNumber": 3,
  "currentPlayerId": "player-1",
  "players": [
    {
      "playerId": "player-1",
      "deck": [
        {"instanceId": "ci-1", "cardDefinitionId": "xy1-1"},
        {"instanceId": "ci-2", "cardDefinitionId": "xy1-1"}
      ],
      "hand": [
        {"instanceId": "ci-10", "cardDefinitionId": "xy1-10"}
      ],
      "prizes": [
        {"instanceId": "ci-20", "cardDefinitionId": "xy1-1"},
        {"instanceId": "ci-21", "cardDefinitionId": "xy1-1"},
        {"instanceId": "ci-22", "cardDefinitionId": "xy1-1"},
        {"instanceId": "ci-23", "cardDefinitionId": "xy1-1"},
        {"instanceId": "ci-24", "cardDefinitionId": "xy1-1"},
        {"instanceId": "ci-25", "cardDefinitionId": "xy1-1"}
      ],
      "discard": [],
      "activePokemon": {
        "instanceId": "ci-30",
        "cardDefinitionId": "xy1-10",
        "damageCounters": 2,
        "specialConditions": [],
        "attachedEnergies": [
          {"instanceId": "ci-40", "cardDefinitionId": "energy-fire-basic"}
        ]
      },
      "bench": []
    }
  ],
  "turnFlags": {
    "hasDrawnForTurn": true,
    "hasAttachedEnergy": false,
    "hasRetreated": false,
    "hasPlayedSupporter": false,
    "hasPlayedStadium": false,
    "hasAttacked": false
  }
}
```

## StatePersisterPort

```java
package ar.edu.utn.frc.tup.piii.engine.ports;

public interface StatePersisterPort {
  void saveState(UUID matchId, GameState state);
  Optional<GameState> loadState(UUID matchId);
}
```

The port uses `GameState` (engine model), not `MatchEntity` (JPA entity). The adapter (`StatePersisterAdapter`) handles the conversion.

## StatePersisterAdapter

Resides in `engine/ports/impl/StatePersisterAdapter.java`. Delegates to `MatchRepository`:
- `saveState`: saves `MatchEntity` with updated `GameState` JSON
- `loadState`: loads `MatchEntity` by matchId, deserializes `GameState` via `GameStateConverter`

## No event log in V1

- No `MatchLogEntity` or `MatchLogJpaRepository` exist.
- No `GameEvent` / `GameEventType` classes exist.
- Inline event descriptions are returned in `GameActionResponse.events[]` as plain string messages, not persisted.
- If historical audit is required in a later version, it must be added as a new feature without modifying existing game logic.

## Immutability rule

Since no separate log is persisted, the `GameState` JSON is the single source of truth. Every mutation overwrites the entire state column.
