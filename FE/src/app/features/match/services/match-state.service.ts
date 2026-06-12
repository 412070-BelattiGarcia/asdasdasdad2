import { computed, inject, Injectable, signal } from '@angular/core';
import { AuthService } from '../../../core/services/auth.service';
import { CardRepositoryService } from '../../../core/services/card-repository.service';
import { MatchApiService } from '../../../core/api/match-api.service';
import { MatchSocketService } from '../../../core/websocket/match-socket.service';
import {
  GameErrorModel,
  GameEventDto,
} from '../../../shared/models/game-action.models';
import {
  PrivatePlayerStateModel,
  PublicGameStateModel,
  PublicPokemonSlotModel,
} from '../../../shared/models/game-state.models';
import { EnergyType } from '../../../shared/models/card.models';
import { filter, Subscription } from 'rxjs';

export type ConnectionStatus = 'DISCONNECTED' | 'CONNECTED' | 'RECONNECTING';

@Injectable({ providedIn: 'root' })
export class MatchStateService {
  private readonly matchApi = inject(MatchApiService);
  private readonly matchSocket = inject(MatchSocketService);
  private readonly authService = inject(AuthService);
  private readonly cardRepo = inject(CardRepositoryService);

  private readonly _matchId = signal<string | null>(null);
  readonly matchId = this._matchId.asReadonly();

  private readonly _publicState = signal<PublicGameStateModel | null>(null);
  readonly publicState = this._publicState.asReadonly();

  private readonly _privateState = signal<PrivatePlayerStateModel | null>(null);
  readonly privateState = this._privateState.asReadonly();

  private readonly _events = signal<GameEventDto[]>([]);
  readonly events = this._events.asReadonly();

  private readonly _pendingRemovals = signal<Set<string>>(new Set());

  private readonly _lastError = signal<GameErrorModel | null>(null);
  readonly lastError = this._lastError.asReadonly();

  private readonly _connectionStatus = signal<ConnectionStatus>('DISCONNECTED');
  readonly connectionStatus = this._connectionStatus.asReadonly();

  readonly isMyTurn = computed(() => {
    const currentId = this._publicState()?.currentPlayerId ?? null;
    const myId = this.myPlayerId();
    return currentId !== null && myId !== null && currentId === myId;
  });

  readonly currentPhase = computed(() => {
    const phase = this._publicState()?.phase;
    if (phase === 'DRAW' || phase === 'MAIN' || phase === 'ATTACK' || phase === 'BETWEEN_TURNS') {
      return phase;
    }
    return null;
  });

  readonly canDraw = computed(() => {
    const state = this._publicState();
    if (!state || state.status !== 'ACTIVE' || state.phase !== 'DRAW') return false;
    if (!this.isMyTurn()) return false;
    // First player doesn't draw on their first turn (TCG rule)
    if (state.turnNumber === 1 && state.currentPlayerId === state.firstPlayerId) return false;
    return true;
  });

  readonly myPlayerId = computed(() => this.authService.playerId());

  readonly myActivePokemon = computed(() => {
    const publicState = this._publicState();
    const myId = this.myPlayerId();
    if (!publicState || !myId) {
      return null;
    }
    const playerState = publicState.players.find(p => p.playerId === myId);
    return playerState?.activePokemon ?? null;
  });

  readonly myBench = computed(() => {
    const publicState = this._publicState();
    const myId = this.myPlayerId();
    if (!publicState || !myId) return [];
    const playerState = publicState.players.find(p => p.playerId === myId);
    return playerState?.bench ?? [];
  });

  readonly opponentActivePokemon = computed(() => {
    const publicState = this._publicState();
    const myId = this.myPlayerId();
    if (!publicState || !myId) {
      return null;
    }
    const opponentState = publicState.players.find(p => p.playerId !== myId);
    return opponentState?.activePokemon ?? null;
  });

  readonly activePokemonRetreatCost = computed<EnergyType[]>(() => {
    const active = this.myActivePokemon();
    if (!active) return [];
    const def = this.cardRepo.getFromCache(active.cardId);
    return (def?.retreatCost as EnergyType[]) ?? [];
  });

  private resolveToEnergyType(value: string): string | null {
    const upper = value.toUpperCase();
    const KNOWN = new Set(['GRASS','FIRE','WATER','LIGHTNING','PSYCHIC','FIGHTING','DARKNESS','METAL','FAIRY','COLORLESS']);
    if (KNOWN.has(upper)) return upper;
    console.warn(`[DEBUG] resolveToEnergyType: '${value}' not a known type, trying card cache`);
    const def = this.cardRepo.getFromCache(value);
    if (def?.types?.length) {
      console.warn(`[DEBUG] resolveToEnergyType: resolved '${value}' via cache to type '${def.types[0]}'`);
      return def.types[0].toUpperCase();
    }
    console.warn(`[DEBUG] resolveToEnergyType: FAILED to resolve '${value}'`);
    return null;
  }

  readonly activePokemonEnergyTypes = computed<EnergyType[]>(() => {
    const active = this.myActivePokemon();
    if (!active) {
      console.warn(`[DEBUG] activePokemonEnergyTypes: no active Pokemon`);
      return [];
    }
    if (!active.attachedCards || active.attachedCards.length === 0) {
      console.warn(`[DEBUG] activePokemonEnergyTypes: active=${active.instanceId} has NO attachedCards`);
      return [];
    }
    const result: EnergyType[] = [];
    for (const c of active.attachedCards) {
      const resolved = this.resolveToEnergyType(c);
      if (resolved) result.push(resolved as EnergyType);
    }
    console.warn(`[DEBUG] activePokemonEnergyTypes: active=${active.instanceId} cards=[${active.attachedCards.join(',')}] resolved=[${result.join(',')}]`);
    return result;
  });

  private wsSubscription: Subscription | null = null;

  initialize(matchId: string): void {
    if (this._matchId() === matchId) return;

    this.wsSubscription?.unsubscribe();
    this.wsSubscription = null;
    this.matchSocket.disconnect();
    this._matchId.set(matchId);

    const playerId = this.myPlayerId();
    if (!playerId) {
      this._lastError.set({ code: 'AUTH_REQUIRED', message: 'Player not authenticated' });
      return;
    }

    this.matchSocket.connect(matchId, playerId);
    this._connectionStatus.set('CONNECTED');

    const publicSub = this.matchSocket.publicEvents$.subscribe(event => {
      if (event.type === 'STATE_UPDATED') {
        const publicStatePayload = event.payload?.['publicState'];
        if (publicStatePayload) {
          this.updatePublicState(publicStatePayload as PublicGameStateModel);
        }
      }
      this.addEvent(event);
    });

    const privateSub = this.matchSocket.privateState$.subscribe(state => {
      this.updatePrivateState(state);
    });

    const errorSub = this.matchSocket.actionErrors$.subscribe(() => {
      this.clearPendingRemovals();
    });

    const connectionSub = this.matchSocket.connectionStatus$.subscribe(status => {
      this._connectionStatus.set(status as ConnectionStatus);
    });

    this.wsSubscription = new Subscription();
    this.wsSubscription.add(publicSub);
    this.wsSubscription.add(privateSub);
    this.wsSubscription.add(errorSub);
    this.wsSubscription.add(connectionSub);

    this.pollMatchState(matchId, playerId);
  }

  private pollMatchState(matchId: string, playerId: string, attempt: number = 0): void {
    if (this._matchId() !== matchId || attempt >= 60) return;

    this.matchApi.getMatchState(matchId, playerId).subscribe({
      next: (response) => {
        this._publicState.set(response.publicState);
        this._privateState.set(response.privateState);
      },
      error: () => {
        setTimeout(() => this.pollMatchState(matchId, playerId, attempt + 1), 2000);
      },
    });
  }

  updatePublicState(state: PublicGameStateModel): void {
    this._publicState.set(state);
  }

  private clearPendingRemovals(): void {
    this._pendingRemovals.set(new Set());
  }

  private trackPendingRemoval(instanceId: string): void {
    this._pendingRemovals.update(set => {
      const next = new Set(set);
      next.add(instanceId);
      return next;
    });
  }

  optimisticallyRemoveCardFromHand(handIndex: number): void {
    this._privateState.update(state => {
      if (!state) {
        console.warn('[DEBUG] optimisticallyRemoveCardFromHand: state is null');
        return state;
      }
      if (handIndex < 0 || handIndex >= state.hand.length) {
        console.warn(`[DEBUG] optimisticallyRemoveCardFromHand: handIndex ${handIndex} out of bounds (hand size ${state.hand.length})`);
        return state;
      }
      const removed = state.hand[handIndex];
      const newHand = state.hand.filter((_, i) => i !== handIndex);
      console.warn(`[DEBUG] optimisticallyRemoveCardFromHand: removed ${removed?.name} (idx ${handIndex}), hand size ${state.hand.length} -> ${newHand.length}`);
      this.trackPendingRemoval(removed.instanceId);
      return { ...state, hand: newHand };
    });
  }

  optimisticallyRemoveCardByInstanceId(cardInstanceId: string): void {
    this._privateState.update(state => {
      if (!state) return state;
      const index = state.hand.findIndex(c => c.instanceId === cardInstanceId);
      if (index < 0) {
        console.warn(`[DEBUG] optimisticallyRemoveCardByInstanceId: instanceId ${cardInstanceId} not found in hand`);
        return state;
      }
      const newHand = state.hand.filter((_, i) => i !== index);
      console.warn(`[DEBUG] optimisticallyRemoveCardByInstanceId: removed idx ${index}, hand size ${state.hand.length} -> ${newHand.length}`);
      this.trackPendingRemoval(cardInstanceId);
      return { ...state, hand: newHand };
    });
  }

  updatePrivateState(state: PrivatePlayerStateModel): void {
    const pending = this._pendingRemovals();
    if (pending.size > 0) {
      const confirmedRemoved = Array.from(pending).filter(
        id => !state.hand.some(c => c.instanceId === id)
      );
      if (confirmedRemoved.length > 0) {
        const filtered = state.hand.filter(c => !confirmedRemoved.includes(c.instanceId));
        console.warn(`[DEBUG] updatePrivateState: filtered ${confirmedRemoved.length} confirmed removals from incoming state`);
        this._privateState.set({ ...state, hand: filtered });
        this._pendingRemovals.update(set => {
          const next = new Set(set);
          for (const id of confirmedRemoved) next.delete(id);
          return next;
        });
        return;
      }
      // Merge incoming state (new cards from draw, etc.) while keeping pending removals filtered out
      const filtered = state.hand.filter(c => !pending.has(c.instanceId));
      this._privateState.set({ ...state, hand: filtered });
      return;
    }
    this._privateState.set(state);
  }

  addEvent(event: GameEventDto): void {
    this._events.update(events => [...events, event]);
  }

  setError(error: GameErrorModel | null): void {
    this._lastError.set(error);
  }

  reset(): void {
    this.wsSubscription?.unsubscribe();
    this.wsSubscription = null;
    this.matchSocket.disconnect();
    this._matchId.set(null);
    this._publicState.set(null);
    this._privateState.set(null);
    this._events.set([]);
    this._lastError.set(null);
    this._connectionStatus.set('DISCONNECTED');
    this._pendingRemovals.set(new Set());
  }
}
