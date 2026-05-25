import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { MatchApiService } from '../../../core/api/match-api.service';
import { GameActionRequest, GameActionResponse, GameActionType } from '../../../shared/models/game-action.models';

@Injectable({ providedIn: 'root' })
export class GameActionDispatcherService {
  private readonly matchApi = inject(MatchApiService);

  dispatchAction(
    matchId: string,
    playerId: string,
    actionType: GameActionType,
    payload: Record<string, unknown>,
    clientRequestId?: string,
  ): Observable<GameActionResponse> {
    const request: GameActionRequest = {
      type: actionType,
      playerId,
      payload,
      clientRequestId: clientRequestId ?? crypto.randomUUID(),
    };
    return this.matchApi.sendAction(matchId, request) as Observable<GameActionResponse>;
  }

  drawCard(matchId: string, playerId: string): Observable<GameActionResponse> {
    return this.dispatchAction(matchId, playerId, 'DRAW_CARD', {});
  }

  endTurn(matchId: string, playerId: string): Observable<GameActionResponse> {
    return this.dispatchAction(matchId, playerId, 'END_TURN', {});
  }

  attachEnergy(
    matchId: string,
    playerId: string,
    energyCardInstanceId: string,
    targetPokemonInstanceId: string,
  ): Observable<GameActionResponse> {
    return this.dispatchAction(matchId, playerId, 'ATTACH_ENERGY', {
      energyCardInstanceId,
      targetPokemonInstanceId,
    });
  }

  declareAttack(
    matchId: string,
    playerId: string,
    attackerPokemonInstanceId: string,
    attackIndex: number,
  ): Observable<GameActionResponse> {
    return this.dispatchAction(matchId, playerId, 'DECLARE_ATTACK', {
      attackerPokemonInstanceId,
      attackIndex,
    });
  }

  retreatActive(
    matchId: string,
    playerId: string,
    newActiveInstanceId: string,
  ): Observable<GameActionResponse> {
    return this.dispatchAction(matchId, playerId, 'RETREAT_ACTIVE', {
      newActiveInstanceId,
    });
  }
}
