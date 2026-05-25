# División Catálogo y API Externa

Este reparto contempla únicamente el módulo **catálogo/API externa** del backend, sin el engine, frontend ni match persistence.

---

## Persona A — Catálogo de Cartas y Sincronización con API Externa

**Carpetas propias:** `clients/`, `services/cards/`, `controllers/cards/`, `dtos/cards/`, `engine/ports/impl/CardLookupAdapter.java`

### Clases
- `clients/PokemonTcgApiClient` *(implementar — stub existente)*
- `clients/PokemonTcgApiResponse` *(nuevo: wrapper para deserializar response de API)*
- `services/cards/CardCacheSyncService` *(implementar — stub existente)*
- `services/cards/CardCatalogService` *(implementar — stub existente)*
- `controllers/cards/CardController` *(implementar — stub existente)*
- `mappers/CardMapper` *(nuevo: mapear API response → CardEntity → CardDefinition)*
- `engine/ports/impl/CardLookupAdapter` *(implementar — stub existente)*
- DTOs ya existen (`CardDetailResponse`, `CardSummaryResponse`, `CardSearchRequest`)
- Domain models ya existen (`CardDefinition`, `PokemonCardDefinition`, `EnergyCardDefinition`, `TrainerCardDefinition`, enums)
- Entities ya existen (`CardEntity`, `CardAttackEntity`, `CardWeaknessEntity`, `CardResistanceEntity`)
- Repositories ya existen (`CardJpaRepository`, `CardAttackJpaRepository`, `CardWeaknessJpaRepository`, `CardResistanceJpaRepository`)

### Tareas
- **PokemonTcgApiClient:** consumir Pokemon TCG API v2 (`https://api.pokemontcg.io/v2/cards`). Implementar paginación, rate limiting básico, manejo de errores HTTP, mapeo del response JSON a DTOs intermedios.
- **CardCacheSyncService:** sincronización batch o incremental. Obtener cartas desde API externa, mapear a `CardEntity`, hacer upsert por `cardId` (PK de la API). Ejecutar al startup (`@PostConstruct` o `CommandLineRunner`) y opcionalmente endpoint manual.
- **CardCatalogService:** consultas sobre catálogo local. Buscar por id exacto, por nombre (LIKE/ILIKE), por supertype, por tipo de energía, por set, por stage evolutivo. Paginación via `CardSearchRequest` con `Pageable` de Spring Data.
- **CardController:**
  - `GET /api/cards` — listar con filtros (`CardSearchRequest` como query params)
  - `GET /api/cards/{id}` — detalle completo
  - `POST /api/cards/sync` — gatillar sincronización manual (POST porque modifica estado)
- **CardLookupAdapter:** implementar `CardLookupPort.getCardById(String cardId)`. Consultar `CardJpaRepository` por id, mapear a `CardDefinition` (la jerarquía de dominio `cards/domain/` que el engine ya conoce). Agregar `@Cacheable` para evitar consultas repetidas a BD. **Esta es la única clase del catálogo que el engine conoce.**
- **CardMapper:** mapear `CardEntity` → `CardDefinition`, `CardEntity` → `CardDetailResponse`, `PokemonTcgApiResponse` → `CardEntity`. Tener en cuenta los subtipos: si `supertype == POKEMON` → `PokemonCardDefinition` con hp, ataques, debilidad, resistencia, etapa, etc.
- **Config:** agregar dependency `spring-boot-starter-webflux` o `RestTemplate` en `pom.xml` para llamadas HTTP. Agregar `@EnableCaching` en alguna clase `@Configuration` para que `@Cacheable` de `CardLookupAdapter` funcione.
- **Documentar:** endpoints de catálogo en Swagger (SpringDoc ya configurado).

### RFs cubiertos
RF-1 (consultar cartas desde API externa), RF-2 (almacenar localmente), RF-3 (consultar desde catálogo local), RF-4 (búsqueda por id/parámetros), RF-8 (implementar `CardLookupPort`), RF-10 (proveer info mínima al engine: tipo, HP, ataques, costos, debilidad, resistencia, etapa evolutiva, indicador de básico)

### No toca
Mazos, persistencia/validación de mazos, `DeckLoadPort`/`DeckLoadAdapter`, engine (`GameEngine`, `MatchApplicationService`). **No crea ni modifica las interfaces `CardLookupPort` ni `DeckLoadPort`** — esas interfaces las crea y es dueña Persona 4 del engine. Solo implementa `CardLookupAdapter`.

---

## Persona B — Mazos, Validación y Adapter de Carga

**Carpetas propias:** `services/decks/`, `controllers/decks/`, `dtos/decks/`, `engine/ports/impl/DeckLoadAdapter.java`

### Clases
- `services/decks/DeckService` *(implementar — stub existente)*
- `services/decks/DeckValidator` *(implementar — stub existente)*
- `services/decks/SeedDeckService` *(implementar — solo para dev/testing)*
- `controllers/decks/DeckController` *(implementar — stub existente)*
- `mappers/DeckMapper` *(nuevo: mapear DeckEntity ↔ DeckResponse/CreateDeckRequest)*
- `engine/ports/impl/DeckLoadAdapter` *(nuevo: implementación)*
- DTOs ya existen (`DeckResponse`, `DeckCardResponse`, `CreateDeckRequest`, `UpdateDeckRequest`, `DeckValidationResponse`)
- Domain models ya existen (`Deck`, `DeckCard`, `DeckValidationResult`, `DeckValidationError`)
- Entities ya existen (`DeckEntity`, `DeckCardEntity`)
- Repositories ya existen (`DeckJpaRepository`, `DeckCardJpaRepository`)

### Sobre DeckLoadPort
- `engine/ports/DeckLoadPort` **ya existe como interface — la creó y es dueña Persona 4 del engine**.
- Persona B **no crea ni modifica** `DeckLoadPort`.
- Persona B únicamente implementa `engine/ports/impl/DeckLoadAdapter`, que es la clase concreta que cumple ese contrato.

### Tareas
- **DeckLoadAdapter:** implementar `DeckLoadPort.loadDeck(UUID deckId)`. Consultar `DeckJpaRepository.findById()`, mapear `DeckEntity` → `Deck` domain. **Llamar a `DeckValidator.validate()` antes de retornar** para garantizar que el engine nunca recibe un mazo inválido, incluso si alguien se saltea los endpoints REST.
- **DeckValidator (reutilizable desde REST y desde DeckLoadAdapter):**
  - `validate(List<DeckCard>)` → `DeckValidationResult`
  - Reglas V1: 60 cartas exactas, máximo 4 copias por `cardId`, al menos un Pokémon básico.
  - Usar `CardJpaRepository` para consultar supertype/categoría de cada carta.
  - **Doble uso:** llamado desde `DeckService` (REST) y desde `DeckLoadAdapter` (engine).
- **DeckService:**
  - `createDeck(CreateDeckRequest)` → validar via `DeckValidator` + persistir
  - `getDeck(UUID deckId)` → consultar
  - `updateDeck(UUID deckId, UpdateDeckRequest)` → re-validar + actualizar
  - `deleteDeck(UUID deckId)` → eliminar
  - `listDecksByPlayer(UUID playerId)` → listar mazos de un jugador
  - `validateDeck(UUID deckId)` → retornar `DeckValidationResponse`
- **DeckController:**
  - `POST /api/decks` (crear)
  - `GET /api/decks/{id}` (obtener)
  - `PUT /api/decks/{id}` (actualizar)
  - `DELETE /api/decks/{id}` (eliminar)
  - `GET /api/decks?playerId={id}` (listar)
  - `POST /api/decks/{id}/validate` (validar)
- **SeedDeckService (opcional — solo dev/testing, no obligatorio V1):** crear 2 mazos precargados para desarrollo. Ejecutar en `CommandLineRunner` solo si perfil `dev` está activo.
- **DeckMapper:** mapear `DeckEntity` ↔ `DeckResponse`, `CreateDeckRequest` → `DeckEntity`.

### RFs cubiertos
RF-5 (guardar/consultar/eliminar mazos personalizados), RF-6 (crear partida con mazo validado — vía `DeckLoadPort` que incluye validación), RF-9 (implementar `DeckLoadPort`), RF-11 (contratos desacoplados: el engine solo conoce la interface, no las entidades JPA)

### No toca
API externa de cartas, sincronización de catálogo, `CardLookupPort`/`CardLookupAdapter`, engine (`GameEngine`). **No crea ni modifica `DeckLoadPort`** — esa interface es responsabilidad de Persona 4 del engine. Usa `CardJpaRepository` solo para leer supertype/atributos durante validación.

---

## Criterio de reparto

| | Modelos | Lógica | Integraciones | Dependencia externa |
|---|---|---|---|---|
| **Persona A** | Media (hereda domain models) | Alta (API sync + mapeo complejo) | API Pokémon TCG externa | `spring-boot-starter-webflux` |
| **Persona B** | Media (hereda domain models) | Alta (validación + CRUD) | engine (`DeckLoadPort`) | Ninguna |

**Persona A** tiene la tarea más riesgosa del módulo: integrar con una API externa real, manejar paginación, rate limiting, mapeo de JSON profundo (ataques, debilidades, resistencias, 200+ cartas por página). `CardLookupAdapter` es crítico porque lo consume todo el engine durante la partida.

**Persona B** tiene lógica densa pero acotada: validación de mazos (60 cartas, 4 copias, básico), CRUD estándar, e implementar `DeckLoadAdapter`. `DeckValidator` reutilizable desde REST y engine aporta más impacto que `SeedDeckService`.

### Dependencia entre personas

Ambas personas pueden trabajar en paralelo desde el momento en que Persona 4 del engine entrega las interfaces stub.

### Resumen archivos a implementar (vs existentes)

| Persona | Implementar | Ya existen (stubs o creados por engine) |
|---------|------------|-------------------|
| **A** | `PokemonTcgApiClient`, `PokemonTcgApiResponse`, `CardCacheSyncService`, `CardCatalogService`, `CardController`, `CardMapper`, `CardLookupAdapter` | DTOs, domain models, entities, repositories, `CardLookupPort` (Persona 4 engine) |
| **B** | `DeckLoadAdapter`, `DeckService`, `DeckValidator`, `DeckController`, `DeckMapper` (+ `SeedDeckService` opcional) | DTOs, domain models, entities, repositories, `DeckLoadPort` (Persona 4 engine) |