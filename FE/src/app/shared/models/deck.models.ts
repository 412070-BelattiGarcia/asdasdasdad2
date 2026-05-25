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

export interface DeckModel {
  id: string;
  name: string;
  ownerPlayerId: string | null;
  source: 'SEED' | 'CUSTOM';
  totalCards: number;
  valid: boolean;
  cards: DeckCardModel[];
  validation: DeckValidationModel;
}
