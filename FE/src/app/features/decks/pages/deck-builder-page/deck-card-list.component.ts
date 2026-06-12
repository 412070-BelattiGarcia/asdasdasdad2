import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { DeckCardEntry } from '../../services/deck-builder-facade.service';

@Component({
  selector: 'app-deck-card-list',
  template: `
    <div class="flex flex-col gap-2">
      @for (entry of cards(); track entry.cardId) {
        <div class="flex items-center justify-between rounded-md border bg-white px-3 py-2">
          <div class="flex-1">
            <p class="text-sm font-medium text-gray-900">{{ entry.name }}</p>
            <p class="text-xs text-gray-500">{{ entry.supertype }}</p>
          </div>
          <div class="flex items-center gap-1">
            <button
              class="flex h-7 w-7 items-center justify-center rounded-md bg-gray-100 text-sm font-medium text-gray-700 hover:bg-gray-200 disabled:opacity-30"
              (click)="decrement.emit(entry.cardId)"
            >
              −
            </button>
            <span class="w-6 text-center text-sm font-semibold text-gray-900">
              {{ entry.quantity }}
            </span>
            <button
              class="flex h-7 w-7 items-center justify-center rounded-md bg-blue-50 text-sm font-medium text-blue-700 hover:bg-blue-100 disabled:opacity-30"
              [disabled]="entry.quantity >= 4"
              (click)="increment.emit(entry.cardId)"
            >
              +
            </button>
          </div>
        </div>
      } @empty {
        <p class="py-8 text-center text-sm text-gray-400">
          No hay cartas en el mazo
        </p>
      }
    </div>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DeckCardListComponent {
  cards = input<DeckCardEntry[]>([]);
  readonly increment = output<string>();
  readonly decrement = output<string>();
}
