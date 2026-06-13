# AI Proposal Spec: FE-deck-catalog-search

## Change name

FE-deck-catalog-search

## Purpose

Definir la UI del catálogo y del buscador del editor de mazos, incluyendo paginación, filtros y límites de copias.

## Requirements

### Requirement: Render all cards returned for each tab
El sistema SHALL mostrar todas las cartas devueltas por la consulta de la pestaña actual.

#### Scenario: Last items are present
- WHEN a tab returns a page that includes the last available cards
- THEN the system SHALL render those cards in the list
- AND the system SHALL not drop the tail of the result set

#### Scenario: Pagination reaches the end
- WHEN the user navigates to the last page of a tab
- THEN the system SHALL show the remaining cards even if the page is not full

### Requirement: Limit Pokémon cards to four copies
El sistema SHALL impedir que se agreguen más de 4 copias de la misma carta Pokémon al mazo desde el catálogo.

#### Scenario: Add first copies
- WHEN the user adds the same Pokémon card fewer than 4 times
- THEN the system SHALL allow the addition

#### Scenario: Reach the limit
- WHEN the user reaches 4 copies of the same Pokémon card
- THEN the system SHALL block any further additions of that card

#### Scenario: Limit feedback
- WHEN the add action is blocked by the copy limit
- THEN the system SHALL inform the user that the maximum has been reached

### Requirement: Reuse catalog filter behavior in deck search
El sistema SHALL usar el mismo comportamiento de filtros en el catálogo y en la búsqueda del editor de mazos.

#### Scenario: Same filter set
- WHEN the user applies the same filters in both screens
- THEN the system SHALL return consistent results

#### Scenario: Filter updates
- WHEN the user changes a filter in deck search
- THEN the system SHALL update the result list using the same filtering rules as the catalog

#### Scenario: Shared implementation
- WHEN a catalog filter component or service already exists
- THEN the deck editor search SHALL reuse it or its underlying logic

## Non-goals

- No cambiar la fuente de datos del catálogo.
- No modificar la lógica de backend.
