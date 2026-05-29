# AI Proposal Spec: card-catalog-person-2

## Change name

card-catalog-person-2

## Purpose

Definir el trabajo de **Persona B** para el módulo de catálogo de cartas y mazos, con foco en validación de mazos, CRUD de decks y el adapter de carga para el engine.

Este cambio debe seguir la división indicada en `/divisionCatalogo.md` y respetar las reglas de `/openspec/config.yaml`.

## Mandatory context files

OpenCode MUST read and obey:

- `/openspec/config.yaml`
- `/docs/contracts_ai/00-contract-index.md`
- `/docs/contracts_ai/01-project-scope-contract.md`
- `/docs/contracts_ai/02-project-structure-contract.md`
- `/docs/contracts_ai/04-card-model-contract.md`
- `/docs/contracts_ai/05-deck-contract.md`
- `/docs/contracts_ai/06-game-state-contract.md`
- `/docs/contracts_ai/07-setup-flow-contract.md`
- `/docs/contracts_ai/08-game-action-contract.md`
- `/docs/contracts_ai/09-rule-validation-contract.md`
- `/docs/contracts_ai/10-attack-pipeline-contract.md`
- `/docs/contracts_ai/11-status-effects-contract.md`
- `/docs/contracts_ai/13-rest-api-contract.md`
- `/docs/contracts_ai/14-websocket-contract.md`
- `/docs/contracts_ai/15-frontend-state-contract.md`
- `/docs/contracts_ai/16-test-scenarios-contract.md`
- `/divisionCatalogo.md`
- `/openspec/specs/engine-persona1-contracts-and-gameengine.md`

## Architecture constraints

- Backend is the source of truth for deck validation and deck loading.
- Frontend only consumes server state and does not enforce deck rules.
- `DeckLoadAdapter` must stay isolated from Spring/JPA details except for repository access through the adapter implementation.
- No postponed features should be implemented: auth, JWT, ranking, chat, animations, multiple expansions, Mega Evolution.

## Package root

```
ar.edu.utn.frc.tup.piii
```

## Scope — clases a implementar o verificar

Todas viven bajo `BE/src/main/java/ar/edu/utn/frc/tup/piii/`.

### services/decks/

| Clase | Estado actual | Acción |
|-------|--------------|--------|
| `DeckService` | Stub existente | Completar |
| `DeckValidator` | Stub existente | Completar |
| `SeedDeckService` | Stub existente | Completar solo para dev/testing |

### controllers/decks/

| Clase | Estado actual | Acción |
|-------|--------------|--------|
| `DeckController` | Stub existente | Completar |

### mappers/

| Clase | Estado actual | Acción |
|-------|--------------|--------|
| `DeckMapper` | No existe | Crear |

### engine/ports/impl/

| Clase | Estado actual | Acción |
|-------|--------------|--------|
| `DeckLoadAdapter` | No existe | Crear |

## Requirements

### Requirement 1 — Deck validation must be reusable

El sistema MUST validar mazos con la misma lógica desde REST y desde el engine.

#### Scenarios

- Given un mazo con 60 cartas, máximo 4 copias por `cardId` y al menos un Pokémon básico, When se llama a la validación, Then el resultado debe ser válido.
- Given un mazo con menos de 60 cartas, When se llama a la validación, Then el resultado debe indicar que el mazo es inválido.
- Given un mazo con más de 4 copias de una misma carta, When se llama a la validación, Then el resultado debe indicar infracción por duplicados.

### Requirement 2 — DeckService must manage deck CRUD

El sistema MUST exponer la lógica de creación, lectura, actualización, eliminación y listado de mazos de un jugador.

#### Scenarios

- Given un `CreateDeckRequest` válido, When `createDeck` se ejecuta, Then el mazo debe persistirse.
- Given un `deckId` existente, When `getDeck` se ejecuta, Then debe devolver el mazo correspondiente.
- Given un `UpdateDeckRequest` inválido, When `updateDeck` se ejecuta, Then debe rechazar la operación.
- Given un `playerId`, When `listDecksByPlayer` se ejecuta, Then debe devolver solo los mazos de ese jugador.

### Requirement 3 — DeckController must expose deck endpoints

El sistema MUST exponer endpoints REST para CRUD y validación de mazos.

#### Scenarios

- Given una request `POST /api/decks`, When el payload es válido, Then el controlador debe crear el mazo.
- Given una request `GET /api/decks/{id}`, When el mazo existe, Then el controlador debe devolver su detalle.
- Given una request `POST /api/decks/{id}/validate`, When el mazo existe, Then el controlador debe devolver la validación.

### Requirement 4 — DeckLoadAdapter must load decks for the engine

El sistema MUST implementar `DeckLoadPort.loadDeck(UUID deckId)` mediante `DeckLoadAdapter`.

#### Scenarios

- Given un `deckId` existente, When el engine solicita el mazo, Then el adapter debe devolver el dominio `Deck`.
- Given un mazo inválido persistido por error, When `DeckLoadAdapter` lo carga, Then debe validarlo antes de entregarlo al engine.
- Given un `deckId` inexistente, When `loadDeck` se ejecuta, Then el adapter debe resolver el caso como error de carga.

### Requirement 5 — SeedDeckService is optional for dev/testing

El sistema SHOULD permitir crear mazos semilla solo para desarrollo o testing.

#### Scenarios

- Given el perfil `dev` activo, When arranca la aplicación, Then `SeedDeckService` puede precargar mazos.
- Given un perfil no dev, When arranca la aplicación, Then no debe ejecutarse la carga semilla.

## Explicit non-goals

No implementar en este change:

- Lógica externa de catálogo de cartas.
- Sincronización con la API Pokémon TCG.
- `CardLookupAdapter`.
- Engine, `GameEngine` o handlers de acciones.
- Autenticación, JWT, ranking, chat o animaciones.
- Nuevas expansiones o reglas fuera del MVP.

## Verification requirements

El change MUST terminar con:

1. `mvn compile` dentro de `BE/` sin errores.
2. `mvn test` dentro de `BE/` sin fallos.
3. Verificación de que `DeckLoadAdapter` solo usa el engine a través de la interfaz `DeckLoadPort`.

## Expected output

Generar este OpenSpec spec en:

```
openspec/specs/card-catalog-person-2.md
```

El archivo debe quedar listo para que OpenCode implemente el alcance de Persona B sin extenderse a otras personas del reparto.
