export type GameActionType =
  | 'DRAW_CARD'
  | 'PUT_BASIC_ON_BENCH'
  | 'ATTACH_ENERGY'
  | 'EVOLVE_POKEMON'
  | 'PLAY_TRAINER'
  | 'RETREAT_ACTIVE'
  | 'DECLARE_ATTACK'
  | 'END_TURN'
  | 'CHOOSE_KNOCKOUT_REPLACEMENT'
  | 'USE_ABILITY';

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
  publicState: unknown;
  privateState: unknown;
  events: GameEventModel[];
  error: GameErrorModel | null;
}

export interface GameEventModel {
  type: GameEventType;
  matchId: string;
  turnNumber: number;
  createdAt: string;
  message: string;
  payload?: Record<string, unknown>;
}

export type GameEventType =
  | 'MATCH_CREATED'
  | 'PLAYER_JOINED'
  | 'SETUP_COMPLETED'
  | 'TURN_STARTED'
  | 'PHASE_CHANGED'
  | 'CARD_DRAWN'
  | 'POKEMON_PLACED_ON_BENCH'
  | 'ENERGY_ATTACHED'
  | 'POKEMON_EVOLVED'
  | 'TRAINER_PLAYED'
  | 'RETREAT_EXECUTED'
  | 'ATTACK_DECLARED'
  | 'DAMAGE_APPLIED'
  | 'SPECIAL_CONDITION_APPLIED'
  | 'SPECIAL_CONDITION_REMOVED'
  | 'KNOCKOUT_OCCURRED'
  | 'PRIZE_TAKEN'
  | 'VICTORY_DECIDED'
  | 'STATE_UPDATED';
