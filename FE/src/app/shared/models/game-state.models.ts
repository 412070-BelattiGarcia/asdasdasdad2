export type MatchStatus = 'WAITING' | 'SETUP' | 'ACTIVE' | 'FINISHED';

export type TurnPhase = 'DRAW' | 'MAIN' | 'ATTACK' | 'BETWEEN_TURNS';

export type PlayerSide = 'PLAYER_ONE' | 'PLAYER_TWO';

export type SpecialCondition = 'ASLEEP' | 'BURNED' | 'CONFUSED' | 'PARALYZED' | 'POISONED';

export type FinishReason = 'KNOCKOUT' | 'PRIZES' | 'DECK_OUT' | 'CONCEDE';

export interface PublicGameStateModel {
  matchId: string;
  status: string;
  phase: string;
  turnNumber: number;
  currentPlayerId: string;
  firstPlayerId: string;
  players: PublicPlayerStateModel[];
}

export interface PublicPlayerStateModel {
  playerId: string;
  side: string;
  activePokemon: PublicPokemonSlotModel | null;
  bench: PublicPokemonSlotModel[];
  prizes: string[];
}

export interface PublicPokemonSlotModel {
  instanceId: string;
  cardId: string;
  damageCounters: number;
  specialConditions: string[];
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
  cardId: string | null;
}

export interface MatchStateResponse {
  matchId: string;
  publicState: PublicGameStateModel;
  privateState: PrivatePlayerStateModel;
}
