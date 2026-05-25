## 1. Organización por Carpetas (Vertical Slices)

Dividan el proyecto de forma que cada programador sea "dueño" de una carpeta física. Eviten que los agentes naveguen libremente por todo el repositorio.

- **Frontend:** En lugar de agrupar por `/components` y `/services`, agrupen por `/features/login`, `/features/perfil`, etc.
- **Backend:** Si usan .NET o Java, usen **Vertical Slices**. Metan el Controller, el DTO y la lógica en una misma carpeta de funcionalidad.
- **Regla de Oro:** Si Dev A está en la carpeta `/feature-A` y Dev B en `/feature-B`, sus agentes nunca se van a pisar porque no comparten archivos.

## 2. El Workflow de Git "Mínimo Viable"

No se compliquen con GitFlow. Usen **Feature Branching** básico:

1.  **Branch:** Cada uno crea una rama: `git checkout -b feature/nombre-tarea`.
2.  **Agent Work:** Le ordenan al agente trabajar _únicamente_ en esa rama y sobre sus carpetas asignadas.
3.  **Sync Local:** Antes de subir nada, hacen `git pull origin main` en su rama para traer lo que otros ya terminaron.
4.  **Merge:** Suben su rama y hacen un **Pull Request (PR)** rápido.

## 3. El Rol del "Humano Validador"

Nunca dejen que el agente haga el `commit` o el `push` directamente.

- **Revisión Visual:** El humano debe mirar el "Diff" de Git antes de aceptar lo que hizo el agente.
- **Linter:** Tengan un formateador de código (Prettier para TS/JS, EditorConfig para .NET). Si todos los agentes escriben con el mismo formato, Git no marcará conflictos por simples espacios o comas.

---

### Tips

1.  **Creen el esqueleto del proyecto** con las carpetas de cada feature ya vacías.
2.  **Definan los DTOs/Modelos** básicos en un documento compartido.
3.  **Cada uno a su rama** y el agente solo tiene permiso de leer su carpeta asignada.
