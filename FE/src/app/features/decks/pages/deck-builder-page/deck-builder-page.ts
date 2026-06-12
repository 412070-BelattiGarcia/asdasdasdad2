import { ChangeDetectionStrategy, Component, inject, signal, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { tap, catchError, of, filter, map, Subscription } from 'rxjs';
import { DeckApiService } from '../../../../core/api/deck-api.service';
import { AuthService } from '../../../../core/services/auth.service';
import { NotificationService } from '../../../../core/services/notification.service';
import { DeckBuilderFacadeService } from '../../services/deck-builder-facade.service';
import { DeckValidationModel } from '../../../../shared/models/deck.models';
import { DeckSearchComponent } from './deck-search.component';
import { DeckCardListComponent } from './deck-card-list.component';
import { DeckSummaryComponent } from './deck-summary.component';

@Component({
  selector: 'app-deck-builder-page',
  imports: [DeckSearchComponent, DeckCardListComponent, DeckSummaryComponent],
  template: `
    <div class="mx-auto max-w-6xl px-4 py-8">
      <div class="mb-6">
        <button
          class="mb-4 text-sm text-blue-600 hover:text-blue-800"
          (click)="router.navigate(['/decks'])"
        >
          ← Volver a mis mazos
        </button>
        <input
          #nameInput
          type="text"
          placeholder="Nombre del mazo"
          (input)="deckName.set(nameInput.value)"
          [value]="deckName()"
          class="w-full max-w-md rounded-md border border-gray-300 px-3 py-2 text-lg font-semibold shadow-sm focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
      </div>

      <div class="grid grid-cols-1 gap-6 lg:grid-cols-2">
        <div class="rounded-lg border bg-white p-4 shadow-sm">
          <h2 class="mb-3 text-sm font-semibold uppercase tracking-wide text-gray-500">Buscar cartas</h2>
          <app-deck-search (cardSelected)="onCardSelected($event)" />
        </div>

        <div class="flex flex-col gap-4">
          <div class="rounded-lg border bg-white p-4 shadow-sm">
            <h2 class="mb-3 text-sm font-semibold uppercase tracking-wide text-gray-500">Mazo</h2>
            <app-deck-card-list
              [cards]="facade.cards()"
              (increment)="onIncrement($event)"
              (decrement)="onDecrement($event)"
            />
          </div>

          <app-deck-summary
            [totalCards]="facade.totalCards()"
            [validation]="validationResult()"
          />

          <div class="flex gap-2">
            <button
              class="flex-1 rounded-md bg-green-600 px-4 py-2 text-sm font-medium text-white hover:bg-green-700 disabled:cursor-not-allowed disabled:opacity-50"
              [disabled]="!deckName().trim() || facade.isEmpty()"
              (click)="onSave()"
            >
              Guardar
            </button>
            <button
              class="rounded-md bg-gray-100 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-200"
              (click)="onValidate()"
            >
              Validar
            </button>
          </div>
        </div>
      </div>
    </div>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DeckBuilderPage implements OnDestroy {
  protected readonly facade = inject(DeckBuilderFacadeService);
  private readonly deckApi = inject(DeckApiService);
  private readonly authService = inject(AuthService);
  protected readonly notificationService = inject(NotificationService);
  protected readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  protected readonly deckName = signal('');
  protected readonly validationResult = signal<DeckValidationModel | null>(null);
  protected readonly saving = signal(false);

  private deckId: string | null = null;
  private playerId: string | null = null;
  private subscription: Subscription | null = null;

  constructor() {
    this.subscription = this.route.paramMap.pipe(
      map((params) => params.get('id')),
      filter((id): id is string => id !== null),
      tap((id) => {
        this.deckId = id;
        this.loadDeck(id);
      }),
    ).subscribe();
    this.route.queryParamMap.pipe(
      map((params) => params.get('playerId')),
      tap((id) => { this.playerId = id; }),
    ).subscribe();
  }

  ngOnDestroy(): void {
    this.subscription?.unsubscribe();
  }

  private loadDeck(id: string): void {
    this.deckApi.get(id).pipe(
      tap((deck) => {
        this.deckName.set(deck.name);
        this.facade.setCards(
          deck.cards.map((c) => ({
            cardId: c.cardId,
            name: c.name,
            supertype: c.supertype,
            isBasicEnergy: c.isBasicEnergy,
            quantity: c.quantity,
          })),
        );
      }),
      catchError(() => {
        this.notificationService.show('Error al cargar el mazo', 'error');
        return of(null);
      }),
    ).subscribe();
  }

  private isEditMode(): boolean {
    return this.deckId !== null;
  }

  onCardSelected(event: { cardId: string; name: string; supertype: string }): void {
    this.facade.addCard(event.cardId, event.name, event.supertype);
    this.validationResult.set(null);
  }

  onIncrement(cardId: string): void {
    const entry = this.facade.cards().find((c) => c.cardId === cardId);
    if (entry) {
      this.facade.addCard(cardId, entry.name, entry.supertype, entry.isBasicEnergy);
    }
    this.validationResult.set(null);
  }

  onDecrement(cardId: string): void {
    this.facade.removeCard(cardId);
    this.validationResult.set(null);
  }

  onValidate(): void {
    const obs = this.isEditMode()
      ? this.deckApi.validate(this.deckId!)
      : this.facade.validate();

    obs.subscribe({
      next: (result) => {
        this.validationResult.set(result);
        this.notificationService.show(
          result.valid ? 'Mazo válido' : 'Mazo inválido',
          result.valid ? 'success' : 'warning',
        );
      },
      error: () => {
        this.notificationService.show('Error al validar', 'error');
      },
    });
  }

  onSave(): void {
    const name = this.deckName().trim();
    if (!name || this.facade.isEmpty()) return;

    this.saving.set(true);
    const cards = this.facade.cards().map((c) => ({ cardId: c.cardId, quantity: c.quantity }));

    const obs = this.isEditMode()
      ? this.deckApi.update(this.deckId!, { name, cards })
      : this.deckApi.create({ name, playerId: this.playerId ?? this.authService.playerId() ?? '', cards });

    obs.subscribe({
      next: () => {
        this.facade.reset();
        this.notificationService.show(
          this.isEditMode() ? 'Mazo actualizado' : 'Mazo creado',
          'success',
        );
        this.router.navigate(['/decks']);
      },
      error: () => {
        this.saving.set(false);
        this.notificationService.show('Error al guardar el mazo', 'error');
      },
    });
  }
}
