# Project Scope Contract

## Product goal

Build a playable web version of Pokémon TCG for the Programming III final project.

The system must support:
- Java 21 + Spring Boot backend
- Angular 21+ frontend
- PostgreSQL database
- WebSockets for real-time match sync
- Local card cache based on pokemontcg.io data
- XY1 set as mandatory base set
- Backend-authoritative game rules

## MVP first

The first playable version must support:
- Two guest players
- Seeded valid decks
- Match creation
- Joining a match
- Automatic setup
- Turn cycle:
  - DRAW
  - MAIN
  - ATTACK
  - BETWEEN_TURNS
- Put Basic Pokémon on bench
- Attach 1 Energy per turn
- Attack with Active Pokémon
- Damage calculation
- Knockout
- Prize taking
- Victory by:
  - taking last Prize card
  - opponent has no Pokémon in play
  - opponent cannot draw at start of turn

## Postponed until MVP works

Do not implement these before the MVP match is playable:
- JWT authentication
- User registration/login
- Ranking
- Chat
- Animations
- Multiple expansions
- Mega Evolution
- Complex card text interpreter
- Full trainer/effect engine for every card

## Technical priority

Priority order:
1. Game engine rules
2. Match state model
3. Seed decks
4. REST actions
5. WebSocket sync
6. Frontend playable board
7. Deck Builder
8. Auth/login
9. Optional features

## Out of scope for MVP

- AI opponent
- Mobile-first UI
- Tournament system
- Trading cards between users
- Payment/store features