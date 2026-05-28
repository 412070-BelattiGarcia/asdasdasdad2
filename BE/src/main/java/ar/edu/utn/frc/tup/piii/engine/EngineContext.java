package ar.edu.utn.frc.tup.piii.engine;

import ar.edu.utn.frc.tup.piii.engine.model.GameState;
import ar.edu.utn.frc.tup.piii.engine.ports.CardLookupPort;
import ar.edu.utn.frc.tup.piii.engine.ports.RandomizerPort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EngineContext {
    private final GameState state;
    private final List<String> events = new ArrayList<>();
    private final CardLookupPort cardLookup;
    private final RandomizerPort randomizer;

    public EngineContext(GameState state, CardLookupPort cardLookup, RandomizerPort randomizer) {
        this.state = state;
        this.cardLookup = cardLookup;
        this.randomizer = randomizer;
    }

    public void addEvent(String message) { events.add(message); }

    public List<String> getEvents() { return Collections.unmodifiableList(events); }

    public GameState getState() { return state; }

    public CardLookupPort getCardLookup() { return cardLookup; }

    public RandomizerPort getRandomizer() { return randomizer; }
}
