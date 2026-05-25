export type MatchStatus = 'WAITING' | 'SETUP' | 'ACTIVE' | 'FINISHED';

export type TurnPhase = 'DRAW' | 'MAIN' | 'ATTACK' | 'BETWEEN_TURNS';

export type PlayerSide = 'PLAYER_ONE' | 'PLAYER_TWO';

export type SpecialCondition = 'ASLEEP' | 'BURNED' | 'CONFUSED' | 'PARALYZED' | 'POISONED';

export interface PublicGameStateModel {
  matchId: string;
  status: MatchStatus;
  phase: TurnPhase;
  turnNumber: number;
  currentPlayerId: string;
  firstPlayerId: string;
  players: PublicPlayerStateModel[];
}

export interface PublicPlayerStateModel {
  playerId: string;
  side: PlayerSide;
  prizes: string[];
  activePokemon: PublicPokemonSlotModel | null;
  bench: PublicPokemonSlotModel[];
}

export interface PublicPokemonSlotModel {
  instanceId: string;
  cardId: string;
  damageCounters: number;
  specialConditions: SpecialCondition[];
  attachedCards: string[];
}

export interface PrivatePlayerStateModel {
  playerId: string;
  hand: PrivateHandCardModel[];
  deckCount: number;
  discardCount: number;
  prizes: PrizeSlotModel[];
}

export interface PrivateHandCardModel {
  instanceId: string;
  cardId: string;
  name: string;
  supertype: string;
}

export interface PrizeSlotModel {
  slot: number;
  known: boolean;
  card: CardModel | null;
}

import { CardModel } from './card.models';
