export type GameActionType =
  | 'PUT_BASIC_ON_BENCH'
  | 'ATTACH_ENERGY'
  | 'EVOLVE_POKEMON'
  | 'PLAY_TRAINER'
  | 'DECLARE_ATTACK'
  | 'RETREAT_ACTIVE'
  | 'END_TURN'
  | 'DRAW_CARD'
  | 'CHOOSE_KNOCKOUT_REPLACEMENT'
  | 'TAKE_PRIZE_CARD';

export type GameEventType =
  | 'CARD_DRAWN'
  | 'VICTORY_DECIDED'
  | 'POKEMON_PLACED_ON_BENCH'
  | 'ENERGY_ATTACHED'
  | 'POKEMON_EVOLVED'
  | 'TRAINER_PLAYED'
  | 'RETREAT_EXECUTED'
  | 'DAMAGE_APPLIED'
  | 'KNOCKOUT_OCCURRED'
  | 'ATTACK_DECLARED'
  | 'PHASE_CHANGED'
  | 'STATE_UPDATED'
  | 'PRIZE_TAKEN'
  | 'MULLIGAN_REVEALED';

export interface GameActionRequest {
  type: GameActionType;
  playerId: string;
  payload: Record<string, unknown>;
  clientRequestId: string;
}

export interface GameErrorModel {
  code: string;
  message: string;
  details?: Record<string, unknown>;
}

export interface GameActionResponse {
  success: boolean;
  clientRequestId: string;
  publicState: import('./game-state.models').PublicGameStateModel | null;
  privateState: import('./game-state.models').PrivatePlayerStateModel | null;
  events: GameEventDto[];
  error: GameErrorModel | null;
}

export interface GameEventDto {
  type: string;
  message: string;
  payload?: Record<string, unknown>;
}
