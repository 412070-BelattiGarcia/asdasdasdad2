package ar.edu.utn.frc.tup.piii.configs;

import ar.edu.utn.frc.tup.piii.engine.GameEngine;
import ar.edu.utn.frc.tup.piii.engine.action.GameActionType;
import ar.edu.utn.frc.tup.piii.engine.handlers.ActionHandler;
import ar.edu.utn.frc.tup.piii.engine.ports.CardLookupPort;
import ar.edu.utn.frc.tup.piii.engine.ports.EventPublisherPort;
import ar.edu.utn.frc.tup.piii.engine.ports.RandomizerPort;
import ar.edu.utn.frc.tup.piii.engine.ports.StatePersisterPort;
import ar.edu.utn.frc.tup.piii.engine.rules.RuleValidator;
import ar.edu.utn.frc.tup.piii.engine.victory.VictoryConditionChecker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class GameEngineConfig {

    @Bean
    public RuleValidator ruleValidator() {
        return new RuleValidator();
    }

    @Bean
    public VictoryConditionChecker victoryConditionChecker() {
        return new VictoryConditionChecker();
    }

    @Bean
    public GameEngine gameEngine(CardLookupPort cardLookupPort,
                                  RandomizerPort randomizerPort,
                                  StatePersisterPort statePersisterPort,
                                  EventPublisherPort eventPublisherPort,
                                  RuleValidator ruleValidator,
                                  VictoryConditionChecker victoryConditionChecker) {
        Map<GameActionType, ActionHandler> handlers = new HashMap<>();
        return new GameEngine(cardLookupPort, randomizerPort, statePersisterPort,
                eventPublisherPort, ruleValidator, victoryConditionChecker, handlers);
    }
}
