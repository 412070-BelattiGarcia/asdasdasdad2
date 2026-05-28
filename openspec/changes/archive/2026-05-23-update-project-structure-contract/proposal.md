## Why

El archivo `BE/docs/contracts_ai/02-project-structure-contract.md` describe una estructura de proyecto que no coincide con la realidad del código. El contrato actual referencia un package root `com.pokemontcg` que no existe, carpetas que no están presentes y omite archivos y paquetes reales. Esto genera confusión cuando OpenCode u otros desarrolladores intentan usar el contrato como fuente de verdad para navegar o generar código.

## What Changes

1. **Corregir el package root** de `com.pokemontcg` a `ar.edu.utn.frc.tup.piii`.
2. **Corregir el nombre de la clase principal** de `PokemonTcgApplication.java` a `Application.java`.
3. **Reemplazar la estructura plana del backend** (que mezcla capas `api/`, `application/`, `domain/`, `infrastructure/` dentro de cada módulo) con la estructura plana real (controllers, services, dtos, repositories, engine, etc. separados por tipo).
4. **Actualizar la estructura del frontend** para reflejar:
   - Componentes standalone sin sufijo `.component` en los nombres.
   - Archivos `routes.ts` por feature (lazy loading).
   - Solo las carpetas y componentes que realmente existen.
   - Remover `auth/` y subcarpetas de componentes que no existen.
5. **Agregar archivos faltantes** en ambas estructuras.
6. **Actualizar las reglas de dependencia** si aplica según la estructura real.

## Capabilities

### New Capabilities

- `project-structure-contract`: Contrato actualizado con la estructura real de backend y frontend del proyecto, incluyendo todas las clases, paquetes y archivos existentes.

### Modified Capabilities

<!-- Sin cambios en specs existentes porque este cambio solo actualiza un contrato de documentación. -->

## Impact

- **Archivo modificado**: `BE/docs/contracts_ai/02-project-structure-contract.md` (único archivo afectado).
- **Contratos dependientes**: Ninguno, pero cualquier contrato que referencie packages inexistentes (`com.pokemontcg`) debería actualizarse por separado.
- **OpenCode**: Las instrucciones de OpenCode que dependen de este contrato para generar código usarán la estructura correcta.
