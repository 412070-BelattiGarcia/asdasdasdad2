import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { DeckValidationModel } from '../../../../shared/models/deck.models';

@Component({
  selector: 'app-deck-summary',
  template: `
    <div class="rounded-lg border bg-white p-4 shadow-sm">
      <div class="mb-3 flex items-center justify-between">
        <h3 class="text-sm font-semibold text-gray-900">Resumen</h3>
        <span class="text-lg font-bold text-gray-900">{{ totalCards() }}</span>
      </div>

      @if (validation(); as v) {
        @if (v.valid) {
          <p class="text-sm font-medium text-green-700">✅ Listo para jugar.</p>
        } @else {
          <div class="space-y-1">
            <p class="text-sm font-medium text-red-700">❌ Inválido</p>
            <ul class="list-inside list-disc text-xs text-red-600">
              @for (err of v.errors; track err.code) {
                <li>{{ err.message }}</li>
              }
            </ul>
          </div>
        }
      } @else {
        <p class="text-sm text-gray-500">Aún no validado.</p>
      }
    </div>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DeckSummaryComponent {
  totalCards = input(0);
  validation = input<DeckValidationModel | null>(null);
}
