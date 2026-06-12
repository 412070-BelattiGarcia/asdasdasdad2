# Informe de Validaciones y Cuestiones Faltantes en el Backend

> Generado el 08/06/2026
> Basado en: `Reglas_Pokemon_TCG.md` (reglamento oficial XY1) y `TPI_Pokemon_TCG.md` (requerimientos funcionales)

---

## Resumen Ejecutivo

El backend implementa una estructura sólida con **Game Engine**, **RuleValidator**, **TurnManager**, **SetupManager**, **VictoryConditionChecker** y **AttackResolver**. Sin embargo, existen brechas significativas entre lo implementado y lo requerido por las reglas oficiales. Este informe detalla cada aspecto faltante o incompleto, organizado por requerimiento funcional (RF).

---

## RF-01a) Preparación de la Partida

| # | Aspecto | Estado | Archivo | Líneas |
|---|---------|--------|---------|--------|
| 1 | Barajar y robar 7 cartas iniciales | ✅ | `SetupManager.java` | 87-97 |
| 2 | Mulligan (mostrar mano, barajar, robar de nuevo) | ✅ | `SetupManager.java` | 99-170 |
| 3 | Robo extra por cada mulligan del rival | ✅ | `SetupManager.java` | 163-168 |
| 4 | Colocar Pokémon Activo boca abajo | ✅ | `SetupManager.java` | 174-179 |
| 5 | Colocar hasta 5 Pokémon en Banca | ✅ | `SetupManager.java` | 182-182 |
| 6 | Tomar 6 cartas de Premio | ✅ | `SetupManager.java` | 185-194 |
| 7 | Lanzar moneda para decidir quién empieza | ✅ | `SetupManager.java` | 196-210 |
| **8** | **Revelar Pokémon al iniciar la partida** | ❌ | `SetupManager.java` | — |

**Detalle 8:** Al completar el setup, ambos jugadores deben revelar sus Pokémon (dar vuelta las cartas que estaban boca abajo). No se publica un evento `SETUP_COMPLETED` ni se notifica a los clientes la revelación. El estado del tablero no distingue entre cartas boca arriba y boca abajo.

---

## RF-01b) Estructura del Turno

| # | Aspecto | Estado | Archivo | Líneas |
|---|---------|--------|---------|--------|
| 9 | Fase 1 – Robo obligatorio | ✅ | `DrawCardHandler.java` | 17-80 |
| 10 | Jugador que empieza no roba en primer turno | ✅ | `DrawCardHandler.java`, `TurnManager.java` | 76-78, 99-101 |
| 11 | Derrota por mazo vacío al robar | ✅ | `DrawCardHandler.java` | 46-50 |
| 12 | Colocar Pokémon Básico en Banca | ✅ | `PutBasicOnBenchHandler.java` | 18-59 |
| 13 | Evolucionar: no en turno que entró en juego | ✅ | `EvolvePokemonHandler.java` | 70-72 |
| **14** | **Evolucionar: no en primer turno del jugador** | ❌ | `RuleValidator.java` | 76-112 |
| 15 | Unir 1 Energía por turno | ✅ | `RuleValidator.java`, `AttachEnergyHandler.java` | 40-56, 16-51 |
| 16 | Jugar Objetos sin límite | ✅ | `RuleValidator.java` | 126-127 |
| 17 | Jugar 1 Partidario por turno | ✅ | `RuleValidator.java` | 128 |
| 18 | Jugar 1 Estadio por turno | ✅ | `RuleValidator.java` | 130-131 |
| **19** | **Efectos reales de cartas Entrenador** | ❌ | `PlayTrainerHandler.java` | 17-22 |
| 20 | Retirar Pokémon Activo (1 vez por turno) | ✅ | `RuleValidator.java`, `RetreatActiveHandler.java` | 137-183, 15-76 |
| 21 | No retirar si está Dormido o Paralizado | ✅ | `RuleValidator.java` | 179-183 |
| **22** | **Usar Habilidades de Pokémon (Abilities)** | ❌ | — | — |
| 23 | Atacar no disponible en primer turno | ✅ | `RuleValidator.java` | 192-193 |
| 24 | Ataque finaliza el turno automáticamente | ✅ | `DeclareAttackHandler.java` | — |

**Detalle 14:** `RuleValidator.java:76-112` solo verifica `turnNumber == 1` (primer turno global), pero la regla oficial dice: *"Ningún jugador puede hacer evolucionar a un Pokémon en el primer turno de dicho jugador"*, que es distinto. Si el jugador 2 está en su primer turno pero el `turnNumber` global es 2, la validación falla al permitir evolución.

```java
// Código actual (incorrecto):
if (gameState.getTurnNumber() == 1) {
    throw new GameEngineException(ErrorCode.EVOLVE_NOT_ALLOWED);
}
// Debería verificar: if (isFirstTurnOfCurrentPlayer) { ... }
```

**Detalle 19:** `PlayTrainerHandler.java` tiene un comentario explícito: *"MVP: Trainer effects are not implemented."* Solo mueve la carta de la mano al descarte y actualiza flags (`hasPlayedSupporter`, `hasPlayedStadium`). No ejecuta ningún efecto:
- Objetos como Pociones, Super Ball, etc. no tienen efecto
- Partidarios como Profesor Ciprés no roban cartas
- Estadios no impactan el estado del juego ni permanecen en una zona compartida
- Herramientas Pokémon no pueden equiparse
- AS TÁCTICO no tiene comportamiento especial

**Detalle 22:** No existe `GameActionType.USE_ABILITY` ni handler asociado. El modelo `PokemonInPlay` no tiene campo para identificar habilidades activas ni estado de uso. No hay forma de que un jugador active habilidades como *"Mano Siniestra"* o similares.

---

## RF-01c) Sistema de Ataque — Secuencia de Resolución

| # | Paso | Estado | Archivo | Líneas |
|---|------|--------|---------|--------|
| 25 | 1. Anunciar ataque y verificar Energía | ✅ | `AttackResolver.java` | 223-268 |
| **26** | **2. Confundido → moneda; si cruz: falla + 3 daño** | ⚠️ | `AttackResolver.java`, `DeclareAttackHandler.java` | 47-83 |
| **27** | **3. Selecciones del ataque (elegir objetivo, etc.)** | ❌ | — | — |
| **28** | **4. Requisitos previos del texto del ataque** | ❌ | — | — |
| **29** | **5. Efectos que modifican o cancelan el ataque** | ❌ | — | — |
| 30 | 6a. Daño base | ✅ | `AttackResolver.java` | 271-273 |
| **31** | **6b. Modificadores por efectos sobre el atacante** | ❌ | `AttackResolver.java` | 275-277 |
| 32 | 6c. Aplicar Debilidad (×2) | ✅ | `AttackResolver.java` | 279-285 |
| 33 | 6d. Aplicar Resistencia (−20, mínimo 0) | ✅ | `AttackResolver.java` | 287-296 |
| **34** | **6e. Modificadores por efectos sobre el defensor** | ❌ | `AttackResolver.java` | 298-300 |
| 35 | 6f. Colocar 1 contador cada 10 pts de daño | ✅ | `AttackResolver.java`, `DeclareAttackHandler.java` | 303-304, 55-61 |
| **36** | **7. Efectos posteriores al daño** | ❌ | `DeclareAttackHandler.java` | — |

**Detalle 26:** `AttackResolver.java:47-54` aplica los 3 contadores de daño al Pokémon confundido, pero luego `DeclareAttackHandler.java:81-83` reporta el ataque como fallido. El daño ya fue aplicado al atacante pero la acción completa se reporta como error, dejando el estado del juego en un estado inconsistente (el daño está aplicado pero el jugador no avanzó de fase ni consumió su ataque).

**Detalle 27:** No hay infraestructura para que el atacante seleccione un objetivo cuando el ataque lo requiere (ej. elegir un Pokémon de la Banca del rival, elegir qué Energía descartar, etc.). No existe `GameActionType` para selección de objetivo ni manejo de flujo multi-paso.

**Detalle 28:** No se ejecuta nada del texto de la carta de ataque. Por ejemplo, si un ataque dice *"Lanza una moneda. Si sale cara, el Pokémon Activo del rival queda Paralizado"*, esa condición no se aplica.

**Detalle 29:** No hay pipeline para aplicar efectos de turnos anteriores que modifiquen el ataque (ej. un ataque que dice *"Durante el próximo turno del rival, ese Pokémon no puede atacar"*).

**Detalle 31 y 34:** `AttackResolver.java:275-277` y `298-300` — los modificadores `attackerModifiers` y `defenderModifiers` están hardcodeados en 0. No hay forma de que cartas de Entrenador o efectos de Estadio modifiquen el daño.

**Detalle 36:** Después del daño, no se aplican:
- Condiciones especiales al defensor (Quemado, Dormido, Paralizado, Confundido, Envenenado)
- Descartes de Energía del defensor
- Daño a Pokémon en Banca (ej. *"este ataque hace 10 de daño a 2 Pokémon en Banca"*)
- Curación del atacante
- Robo de cartas adicional
- Cualquier otro efecto descrito en el texto del ataque

---

## RF-01d) Proceso de Knockout

| # | Aspecto | Estado | Archivo | Líneas |
|---|---------|--------|---------|--------|
| 37 | KO cuando daño × 10 ≥ HP | ✅ | `DeclareAttackHandler.java` | 68-72 |
| 38 | Pokémon + cartas unidas se descartan | ✅ | `DeclareAttackHandler.java` | 78-92 |
| 39 | Oponente toma 1 carta de Premio | ✅ | `DeclareAttackHandler.java` | 114 |
| 40 | Pokémon-EX: 2 cartas de Premio | ✅ | `DeclareAttackHandler.java` | 114 |
| 41 | Reemplazar Activo con uno de Banca | ✅ | `DeclareAttackHandler.java` | 98-106 |
| **42** | **Elección del reemplazo por el jugador** | ❌ | `DeclareAttackHandler.java` | 98-106 |
| 43 | Derrota si Banca vacía | ✅ | `VictoryConditionChecker.java` | 33-36 |

**Detalle 42:** `DeclareAttackHandler.java:98-106` selecciona automáticamente `getBench().get(0)` (el primer Pokémon de la Banca). No existe `GameActionType.CHOOSE_KO_REPLACEMENT` para que el jugador elija qué Pokémon pasar a Activo.

---

## RF-01e) Condiciones Especiales

| # | Aspecto | Estado | Archivo | Líneas |
|---|---------|--------|---------|--------|
| 44 | Dormido: no atacar ni retirarse | ✅ | `RuleValidator.java` | 179-183, 210-214 |
| 45 | Dormido: moneda entre turnos | ✅ | `AttackResolver.java` | 119-131 |
| 46 | Quemado: moneda entre turnos, 2 daño si cruz | ✅ | `AttackResolver.java` | 94-117 |
| 47 | Confundido: moneda antes de atacar | ✅ | `AttackResolver.java` | 47-54 |
| 48 | Paralizado: no atacar ni retirarse | ✅ | `RuleValidator.java` | 179-183, 210-214 |
| 49 | Paralizado: se cura automáticamente entre turnos | ✅ | `AttackResolver.java` | 133-143 |
| 50 | Envenenado: 1 daño entre turnos | ✅ | `AttackResolver.java` | 81-92 |
| **51** | **Aplicar condiciones especiales desde ataques** | ❌ | `DeclareAttackHandler.java` | — |
| **52** | **Incompatibilidad: Dormido/Confundido/Paralizado mutuamente excluyentes** | ⚠️ | `AttackResolver.java` | 202-221 |
| 53 | Quemado + Envenenado pueden coexistir | ✅ | `PokemonInPlay.java` | — |
| **54** | **Orden fijo entre turnos con verificación de KO entre cada paso** | ⚠️ | `AttackResolver.java` | 70-143 |
| 55 | Condiciones se eliminan al ir a Banca o evolucionar | ✅ | `RetreatActiveHandler.java`, `EvolvePokemonHandler.java` | 51-53, 62 |

**Detalle 51:** `DeclareAttackHandler.java` nunca invoca `AttackResolver.applyCondition()` sobre el Pokémon defensor. Los ataques que deberían aplicar Dormido, Quemado, Confundido, Paralizado o Envenenado al rival no tienen efecto.

**Detalle 52:** `AttackResolver.applyCondition()` (líneas 202-221) maneja la exclusividad de Quemado/Envenenado (no se duplican), pero no implementa la regla de que **Dormido, Confundido y Paralizado son mutuamente excluyentes**: la última condición aplicada reemplaza a las anteriores. Actualmente, si un Pokémon está Dormido y recibe Confundido, ambas condiciones coexisten.

**Detalle 54:** `AttackResolver.processBetweenTurnStatuses()` (líneas 70-143) aplica en el orden correcto (Envenenado → Quemado → Dormido → Paralizado) pero no verifica KO entre cada paso. Si un Pokémon queda KO por Envenenado, no deberían procesarse Quemado, Dormido ni Paralizado en ese mismo ciclo entre turnos.

---

## RF-01f) Condiciones de Victoria y Derrota

| # | Aspecto | Estado | Archivo | Líneas |
|---|---------|--------|---------|--------|
| 56 | Victoria por Premios (última carta) | ✅ | `TakePrizeCardHandler.java`, `VictoryConditionChecker.java` | 53-58, 26-29 |
| 57 | Victoria por KO total (sin Pokémon rival) | ✅ | `VictoryConditionChecker.java` | 33-36 |
| 58 | Derrota por mazo vacío | ✅ | `DrawCardHandler.java` | 46-50 |
| **59** | **Muerte Súbita (victoria simultánea)** | ❌ | `VictoryConditionChecker.java` | 36-42 |
| **60** | **Conceder partida** | ❌ | — | — |

**Detalle 59:** `VictoryConditionChecker.java:36-42` detecta correctamente cuando ambos jugadores ganan simultáneamente y marca `suddenDeath = true`, pero **no inicia una nueva partida** con 1 carta de Premio. No hay flujo de juego que maneje la transición a Muerte Súbita.

**Detalle 60:** `FinishReason.CONCEDE` existe en el enum pero no hay `GameActionType.CONCEDE` ni handler para procesarlo. Un jugador no puede rendirse.

---

## RF-02) Tipos de Cartas

| # | Aspecto | Estado | Archivo | Líneas |
|---|---------|--------|---------|--------|
| 61 | Pokémon Básico, Fase 1, Fase 2 | ✅ | `PokemonStage`, `EvolvePokemonHandler.java` | — |
| 62 | Pokémon-EX: 2 Premios al ser derrotado | ✅ | `DeclareAttackHandler.java` | 114 |
| 63 | Pokémon-EX: -EX es parte del nombre | ✅ | `PokemonCardDefinition.isEx` | — |
| 64 | Energía Básica sin límite de copias | ✅ | `DeckValidator.java` | 42-44 |
| **65** | **Energía Especial: máximo 4 copias** | ⚠️ | `DeckValidator.java` | 37-46 |
| 66 | Entrenador Objeto: sin límite por turno | ✅ | `RuleValidator.java` | 126-127 |
| **67** | **Entrenador AS TÁCTICO: máximo 1 por mazo** | ❌ | `DeckValidator.java` | 27-54 |
| 68 | Entrenador Partidario: 1 por turno | ✅ | `RuleValidator.java` | 128 |
| 69 | Entrenador Estadio: 1 por turno | ✅ | `RuleValidator.java` | 130-131 |
| **70** | **Entrenador Estadio: permanece en zona compartida** | ❌ | `PlayTrainerHandler.java` | — |
| **71** | **Entrenador Herramienta Pokémon** | ❌ | — | — |
| **72** | **Pokémon Megaevolución (opcional)** | ❌ | — | — |
| **73** | **Pokémon Recreados (Fósil)** | ❌ | — | — |

**Detalle 65:** El `DeckValidator` chequea por nombre genérico pero no distingue si una carta de Energía es Básica o Especial. Las Energías Especiales tienen límite de 4 copias pero no se valida correctamente.

**Detalle 67:** `DeckValidationError.ACE_SPEC_LIMIT_EXCEEDED` está definido en el enum pero `DeckValidator.java:27-54` nunca lo verifica. No hay lógica para contar cartas con `isAceSpec = true` y asegurar que haya solo 1.

**Detalle 70:** `PlayTrainerHandler.java` descarta el Estadio inmediatamente después de jugarlo. La regla oficial dice que los Estadios permanecen en juego (zona compartida) hasta que otro Estadio los reemplace o una carta los elimine.

**Detalle 71:** No existe `GameActionType.ATTACH_TOOL` ni handler. El modelo `PokemonInPlay` no tiene campo para herramienta equipada. No hay lógica para verificar que solo haya 1 herramienta por Pokémon.

**Detalle 72:** No hay modelado para `PokemonStage.MEGA` como fase de evolución. No hay lógica para que el turno termine al mega-evolucionar. `EvolvePokemonHandler` no reconoce MEGA como etapa válida.

**Detalle 73:** No hay lógica para que los Pokémon Recreados (Fósil) requieran una carta de Objeto específica para ser jugados. No hay distinción de `PokemonStage.RESTORED` como fase especial.

---

## RF-04) Construcción de Mazos (Deck Builder)

| # | Validación | Estado | Archivo | Líneas |
|---|------------|--------|---------|--------|
| 74 | Exactamente 60 cartas | ✅ | `DeckValidator.java` | 31 |
| 75 | Máximo 4 copias del mismo nombre | ✅ | `DeckValidator.java` | 37-46 |
| 76 | Sin límite para Energía Básica | ✅ | `DeckValidator.java` | 42-44 |
| **77** | **Máximo 1 carta AS TÁCTICO** | ❌ | `DeckValidator.java` | 27-54 |
| 78 | Al menos 1 Pokémon Básico | ✅ | `DeckValidator.java` | 48-51 |
| **79** | **Cartas duplicadas (misma carta+set en distintas entradas)** | ❌ | `DeckValidator.java` | — |
| **80** | **Formato de mazo válido (solo set xy1)** | ❌ | `DeckValidator.java` | — |

**Detalle 77:** Ídem detalle 67.

**Detalle 79:** `DeckValidationError.DUPLICATE_CARDS` definido pero no verificado. No chequea si la misma carta aparece en múltiples entradas del mazo.

**Detalle 80:** `DeckValidationError.INVALID_DECK_FORMAT` definido pero no verificado. No hay lógica para asegurar que todas las cartas pertenezcan al set `xy1` (o sets permitidos).

---

## RF-05) Guardado y Persistencia del Estado

| # | Aspecto | Estado | Archivo | Líneas |
|---|---------|--------|---------|--------|
| 81 | Estado del tablero persistido | ✅ | `StatePersisterAdapter.java` | — |
| 82 | Manos de ambos jugadores | ✅ | — | — |
| 83 | Mazos con orden | ✅ | — | — |
| 84 | Pilas de descarte | ✅ | — | — |
| 85 | Cartas de Premio | ✅ | — | — |
| 86 | Contadores de daño | ✅ | — | — |
| 87 | Condiciones especiales activas | ✅ | — | — |
| 88 | Flags del turno | ✅ | — | — |
| **89** | **Log de acciones persistido (event sourcing)** | ❌ | — | — |
| **90** | **Reconstrucción de partida desde el log** | ❌ | — | — |

**Detalle 89 y 90:** Los eventos del juego se publican vía WebSocket pero **no se persisten**. El requerimiento RF-05 especifica que debe mantenerse un *"registro de acciones (log) completo e inmutable"* con turno, jugador, tipo de acción y resultado. Sin esto, no es posible reconstruir el estado de una partida perdida por desconexión a través del log (solo se puede recuperar el último estado guardado).

---

## RF-06) Comunicación en Tiempo Real (WebSocket)

| # | Aspecto | Estado | Archivo | Líneas |
|---|---------|--------|---------|--------|
| 91 | Comunicación bidireccional vía WebSocket | ✅ | `MatchWebSocketController.java`, `MatchWebSocketPublisher.java` | — |
| 92 | Sincronizar estado tras cada acción | ✅ | `MatchApplicationService.java` | — |
| 93 | Notificar eventos relevantes | ⚠️ | `MatchWebSocketPublisher.java` | — |
| **94** | **Reconexión con replay de eventos** | ❌ | — | — |

**Detalle 93:** Se publican eventos pero no todos los requeridos:
- Inicio de turno → ❌ no se emite evento `TURN_STARTED`
- Knockout → ✅ `KNOCKOUT_OCCURRED`
- Toma de carta de Premio → ✅ `PRIZE_TAKEN`
- Condiciones especiales aplicadas → ❌ no se emite evento `STATUS_APPLIED`
- Fin de partida → ✅ `VICTORY_DECIDED`
- Revelación de Pokémon en setup → ❌ no se emite evento `SETUP_COMPLETED`

**Detalle 94:** No hay mecanismo para que un jugador reconectado reciba el historial de eventos ocurridos durante su desconexión. Solo puede obtener el estado actual vía `GET /state`.

---

## RNF-04) Patrones de Diseño y Arquitectura

| # | Componente / Patrón | Estado | Archivo | Detalle |
|---|---------------------|--------|---------|---------|
| 95 | Game Engine aislado del transporte | ✅ | `GameEngine.java` | Principio de inversión de dependencias |
| 96 | **RuleValidator como componente independiente** | ⚠️ | `RuleValidator.java` | Existe pero es una sola clase con switch — no usa Chain of Responsibility |
| **97** | **DamageCalculator como componente independiente** | ❌ | `AttackResolver.java` | Lógica embebida dentro de AttackResolver |
| **98** | **StatusEffectManager como componente independiente** | ❌ | `AttackResolver.java` | Lógica embebida dentro de AttackResolver |
| 99 | VictoryConditionChecker como componente independiente | ✅ | `VictoryConditionChecker.java` | — |
| 100 | TurnManager como componente independiente | ✅ | `TurnManager.java` | — |
| **101** | **Patrón State para fases del turno** | ⚠️ | `TurnManager.java`, `TurnPhase.java` | TurnPhase es enum, manejado con if/switch — no hay máquina de estados formal |
| **102** | **Patrón Chain of Responsibility para pipeline de ataque** | ❌ | — | Cada paso del ataque está hardcodeado en `DeclareAttackHandler` y `AttackResolver` |
| **103** | **Patrón Strategy para efectos de cartas de Entrenador** | ❌ | `PlayTrainerHandler.java` | Sin implementación de efectos reales |
| **104** | **Patrón Strategy para efectos de ataques** | ❌ | `AttackResolver.java` | Todo el daño es numérico, sin efectos de texto |
| **105** | **Patrón Observer para eventos de juego** | ⚠️ | `EventPublisherPort.java` | Se emiten eventos pero no hay suscripción formal de componentes del engine |

---

## Resumen de Prioridades para Implementación

### 🔴 Crítico (bloquea funcionalidad básica del juego)

| # | Item | Impacto |
|---|------|---------|
| 19 | Efectos de cartas Entrenador no implementados | Ninguna carta de Entrenador funciona |
| 36 | Efectos posteriores al daño no implementados | Ataques con condiciones, descartes, curación no funcionan |
| 22 | Habilidades de Pokémon no implementadas | Ningún Pokémon con habilidad funciona |
| 31/34 | Modificadores de daño no implementados | Bonus de daño por Entrenadores/Estadios no funciona |
| 51 | Condiciones especiales desde ataques no aplican | Ataques que envenenan/queman/duermen/etc. no funcionan |
| 77 | Límite de AS TÁCTICO no validado en mazos | Mazos inválidos pasan la validación |

### 🟠 Alto (reglas oficiales incompletas)

| # | Item | Impacto |
|---|------|---------|
| 14 | Evolución permitida en primer turno del jugador | Viola regla oficial |
| 59 | Muerte Súbita no implementada | Victoria simultánea no se resuelve |
| 26 | Confusión deja estado inconsistente | El daño se aplica pero la acción falla |
| 42 | Reemplazo de KO automático | Jugador no elige reemplazo |
| 70 | Estadio no permanece en juego | Los Estadios no funcionan según reglas |
| 89/90 | Log de acciones no persiste | No se puede reconstruir partida desde eventos |
| 67 | AS TÁCTICO no validado | Deck builder permite mazos inválidos |
| 65 | Energía Especial no validada correctamente | Límite de 4 copias no se aplica |

### 🟡 Medio (funcionalidad parcialmente implementada)

| # | Item | Impacto |
|---|------|---------|
| 8 | No hay evento de revelación al inicio | UI no puede sincronizar revelación |
| 27 | No hay selección de objetivo | Ataques con targeting no funcionan |
| 28 | No hay requisitos previos de ataque | Texto de ataque con monedas no se ejecuta |
| 29 | No hay modificadores pre-ataque | Efectos que cancelan/modifican ataques no funcionan |
| 52 | Exclusividad de condiciones mal implementada | Dormido + Confundido pueden coexistir incorrectamente |
| 54 | KO entre turnos no verificado por paso | Quemado puede procesarse después de KO por veneno |
| 60 | Conceder partida no implementado | Jugador no puede rendirse |
| 71 | Herramienta Pokémon no implementada | No se pueden equipar herramientas |
| 93 | Eventos faltantes en WebSocket | UI no recibe todas las notificaciones |
| 94 | Reconexión sin replay | Jugador reconectado pierde historial |

### 🟢 Bajo (mejora / opcional)

| # | Item | Impacto |
|---|------|---------|
| 72 | Pokémon Megaevolución | Opcional con bonus |
| 73 | Pokémon Recreados (Fósil) | No hay cartas de este tipo en xy1 |
| 79/80 | Validaciones adicionales de deck builder | Casos borde |
| 96-105 | Refactor a patrones de diseño | Calidad de código, testeabilidad |

---

## Mapa de Archivos Clave

| Archivo | Propósito | Líneas |
|---------|-----------|--------|
| `BE/.../engine/GameEngine.java` | Orquestador del motor de juego | 19-102 |
| `BE/.../engine/rules/RuleValidator.java` | Validaciones de acciones | 1-234 |
| `BE/.../engine/attack/AttackResolver.java` | Resolución de ataques, daño, condiciones | 1-328 |
| `BE/.../engine/victory/VictoryConditionChecker.java` | Verificación de condiciones de victoria | 1-51 |
| `BE/.../engine/turn/TurnManager.java` | Ciclo de turnos | 1-159 |
| `BE/.../engine/setup/SetupManager.java` | Preparación de la partida | 1-263 |
| `BE/.../engine/handlers/PlayTrainerHandler.java` | Handler de cartas Entrenador (MVP) | 24-63 |
| `BE/.../engine/handlers/DeclareAttackHandler.java` | Handler de ataque | 23-167 |
| `BE/.../engine/handlers/EvolvePokemonHandler.java` | Handler de evolución | 17-88 |
| `BE/.../services/decks/DeckValidator.java` | Validación de mazos | 1-63 |
| `BE/.../services/matches/MatchApplicationService.java` | Servicio principal de partidas | 1-305 |
| `BE/.../websocket/MatchWebSocketPublisher.java` | Publicación de eventos WebSocket | — |

---

*Fin del informe*
