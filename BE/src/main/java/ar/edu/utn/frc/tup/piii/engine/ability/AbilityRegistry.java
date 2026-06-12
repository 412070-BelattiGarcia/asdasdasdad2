package ar.edu.utn.frc.tup.piii.engine.ability;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class AbilityRegistry {

    private final Map<String, AbilityResolver> resolvers = new HashMap<>();

    public void register(String abilityName, AbilityResolver resolver) {
        resolvers.put(abilityName, resolver);
    }

    public AbilityResolver get(String abilityName) {
        return resolvers.get(abilityName);
    }

    public boolean has(String abilityName) {
        return resolvers.containsKey(abilityName);
    }
}
