package ar.edu.utn.frc.tup.piii.configs;

import ar.edu.utn.frc.tup.piii.engine.GameEngine;
import ar.edu.utn.frc.tup.piii.engine.ports.CardLookupPort;
import ar.edu.utn.frc.tup.piii.engine.ports.EventPublisherPort;
import ar.edu.utn.frc.tup.piii.engine.ports.RandomizerPort;
import ar.edu.utn.frc.tup.piii.engine.ports.StatePersisterPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GameEngineConfig {

    @Bean
    public GameEngine gameEngine(CardLookupPort cardLookupPort,
                                  RandomizerPort randomizerPort,
                                  StatePersisterPort statePersisterPort,
                                  EventPublisherPort eventPublisherPort) {
        return new GameEngine(cardLookupPort, randomizerPort, statePersisterPort, eventPublisherPort);
    }
}
