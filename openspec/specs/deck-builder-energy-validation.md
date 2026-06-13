# AI Proposal Spec: deck-builder-energy-validation

## Change name

deck-builder-energy-validation

## Purpose

Definir la validación de mazos respecto a energías y tipos de Pokémon, incluyendo advertencias por mazos inviables.

## Requirements

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
- THEN the system SHALL warn the user that the deck may be difficult to play

#### Scenario: Mixed type deck
- WHEN the deck uses multiple Pokémon types
- THEN the system SHALL evaluate whether the energy base is broad enough to support them

### Requirement: Surface validation feedback
El sistema SHALL mostrar al usuario el resultado de la validación de forma clara.

#### Scenario: Validation warning
- WHEN the deck is playable but risky
- THEN the system SHALL display a warning message

#### Scenario: Validation error
- WHEN the deck is impossible to play
- THEN the system SHALL display an error message that blocks the invalid state

## Non-goals

- No redefinir todas las reglas oficiales de construcción.
- No modificar el catálogo de cartas.
