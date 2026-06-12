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
  selector: 'app-match-create',
  standalone: true,
  imports: [FormsModule, ButtonComponent, LoadingSpinnerComponent],
  template: `
    <div class="match-create">
      <h3>Crear partida</h3>

      @if (loading() && decks().length === 0) {
        <app-loading-spinner />
      } @else {
        <div class="form-group">
          <label for="create-name">Tu nombre:</label>
          <input
            id="create-name"
            type="text"
            [(ngModel)]="playerName"
            class="input"
            placeholder="Ingresá tu nombre"
          />
        </div>

        <div class="form-group">
          <label for="create-deck">Mazo:</label>
          <select
            id="create-deck"
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
          [disabled]="!playerName() || !selectedDeckId()"
          [loading]="loading()"
          (click)="onSubmit()"
        >
          Crear partida
        </app-button>
      }
    </div>
  `,
  styles: [`
    :host { display: block; }
    .match-create { padding: 1rem; border: 1px solid #e5e7eb; border-radius: 0.5rem; }
    h3 { margin: 0 0 1rem; font-size: 1.125rem; }
    .form-group { margin-bottom: 0.75rem; }
    label { display: block; margin-bottom: 0.25rem; font-size: 0.875rem; color: #374151; }
    .input { width: 100%; padding: 0.5rem; border: 1px solid #d1d5db; border-radius: 0.375rem; box-sizing: border-box; }
    .error { color: #dc2626; font-size: 0.875rem; margin: 0.5rem 0; }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MatchCreateComponent implements OnInit {
  private readonly deckApi = inject(DeckApiService);
  private readonly matchFacade = inject(MatchFacadeService);
  private readonly notification = inject(NotificationService);

  playerId = input.required<string>();
  preSelectedDeckId = input<string | null>(null);
  created = output<MatchResponse>();

  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly decks = signal<DeckResponse[]>([]);
  readonly playerName = signal('');
  readonly selectedDeckId = signal<string | null>(null);

  ngOnInit(): void {
    this.deckApi.listByPlayer(this.playerId()).subscribe({
      next: (decks) => {
        this.decks.set(decks);
        const preSelected = this.preSelectedDeckId();
        if (preSelected && decks.some(d => d.id === preSelected)) {
          this.selectedDeckId.set(preSelected);
        }
      },
      error: (err) => {
        this.error.set('Error al cargar los mazos');
        this.notification.show('No se pudieron cargar los mazos', 'error');
      },
    });
  }

  onSubmit(): void {
    const name = this.playerName();
    const deckId = this.selectedDeckId();
    if (!name || !deckId) {
      return;
    }

    this.loading.set(true);
    this.error.set(null);

    this.matchFacade.createMatch(name, deckId).subscribe({
      next: (response) => {
        this.loading.set(false);
        this.created.emit(response);
      },
      error: (err) => {
        this.loading.set(false);
        this.error.set(err.message ?? 'Error al crear la partida');
        this.notification.show(this.error()!, 'error');
      },
    });
  }
}
