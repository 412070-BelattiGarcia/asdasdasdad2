import { computed, inject, Injectable, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { DeckApiService } from '../../../core/api/deck-api.service';
import { DeckValidationModel } from '../../../shared/models/deck.models';
import { AuthService } from '../../../core/services/auth.service';

export interface DeckCardEntry {
  cardId: string;
  name: string;
  supertype: string;
  isBasicEnergy: boolean;
  quantity: number;
}

@Injectable({ providedIn: 'root' })
export class DeckBuilderFacadeService {
  private readonly deckApi = inject(DeckApiService);
  private readonly authService = inject(AuthService);

  private readonly _cards = signal<DeckCardEntry[]>([]);
  readonly cards = this._cards.asReadonly();
  readonly totalCards = computed(() =>
    this.cards().reduce((sum, c) => sum + c.quantity, 0),
  );
  readonly isEmpty = computed(() => this.totalCards() === 0);

  addCard(cardId: string, name: string, supertype: string, isBasicEnergy = false): void {
    this._cards.update((prev) => {
      const existing = prev.find((c) => c.cardId === cardId);
      if (existing) {
        return prev.map((c) =>
          c.cardId === cardId ? { ...c, quantity: c.quantity + 1 } : c,
        );
      }
      return [...prev, { cardId, name, supertype, isBasicEnergy, quantity: 1 }];
    });
  }

  removeCard(cardId: string): void {
    this._cards.update((prev) => {
      const existing = prev.find((c) => c.cardId === cardId);
      if (!existing) return prev;
      if (existing.quantity <= 1) return prev.filter((c) => c.cardId !== cardId);
      return prev.map((c) =>
        c.cardId === cardId ? { ...c, quantity: c.quantity - 1 } : c,
      );
    });
  }

  setCards(cards: DeckCardEntry[]): void {
    this._cards.set(cards);
  }

  reset(): void {
    this._cards.set([]);
  }

  validate(): Observable<DeckValidationModel> {
    return this.deckApi.validateCards(this._cards());
  }

  createDeck(name: string): Observable<unknown> {
    const playerId = this.authService.playerId();
    return this.deckApi
      .create({ name, playerId: playerId ?? '', cards: this._cards() })
      .pipe(tap(() => this.reset()));
  }
}
