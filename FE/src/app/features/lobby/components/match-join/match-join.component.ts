import { ChangeDetectionStrategy, Component, inject, input, output, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DeckApiService } from '../../../../core/api/deck-api.service';
import { MatchFacadeService } from '../../../match/services/match-facade.service';
import { NotificationService } from '../../../../core/services/notification.service';
import { ButtonComponent } from '../../../../shared/components/button/button.component';
import { LoadingSpinnerComponent } from '../../../../shared/components/loading-spinner/loading-spinner.component';
import { DeckResponse } from '../../../../shared/models/deck.models';
import { MatchResponse } from '../../../../core/api/match-api.service';

@Component({
  selector: 'app-match-join',
  standalone: true,
  imports: [FormsModule, ButtonComponent, LoadingSpinnerComponent],
  template: `
    <div class="match-join">
      <h3>Unirse a partida</h3>

      @if (loading() && decks().length === 0) {
        <app-loading-spinner />
      } @else {
        <div class="form-group">
          <label for="join-match-id">ID de la partida:</label>
          <input
            id="join-match-id"
            type="text"
            [(ngModel)]="matchIdField"
            class="input"
            placeholder="Ingresá el ID"
          />
        </div>

        <div class="form-group">
          <label for="join-name">Tu nombre:</label>
          <input
            id="join-name"
            type="text"
            [(ngModel)]="playerName"
            class="input"
            placeholder="Ingresá tu nombre"
          />
        </div>

        <div class="form-group">
          <label for="join-deck">Mazo:</label>
          <select
            id="join-deck"
            [(ngModel)]="selectedDeckId"
            class="input"
          >
            <option [ngValue]="null" disabled>Seleccioná un mazo</option>
            @for (deck of decks(); track deck.id) {
              <option [ngValue]="deck.id">{{ deck.name }}</option>
            }
          </select>
        </div>

        @if (error()) {
          <p class="error">{{ error() }}</p>
        }

        <app-button
          variant="primary"
          [disabled]="!matchIdField() || !playerName() || !selectedDeckId()"
          [loading]="loading()"
          (click)="onSubmit()"
        >
          Unirse
        </app-button>
      }
    </div>
  `,
  styles: [`
    :host { display: block; }
    .match-join { padding: 1rem; border: 1px solid #e5e7eb; border-radius: 0.5rem; }
    h3 { margin: 0 0 1rem; font-size: 1.125rem; }
    .form-group { margin-bottom: 0.75rem; }
    label { display: block; margin-bottom: 0.25rem; font-size: 0.875rem; color: #374151; }
    .input { width: 100%; padding: 0.5rem; border: 1px solid #d1d5db; border-radius: 0.375rem; box-sizing: border-box; }
    .error { color: #dc2626; font-size: 0.875rem; margin: 0.5rem 0; }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MatchJoinComponent implements OnInit {
  private readonly deckApi = inject(DeckApiService);
  private readonly matchFacade = inject(MatchFacadeService);
  private readonly notification = inject(NotificationService);

  playerId = input.required<string>();
  joined = output<MatchResponse>();

  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly decks = signal<DeckResponse[]>([]);
  readonly matchIdField = signal('');
  readonly playerName = signal('');
  readonly selectedDeckId = signal<string | null>(null);

  ngOnInit(): void {
    this.deckApi.listByPlayer(this.playerId()).subscribe({
      next: (decks) => this.decks.set(decks),
      error: (err) => {
        this.error.set('Error al cargar los mazos');
        this.notification.show('No se pudieron cargar los mazos', 'error');
      },
    });
  }

  setMatchId(id: string): void {
    this.matchIdField.set(id);
  }

  onSubmit(): void {
    const matchId = this.matchIdField();
    const name = this.playerName();
    const deckId = this.selectedDeckId();
    if (!matchId || !name || !deckId) {
      return;
    }

    this.loading.set(true);
    this.error.set(null);

    this.matchFacade.joinMatch(matchId, name, deckId).subscribe({
      next: (response) => {
        this.loading.set(false);
        this.joined.emit(response);
      },
      error: (err) => {
        this.loading.set(false);
        this.error.set(err.message ?? 'Error al unirse a la partida');
        this.notification.show(this.error()!, 'error');
      },
    });
  }
}
