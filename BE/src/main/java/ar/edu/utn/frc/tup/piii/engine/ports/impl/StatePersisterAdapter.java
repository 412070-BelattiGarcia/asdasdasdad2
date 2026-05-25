package ar.edu.utn.frc.tup.piii.engine.ports.impl;

import ar.edu.utn.frc.tup.piii.engine.model.GameState;
import ar.edu.utn.frc.tup.piii.engine.ports.StatePersisterPort;
import ar.edu.utn.frc.tup.piii.repositories.entities.MatchEntity;
import ar.edu.utn.frc.tup.piii.repositories.entities.MatchStateEntity;
import ar.edu.utn.frc.tup.piii.repositories.jpa.MatchJpaRepository;
import ar.edu.utn.frc.tup.piii.repositories.jpa.MatchStateJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;

@Component
public class StatePersisterAdapter implements StatePersisterPort {

    private final MatchStateJpaRepository matchStateJpaRepository;
    private final MatchJpaRepository matchJpaRepository;
    private final ObjectMapper objectMapper;

    public StatePersisterAdapter(MatchStateJpaRepository matchStateJpaRepository,
                                  MatchJpaRepository matchJpaRepository,
                                  ObjectMapper objectMapper) {
        this.matchStateJpaRepository = matchStateJpaRepository;
        this.matchJpaRepository = matchJpaRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void saveState(UUID matchId, GameState state) {
        Optional<MatchEntity> matchOpt = matchJpaRepository.findById(matchId);
        if (matchOpt.isEmpty()) {
            return;
        }
        Optional<MatchStateEntity> existing = matchStateJpaRepository.findAll().stream()
                .filter(ms -> ms.getMatch().getId().equals(matchId))
                .max(Comparator.comparing(MatchStateEntity::getVersion));

        MatchStateEntity entity = existing.orElseGet(MatchStateEntity::new);
        entity.setMatch(matchOpt.get());
        entity.setVersion(existing.map(ms -> ms.getVersion() + 1).orElse(1L));
        try {
            entity.setSerializedState(objectMapper.writeValueAsString(state));
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize GameState", e);
        }
        matchStateJpaRepository.save(entity);
    }

    @Override
    public GameState loadState(UUID matchId) {
        Optional<MatchStateEntity> latest = matchStateJpaRepository.findAll().stream()
                .filter(ms -> ms.getMatch().getId().equals(matchId))
                .max(Comparator.comparing(MatchStateEntity::getVersion));

        if (latest.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(latest.get().getSerializedState(), GameState.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize GameState", e);
        }
    }
}
