import { ChangeDetectionStrategy, Component, computed, inject, output } from '@angular/core';
import { MatchStateService } from '../../services/match-state.service';
import { MatchInteractionService } from '../../services/match-interaction.service';
import { CardRepositoryService } from '../../../../core/services/card-repository.service';
import { LoadingSpinnerComponent } from '../../../../shared/components/loading-spinner/loading-spinner.component';
import { GameActionType } from '../../../../shared/models/game-action.models';

@Component({
  selector: 'app-action-panel',
  imports: [LoadingSpinnerComponent],
  template: `
    <div class="action-panel">
      @if (actionInProgress()) {
        <div class="action-loading">
          <app-loading-spinner />
          <span>Enviando acción...</span>
        </div>
      }

      @if (!isMyTurn() && !actionInProgress()) {
        <div class="waiting-turn">
          Esperando al oponente...
        </div>
      }

      @if (isMyTurn() && currentPhase() === 'DRAW' && !actionInProgress()) {
        <div class="draw-phase-message">
          <span class="draw-icon">🃏</span>
          <span>Debés robar una carta para comenzar tu turno</span>
        </div>
        <div class="action-buttons">
          <button class="btn btn-draw" [disabled]="!canDraw()" (click)="emitAction('DRAW_CARD')">
            Robar carta
          </button>
        </div>
      }

      @if (isMyTurn() && currentPhase() === 'MAIN' && !actionInProgress()) {
        <div class="action-buttons">
          <button class="btn btn-primary" [disabled]="isSelecting()" (click)="emitAction('END_TURN')">
            Finalizar turno
          </button>
        </div>
      }

      @if (isSelecting() && !actionInProgress()) {
        <div class="cancel-row">
          <button class="btn btn-cancel" (click)="onCancel()">
            Cancelar
          </button>
        </div>
      }
    </div>
  `,
  styles: [`
    .action-panel {
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
      padding: 0.75rem;
      background: #1e293b;
      border-top: 1px solid #334155;
    }
    .waiting-turn {
      text-align: center;
      color: #94a3b8;
      font-size: 0.875rem;
      padding: 0.5rem;
    }
    .action-loading {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 0.5rem;
      color: #94a3b8;
      font-size: 0.875rem;
      padding: 0.5rem;
    }
    .action-buttons {
      display: flex;
      flex-wrap: wrap;
      gap: 0.5rem;
      justify-content: center;
    }
    .btn {
      padding: 0.5rem 1rem;
      border: 1px solid #475569;
      border-radius: 0.375rem;
      background: #0f172a;
      color: #e2e8f0;
      font-size: 0.8125rem;
      font-weight: 600;
      cursor: pointer;
      transition: background 0.15s, border-color 0.15s;
      font-family: inherit;
    }
    .btn:hover:not(:disabled) {
      background: #1e293b;
      border-color: #60a5fa;
    }
    .btn:disabled {
      opacity: 0.4;
      cursor: not-allowed;
    }
    .btn-primary {
      background: #2563eb;
      border-color: #2563eb;
      color: #fff;
    }
    .btn-primary:hover:not(:disabled) {
      background: #3b82f6;
    }
    .btn-attack {
      background: #991b1b;
      border-color: #991b1b;
      color: #fff;
    }
    .btn-attack:hover:not(:disabled) {
      background: #b91c1c;
    }
    .btn-draw {
      background: #2563eb;
      border-color: #2563eb;
      color: #fff;
    }
    .btn-draw:hover:not(:disabled) {
      background: #3b82f6;
    }
    .btn-cancel {
      background: transparent;
      border-color: #ef4444;
      color: #ef4444;
    }
    .draw-phase-message {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 0.5rem;
      padding: 0.5rem;
      background: #1e3a5f;
      border: 1px solid #3b82f6;
      border-radius: 0.375rem;
      color: #93c5fd;
      font-size: 0.875rem;
      font-weight: 600;
      animation: draw-pulse 1.5s ease-in-out infinite;
    }
    .draw-icon { font-size: 1.25rem; }
    @keyframes draw-pulse {
      0%, 100% { opacity: 1; }
      50% { opacity: 0.7; }
    }
    .btn-cancel:hover:not(:disabled) {
      background: #450a0a;
    }
    .cancel-row {
      display: flex;
      justify-content: center;
    }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ActionPanelComponent {
  private readonly matchState = inject(MatchStateService);
  protected readonly interactionService = inject(MatchInteractionService);
  private readonly cardRepo = inject(CardRepositoryService);

  readonly actionSelected = output<{ type: GameActionType; payload?: Record<string, unknown> }>();

  protected readonly isMyTurn = this.matchState.isMyTurn;
  protected readonly currentPhase = this.matchState.currentPhase;
  protected readonly isSelecting = this.interactionService.isSelecting;
  protected readonly actionInProgress = this.interactionService.actionInProgress;
  protected readonly canDraw = this.matchState.canDraw;

  protected readonly myActiveCardDef = computed(() => {
    const active = this.matchState.myActivePokemon();
    if (!active) return null;
    return this.cardRepo.getFromCache(active.cardId);
  });

  protected readonly opponentActiveInstanceId = computed(() => {
    return this.matchState.opponentActivePokemon()?.instanceId ?? null;
  });

  protected emitAction(type: GameActionType, payload?: Record<string, unknown>): void {
    // For DECLARE_ATTACK, inject opponent active as the default target (MVP)
    if (type === 'DECLARE_ATTACK') {
      const targetId = this.opponentActiveInstanceId();
      if (!targetId) return;
      this.actionSelected.emit({ type, payload: { ...payload, targetPokemonInstanceId: targetId } });
      return;
    }
    this.actionSelected.emit({ type, payload });
  }

  protected onCancel(): void {
    this.interactionService.cancelSelection();
  }
}
