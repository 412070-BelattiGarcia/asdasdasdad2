# AI Proposal Spec: deck-search-filters-reuse

## Change name

deck-search-filters-reuse

## Purpose

Definir la reutilización del sistema de filtros del catálogo de cartas en la búsqueda del editor de mazos.

## Requirements

### Requirement: Reuse catalog filter behavior in deck search
El sistema SHALL usar el mismo comportamiento de filtros en el catálogo y en la búsqueda del editor de mazos.

#### Scenario: Same filter set
- WHEN the user applies the same filters in both screens
- THEN the system SHALL return consistent results

#### Scenario: Filter updates
- WHEN the user changes a filter in deck search
- THEN the system SHALL update the result list using the same filtering rules as the catalog

#### Scenario: Clear filters
- WHEN the user clears filters in deck search
- THEN the system SHALL reset the search state in the same way as the catalog view

### Requirement: Share filter logic
El sistema SHALL preferir reutilizar componentes o lógica existente antes que duplicarla.

#### Scenario: Shared implementation
- WHEN a catalog filter component or service already exists
- THEN the deck editor search SHALL reuse it or its underlying logic

## Non-goals

- No redefinir el modelo de filtros.
- No modificar el catálogo de cartas ni el backend.
