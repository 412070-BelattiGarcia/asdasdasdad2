import { inject, Injectable } from '@angular/core';
import { Client, IFrame, IMessage, StompSubscription } from '@stomp/stompjs';
import { Observable, Subject } from 'rxjs';
import SockJS from 'sockjs-client';
import { GameActionResponse, GameEventDto } from '../../shared/models/game-action.models';
import { PrivatePlayerStateModel } from '../../shared/models/game-state.models';

export type ConnectionStatus = 'CONNECTED' | 'DISCONNECTED' | 'RECONNECTING';

@Injectable({ providedIn: 'root' })
export class MatchSocketService {
  private readonly brokerUrl = 'http://localhost:8080/ws';

  private client: Client | null = null;
  private publicSub: StompSubscription | null = null;
  private privateSub: StompSubscription | null = null;

  private readonly _publicEvents = new Subject<GameEventDto>();
  readonly publicEvents$ = this._publicEvents.asObservable();

  private readonly _privateState = new Subject<PrivatePlayerStateModel>();
  readonly privateState$ = this._privateState.asObservable();

  private readonly _actionErrors = new Subject<GameActionResponse['error']>();
  readonly actionErrors$ = this._actionErrors.asObservable();

  private readonly _connectionStatus = new Subject<ConnectionStatus>();
  readonly connectionStatus$ = this._connectionStatus.asObservable();

  private currentMatchId: string | null = null;
  private currentPlayerId: string | null = null;

  connect(matchId: string, playerId: string): void {
    if (this.client?.active) {
      this.disconnect();
    }

    this.currentMatchId = matchId;
    this.currentPlayerId = playerId;
    this._connectionStatus.next('RECONNECTING');

    this.client = new Client({
      webSocketFactory: () => new SockJS(this.brokerUrl),
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      onConnect: () => this.onConnected(),
      onDisconnect: () => this._connectionStatus.next('DISCONNECTED'),
      onStompError: (frame: IFrame) => {
        console.error('STOMP error:', frame.headers['message']);
        this._connectionStatus.next('DISCONNECTED');
      },
    });

    this.client.activate();
  }

  disconnect(): void {
    this.unsubscribeAll();
    this.client?.deactivate();
    this.client = null;
    this.currentMatchId = null;
    this.currentPlayerId = null;
    this._connectionStatus.next('DISCONNECTED');
  }

  sendAction(action: unknown): void {
    if (!this.client?.active || !this.currentMatchId) {
      console.warn('Cannot send action: not connected');
      return;
    }
    this.client.publish({
      destination: `/app/matches/${this.currentMatchId}/actions`,
      body: JSON.stringify(action),
    });
  }

  private onConnected(): void {
    this._connectionStatus.next('CONNECTED');

    if (this.currentMatchId && this.currentPlayerId) {
      this.subscribeToMatch(this.currentMatchId, this.currentPlayerId);
    }
  }

  private subscribeToMatch(matchId: string, playerId: string): void {
    if (!this.client) {
      return;
    }

    this.publicSub = this.client.subscribe(
      `/topic/matches/${matchId}/events`,
      (message: IMessage) => {
        try {
          const response = JSON.parse(message.body);

          // Standalone event (e.g., MULLIGAN_REVEALED published directly
          // by EventPublisherPort during SetupManager.setup())
          if (response.type && !response.publicState && !Array.isArray(response.events)) {
            this._publicEvents.next({
              type: response.type,
              message: response.message ?? response.type,
              payload: response.payload,
            });
            return;
          }

          if (response.publicState) {
            this._publicEvents.next({
              type: 'STATE_UPDATED',
              message: 'State updated',
              payload: { publicState: response.publicState },
            });
          }
          if (response.error) {
            this._actionErrors.next(response.error);
          }
          if (Array.isArray(response.events)) {
            for (const eventType of response.events) {
              if (eventType !== 'STATE_UPDATED') {
                this._publicEvents.next({ type: eventType, message: eventType });
              }
            }
          }
          if (response.privateState) {
            this._privateState.next(response.privateState);
          }
        } catch {
          // ignore parse errors
        }
      },
    );

    this.privateSub = this.client.subscribe(
      `/topic/matches/${matchId}/player/${playerId}`,
      (message: IMessage) => {
        try {
          const state: PrivatePlayerStateModel = JSON.parse(message.body);
          this._privateState.next(state);
        } catch {
          // ignore parse errors
        }
      },
    );
  }

  private unsubscribeAll(): void {
    this.publicSub?.unsubscribe();
    this.publicSub = null;
    this.privateSub?.unsubscribe();
    this.privateSub = null;
  }
}
