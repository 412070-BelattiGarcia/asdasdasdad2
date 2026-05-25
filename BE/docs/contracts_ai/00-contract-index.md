# Contract Index

These contracts are mandatory context for OpenCode and OpenSpec.

## Rule

Before implementing or modifying code, OpenCode must read the relevant contract files.

If a task conflicts with these contracts:
- stop
- report the conflict
- do not invent alternative structures

## Contract list

- 01-project-scope-contract.md
- 02-project-structure-contract.md
- 03-enums-contract.md
- 04-card-model-contract.md
- 05-deck-contract.md
- 06-game-state-contract.md
- 07-setup-flow-contract.md
- 08-game-action-contract.md
- 09-rule-validation-contract.md
- 10-attack-pipeline-contract.md
- 11-status-effects-contract.md
- 12-persistence-log-contract.md
- 13-rest-api-contract.md
- 14-websocket-contract.md
- 15-frontend-state-contract.md
- 16-test-scenarios-contract.md

## Global implementation rule

Do not create duplicated DTOs, enums, services, folders or payload formats.

If a missing field or rule is detected, update the relevant contract first.