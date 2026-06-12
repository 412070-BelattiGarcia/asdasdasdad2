import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { DeckResponse } from '../../../../shared/models/deck.models';
import { DeckItemComponent } from './deck-item.component';

@Component({
  selector: 'app-deck-list',
  imports: [DeckItemComponent],
  template: `
    @if (loading()) {
      <div class="flex justify-center py-12">
        <div class="h-8 w-8 animate-spin rounded-full border-4 border-blue-600 border-t-transparent"></div>
      </div>
    } @else if (error()) {
      <div class="rounded-lg border border-red-200 bg-red-50 p-6 text-center">
        <p class="text-red-700">{{ error() }}</p>
        <button
          class="mt-3 rounded-md bg-red-600 px-4 py-2 text-sm font-medium text-white hover:bg-red-700"
          (click)="retry.emit()"
        >
          Reintentar
        </button>
      </div>
    } @else if (empty()) {
      <div class="rounded-lg border border-gray-200 bg-gray-50 p-6 text-center">
        <p class="text-gray-500">No hay mazos disponibles.</p>
      </div>
    } @else {
      <div class="flex flex-col gap-3">
        @for (deck of decks(); track deck.id) {
          <app-deck-item
            [deck]="deck"
            (delete)="delete.emit($event)"
            (validate)="validate.emit($event)"
            (play)="play.emit($event)"
            (edit)="edit.emit($event)"
          />
        }
      </div>
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DeckListComponent {
  decks = input<DeckResponse[]>([]);
  loading = input(false);
  error = input<string | null>(null);
  empty = input(false);

  readonly delete = output<string>();
  readonly validate = output<string>();
  readonly play = output<string>();
  readonly edit = output<string>();
  readonly retry = output<void>();
}
