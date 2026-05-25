import { inject, Injectable, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { MatchApiService, MatchResponse, MatchStateResponse } from '../../../core/api/match-api.service';

@Injectable({ providedIn: 'root' })
export class MatchFacadeService {
  private readonly matchApi = inject(MatchApiService);

  private readonly _matchId = signal<string | null>(null);
  readonly matchId = this._matchId.asReadonly();

  private readonly _playerId = signal<string | null>(null);
  readonly playerId = this._playerId.asReadonly();

  private readonly _side = signal<string | null>(null);
  readonly side = this._side.asReadonly();

  private readonly _status = signal<string | null>(null);
  readonly status = this._status.asReadonly();

  createMatch(playerName: string, deckId: string): Observable<MatchResponse> {
    return this.matchApi.createMatch({ playerName, deckId }).pipe(
      tap((res) => {
        this._matchId.set(res.matchId);
        this._playerId.set(res.playerId);
        this._side.set(res.side);
        this._status.set(res.status);
      }),
    );
  }

  joinMatch(matchId: string, playerName: string, deckId: string): Observable<MatchResponse> {
    return this.matchApi.joinMatch(matchId, { playerName, deckId }).pipe(
      tap((res) => {
        this._matchId.set(res.matchId);
        this._playerId.set(res.playerId);
        this._side.set(res.side);
        this._status.set(res.status);
      }),
    );
  }

  getMatchState(): Observable<MatchStateResponse> {
    const mId = this._matchId();
    const pId = this._playerId();
    if (!mId || !pId) {
      throw new Error('No active match');
    }
    return this.matchApi.getMatchState(mId, pId);
  }

  reset(): void {
    this._matchId.set(null);
    this._playerId.set(null);
    this._side.set(null);
    this._status.set(null);
  }
}
