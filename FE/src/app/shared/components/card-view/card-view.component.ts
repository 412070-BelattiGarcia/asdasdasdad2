import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { CardSummaryResponse } from '../../models/card.models';
import { CardImagePipe } from '../../pipes/card-image.pipe';

@Component({
  selector: 'app-card-view',
  imports: [CardImagePipe],
  template: `
    <div class="overflow-hidden rounded-lg border bg-white shadow-sm transition-shadow hover:shadow-md">
      <div class="aspect-[2.5/3.5] bg-gray-100">
        <img
          [src]="card().id | cardImage:'small'"
          [alt]="card().name"
          class="h-full w-full object-contain"
          (error)="onImageError($event)"
        />
      </div>
      <div class="p-2">
        <p class="truncate text-sm font-medium">{{ card().name }}</p>
        <p class="text-xs text-gray-500">{{ card().supertype }} · {{ card().setCode }}</p>
      </div>
    </div>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CardViewComponent {
  card = input.required<CardSummaryResponse>();

  onImageError(event: Event): void {
    const img = event.target as HTMLImageElement;
    img.style.display = 'none';
    const parent = img.parentElement;
    if (parent) {
      parent.classList.add('flex', 'items-center', 'justify-center');
      const placeholder = document.createElement('div');
      placeholder.className = 'text-center text-sm text-gray-400';
      placeholder.textContent = this.card().name;
      parent.appendChild(placeholder);
    }
  }
}
