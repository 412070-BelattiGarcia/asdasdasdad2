import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiClientService } from './api-client.service';
import { CreateDeckRequest, DeckResponse, DeckValidationResponse, UpdateDeckRequest } from '../../shared/models/deck.models';

@Injectable({ providedIn: 'root' })
export class DeckApiService {
  private readonly apiClient = inject(ApiClientService);

  listByPlayer(playerId: string): Observable<DeckResponse[]> {
    return this.apiClient.get<DeckResponse[]>(`/decks?playerId=${playerId}`);
  }

  get(deckId: string): Observable<DeckResponse> {
    return this.apiClient.get<DeckResponse>(`/decks/${deckId}`);
  }

  create(request: CreateDeckRequest): Observable<DeckResponse> {
    return this.apiClient.post<DeckResponse>('/decks', request);
  }

  update(deckId: string, req: UpdateDeckRequest): Observable<DeckResponse> {
    return this.apiClient.put<DeckResponse>(`/decks/${deckId}`, req);
  }

  delete(deckId: string): Observable<void> {
    return this.apiClient.delete<void>(`/decks/${deckId}`);
  }

  validate(deckId: string): Observable<DeckValidationResponse> {
    return this.apiClient.post<DeckValidationResponse>(`/decks/${deckId}/validate`, {});
  }

  validateCards(cards: { cardId: string; quantity: number }[]): Observable<DeckValidationResponse> {
    return this.apiClient.post<DeckValidationResponse>('/decks/validate', { cards });
  }
}
