import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiClientService } from './api-client.service';
import { MatchStateResponse } from '../../shared/models/game-state.models';
import { GameActionRequest, GameActionResponse } from '../../shared/models/game-action.models';

export interface MatchPlayerResponse {
  playerId: string;
  side: string;
  displayName: string;
}

export interface MatchResponse {
  id: string;
  status: string;
  currentPhase: string | null;
  turnNumber: number;
  currentPlayerId: string | null;
  firstPlayerId: string | null;
  winnerPlayerId: string | null;
  finishReason: string | null;
  players: MatchPlayerResponse[];
  createdAt: string;
}

export interface CreateMatchRequest {
  player1Id: string;
  player1Name: string;
  player1DeckId: string;
  player2Name?: string;
  player2DeckId?: string;
}

export interface JoinMatchRequest {
  playerId: string;
  playerName: string;
  deckId: string;
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

  listMatches(status?: string): Observable<MatchResponse[]> {
    const params = new URLSearchParams();
    if (status) params.set('status', status);
    const query = params.toString();
    return this.apiClient.get<MatchResponse[]>(`/matches${query ? '?' + query : ''}`);
  }

  sendAction(matchId: string, action: GameActionRequest): Observable<GameActionResponse> {
    return this.apiClient.post<GameActionResponse>(`/matches/${matchId}/actions`, action);
  }
}
