package ar.edu.utn.frc.tup.piii.engine.attack;

import ar.edu.utn.frc.tup.piii.engine.SpecialCondition;
import ar.edu.utn.frc.tup.piii.engine.model.PokemonInPlay;
import ar.edu.utn.frc.tup.piii.engine.ports.CardLookupPort;
import ar.edu.utn.frc.tup.piii.engine.ports.RandomizerPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AttackResolverTest {

    @Mock
    private CardLookupPort cardLookup;
    @Mock
    private RandomizerPort randomizer;

    private PokemonInPlay attacker;
    private PokemonInPlay defender;

    @BeforeEach
    void setUp() {
        attacker = new PokemonInPlay();
        attacker.setInstanceId(UUID.randomUUID());
        attacker.setDamageCounters(5);
        attacker.setSpecialConditions(new ArrayList<>());
        attacker.setAttachedEnergies(new ArrayList<>());

        defender = new PokemonInPlay();
        defender.setInstanceId(UUID.randomUUID());
        defender.setDamageCounters(10);
        defender.setSpecialConditions(new ArrayList<>());
        defender.setAttachedEnergies(new ArrayList<>());
    }

    @Test
    void shouldNotMutateAttackerDamageCountersOnConfusionSelfHit() {
        attacker.getSpecialConditions().add(SpecialCondition.CONFUSED);
        // Confusion self-hit happens when coin flip is 0 (tails/confused)
        // In this case randomizer.nextInt(2) == 0 → confused
        // AttackResolver.resolve is static

        AttackResolver.AttackResolutionResult result =
                AttackResolver.resolve(attacker, defender, cardLookup, randomizer, 0);

        assertTrue(result.confusedSelfHit());

        // The key assertion: resolver should NOT have mutated attacker
        assertEquals(5, attacker.getDamageCounters(),
                "Resolver should NOT mutate attacker damage counters on confusion self-hit");
    }

    @Test
    void shouldReportConfusedSelfHitWhenConfused() {
        attacker.getSpecialConditions().add(SpecialCondition.CONFUSED);

        AttackResolver.AttackResolutionResult result =
                AttackResolver.resolve(attacker, defender, cardLookup, randomizer, 0);

        assertTrue(result.confusedSelfHit(), "Should report confusedSelfHit=true when confused and coin flip 0");
        assertEquals(3, result.selfDamageCounters(), "Self-hit should deal 30 damage (3 counters)");
        assertEquals("CONFUSED_SELF_HIT", result.errorMessage());
    }

    @Test
    void shouldNotReportConfusedSelfHitWhenNotConfused() {
        AttackResolver.AttackResolutionResult result =
                AttackResolver.resolve(attacker, defender, cardLookup, randomizer, 0);

        assertFalse(result.confusedSelfHit(), "Should NOT report confusedSelfHit when not confused");
        assertEquals(0, result.selfDamageCounters());
    }
}
