import { ChangeDetectionStrategy, Component, inject, output } from '@angular/core';
import { Subject, switchMap, debounceTime, distinctUntilChanged, catchError, of } from 'rxjs';
import { toSignal } from '@angular/core/rxjs-interop';
import { CardApiService } from '../../../../core/api/card-api.service';
import { CardViewComponent } from '../../../../shared/components/card-view/card-view.component';

@Component({
  selector: 'app-deck-search',
  imports: [CardViewComponent],
  template: `
    <div class="flex flex-col gap-3">
      <input
        #searchInput
        type="text"
        placeholder="Buscar cartas..."
        (input)="searchSubject.next(searchInput.value)"
        class="w-full rounded-md border border-gray-300 px-3 py-2 text-sm shadow-sm focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-500"
      />
      <div class="grid grid-cols-3 gap-2">
        @for (card of searchResult()?.items ?? []; track card.id) {
          <button
            class="cursor-pointer text-left transition-opacity hover:opacity-80"
            (click)="selectCard(card.id, card.name, card.supertype)"
          >
            <app-card-view [card]="card" />
          </button>
        } @empty {
          @if (!searchResult()) {
            <p class="col-span-3 py-8 text-center text-sm text-gray-400">
              Escribí para buscar cartas
            </p>
          }
        }
      </div>
    </div>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DeckSearchComponent {
  private readonly cardApi = inject(CardApiService);

  protected readonly searchSubject = new Subject<string>();

  private readonly search$ = this.searchSubject.pipe(
    debounceTime(300),
    distinctUntilChanged(),
    switchMap((query) =>
      this.cardApi.searchCards({ query, size: 20 }).pipe(
        catchError(() => of({ items: [], page: 0, size: 20, totalItems: 0 })),
      ),
    ),
  );

  protected readonly searchResult = toSignal(this.search$);

  readonly cardSelected = output<{ cardId: string; name: string; supertype: string }>();

  selectCard(cardId: string, name: string, supertype: string): void {
    this.cardSelected.emit({ cardId, name, supertype });
  }
}
