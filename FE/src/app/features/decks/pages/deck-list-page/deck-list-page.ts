import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Subject, switchMap, debounceTime, distinctUntilChanged, catchError, of, startWith, map } from 'rxjs';
import { toSignal } from '@angular/core/rxjs-interop';
import { DeckApiService } from '../../../../core/api/deck-api.service';
import { AuthService } from '../../../../core/services/auth.service';
import { NotificationService } from '../../../../core/services/notification.service';
import { DeckResponse } from '../../../../shared/models/deck.models';
import { DeckListComponent } from './deck-list.component';

const DEFAULT_PLAYER_ID = 'player-dev';

interface DeckDataState {
  decks: DeckResponse[];
  loading: boolean;
  error: string | null;
}

@Component({
  selector: 'app-deck-list-page',
  imports: [DeckListComponent],
  template: `
    <div class="mx-auto max-w-4xl px-4 py-8">
      <div class="mb-6 flex items-center justify-between">
        <h1 class="text-2xl font-bold text-gray-900">Mis Mazos</h1>
        <button
          class="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700"
          (click)="onNewDeck()"
        >
          + Nuevo mazo
        </button>
      </div>

      <div class="mb-6 flex items-center gap-3">
        <input
          #playerInput
          type="text"
          placeholder="ID del jugador"
          (input)="rawInput.set(playerInput.value); playerIdSubject.next(playerInput.value)"
          class="w-full max-w-xs rounded-md border border-gray-300 px-3 py-2 text-sm shadow-sm focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
        <button
          class="rounded-md bg-gray-800 px-4 py-2 text-sm font-medium text-white hover:bg-gray-900"
          (click)="rawInput.set(playerInput.value); playerIdSubject.next(playerInput.value)"
        >
          Cargar mazos
        </button>
      </div>

      <app-deck-list
        [decks]="deckData().decks"
        [loading]="deckData().loading"
        [error]="deckData().error"
        [empty]="deckData().decks.length === 0 && !deckData().loading && !deckData().error"
        (delete)="onDelete($event)"
        (validate)="onValidate($event)"
        (play)="onPlay($event)"
        (edit)="onEdit($event)"
        (retry)="playerIdSubject.next(playerInput.value)"
      />
    </div>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DeckListPage {
  private readonly deckApi = inject(DeckApiService);
  private readonly authService = inject(AuthService);
  private readonly notificationService = inject(NotificationService);
  private readonly router = inject(Router);

  protected readonly playerIdSubject = new Subject<string>();
  protected readonly rawInput = signal('');

  private resolvePlayerId(raw: string): string {
    const trimmed = raw.trim();
    if (trimmed) return trimmed;
    return this.authService.playerId() || DEFAULT_PLAYER_ID;
  }

  private readonly loadDecks$ = this.playerIdSubject.pipe(
    debounceTime(300),
    distinctUntilChanged(),
    map((raw) => this.resolvePlayerId(raw)),
    distinctUntilChanged(),
    switchMap((playerId) =>
      this.deckApi.listByPlayer(playerId).pipe(
        map((decks) => ({ decks, loading: false, error: null })),
        catchError((err) =>
          of({
            decks: [] as DeckResponse[],
            loading: false,
            error: err?.error?.message || 'Error al cargar mazos.',
          }),
        ),
        startWith({ decks: [] as DeckResponse[], loading: true, error: null }),
      ),
    ),
  );

  protected readonly deckData = toSignal(this.loadDecks$, {
    initialValue: { decks: [] as DeckResponse[], loading: false, error: null },
  });

  constructor() {
    this.playerIdSubject.next('');
  }

  onNewDeck(): void {
    const playerId = this.resolvePlayerId(this.rawInput());
    this.router.navigate(['/decks/new'], { queryParams: { playerId } });
  }

  onEdit(id: string): void {
    this.router.navigate(['/decks', id, 'edit']);
  }

  onPlay(id: string): void {
    this.router.navigate(['/lobby'], { queryParams: { deckId: id } });
  }

  onDelete(id: string): void {
    this.deckApi.delete(id).subscribe({
      next: () => {
        this.notificationService.show('Mazo eliminado', 'success');
        this.playerIdSubject.next('');
      },
      error: () => {
        this.notificationService.show('Error al eliminar el mazo', 'error');
      },
    });
  }

  onValidate(id: string): void {
    this.deckApi.validate(id).subscribe({
      next: (validation) => {
        this.notificationService.show(
          validation.valid ? 'Mazo válido' : 'Mazo inválido',
          validation.valid ? 'success' : 'warning',
        );
      },
      error: () => {
        this.notificationService.show('Error al validar el mazo', 'error');
      },
    });
  }
}
