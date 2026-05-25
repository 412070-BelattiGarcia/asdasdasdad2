package ar.edu.utn.frc.tup.piii.decks.domain;

import java.util.List;

public class DeckValidationResult {
    private boolean valid;
    private List<DeckValidationError> errors;

    public boolean isValid() { return valid; }
    public List<DeckValidationError> getErrors() { return errors; }
}
