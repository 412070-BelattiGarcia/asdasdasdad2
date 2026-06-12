# Fix: playerId vacío al crear mazo

## Problema

Al hacer click en "Guardar" en la página de crear mazo (`/decks/new`), se enviaba una petición `POST /api/decks` con `playerId: ""` (string vacío). El backend intentaba `UUID.fromString("")` y respondía con HTTP 400.

## Causa

- `DeckBuilderPage.onSave()` usaba `this.authService.playerId() ?? ''`
- Como no hay página de login, `AuthService` nunca tiene sesión, por lo que `playerId()` retorna `null` y se enviaba `""`
- La página "Mis Mazos" sí tiene un input para ingresar el `playerId` manualmente, pero ese valor no se pasaba al crear un mazo nuevo

## Solución aplicada

Se modificaron 2 archivos del frontend (`FE/src/app/features/decks/pages/`):

### 1. `deck-list-page.ts`

- Agregado `rawInput` signal que captura en tiempo real el valor del input de playerId
- `onNewDeck()` ahora resuelve el playerId actual (input del usuario o default `player-dev`) y lo pasa como query param al navegar a `/decks/new?playerId=...`

### 2. `deck-builder-page.ts`

- Agregado campo `playerId` que se lee del query param `playerId` en el constructor
- `onSave()` usa `this.playerId ?? this.authService.playerId() ?? ''` — prioriza el query param, luego auth service, y por último string vacío como fallback

## Flujo corregido

1. Usuario ingresa un playerId en "Mis Mazos" y carga sus mazos
2. Usuario hace click en "+ Nuevo mazo"
3. Navega a `/decks/new?playerId=<id ingresado>`
4. Al guardar, se envía `POST /api/decks` con el `playerId` correcto
