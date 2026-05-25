import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiClientService } from './api-client.service';

export interface CardSearchRequest {
  query?: string;
  supertype?: string;
  setCode?: string;
  page?: number;
  size?: number;
}

export interface CardSummaryResponse {
  id: string;
  name: string;
  supertype: string;
  setCode: string;
  number: string;
  imageSmallUrl: string;
}

export interface CardSearchResponse {
  items: CardSummaryResponse[];
  page: number;
  size: number;
  totalItems: number;
}

@Injectable({ providedIn: 'root' })
export class CardApiService {
  private readonly apiClient = inject(ApiClientService);

  searchCards(request: CardSearchRequest): Observable<CardSearchResponse> {
    const params = new URLSearchParams();
    if (request.query) params.set('query', request.query);
    if (request.supertype) params.set('supertype', request.supertype);
    if (request.setCode) params.set('setCode', request.setCode);
    if (request.page !== undefined) params.set('page', request.page.toString());
    if (request.size !== undefined) params.set('size', request.size.toString());
    return this.apiClient.get<CardSearchResponse>(`/cards?${params.toString()}`);
  }

  getCardById(cardId: string): Observable<unknown> {
    return this.apiClient.get<unknown>(`/cards/${cardId}`);
  }
}
