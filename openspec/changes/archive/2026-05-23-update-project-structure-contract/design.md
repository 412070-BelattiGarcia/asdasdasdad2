## Context

El contrato `02-project-structure-contract.md` fue creado como una especulación inicial de la estructura del proyecto. Desde entonces, el código real evolucionó de forma diferente: el package root cambió, los archivos se organizaron en una estructura plana por tipo (controllers, services, dtos, repositories, engine) en lugar de una estructura anidada por módulo (cards/api/, cards/application/, cards/domain/, cards/infrastructure/), y el frontend adoptó Angular standalone components sin el sufijo `.component`.

El contrato actual es la fuente de verdad que OpenCode usa para generar código. Si está desactualizado, el código generado no va a coincidir con la estructura real del proyecto.

## Goals / Non-Goals

**Goals:**
- Reflejar la estructura real de carpetas y archivos del backend (`BE/src/main/java/ar/edu/utn/frc/tup/piii/`).
- Reflejar la estructura real de carpetas y archivos del frontend (`FE/src/app/`).
- Corregir el package root de `com.pokemontcg` a `ar.edu.utn.frc.tup.piii`.
- Corregir el nombre de la clase principal de `PokemonTcgApplication` a `Application`.
- Mantener intactas las reglas de dependencia (api → application → domain/engine, engine aislado de Spring/JPA).
- Actualizar la sección de frontend para usar la convención Angular standalone actual.

**Non-Goals:**
- No cambiar la arquitectura del código (solo se actualiza el documento que la describe).
- No mover archivos ni refactorizar el código real.
- No crear carpetas o componentes que aún no existen en el frontend (board, bench-zone, etc.).
- No implementar nuevas funcionalidades.
- No actualizar otros contratos que referencien packages incorrectos (eso queda para otro cambio).

## Decisions

1. **Estructura plana por tipo** en vez de por módulo.
   - El código real organiza controllers, services, dtos, repositories y engine en paquetes separados por tipo, no por feature.
   - Esto es más simple y consistente con el tamaño actual del proyecto.
   - Si el proyecto creciera, se podría reconsiderar una estructura por módulo, pero por ahora reflejamos la realidad.

2. **Package root real**: `ar.edu.utn.frc.tup.piii`.
   - Coincide con el grupo/artifact de Maven y la facultad.
   - Todos los archivos Java existentes usan este package.

3. **Sin sufijo `.component` en frontend**.
   - Angular standalone components con `standalone: true` por defecto no requieren el sufijo.
   - Los archivos reales se llaman `card-catalog-page.ts`, no `card-catalog-page.component.ts`.

4. **Archivos `routes.ts` por feature**.
   - Cada feature (cards, decks, lobby, match) tiene su propio `routes.ts` para lazy loading.
   - El `app.routes.ts` principal importa los `loadChildren` de cada feature.

5. **Solo lo que existe**.
   - No se incluyen carpetas o componentes que no existen (auth, board, player-area, etc.).
   - Si se crean en el futuro, el contrato se actualizará en ese momento.

6. **Engine se mantiene igual**.
   - La estructura del engine (model, action, event, setup, turn, rules, attack, status, victory, ports) coincide con el contrato actual.
   - Solo se agregan archivos que existen y no estaban listados: `PlayerSide.java`, `SpecialCondition.java`, `ports/impl/*`.

## Risks / Trade-offs

- **Riesgo**: Que el contrato se desactualice de nuevo si se añaden archivos sin actualizarlo.
  → **Mitigación**: Al ser un contrato para OpenCode, se actualizará naturalmente cuando se generen cambios que agreguen archivos.

- **Riesgo**: Perder la vista arquitectónica de "capa por módulo" que el contrato anterior intentaba proyectar.
  → **Trade-off**: Preferimos reflejar la realidad actual que mantener una visión idealizada pero incorrecta. Si el equipo quiere migrar a una estructura por módulo, ese cambio debe hacerse explícitamente.
