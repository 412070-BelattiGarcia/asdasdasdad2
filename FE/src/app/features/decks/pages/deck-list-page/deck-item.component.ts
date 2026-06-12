import { ChangeDetectionStrategy, Component, input, output, signal } from '@angular/core';
import { DeckResponse } from '../../../../shared/models/deck.models';
import { DeckValidationComponent } from './deck-validation.component';

@Component({
  selector: 'app-deck-item',
  imports: [DeckValidationComponent],
  template: `
    <div class="flex items-center justify-between rounded-lg border bg-white p-4 shadow-sm">
      <div class="flex-1">
        <div class="flex items-center gap-2">
          <h3 class="text-lg font-semibold text-gray-900">{{ deck().name }}</h3>
          <app-deck-validation [valid]="deck().valid" />
        </div>
        <p class="mt-1 text-sm text-gray-500">
          {{ deck().totalCards }} cartas · {{ deck().source }}
        </p>
      </div>
      <div class="flex items-center gap-2">
        @if (confirmingDelete()) {
          <span class="text-sm text-gray-600">¿Eliminar?</span>
          <button
            class="rounded-md bg-red-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-red-700"
            (click)="onConfirmDelete()"
          >
            Eliminar ⚠️
          </button>
          <button
            class="rounded-md bg-gray-200 px-3 py-1.5 text-sm font-medium text-gray-700 hover:bg-gray-300"
            (click)="confirmingDelete.set(false)"
          >
            Cancelar
          </button>
        } @else {
          <button
            class="rounded-md bg-gray-100 px-3 py-1.5 text-sm font-medium text-gray-700 hover:bg-gray-200"
            (click)="onEdit()"
          >
            Editar
          </button>
          <button
            class="rounded-md bg-gray-100 px-3 py-1.5 text-sm font-medium text-gray-700 hover:bg-gray-200"
            (click)="onValidate()"
          >
            Validar
          </button>
          @if (deck().valid) {
            <button
              class="rounded-md bg-blue-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-blue-700"
              (click)="onPlay()"
            >
              Jugar
            </button>
          }
          <button
            class="rounded-md bg-red-50 px-3 py-1.5 text-sm font-medium text-red-700 hover:bg-red-100"
            (click)="confirmingDelete.set(true)"
          >
            Eliminar
          </button>
        }
      </div>
    </div>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DeckItemComponent {
  deck = input.required<DeckResponse>();
  readonly delete = output<string>();
  readonly validate = output<string>();
  readonly play = output<string>();
  readonly edit = output<string>();

  protected readonly confirmingDelete = signal(false);

  onEdit(): void {
    this.edit.emit(this.deck().id);
  }

  onValidate(): void {
    this.validate.emit(this.deck().id);
  }

  onPlay(): void {
    this.play.emit(this.deck().id);
  }

  onConfirmDelete(): void {
    this.delete.emit(this.deck().id);
    this.confirmingDelete.set(false);
  }
}
