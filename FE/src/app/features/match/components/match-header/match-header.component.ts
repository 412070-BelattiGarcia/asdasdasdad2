import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { PublicGameStateModel } from '../../../../shared/models/game-state.models';
import { LoadingSpinnerComponent } from '../../../../shared/components/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-match-header',
  standalone: true,
  imports: [LoadingSpinnerComponent],
  template: `
    @if (publicState(); as state) {
      <header class="match-header">
        <span class="turn">Turno {{ state.turnNumber }}</span>
        <span class="phase">Fase: {{ state.phase }}</span>
        @if (state.currentPlayerId === myPlayerId()) {
          <span class="indicator my-turn">⭐ Es tu turno</span>
        } @else {
          <span class="indicator waiting">⏳ Esperando oponente</span>
        }
      </header>
    } @else {
      <app-loading-spinner />
    }
  `,
  styles: [`
    :host { display: block; }
    .match-header {
      display: flex; gap: 1rem; align-items: center;
      padding: 0.75rem 1.5rem;
      background: #1e293b; color: #f1f5f9;
      border-radius: 0.5rem;
    }
    .turn { font-weight: 600; }
    .phase { color: #94a3b8; }
    .indicator { margin-left: auto; font-weight: 500; }
    .my-turn { color: #22c55e; }
    .waiting { color: #facc15; }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MatchHeaderComponent {
  readonly publicState = input<PublicGameStateModel | null>(null);
  readonly myPlayerId = input<string | null>(null);
}
