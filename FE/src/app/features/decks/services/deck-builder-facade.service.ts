import { computed, inject, Injectable, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { DeckApiService } from '../../../core/api/deck-api.service';
import { DeckValidationModel } from '../../../shared/models/deck.models';

export interface DeckCardEntry {
  cardId: string;
  quantity: number;
}

@Injectable({ providedIn: 'root' })
export class DeckBuilderFacadeService {
  private readonly deckApi = inject(DeckApiService);

  private readonly _cards = signal<DeckCardEntry[]>([]);
  readonly cards = this._cards.asReadonly();
  readonly totalCards = computed(() =>
    this.cards().reduce((sum, c) => sum + c.quantity, 0),
  );
  readonly isEmpty = computed(() => this.totalCards() === 0);

  addCard(cardId: string): void {
    this._cards.update((prev) => {
      const existing = prev.find((c) => c.cardId === cardId);
      if (existing) {
        return prev.map((c) =>
          c.cardId === cardId ? { ...c, quantity: c.quantity + 1 } : c,
        );
      }
      return [...prev, { cardId, quantity: 1 }];
    });
  }

  removeCard(cardId: string): void {
    this._cards.update((prev) => prev.filter((c) => c.cardId !== cardId));
  }

  setCards(cards: DeckCardEntry[]): void {
    this._cards.set(cards);
  }

  reset(): void {
    this._cards.set([]);
  }

  validate(): Observable<DeckValidationModel> {
    return this.deckApi.validateDeck(this._cards());
  }

  createDeck(name: string): Observable<unknown> {
    return this.deckApi
      .createDeck({ name, cards: this._cards() })
      .pipe(tap(() => this.reset()));
  }
}
