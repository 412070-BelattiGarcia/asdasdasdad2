# AI Proposal Spec: deck-builder-random-valid-decks

## Change name

deck-builder-random-valid-decks

## Purpose

Definir la generación aleatoria de mazos válidos dentro del editor de mazos.

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
- THEN the system SHALL inform the user that a valid random deck could not be produced
- AND the system SHALL not create a broken deck

## Non-goals

- No cambiar reglas oficiales del juego.
- No modificar catálogo ni backend de sincronización.
