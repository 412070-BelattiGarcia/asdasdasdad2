# División Frontend — 4 Personas
# Pokémon TCG Digital (Angular 20)

> Basado en `FRONTEND_PLAN.md`. Dividido para máximo paralelismo.
> La infraestructura base (routing, HttpClient, STOMP, scaffolding) ya existe y compila.

---

## Principio de la división

El proyecto tiene dos cuellos de botella naturales que limitan el paralelismo con 2 personas:

- **FE-00** (alineación de modelos) bloquea a todos hasta que compila.
- **Shared components** los necesitan tanto cards/decks como match, y nadie los hacía en paralelo.

Con 4 personas la solución es:

| Persona | Dominio | Qué desbloquea |
|---------|---------|----------------|
| **Persona A** | Fundación: modelos + shared | A todos los demás |
| **Persona B** | Catálogo de cartas | A Persona C (DeckBuilder reutiliza búsqueda) |
| **Persona C** | Mazos | A Persona D (Lobby necesita lista de mazos) |
| **Persona D** | Lobby + Match (tablero y acciones) | Nada (es el destino final) |

Persona A arranca primero y en cuanto termina, Persona B, Persona C y Persona D pueden trabajar en paralelo casi sin bloquearse entre sí. El único punto de contacto es `CardRepositoryService` (Persona A lo crea) y `CardItemComponent` (Persona B lo crea y Persona C lo reutiliza en el Deck Builder).

---

## Enums congelados (referencia rápida)

```typescript
GameActionType = 'PUT_BASIC_ON_BENCH' | 'ATTACH_ENERGY' | 'EVOLVE_POKEMON' |
                 'PLAY_TRAINER' | 'DECLARE_ATTACK' | 'RETREAT_ACTIVE' |
                 'END_TURN' | 'TAKE_PRIZE_CARD'

TurnPhase     = 'DRAW' | 'MAIN' | 'ATTACK' | 'BETWEEN_TURNS'

MatchStatus   = 'WAITING' | 'SETUP' | 'ACTIVE' | 'FINISHED'

PlayerSide    = 'PLAYER_ONE' | 'PLAYER_TWO'

SpecialCondition = 'ASLEEP' | 'BURNED' | 'CONFUSED' | 'PARALYZED' | 'POISONED'
```

---

## Stack de estilos

El proyecto usa **Tailwind CSS** para estilos. No se usa CSS global ni SCSS.

---

## Reglas generales (aplican a todos)

- `ChangeDetectionStrategy.OnPush` en todos los componentes.
- `input()` y `output()` en lugar de `@Input()` / `@Output()`.
- `inject()` en lugar de inyección por constructor.
- `@if`, `@for`, `@switch` en templates (no `*ngIf`, `*ngFor`).
- No usar `any`. Usar `unknown` si el tipo es incierto.
- No llamar al backend desde componentes directamente — solo a través de services.
- El store (`GameStoreService`) es la única fuente de verdad durante una partida.
- Ningún componente calcula reglas, daño, ni valida acciones — eso es responsabilidad del backend.

---

## Convención para estados de carga y error

Todos usan el mismo patrón en signals locales:

```typescript
loading = signal(false);
error   = signal<string | null>(null);

// Al hacer una llamada:
this.loading.set(true);
this.error.set(null);
try { ... } 
catch { this.error.set('Mensaje amigable'); }
finally { this.loading.set(false); }
```

Los mensajes de error amigables para el usuario (nunca exponer el error técnico):

| Situación | Mensaje |
|-----------|---------|
| HTTP 4xx/5xx en carga | `"Error al cargar los datos. Intentá de nuevo."` |
| HTTP 4xx/5xx en guardado | `"No se pudo guardar. Intentá de nuevo."` |
| Error de red | `"Sin conexión con el servidor."` |
| Error de acción en partida | Usar `response.error.message` directamente (viene del BE) |

---

## Criterio de Done universal

Una tarea está terminada cuando:
- [ ] Compila sin errores TypeScript (`ng build` limpio).
- [ ] Tipado estricto (no `any`).
- [ ] Consume el endpoint correcto del backend.
- [ ] Maneja estados de carga y error.
- [ ] Usa `ChangeDetectionStrategy.OnPush`.
- [ ] Probada manualmente en el navegador.

---
---

# Persona A — Fundación: Modelos, Servicios Core y Shared Components

**Carpetas propias:**
`core/models/`, `core/api/`, `core/services/`, `shared/`

**Debe completarse antes de que cualquier otra persona avance en features.**
En cuanto terminen la Fase A (modelos + servicios), las demás personas pueden arrancar aunque Fase B (shared) aún esté en curso — los shared components se van entregando incrementalmente.

---

### Fase A — Alineación de modelos y servicios (FE-00)

> Prioridad máxima. Bloquea a todos hasta que `ng build` compile limpio.

| # | Tarea | Archivo | Criterio de done |
|---|-------|---------|-----------------|
| A.1 | Corregir `game-state.models.ts` | `core/models/game-state.models.ts` | `MatchStatus`, `TurnPhase`, `PlayerSide`, `PublicGameStateModel`, `PrivatePlayerStateModel`, `MatchStateResponse` alineados con el plan |
| A.2 | Corregir `game-action.models.ts` | `core/models/game-action.models.ts` | `GameActionType` sin `DRAW_CARD` / `CHOOSE_KNOCKOUT_REPLACEMENT`. `GameActionResponse` con `publicState`, `privateState`, `events`, `error` |
| A.3 | Crear `ui-state.models.ts` | `core/models/ui-state.models.ts` | `SelectionMode`, `SelectionState` definidos |
| A.4 | Corregir `card.models.ts` | `core/models/card.models.ts` | `CardSummaryResponse` y `CardDetailResponse` correctos (ver DTOs §4.1 del plan) |
| A.5 | Corregir `deck.models.ts` | `core/models/deck.models.ts` | `DeckResponse` con `ownerPlayerId`, `source`, `validation`. `CreateDeckRequest` correcto |
| A.6 | Corregir `card-api.service.ts` | `core/api/card-api.service.ts` | `searchCards()` usa `query`, `supertype`, `setCode`, `page`, `size`. `getCardById()` tipado a `CardDetailResponse` |
| A.7 | Corregir `deck-api.service.ts` | `core/api/deck-api.service.ts` | CRUD completo: `listByPlayer`, `create`, `get`, `update`, `delete`, `validate(id)`, `validateCards(req)` |
| A.8 | Corregir `match-api.service.ts` | `core/api/match-api.service.ts` | `createMatch({ playerName, deckId })`, `joinMatch({ playerName, deckId })`, `getState(matchId, playerId)`, `sendAction(matchId, req)` |
| A.9 | Crear `CardRepositoryService` | `core/services/card-repository.service.ts` | Caché de `CardDetailResponse` por `cardId`. `resolve(id)`, `preload(ids[])`, `getFromCache(id)` funcionan |
| A.10 | Corregir facades | `deck-builder-facade.service.ts`, `match-facade.service.ts` | Se adaptan a los modelos corregidos. `ng build` sin errores |

**Criterio de done Fase A:** `ng build` compila limpio. Tests existentes del `AppComponent` pasan.

---

### Fase B — Shared Components y Pipes

> Se entregan incrementalmente. Persona B y Persona C pueden arrancar con sus features básicas mientras esta fase está en curso. Persona D necesita `LoadingSpinnerComponent` y `NotificationComponent`.

| # | Tarea | Archivo | Inputs / Outputs | Especificación visual |
|---|-------|---------|------------------|-----------------------|
| B.1 | `LoadingSpinnerComponent` | `shared/components/loading-spinner/` | Sin inputs | Spinner centrado + texto "Cargando..." |
| B.2 | `ModalComponent` | `shared/components/modal/` | `title: string`, `open: boolean` / `closed: void` | Overlay oscuro + panel centrado + botón ✕ + `ClickOutsideDirective` |
| B.3 | `ButtonComponent` | `shared/components/button/` | `variant: 'primary'\|'secondary'\|'danger'\|'ghost'`, `disabled: boolean`, `loading: boolean` / click nativo | Aplica clase CSS según `variant`. Muestra spinner si `loading` |
| B.4 | `NotificationComponent` | `shared/components/notification/` | `message: string`, `type: 'info'\|'success'\|'warning'\|'error'`, `duration?: number` / `dismissed: void` | Snackbar en esquina inferior derecha. Auto-dismiss en `duration` ms (default 3000) |
| B.5 | `CardViewComponent` | `shared/components/card-view/` | `card: CardDetailResponse` | Ver especificación abajo |
| B.6 | `PokemonCardComponent` | `shared/components/pokemon-card/` | `card: CardDetailResponse` | Ver especificación abajo |
| B.7 | `CardImagePipe` | `shared/pipes/card-image.pipe.ts` | `cardId: string`, size `'small'\|'large'` | `xy1-1` → `https://images.pokemontcg.io/xy1/1.png` |
| B.8 | `EnergyIconPipe` | `shared/pipes/energy-icon.pipe.ts` | `EnergyType` | `'FIRE'` → `assets/icons/energy/energy-fire.svg` |
| B.9 | `ConditionIconPipe` | `shared/pipes/condition-icon.pipe.ts` | `SpecialCondition` | `'BURNED'` → `assets/icons/conditions/condition-burned.svg` |
| B.10 | `ClickOutsideDirective` | `shared/directives/click-outside.directive.ts` | Evento `clickOutside: void` | Emite cuando el click ocurre fuera del elemento host |
| B.11 | SVG assets de energía | `src/assets/icons/energy/` | — | 10 íconos SVG (uno por `EnergyType`). Pueden ser círculos de color con letra inicial |
| B.12 | SVG assets de condiciones | `src/assets/icons/conditions/` | — | 5 íconos SVG (uno por `SpecialCondition`). Pueden ser íconos simples |

#### CardViewComponent (B.5)

Vista compacta de carta para usar en catálogo y búsqueda:

```
┌──────────────────┐
│                  │
│    [imagen]      │  ← cardId | 'small' | CardImagePipe
│                  │
├──────────────────┤
│ Venusaur         │  ← card.name
│ POKEMON          │  ← card.supertype
│ XY Base Set      │  ← card.setCode
└──────────────────┘
```

- Si imagen falla: placeholder gris con `card.name` centrado.
- `ChangeDetectionStrategy.OnPush`.

#### PokemonCardComponent (B.6)

Vista de detalle completo. Usada en `CardDetailPage` y como preview en el tablero:

```
┌──────────────────────────────────────────┐
│  [imagen grande]     Venusaur            │
│                      POKEMON · Stage 2   │
│                      HP: 230             │
│                      XY Base Set         │
│                                          │
│  ── Ataques ──                           │
│  Frenzy Plant  [G][G][G]  150            │
│  (texto del ataque)                      │
│                                          │
│  ── Debilidad / Resistencia ──           │
│  Debilidad: [F] ×2                       │
│  Retiro: [C][C]                          │
│                                          │
│  [EX] [MEGA]    ← badges si aplica       │
└──────────────────────────────────────────┘
```

- Los íconos de energía usan `EnergyIconPipe`.
- Badges `[EX]` si `card.isEx === true`, `[MEGA]` si `card.isMega === true`.

#### NotificationComponent (B.4) — comportamiento

El componente vive en `AppComponent` (una sola instancia global). Las demás personas lo invocan inyectando un `NotificationService` (también creado por Persona A):

```typescript
// core/services/notification.service.ts
@Injectable({ providedIn: 'root' })
export class NotificationService {
  private _notifications = signal<Notification[]>([]);
  readonly notifications = this._notifications.asReadonly();

  show(message: string, type: NotificationType, duration = 3000): void { ... }
  dismiss(id: string): void { ... }
}
```

Las demás personas llaman `notificationService.show('mensaje', 'error')` desde sus páginas.

---

### No toca
Ningún archivo de `features/`. No implementa lógica de negocio ni reglas de juego.

---

### Resumen de archivos Persona A

| Fase | Archivos |
|------|---------|
| A (modelos) | `card.models.ts`, `deck.models.ts`, `game-state.models.ts`, `game-action.models.ts`, `ui-state.models.ts` |
| A (servicios) | `card-api.service.ts`, `deck-api.service.ts`, `match-api.service.ts`, `card-repository.service.ts`, `notification.service.ts` |
| A (facades) | `deck-builder-facade.service.ts`, `match-facade.service.ts` |
| B (shared) | `card-view/`, `pokemon-card/`, `loading-spinner/`, `modal/`, `button/`, `notification/`, pipes × 3, `click-outside.directive.ts`, SVG assets |

---
---

# Persona B — Catálogo de Cartas

**Carpetas propias:** `features/cards/`

**Dependencias:** Persona A Fase A (modelos + servicios). Los shared components de Persona A Fase B se consumen según se entregan.

**No toca:** `features/decks/`, `features/lobby/`, `features/match/`, `core/`.

---

### FE-01 — Card Catalog Page

**Archivos:**
- `features/cards/pages/card-catalog-page/card-catalog-page.component.ts`
- `features/cards/components/card-search/card-search.component.ts`
- `features/cards/components/card-filter/card-filter.component.ts`
- `features/cards/components/card-grid/card-grid.component.ts`
- `features/cards/components/card-item/card-item.component.ts`

#### CardItemComponent

**Inputs:** `card: CardSummaryResponse`
**Outputs:** `selected: CardSummaryResponse`

Wrapper delgado sobre `CardViewComponent` (Persona A). Agrega comportamiento de click y estado de hover/selección.

#### CardSearchComponent

**Inputs:** `initialQuery?: string`
**Outputs:** `queryChange: string`

```
[🔍 ________________]   ← input con debounce 300ms
```

Emite `queryChange` con `debounceTime(300)`.

#### CardFilterComponent

**Inputs:** `initialSupertype?: string`, `initialSetCode?: string`
**Outputs:** `filtersChange: { supertype: string, setCode: string }`

```
[Tipo: ▼ Todos]   [Set: ________]
```

Select con opciones: `Todos`, `POKEMON`, `ENERGY`, `TRAINER`.

#### CardGridComponent

**Inputs:** `cards: CardSummaryResponse[]`, `loading: boolean`
**Outputs:** `cardSelected: CardSummaryResponse`

```
┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐
│      │ │      │ │      │ │      │
└──────┘ └──────┘ └──────┘ └──────┘
```

Responsive: Desktop (≥ 1024px) 4 col · Tablet (≥ 600px) 2 col · Mobile 1 col.
Si `loading`: superponer `LoadingSpinnerComponent`.

#### CardCatalogPage

Layout:

```
┌──────────────────────────────────────────────────┐
│  [CardSearchComponent]  [CardFilterComponent]     │
├──────────────────────────────────────────────────┤
│  CardGridComponent                                │
│  (4 columnas en desktop)                          │
├──────────────────────────────────────────────────┤
│  [← Anterior]   Página 2 de 8   [Siguiente →]    │
└──────────────────────────────────────────────────┘
```

Signals locales: `cards`, `loading`, `error`, `currentPage`, `totalPages`.

Comportamiento:
- Al cambiar filtros: resetear `currentPage` a `0` (el backend usa base-0).
- Cambiar página: llamar `CardApiService.searchCards()` con page actualizada.
- Click en carta: navegar a `/cards/{id}` (no modal — es ruta separada).
- Error: banner con mensaje amigable + botón "Reintentar".

**Criterio de done:**
- [ ] Entrar a `/cards` carga y muestra cartas.
- [ ] Búsqueda por nombre con debounce funciona.
- [ ] Filtros por supertype y set funcionan combinados.
- [ ] Cambiar filtro resetea a página 1.
- [ ] Paginación avanza y retrocede.
- [ ] Click en carta navega a `/cards/{id}`.
- [ ] Error muestra mensaje amigable + Reintentar.

---

### FE-02 — Card Detail Page

**Archivos:**
- `features/cards/pages/card-detail-page/card-detail-page.component.ts`

Lee `cardId` del `ActivatedRoute`. Llama `CardApiService.getCardById(cardId)`. Usa `PokemonCardComponent` (Persona A) para renderizar.

```
┌──────────────────────────────────────┐
│  [← Volver al catálogo]              │
│                                      │
│  PokemonCardComponent                │
│  (detalle completo)                  │
└──────────────────────────────────────┘
```

- Mientras carga: `LoadingSpinnerComponent`.
- Error: `"No se encontró la carta."` + botón "Volver".

**Criterio de done:**
- [ ] Navegar a `/cards/{id}` muestra el detalle de la carta.
- [ ] Todos los campos del `CardDetailResponse` son visibles.
- [ ] Botón "Volver" navega a `/cards`.
- [ ] Error 404 muestra mensaje amigable.

---

### No toca Persona B
Mazos, lobby, match, modelos, servicios core. No modifica nada fuera de `features/cards/`.

---
---

# Persona C — Gestión de Mazos

**Carpetas propias:** `features/decks/`

**Dependencias:** Persona A Fase A (modelos + servicios). `CardItemComponent` de Persona B (para el panel de búsqueda del DeckBuilder) — coordinar entrega con Persona B.

**No toca:** `features/cards/`, `features/lobby/`, `features/match/`, `core/`.

---

### FE-03 — Deck List Page

**Archivos:**
- `features/decks/pages/deck-list-page/deck-list-page.component.ts`
- `features/decks/components/deck-list/deck-list.component.ts`
- `features/decks/components/deck-item/deck-item.component.ts`
- `features/decks/components/deck-validation/deck-validation.component.ts`

#### DeckValidationComponent

**Inputs:** `validation: DeckValidationModel`

```
✅ Válido          ← si validation.valid === true (badge verde)

❌ Inválido        ← si validation.valid === false (badge rojo)
```

Solo muestra el badge. Los errores detallados van en `DeckSummaryComponent` (FE-04).

#### DeckItemComponent

**Inputs:** `deck: DeckResponse`
**Outputs:** `edit: DeckResponse`, `delete: DeckResponse`, `validate: DeckResponse`, `play: DeckResponse`

```
┌──────────────────────────────────────────────────────┐
│  Grass Beatdown                        ✅ Válido      │
│  60 cartas  ·  SEED                                  │
│                 [Validar]  [Editar]  [Eliminar]  [▶] │
└──────────────────────────────────────────────────────┘
```

- `[▶]` (Jugar) solo visible si `deck.valid === true`.
- `[Eliminar]` usa `ButtonComponent` con `variant='danger'`.

#### DeckListComponent

**Inputs:** `decks: DeckResponse[]`
**Outputs:** `edit: DeckResponse`, `delete: DeckResponse`, `validate: DeckResponse`, `play: DeckResponse`

Lista vertical de `DeckItemComponent`. Si `decks.length === 0`: mostrar mensaje "No hay mazos disponibles."

#### DeckListPage

Layout:

```
┌──────────────────────────────────────────────┐
│  Mis mazos                  [+ Nuevo mazo]   │
│  Jugador: [______________]  [Cargar mazos]   │  ← input de playerId
├──────────────────────────────────────────────┤
│  DeckListComponent                           │
└──────────────────────────────────────────────┘
```

- Campo `playerId`: input de texto simple (o usar `'player-dev'` fijo si no se ingresa).
- Al hacer click en "Cargar mazos" (o al cambiar el campo): llamar `DeckApiService.listByPlayer(playerId)`.
- `[+ Nuevo mazo]` → navegar a `/decks/new`.
- Evento `edit` → navegar a `/decks/{id}/edit`.
- Evento `play` → navegar a `/lobby?deckId={id}`.
- Evento `validate` → llamar `DeckApiService.validate(id)`. Actualizar el `deck.validation` en la lista local. Mostrar snackbar con `NotificationService`.
- Evento `delete` → mostrar confirmación inline. Al confirmar: llamar `DeckApiService.delete(id)`, remover de la lista local, mostrar snackbar.

**Confirmación de eliminación inline:**
```
┌────────────────────────────────────────────┐
│  ¿Eliminar "Grass Beatdown"?               │
│                  [Cancelar]  [Eliminar ⚠️] │
└────────────────────────────────────────────┘
```

**Criterio de done:**
- [ ] Ingresar `playerId` y cargar lista de mazos.
- [ ] Badge válido/inválido correcto en cada mazo.
- [ ] `[+ Nuevo mazo]` navega a `/decks/new`.
- [ ] `[Editar]` navega a `/decks/{id}/edit`.
- [ ] `[Jugar]` navega a `/lobby?deckId={id}` y solo aparece si el mazo es válido.
- [ ] `[Validar]` actualiza el badge y muestra snackbar.
- [ ] `[Eliminar]` pide confirmación, elimina y remueve de la lista.
- [ ] Error de carga muestra mensaje + Reintentar.

---

### FE-04 — Deck Builder Page

**Archivos:**
- `features/decks/pages/deck-builder-page/deck-builder-page.component.ts`
- `features/decks/components/deck-search/deck-search.component.ts`
- `features/decks/components/deck-card-list/deck-card-list.component.ts`
- `features/decks/components/deck-summary/deck-summary.component.ts`

#### DeckSearchComponent

**Outputs:** `cardSelected: CardSummaryResponse`

Panel de búsqueda de cartas para agregar al mazo. Internamente usa `CardApiService.searchCards()`. Reutiliza `CardItemComponent` (Persona B) para mostrar resultados. Click en carta emite `cardSelected`.

> Coordinar con Persona B: necesita `CardItemComponent` entregado. Mientras tanto, puede mockear el componente con un div simple para no bloquearse.

```
[🔍 Buscar carta...]
┌──────────────────────────────────┐
│ Venusaur  · POKEMON · xy1       │  ← CardItemComponent (de Persona B)
│ Charizard · POKEMON · base1     │
│ ...                              │
└──────────────────────────────────┘
```

#### DeckCardListComponent

**Inputs:** `cards: DeckCardModel[]`
**Outputs:** `add: string` (cardId), `remove: string` (cardId)

```
Venusaur       ×2    [−] [+]
Fire Energy    ×4    [−] [+]
Potion         ×1    [−] [+]
```

- `[+]` deshabilitado si `quantity >= 4`.
- `[−]` siempre habilitado. Si `quantity === 1` y se hace `[−]`, la carta sale de la lista.

#### DeckSummaryComponent

**Inputs:** `totalCards: number`, `validation: DeckValidationModel | null`

```
── Resumen ──
Total: 58 / 60 cartas

── Estado ──
❌ Inválido
  • El mazo tiene 58 cartas. Se requieren 60.
  • Charizard EX aparece 5 veces. Máximo: 4.
```

Si `validation === null`: mostrar "Aún no validado."
Si `validation.valid === true`: mostrar "✅ Listo para jugar."

#### DeckBuilderPage

Layout dos paneles:

```
┌─────────────────────┬──────────────────────────────────┐
│  BUSCAR CARTAS      │  MI MAZO                         │
│  DeckSearchComponent│  Nombre: [_____________________] │
│                     │  DeckSummaryComponent            │
│                     ├──────────────────────────────────┤
│                     │  DeckCardListComponent           │
│                     ├──────────────────────────────────┤
│                     │  [Validar]          [Guardar]    │
└─────────────────────┴──────────────────────────────────┘
```

En mobile (< 600px): paneles apilados, búsqueda arriba.

**Modo creación** (`/decks/new`): parte de mazo vacío.
**Modo edición** (`/decks/:id/edit`): leer `id` del route, llamar `DeckApiService.get(id)`, cargar en `DeckBuilderFacadeService`.

Comportamiento:
- Click en carta en `DeckSearchComponent` → `DeckBuilderFacadeService.addCard(cardId, name, supertype)`.
- `[+]` / `[−]` en `DeckCardListComponent` → facade.
- `[Validar]`:
  - Si tiene `id` (edición): `DeckApiService.validate(id)`.
  - Si es nuevo: `DeckApiService.validateCards({ cards })`.
  - Actualizar `DeckSummaryComponent`.
- `[Guardar]`:
  - Modo creación: `DeckApiService.create(req)` → navegar a `/decks`.
  - Modo edición: `DeckApiService.update(id, req)` → navegar a `/decks`.
  - Deshabilitado si `isEmpty` o nombre vacío.

**Criterio de done:**
- [ ] `/decks/new` parte de mazo vacío.
- [ ] `/decks/:id/edit` carga las cartas del mazo.
- [ ] Buscar y agregar cartas funciona.
- [ ] `[+]` / `[−]` modifican cantidades. `[−]` en 1 elimina la carta.
- [ ] Contador se actualiza en tiempo real.
- [ ] `[Validar]` muestra errores o badge de éxito.
- [ ] `[Guardar]` con nombre vacío no envía.
- [ ] Guardar exitoso redirige a `/decks`.
- [ ] Error al guardar muestra snackbar sin redirigir.

---

### No toca Persona C
Catálogo de cartas, lobby, match, modelos, servicios core. No modifica nada fuera de `features/decks/`.

---
---

# Persona D — Lobby + Match (Tablero de Juego)

**Carpetas propias:** `features/lobby/`, `features/match/`

**Dependencias:**
- Persona A Fase A (modelos + servicios, incluido `GameStoreService`, `UiStoreService`, `GameActionDispatcherService`).
- Persona A Fase B: `LoadingSpinnerComponent`, `NotificationComponent`, `ButtonComponent`, `EnergyIconPipe`, `ConditionIconPipe`.
- No depende de Persona B ni Persona C — puede arrancar en paralelo con ambos desde que Persona A Fase A termina.

**Nota:** `GameStoreService`, `UiStoreService` y `GameActionDispatcherService` ya existen como stubs (o Persona A los crea en Fase A). Persona D los **implementa** completamente.

**No toca:** `features/cards/`, `features/decks/`, `core/models/`, `core/api/`.

---

### FE-05 — Stores y Dispatcher (base del tablero)

> Completar antes de empezar FE-06. No genera UI visible.

**Archivos:**
- `features/match/services/game-store.service.ts`
- `features/match/services/ui-store.service.ts`
- `features/match/services/game-action-dispatcher.service.ts`

#### GameStoreService

Signals del estado de servidor. Implementar exactamente como se define en §8.2 del plan:

```typescript
// Signals privados
_publicState  = signal<PublicGameStateModel | null>(null)
_privateState = signal<PrivatePlayerStateModel | null>(null)
_events       = signal<GameEventDto[]>([])
_lastError    = signal<GameErrorModel | null>(null)

// Computed
isMyTurn       = computed(() => publicState()?.currentPlayerId === privateState()?.playerId)
currentPhase   = computed(() => publicState()?.phase ?? null)
myActivePokemon    = computed(...)
opponentActivePokemon = computed(...)

// Métodos
updatePublicState(state), updatePrivateState(state), addEvent(event),
setError(error), reset()
```

#### UiStoreService

Signals del estado visual. Implementar exactamente como §8.3 del plan:

```typescript
// Signals privados
_selection      = signal<SelectionState>({ mode: 'NONE', ... })
_hoveredCard    = signal<string | null>(null)
_modalOpen      = signal<boolean>(false)
_modalContent   = signal<...>(null)
_actionInProgress = signal<boolean>(false)

// Computed
isSelecting = computed(() => selection().mode !== 'NONE')
canInteract = computed(() => !actionInProgress() && !modalOpen())

// Métodos
enterSelectBenchSlot(handIndex, validTargets)
enterSelectTargetPokemon(handIndex, targets)
enterSelectRetreatTarget(validTargets)
cancelSelection()
startAction(requestId), completeAction()
openModal(content), closeModal()
```

#### GameActionDispatcherService

Implementar exactamente como §9.1 del plan. Métodos convenientes sobre `dispatchAction()`:

```typescript
drawCard(matchId, playerId)
endTurn(matchId, playerId)
attachEnergy(matchId, playerId, handIndex, targetInstanceId)
putBasicOnBench(matchId, playerId, handIndex)
evolvePokemon(matchId, playerId, handIndex, targetInstanceId)
playTrainer(matchId, playerId, handIndex, targetInstanceId?)
retreatActive(matchId, playerId, benchIndex)
declareAttack(matchId, playerId, attackIndex, targetInstanceId)
```

Regla: si `uiStore.actionInProgress()` es `true`, ignorar la llamada (log de advertencia, no error).

**Criterio de done FE-05:**
- [ ] `GameStoreService`: signals se actualizan correctamente. `isMyTurn` computed funciona.
- [ ] `UiStoreService`: todos los modos de selección funcionan. `canInteract` computed correcto.
- [ ] `GameActionDispatcherService`: `dispatchAction` no dispara si hay acción en progreso. Actualiza stores al recibir respuesta.
- [ ] Tests unitarios de los tres servicios pasan.

---

### FE-06 — Lobby Page

**Archivos:**
- `features/lobby/pages/lobby-page/lobby-page.component.ts`
- `features/lobby/components/match-create/match-create.component.ts`
- `features/lobby/components/match-join/match-join.component.ts`
- `features/lobby/components/match-list/match-list.component.ts`

#### MatchCreateComponent

**Outputs:** `created: MatchResponse`

```
┌─────────────────────────────────────────┐
│  Crear partida                          │
│  Tu nombre: [_________________________] │
│  Mazo:      [▼ Grass Beatdown        ▼] │
│                         [Crear partida] │
└─────────────────────────────────────────┘
```

- Puebla el select de mazos llamando `DeckApiService.listByPlayer('player-dev')` (o el playerId que se ingrese).
- Si llegó con `?deckId=` en la URL: pre-seleccionar ese mazo.
- Llama `MatchApiService.createMatch({ playerName, deckId })`.
- Emite `created` con el `MatchResponse` recibido.

#### MatchJoinComponent

**Outputs:** `joined: MatchResponse`

```
┌─────────────────────────────────────────┐
│  Unirse a partida                       │
│  ID de la partida: [__________________] │
│  Tu nombre:        [__________________] │
│  Mazo:             [▼ Grass Beatdown ▼] │
│                          [Unirse]       │
└─────────────────────────────────────────┘
```

#### MatchListComponent

```
┌──────────────────────────────────────────────────────┐
│  Partidas disponibles            [↻ Actualizar]      │
├──────────────────────────────────────────────────────┤
│  match-abc123   |  Esperando jugador   [Usar este]   │
│  match-def456   |  Esperando jugador   [Usar este]   │
├──────────────────────────────────────────────────────┤
│  No hay partidas disponibles.                        │
└──────────────────────────────────────────────────────┘
```

Click en `[Usar este]` copia el `matchId` al campo de `MatchJoinComponent`.

#### LobbyPage

```
┌────────────────────────────────────────────────────────┐
│  ┌───────────────────────┐  ┌────────────────────────┐ │
│  │  MatchCreateComponent │  │  MatchListComponent    │ │
│  │                       │  │                        │ │
│  └───────────────────────┘  └────────────────────────┘ │
│  ┌──────────────────────────────────────────────────┐   │
│  │  MatchJoinComponent                              │   │
│  └──────────────────────────────────────────────────┘   │
└────────────────────────────────────────────────────────┘
```

Cuando `created` o `joined` se emite:
1. Guardar `matchId` y `playerId` en `MatchFacadeService`.
2. Llamar `GameStoreService.reset()` para limpiar estado previo.
3. Navegar a `/match/{matchId}`.

**Criterio de done:**
- [ ] Crear partida con nombre y mazo funciona. Navega a `/match/{id}`.
- [ ] Unirse con ID de partida existente funciona. Navega a `/match/{id}`.
- [ ] Si llegó con `?deckId=`, el mazo queda pre-seleccionado en crear.
- [ ] Error al crear/unirse muestra snackbar sin navegar.

---

### FE-07 — Match Page: Estructura del Tablero

**Archivos:**
- `features/match/pages/match-page/match-page.component.ts`
- `features/match/components/match-header/match-header.component.ts`
- `features/match/components/opponent-area/opponent-area.component.ts`
- `features/match/components/player-area/player-area.component.ts`
- `features/match/components/active-pokemon-slot/active-pokemon-slot.component.ts`
- `features/match/components/bench-zone/bench-zone.component.ts`
- `features/match/components/prize-zone/prize-zone.component.ts`
- `features/match/components/game-log/game-log.component.ts`
- `features/match/components/pokemon-slot/pokemon-slot.component.ts`

#### MatchHeaderComponent

**Inputs:** `publicState: PublicGameStateModel`, `myPlayerId: string`

```
┌───────────────────────────────────────────────────────────┐
│  Turno 3  |  Fase: MAIN  |  Jugador actual: Santi  |  ⏳  │
└───────────────────────────────────────────────────────────┘
```

`⏳` si no es el turno del jugador, `⭐` si es su turno.

#### PokemonSlotComponent

**Inputs:** `pokemon: PublicPokemonSlotModel | null`, `cardDef: CardDetailResponse | null`, `isActive: boolean`, `isOwn: boolean`

Slot vacío:
```
┌──────────────────┐
│   (slot vacío)   │
└──────────────────┘
```

Slot ocupado:
```
┌──────────────────────┐
│  [imagen]            │
│  Charizard           │
│  HP: 90 / 210        │  ← 210 = cardDef.hp, 90 = hp - damageCounters
│  [F][F] · 🔥 BURNED  │  ← attachedCards count + specialConditions con ConditionIconPipe
│  [T]                 │  ← si hay herramienta equipada
└──────────────────────┘
```

- Usar `cardId | cardImage : 'small' | CardImagePipe`.
- **No calcula daño**: HP mostrado = `cardDef.hp - pokemon.damageCounters`. El componente hace solo esa resta aritmética para display, no lógica de juego.
- Si `cardDef === null` (aún no resuelto del `CardRepositoryService`): mostrar `LoadingSpinnerComponent`.

#### BenchZoneComponent

**Inputs:** `bench: PublicPokemonSlotModel[]`, `cardDefs: Map<string, CardDetailResponse>`, `isOwn: boolean`, `validTargets: string[]`
**Outputs:** `slotSelected: PublicPokemonSlotModel | null` (null = slot vacío seleccionado)

5 slots siempre visibles. Slots con `instanceId` en `validTargets` muestran borde dorado.

#### PrizeZoneComponent

**Inputs:** `prizeCount: number`, `isOwn: boolean`

```
■ ■ ■ ■ ■ ■   ← 6 premios llenos (ocultos)
■ ■ □ □ □ □   ← 2 premios restantes
```

Si `isOwn`: colores vivos. Si `!isOwn`: colores apagados.

#### GameLogComponent

**Inputs:** `events: GameEventDto[]`

```
┌──────────────────────────────────────────────────────┐
│  > Santi attached Fire Energy to Slugma.             │
│  > Slugma dealt 60 damage to Froakie.                │
│  > Turno 3 - Fase MAIN                               │
└──────────────────────────────────────────────────────┘
```

Muestra los últimos 10 eventos. El más reciente primero. Solo usa `event.message`.

#### OpponentAreaComponent / PlayerAreaComponent

**Inputs (opponent):** `playerState: PublicPlayerStateModel`, `cardDefs: Map<string, CardDetailResponse>`, `validTargets: string[]`
**Inputs (player):** igual + `privateState: PrivatePlayerStateModel`
**Outputs:** `pokemonSelected: PublicPokemonSlotModel`

Componen `PokemonSlotComponent` (activo) + `BenchZoneComponent` + `PrizeZoneComponent` + contadores de mazo/descarte.

#### MatchPage — Polling y estructura

Flujo al inicializar:
1. Leer `matchId` del route.
2. Leer `playerId` de `MatchFacadeService.playerId`.
3. Si `playerId` es null: redirigir a `/lobby`.
4. Llamar `MatchApiService.getState(matchId, playerId)` → `GameStoreService.updatePublicState()` / `updatePrivateState()`.
5. Iniciar polling: `setInterval(() => pollState(), 2000)`.
6. Precargar `cardDefs` de todos los Pokémon en juego con `CardRepositoryService.preload(cardIds)`.

Al destruir: `clearInterval(pollingInterval)`.

Cuando `publicState.status === 'FINISHED'`: mostrar overlay de victoria (FE-08 lo implementa).

**Criterio de done FE-07:**
- [ ] Entrar a `/match/:id` sin `playerId` redirige a `/lobby`.
- [ ] Tablero se renderiza con todas las zonas del wireframe del plan.
- [ ] Polling actualiza el tablero cada 2 segundos.
- [ ] Pokémon activo, banca, premios y log se muestran correctamente.
- [ ] `CardRepositoryService.preload()` se llama al iniciar.

---

### FE-08 — Match Page: Acciones e Interacciones

**Archivos:**
- `features/match/components/hand-zone/hand-zone.component.ts`
- `features/match/components/action-panel/action-panel.component.ts`

#### HandZoneComponent

**Inputs:** `hand: PrivateHandCardModel[]`, `selectionMode: SelectionMode`, `validTargets: string[]`
**Outputs:** `cardClicked: { card: PrivateHandCardModel, handIndex: number }`

```
┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐
│Fire E│ │Potion│ │Venusaur│ │...  │
│ENERGY│ │TRAINER│ │POKEMON│ │     │
└──────┘ └──────┘ └──────┘ └──────┘
```

- Cada carta muestra nombre y supertype.
- Si `selectionMode !== 'NONE'` y la carta no está en `validTargets`: opacidad 50%, no clickeable.
- Carta seleccionada (la que inició la selección): borde dorado.

#### ActionPanelComponent

**Inputs:** `gameStore: GameStoreService`, `uiStore: UiStoreService`, `matchId: string`, `playerId: string`

```
─── Si !isMyTurn ────────────────────────────────────
  ⏳ Esperando al oponente...

─── Si isMyTurn, phase = 'MAIN' ─────────────────────
  [Colocar en banca]  [Adjuntar energía]
  [Evolucionar]       [Jugar Entrenador]
  [Retirar activo]    [Fin del turno]

─── Si isMyTurn, phase = 'ATTACK' ───────────────────
  [Frenzy Plant — 150]       ← gameStore.myActivePokemon cardDef
  [Tackle — 30]
  [Fin del turno sin atacar]

─── Si hay acción pendiente (actionInProgress) ──────
  Todos los botones deshabilitados + spinner
```

**Flujos de cada acción:**

**Colocar en banca** (`PUT_BASIC_ON_BENCH`):
1. `uiStore.enterSelectBenchSlot(handIndex, [])` al hacer click en carta de la mano en `HandZoneComponent`.
2. El slot de banca vacío recibe el click → `dispatcher.putBasicOnBench(matchId, playerId, handIndex)`.

**Adjuntar energía** (`ATTACH_ENERGY`):
1. Click en carta de energía en la mano → `uiStore.enterSelectTargetPokemon(handIndex, allPokemonInstanceIds)`.
2. Click en Pokémon (activo o banca) → `dispatcher.attachEnergy(matchId, playerId, handIndex, instanceId)`.

**Evolucionar** (`EVOLVE_POKEMON`):
1. Click en carta de evolución en la mano → `uiStore.enterSelectTargetPokemon(handIndex, allPokemonInstanceIds)`.
2. Click en Pokémon objetivo → `dispatcher.evolvePokemon(matchId, playerId, handIndex, instanceId)`.

**Jugar Entrenador** (`PLAY_TRAINER`):
1. Click en carta de tipo `TRAINER` en la mano.
2. Si `subtypes` incluye `POKEMON_TOOL`: entra en modo `SELECT_TARGET_POKEMON`, igual que energía.
3. Si no requiere target: `dispatcher.playTrainer(matchId, playerId, handIndex)` directamente.

**Retirar activo** (`RETREAT_ACTIVE`):
1. Click en `[Retirar activo]` → `uiStore.enterSelectRetreatTarget(benchInstanceIds)`.
2. Click en slot de banca → `dispatcher.retreatActive(matchId, playerId, benchIndex)`.

**Declarar ataque** (`DECLARE_ATTACK`):
1. Click en botón de ataque → `dispatcher.declareAttack(matchId, playerId, attackIndex, opponentActiveInstanceId)`.

**Fin de turno** (`END_TURN`):
1. Click → `uiStore.openModal('confirm-end-turn')`.
2. Confirmar en modal → `dispatcher.endTurn(matchId, playerId)`.

**Cancelar selección**: botón `[Cancelar]` visible cuando `uiStore.isSelecting()`. También cancela con tecla ESC (usar `HostListener`).

**Overlay de victoria:**
```
┌──────────────────────────────────────┐
│          🏆 Fin de partida           │
│   ¡player-1 ganó la partida!         │
│                                      │
│         [Volver al lobby]            │
└──────────────────────────────────────┘
```

Mostrar cuando `publicState.status === 'FINISHED'`. `[Volver al lobby]` → `GameStoreService.reset()` + navegar a `/lobby`.

**Criterio de done FE-08:**
- [ ] Cada acción del plan se puede enviar desde la UI.
- [ ] El flujo de selección (click carta → highlight targets → click target → dispatch) funciona.
- [ ] `[Cancelar]` y ESC cancelan la selección correctamente.
- [ ] Cuando hay una acción en progreso, todos los botones se deshabilitan.
- [ ] Error de acción muestra snackbar con `error.message`.
- [ ] Overlay de victoria aparece al terminar la partida y vuelve al lobby.

---

### No toca Persona D
Catálogo, mazos, modelos, servicios core. No modifica nada fuera de `features/lobby/` y `features/match/`.

---
---

## Tests

### Tests de servicios

| Task | Persona | Services a testear |
|------|---------|-------------------|
| FE-T1 | Persona A | `CardRepositoryService`, `NotificationService` |
| FE-T2 | Persona B | `CardApiService` |
| FE-T3 | Persona C | `DeckApiService`, `DeckBuilderFacadeService` |
| FE-T4 | Persona D | `GameStoreService`, `UiStoreService`, `GameActionDispatcherService`, `MatchApiService`, `MatchFacadeService` |

Estrategia: mockear `ApiClientService` con Jasmine spies (no usar `HttpTestingController`):

```typescript
const mockApi = jasmine.createSpyObj('ApiClientService', ['get', 'post', 'put', 'delete']);
mockApi.get.and.returnValue(of({ items: [], page: 0, size: 20, totalItems: 0 }));
```

### Tests de componentes de página

| Task | Persona | Componentes |
|------|---------|-------------|
| FE-T5 | Persona B | `CardCatalogPage`, `CardDetailPage` |
| FE-T6 | Persona C | `DeckListPage`, `DeckBuilderPage` |
| FE-T7 | Persona D | `LobbyPage`, `MatchPage` |
| FE-T8 | Ambos | Test E2E: crear mazo → lobby → match → turno |

Cada page debe tener al menos 3 casos: carga OK, estado vacío, error HTTP.

---

## Resumen de paralelismo

```
Persona A Fase A ──────────────────────── [BLOQUEA TODO]
  │
  ├── Persona B ──── card-catalog ──── card-detail
  │
  ├── Persona C ──── deck-list ──── deck-builder
  │
  └── Persona D ──── FE-05 (stores) ──── FE-06 (lobby) ──── FE-07 (tablero) ──── FE-08 (acciones)

Persona A Fase B (shared) ──────────── Entrega incremental mientras Persona B/B/D trabajan
```

### Batches de trabajo

| Batch | Quién | Qué | Desbloquea |
|-------|-------|-----|------------|
| 1 | Persona A | Fase A (modelos + servicios) | A todos |
| 2 | Persona A (Fase B) + Persona B (FE-01) + Persona C (FE-03) + Persona D (FE-05) | Shared + Catálogo + Deck List + Stores | FE-02, FE-04, FE-06 |
| 3 | Persona B (FE-02) + Persona C (FE-04) + Persona D (FE-06 + FE-07) | Card Detail + Deck Builder + Lobby + Tablero | FE-08 |
| 4 | Persona D (FE-08) + Tests (todos) | Acciones del tablero + Tests | — |

### División de archivos sin solapamiento

| Persona | Toca exclusivamente |
|---------|---------------------|
| **Persona A** | `core/models/`, `core/api/`, `core/services/`, `shared/` |
| **Persona B** | `features/cards/` |
| **Persona C** | `features/decks/` |
| **Persona D** | `features/lobby/`, `features/match/` |

Después del Batch 1, ninguna persona toca los archivos de otra.
El único punto de coordinación: Persona C necesita `CardItemComponent` de Persona B para `DeckSearchComponent`. Acordar entrega temprana o usar un placeholder.