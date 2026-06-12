import { ChangeDetectionStrategy, Component, input } from '@angular/core';

@Component({
  selector: 'app-deck-validation',
  template: `
    @if (valid() === true) {
      <span class="inline-flex items-center gap-1 rounded-full bg-green-100 px-2.5 py-0.5 text-xs font-medium text-green-800">
        ✅ Válido
      </span>
    } @else if (valid() === false) {
      <span class="inline-flex items-center gap-1 rounded-full bg-red-100 px-2.5 py-0.5 text-xs font-medium text-red-800">
        ❌ Inválido
      </span>
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DeckValidationComponent {
  valid = input<boolean | null>(null);
}
