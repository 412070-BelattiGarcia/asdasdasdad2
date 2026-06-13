# AI Proposal Spec: card-catalog-pokemon-copy-limit

## Change name

card-catalog-pokemon-copy-limit

## Purpose

Definir el límite de copias al agregar cartas Pokémon desde el catálogo al editor de mazos.

## Requirements

### Requirement: Limit Pokémon cards to four copies
El sistema SHALL impedir que se agreguen más de 4 copias de la misma carta Pokémon al mazo.

#### Scenario: Add first copies
- WHEN the user adds the same Pokémon card fewer than 4 times
- THEN the system SHALL allow the addition

#### Scenario: Reach the limit
- WHEN the user reaches 4 copies of the same Pokémon card
- THEN the system SHALL block any further additions of that card
- AND the system SHALL keep the deck quantity at 4

#### Scenario: Limit feedback
- WHEN the add action is blocked by the copy limit
- THEN the system SHALL inform the user that the maximum has been reached

#### Scenario: Different Pokémon card
- WHEN the user adds another Pokémon card with a different identity
- THEN the system SHALL treat it independently from the first one

## Non-goals

- No cambiar el límite de otras categorías de cartas.
- No modificar las reglas de validación global del mazo.
