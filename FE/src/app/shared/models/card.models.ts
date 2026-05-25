export type CardSupertype = 'POKEMON' | 'ENERGY' | 'TRAINER';

export type PokemonStage = 'BASIC' | 'STAGE_1' | 'STAGE_2' | 'MEGA' | 'RESTORED';

export type EnergyType = 'GRASS' | 'FIRE' | 'WATER' | 'LIGHTNING' | 'PSYCHIC' | 'FIGHTING' | 'DARKNESS' | 'METAL' | 'FAIRY' | 'COLORLESS';

export interface AttackModel {
  index: number;
  name: string;
  cost: EnergyType[];
  damage: string;
  text: string;
}

export interface WeaknessModel {
  type: EnergyType;
  value: string;
}

export interface ResistanceModel {
  type: EnergyType;
  value: string;
}

export interface CardModel {
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
