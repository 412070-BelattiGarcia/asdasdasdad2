import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';

export interface FilterOption {
  label: string;
  value: string;
}

@Component({
  selector: 'app-card-filter',
  template: `
      <select
        #filterSelect
        class="w-full rounded-lg border bg-white px-4 py-2 text-sm shadow-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
        (change)="onChange(filterSelect.value)"
      >
        @for (option of options(); track option.value) {
          <option [value]="option.value">
            {{ option.label }}
          </option>
        }
      </select>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CardFilterComponent {
  options = input<FilterOption[]>([]);
  selected = input<string>('');
  filterChange = output<string>();

  onChange(value: string): void {
    this.filterChange.emit(value);
  }
}
