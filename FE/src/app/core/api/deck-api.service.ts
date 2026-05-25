import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiClientService } from './api-client.service';
import { DeckModel, DeckValidationModel } from '../../shared/models/deck.models';

export interface CreateDeckRequest {
  name: string;
  cards: { cardId: string; quantity: number }[];
}

export interface SeedDeckResponse {
  items: { id: string; name: string; valid: boolean; totalCards: number }[];
}

@Injectable({ providedIn: 'root' })
export class DeckApiService {
  private readonly apiClient = inject(ApiClientService);

  getSeedDecks(): Observable<SeedDeckResponse> {
    return this.apiClient.get<SeedDeckResponse>('/decks/seed');
  }

  getDeck(deckId: string): Observable<DeckModel> {
    return this.apiClient.get<DeckModel>(`/decks/${deckId}`);
  }

  createDeck(request: CreateDeckRequest): Observable<DeckModel> {
    return this.apiClient.post<DeckModel>('/decks', request);
  }

  validateDeck(cards: { cardId: string; quantity: number }[]): Observable<DeckValidationModel> {
    return this.apiClient.post<DeckValidationModel>('/decks/validate', { cards });
  }
}
