# AI Proposal Spec: card-catalog-pagination-last-items

## Change name

card-catalog-pagination-last-items

## Purpose

Definir el comportamiento esperado para que el catálogo muestre también los últimos elementos de cada pestaña.

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

#### Scenario: Page size and query size mismatch
- WHEN the configured page size is smaller than the tab result set
- THEN the system SHALL keep fetching or paginating until the full page can be represented correctly

### Requirement: Keep tab counts consistent
El sistema SHALL mantener consistente el número de cartas visibles con la respuesta del backend.

#### Scenario: Catalog count
- WHEN the backend reports N cards for a tab
- THEN the UI SHALL be able to display those N cards across its pages

## Non-goals

- No cambiar la fuente de datos del catálogo.
- No alterar el contenido de las cartas.
