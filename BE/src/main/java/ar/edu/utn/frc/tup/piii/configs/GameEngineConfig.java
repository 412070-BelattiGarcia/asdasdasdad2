package ar.edu.utn.frc.tup.piii.configs;

import ar.edu.utn.frc.tup.piii.engine.GameEngine;
import ar.edu.utn.frc.tup.piii.engine.ports.CardLookupPort;
import ar.edu.utn.frc.tup.piii.engine.ports.DeckLoadPort;
import ar.edu.utn.frc.tup.piii.engine.ports.EventPublisherPort;
import ar.edu.utn.frc.tup.piii.engine.ports.RandomizerPort;
import ar.edu.utn.frc.tup.piii.engine.ports.StatePersisterPort;
import ar.edu.utn.frc.tup.piii.engine.rules.RuleValidator;
import ar.edu.utn.frc.tup.piii.engine.setup.SetupManager;
import ar.edu.utn.frc.tup.piii.engine.turn.TurnManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GameEngineConfig {

    @Bean
    public SetupManager setupManager(DeckLoadPort deckLoadPort,
                                      CardLookupPort cardLookupPort,
                                      RandomizerPort randomizerPort,
                                      EventPublisherPort eventPublisherPort) {
        return new SetupManager(deckLoadPort, cardLookupPort, randomizerPort, eventPublisherPort);
    }

    @Bean
    public TurnManager turnManager(RandomizerPort randomizerPort) {
        return new TurnManager(randomizerPort);
    }

    @Bean
    public RuleValidator ruleValidator(CardLookupPort cardLookupPort) {
        return new RuleValidator(cardLookupPort);
    }

    @Bean
    public GameEngine gameEngine(CardLookupPort cardLookupPort,
                                  RandomizerPort randomizerPort,
                                  StatePersisterPort statePersisterPort,
                                  EventPublisherPort eventPublisherPort,
                                  TurnManager turnManager,
                                  RuleValidator ruleValidator) {
        return new GameEngine(cardLookupPort, randomizerPort, statePersisterPort,
                eventPublisherPort, turnManager, ruleValidator);
    }
}
