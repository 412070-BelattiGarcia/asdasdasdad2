# AI Proposal Spec: deck-builder-save-validates

## Change name

deck-builder-save-validates

## Purpose

Definir que la acción de guardar ejecute las mismas validaciones que la acción de validar.

## Requirements

### Requirement: Save action reuses deck validation
El botón “Guardar” SHALL ejecutar la misma validación que el botón “Validar” antes de persistir el mazo.

#### Scenario: Save in create mode
- WHEN the user clicks “Guardar” while creating a deck
- THEN the system SHALL run the full deck validation flow
- AND the system SHALL only create the deck if validation succeeds

#### Scenario: Save in edit mode
- WHEN the user clicks “Guardar” while editing a deck
- THEN the system SHALL run the full deck validation flow
- AND the system SHALL only update the deck if validation succeeds

#### Scenario: Validation fails on save
- WHEN validation finds an error
- THEN the system SHALL block the save operation
- AND the system SHALL show the same validation feedback used by the “Validar” action

#### Scenario: Validation passes on save
- WHEN validation succeeds
- THEN the system SHALL persist the deck and finish the save flow normally

## Non-goals

- No cambiar la UI del botón “Validar”.
- No introducir una validación distinta para guardar.
