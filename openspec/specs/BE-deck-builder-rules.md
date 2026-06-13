# AI Proposal Spec: BE-deck-builder-rules

## Change name

BE-deck-builder-rules

## Purpose

Definir la lógica de backend para generación de mazos válidos y validación de construcción.

## Requirements

### Requirement: Generate valid random decks
El sistema SHALL poder generar un mazo aleatorio que respete las reglas de construcción y sea jugable.

#### Scenario: Random deck is generated
- WHEN the user requests a random deck
- THEN the system SHALL build a deck that respects copy limits, deck size rules and card compatibility
- AND the system SHALL avoid impossible or extremely hard-to-play combinations

#### Scenario: Generated deck is invalid
- WHEN the generated deck does not satisfy the validation rules
- THEN the system SHALL regenerate the deck or refuse the result
- AND the system SHALL not persist an invalid deck

#### Scenario: Compatible Pokémon and energy
- WHEN the deck contains Pokémon of a given type
- THEN the system SHALL try to include the corresponding energy cards needed to make the deck playable
- AND the system SHALL keep the energy distribution consistent with the selected Pokémon types

#### Scenario: Not enough compatible cards
- WHEN the available card pool cannot produce a valid deck
- THEN the system SHALL inform the caller that a valid random deck could not be produced

### Requirement: Validate energy and Pokémon type compatibility
El sistema SHALL verificar que la combinación de Pokémon y energías no produzca mazos inviables.

#### Scenario: Basic compatibility check
- WHEN a deck is validated
- THEN the system SHALL verify that the energy mix can support the Pokémon types in the deck

#### Scenario: Deck without usable energies
- WHEN the deck contains Pokémon whose attacks cannot be paid with any energy in the list
- THEN the system SHALL flag the deck as impossible or invalid

#### Scenario: Deck with poor energy balance
- WHEN the deck is technically valid but has very few compatible energies
- THEN the system SHALL warn that the deck may be difficult to play

#### Scenario: Mixed type deck
- WHEN the deck uses multiple Pokémon types
- THEN the system SHALL evaluate whether the energy base is broad enough to support them

### Requirement: Validate deck save flow
El sistema SHALL ejecutar las mismas validaciones al guardar que al validar.

#### Scenario: Save in create mode
- WHEN the user saves a new deck
- THEN the system SHALL run the full deck validation flow before creating it

#### Scenario: Save in edit mode
- WHEN the user saves an existing deck
- THEN the system SHALL run the full deck validation flow before updating it

#### Scenario: Validation fails on save
- WHEN validation finds an error
- THEN the system SHALL block persistence

## Non-goals

- No cambiar reglas oficiales del juego.
- No modificar componentes de interfaz.
