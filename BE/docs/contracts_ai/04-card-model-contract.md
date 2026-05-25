# Card Model Contract

## Goal

Define the canonical internal card model.

The card cache stores official card data.

The game engine uses cached local data only.

The game engine must not call the external Pokémon API during a match.

## Backend location

```
cards/domain/
cards/infrastructure/
engine/ports/CardLookupPort.java
```

## Frontend location

```
shared/models/card.models.ts
```

## CardDefinition

Canonical fields:
- id: string
- name: string
- supertype: CardSupertype
- subtypes: string[]
- setCode: string
- number: string
- imageSmallUrl: string | null
- imageLargeUrl: string | null
- rulesText: string[]

## PokemonCardDefinition

Extends CardDefinition.
- hp: int
- stage: PokemonStage
- evolvesFrom: string | null
- types: EnergyType[]
- attacks: AttackDefinition[]
- weaknesses: WeaknessDefinition[]
- resistances: ResistanceDefinition[]
- retreatCost: EnergyType[]
- isEx: boolean
- isMega: boolean

## EnergyCardDefinition

Extends CardDefinition.
- energyCardType: EnergyCardType
- provides: EnergyType[]

## TrainerCardDefinition

Extends CardDefinition.
- trainerSubtype: TrainerSubtype
- isAceSpec: boolean
- effectCode: string | null

## AttackDefinition

- index: int
- name: string
- cost: EnergyType[]
- damage: string
- text: string

## WeaknessDefinition

- type: EnergyType
- value: string

## ResistanceDefinition

- type: EnergyType
- value: string

## EnergyCost

- required: EnergyType[]
- provided: EnergyType[]

## Card API format

```json
{
  "id": "xy1-10",
  "name": "Slugma",
  "supertype": "POKEMON",
  "subtypes": ["Stage 1"],
  "setCode": "xy1",
  "number": "10",
  "imageSmallUrl": "https://images.pokemontcg.io/xy1/10.png",
  "imageLargeUrl": "https://images.pokemontcg.io/xy1/10_hires.png",
  "rulesText": [],
  "hp": 80,
  "stage": "STAGE_1",
  "evolvesFrom": "magcargo",
  "types": ["FIRE"],
  "attacks": [
    {
      "index": 0,
      "name": "Rock Throw",
      "cost": ["COLORLESS", "COLORLESS"],
      "damage": "30",
      "text": ""
    }
  ],
  "weaknesses": [
    {
      "type": "WATER",
      "value": "x2"
    }
  ],
  "resistances": [],
  "retreatCost": ["COLORLESS", "COLORLESS"],
  "isEx": false,
  "isMega": false
}
```

## Frontend Card model

```typescript
interface CardModel {
  id: string;
  name: string;
  supertype: CardSupertype;
  subtypes: string[];
  setCode: string;
  number: string;
  imageSmallUrl: string | null;
  imageLargeUrl: string | null;
  rulesText: string[];
  hp?: number;
  stage?: PokemonStage;
  evolvesFrom?: string;
  types?: EnergyType[];
  attacks?: AttackModel[];
  weaknesses?: WeaknessModel[];
  resistances?: ResistanceModel[];
  retreatCost?: EnergyType[];
  isEx?: boolean;
}
```