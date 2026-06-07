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
    return this.matchApi.sendAction(matchId, request);
  }

  endTurn(matchId: string, playerId: string): Observable<GameActionResponse> {
    return this.dispatchAction(matchId, playerId, 'END_TURN', {});
  }

  putBasicOnBench(matchId: string, playerId: string, handIndex: number): Observable<GameActionResponse> {
    return this.dispatchAction(matchId, playerId, 'PUT_BASIC_ON_BENCH', { handIndex });
  }

  attachEnergy(
    matchId: string,
    playerId: string,
    handIndex: number,
    targetPokemonInstanceId: string,
  ): Observable<GameActionResponse> {
    return this.dispatchAction(matchId, playerId, 'ATTACH_ENERGY', {
      handIndex,
      targetPokemonInstanceId,
    });
  }

  evolvePokemon(
    matchId: string,
    playerId: string,
    handIndex: number,
    targetPokemonInstanceId: string,
  ): Observable<GameActionResponse> {
    return this.dispatchAction(matchId, playerId, 'EVOLVE_POKEMON', {
      handIndex,
      targetPokemonInstanceId,
    });
  }

  playTrainer(matchId: string, playerId: string, handIndex: number): Observable<GameActionResponse> {
    return this.dispatchAction(matchId, playerId, 'PLAY_TRAINER', { handIndex });
  }

  retreatActive(matchId: string, playerId: string, benchIndex: number): Observable<GameActionResponse> {
    return this.dispatchAction(matchId, playerId, 'RETREAT_ACTIVE', { benchIndex });
  }

  declareAttack(
    matchId: string,
    playerId: string,
    attackIndex: number,
    targetPokemonInstanceId: string,
  ): Observable<GameActionResponse> {
    return this.dispatchAction(matchId, playerId, 'DECLARE_ATTACK', {
      attackIndex,
      targetPokemonInstanceId,
    });
  }
}
