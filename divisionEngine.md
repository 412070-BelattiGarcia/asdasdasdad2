---

## Persona 1 — Contratos congelados + GameEngine

**Carpetas propias:** `engine/model/`, `engine/action/`, `engine/` (raíz), `engine/handlers/` (solo interface)

### Clases
- `engine/model/CardInstance`
- `engine/model/GameState`
- `engine/model/PlayerState`
- `engine/model/PokemonInPlay`
- `engine/model/TurnFlags`
- `engine/model/GamePhase`
- `engine/model/GameMetadata`
- `engine/model/MatchStatus`
- `engine/action/GameAction`
- `engine/action/GameActionType`
- `engine/action/GameActionPayload` *(interface marker)*
- `engine/action/AttachEnergyPayload`
- `engine/action/DeclareAttackPayload`
- `engine/action/RetreatPayload`
- `engine/action/PlayTrainerPayload`
- `engine/action/EvolvePokemonPayload`
- `engine/action/PutBasicOnBenchPayload`
- `engine/action/ActionResult`
- `engine/action/GameError`
- `engine/ErrorCode`
- `engine/PlayerSide`
- `engine/EngineContext`
- `engine/handlers/ActionHandler` *(interface)*
- `engine/GameEngine`

### Tareas
- Definir y congelar todos los modelos del estado.
- `PokemonInPlay` debe incluir `enteredTurnNumber` y `evolvedThisTurn`.
- `TurnFlags` debe incluir `hasPlayedSupporter`.
- Implementar `GameEngine.applyAction()`: idempotencia → validación → handler → endTurn → victory → persistir.
- Implementar `EngineContext` con `addEvent()` / `getEvents()`.
- Dejar `RuleValidator`, `SetupManager`, `VictoryConditionChecker`, `TurnManager` como stubs que compilan.
- **Este trabajo debe quedar compilando antes de que las demás personas arranquen.**

### RFs cubiertos
RF-19 (gestión de turnos durante la partida), RF-20 (validación de acciones y límites por turno).

### No toca
Catálogo, mazos, persistencia de cartas.

---

## Persona 2 — Setup, turno y reglas

**Carpetas propias:** `engine/setup/`, `engine/turn/`, `engine/rules/`

### Clases
- `engine/setup/SetupManager`
- `engine/turn/TurnManager`
- `engine/turn/TurnPhase`
- `engine/rules/RuleValidator`

### Tareas
- `SetupManager`: mezclar mazos via `RandomizerPort`, repartir 7 cartas iniciales,
  asignar 6 prizes, verificar si hay Pokémon básico en mano, resolver mulligan
  (sin básico → revelar al oponente → carta de bonus → repetir).
- `TurnManager`: controlar fases `DRAW → MAIN → ATTACK → BETWEEN_TURNS`,
  avanzar turno, cambiar `currentPlayerId`, resetear `TurnFlags`.
- `RuleValidator`: validar turno/fase general, y por acción:
  `validateAttachEnergy`, `validatePutBasicOnBench`, `validateEvolve`
  (usando `enteredTurnNumber` y `evolvedThisTurn`), `validatePlayTrainer`,
  `validateRetreat`, `validateAttack`.

### RFs cubiertos
RF-14 (inicialización del tablero y setup de partida), RF-15 (resolución de Mulligan), RF-19 (gestión de turnos durante la partida), RF-20 (validación de acciones y límites por turno)

### No toca
Catálogo, mazos, persistencia. Usa `CardLookupPort` como interface solamente
(necesita saber si una carta es Pokémon básico).

---

## Persona 3 — Handlers de acciones + combate + victoria

**Carpetas propias:** `engine/handlers/` (implementaciones), `engine/attack/`, `engine/victory/`

### Clases
- `engine/handlers/PutBasicOnBenchHandler`
- `engine/handlers/AttachEnergyHandler`
- `engine/handlers/EvolvePokemonHandler`
- `engine/handlers/PlayTrainerHandler`
- `engine/handlers/DeclareAttackHandler`
- `engine/handlers/RetreatActiveHandler`
- `engine/handlers/EndTurnHandler`
- `engine/handlers/ChooseNewActiveAfterKnockoutHandler`
- `engine/handlers/TakePrizeCardHandler`
- `engine/attack/AttackResolver`
- `engine/victory/VictoryConditionChecker`
- `engine/victory/FinishReason`

### Tareas
- Cada handler muta `GameState` via `EngineContext` y llama `ctx.addEvent()`. No retorna nada.
- `AttackResolver`: verificar energías requeridas, calcular daño base,
  aplicar debilidad (×2) y resistencia (−20), mínimo 0, aplicar daño a `PokemonInPlay`.
- `DeclareAttackHandler`: delegar a `AttackResolver`, detectar KO,
  si no hay banca setear ganador directamente.
- `VictoryConditionChecker`: las 3 condiciones (prizes agotados,
  sin Pokémon en juego, mazo vacío al robar).
- `TakePrizeCardHandler`: mover carta de prizes a hand del jugador que hizo KO.
- `ChooseNewActiveAfterKnockoutHandler`: mover Pokémon de bench a activo.

### RFs cubiertos
RF-16 (validación de Pokémon en banca), RF-17 (unión válida de energías), RF-18 (uso de ataques y validación de energías requeridas), RF-21 (evolución de Pokémon), RF-22 (uso de cartas Trainer), RF-23 (gestión de Knockout y reemplazo de Pokémon activo), RF-24 (detección de victoria y derrota)

### No toca
Catálogo, mazos, persistencia. Usa `CardLookupPort` como interface
(necesita HP y ataques de las cartas).

---

## Persona 4 — Ports del engine, capa Spring y persistencia del match

**Carpetas propias:** `engine/ports/`, `config/`, `controllers/`, `services/`, `persistence/`, `dtos/`

### Clases
- `engine/ports/CardLookupPort` *(interface — la implementación es del equipo de catálogo)*
- `engine/ports/RandomizerPort` *(interface + implementación propia)*
- `engine/ports/StatePersisterPort` *(interface + implementación propia)*
- `engine/ports/DeckLoadPort` *(interface — la implementación es del equipo de catálogo)*
- `engine/ports/impl/RandomizerAdapter`
- `engine/ports/impl/StatePersisterAdapter`
- `config/GameEngineConfig`
- `config/GlobalExceptionHandler`
- `controllers/MatchController`
- `controllers/GameActionController`
- `services/MatchApplicationService`
- `services/MatchQueryService`
- `persistence/MatchEntity`
- `persistence/MatchRepository`
- `persistence/GameStateConverter`
- `dtos/matches/MatchResponse`
- `dtos/matches/MatchStateResponse`
- `dtos/matches/GameActionRequest`
- `dtos/matches/GameActionResponse`
- `dtos/matches/CreateMatchRequest`
- `dtos/matches/JoinMatchRequest`
- `dtos/matches/SelectActiveRequest`
- `dtos/matches/TakePrizeRequest`

### Tareas
- Persona 4 es la única responsable de crear `engine/ports/CardLookupPort` y `engine/ports/DeckLoadPort` como interfaces stub.
- Estas interfaces deben existir y compilar antes de que nadie más arranque.
- El equipo de catálogo NO crea estas interfaces. Solo las implementa:
  - Persona A (de catálogo) implementa `engine/ports/impl/CardLookupAdapter`
  - Persona B (de catálogo) implementa `engine/ports/impl/DeckLoadAdapter`
- Nadie más toca archivos dentro de `engine/ports/`.
- `RandomizerAdapter`: implementación real con `Collections.shuffle`.
- `StatePersisterAdapter`: `saveState` y `loadState` sobre `MatchRepository`.
- `GameStateConverter`: serializar/deserializar `GameState` como JSON via Jackson.
- `MatchEntity`: columna `state TEXT` con `@Convert(converter = GameStateConverter.class)`.
  No tiene relación JPA con `CardEntity` ni `DeckEntity`.
- `GameEngineConfig`: registrar handlers de Persona 3, `RuleValidator`, `TurnManager`,
  `VictoryConditionChecker` como beans. Aquí se enchufará el `CardLookupAdapter`
  del equipo de catálogo cuando esté listo.
- `MatchApplicationService`: `createMatch` (recibe deckId, llama `DeckLoadPort.loadDeck()` —
  la validación del mazo la hace `DeckLoadAdapter` internamente, no repetirla acá —,
  llama `SetupManager`, persiste), `joinMatch`, `submitAction`.
- Endpoints REST: `POST /api/matches`, `POST /api/matches/{id}/join`,
  `GET /api/matches/{id}/state`, `POST /api/matches/{id}/actions`.
- `GlobalExceptionHandler`: `IllegalArgumentException` → 400, genérico → 500.
- Persistir únicamente el `GameState` actual del match.
- No implementar logging histórico, event sourcing ni auditoría de acciones en V1.

### RFs cubiertos
RF-10 (CardLookupPort para consulta desacoplada de cartas), RF-11 (DeckLoadPort para carga desacoplada de mazos), RF-12 (persistencia del estado actual de la partida), RF-13 (adaptadores e integración entre catálogo y engine)

### No toca
`CardEntity`, `DeckEntity`, `CardJpaRepository`, `DeckJpaRepository`,
endpoints de catálogo, sincronización con API externa.

---

## Criterio de reparto

| | Modelos | Lógica | Integraciones |
|---|---|---|---|
| Persona 1 | Alta | Media (GameEngine) | Ninguna |
| Persona 2 | Baja | Alta (reglas + setup) | 2 ports |
| Persona 3 | Baja | Alta (handlers + combate) | 1 port |
| Persona 4 | Baja | Media (Spring + persistencia) | Ports propios |

Persona 2 tiene pocas clases pero `RuleValidator` y `SetupManager`
son la lógica más densa del proyecto.
Persona 3 tiene muchos handlers pero cada uno sigue el mismo patrón.
Persona 4 conecta todo pero no implementa reglas de juego.