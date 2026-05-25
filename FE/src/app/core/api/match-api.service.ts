import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiClientService } from './api-client.service';

export interface CreateMatchRequest {
  playerName: string;
  deckId: string;
}

export interface JoinMatchRequest {
  playerName: string;
  deckId: string;
}

export interface MatchResponse {
  matchId: string;
  playerId: string;
  side: string;
  status: string;
}

export interface MatchStateResponse {
  publicState: unknown;
  privateState: unknown;
}

@Injectable({ providedIn: 'root' })
export class MatchApiService {
  private readonly apiClient = inject(ApiClientService);

  createMatch(request: CreateMatchRequest): Observable<MatchResponse> {
    return this.apiClient.post<MatchResponse>('/matches', request);
  }

  joinMatch(matchId: string, request: JoinMatchRequest): Observable<MatchResponse> {
    return this.apiClient.post<MatchResponse>(`/matches/${matchId}/join`, request);
  }

  getMatchState(matchId: string, playerId: string): Observable<MatchStateResponse> {
    return this.apiClient.get<MatchStateResponse>(`/matches/${matchId}/state?playerId=${playerId}`);
  }

  sendAction(matchId: string, action: unknown): Observable<unknown> {
    return this.apiClient.post<unknown>(`/matches/${matchId}/actions`, action);
  }
}
