export interface DeckCardModel {
  cardId: string;
  name: string;
  quantity: number;
  supertype: string;
  isBasicEnergy: boolean;
}

export interface DeckValidationErrorModel {
  code: string;
  message: string;
  details?: Record<string, unknown>;
}

export interface DeckValidationModel {
  valid: boolean;
  errors: DeckValidationErrorModel[];
}

export interface DeckValidationResponse {
  valid: boolean;
  errors: DeckValidationErrorModel[];
}

export interface DeckResponse {
  id: string;
  name: string;
  ownerPlayerId: string | null;
  source: string;
  totalCards: number;
  valid: boolean;
  cards: DeckCardModel[];
  validation: DeckValidationModel;
}

export interface CreateDeckRequest {
  name: string;
  playerId: string;
  cards: { cardId: string; quantity: number }[];
}

export interface UpdateDeckRequest {
  name: string;
  cards: { cardId: string; quantity: number }[];
}
