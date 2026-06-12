import { ChangeDetectionStrategy, Component, input } from '@angular/core';

@Component({
  selector: 'app-prize-zone',
  standalone: true,
  template: `
    <div class="prize-zone" [class.own]="isOwn()" [class.opponent]="!isOwn()">
      @for (slot of slots; track $index) {
        <div class="prize-slot" [class.taken]="!slot">
          @if (slot) {
            <img [src]="cardBackUrl" alt="Premio" class="card-back-img" />
          }
        </div>
      }
    </div>
  `,
  styles: [`
    :host { display: block; }
    .prize-zone {
      display: flex; gap: 0.375rem; align-items: center;
    }
    .prize-slot {
      flex: 1; min-width: 0; aspect-ratio: 3/4;
      border-radius: 0.2rem; overflow: hidden;
      display: flex; align-items: center; justify-content: center;
    }
    .own .prize-slot { background: #1e293b; border: 1px solid #f59e0b; }
    .opponent .prize-slot { background: #334155; border: 1px solid #475569; }
    .prize-slot.taken { opacity: 0.25; border-style: dashed; }
    .card-back-img {
      width: 100%; height: 100%; object-fit: cover; display: block;
    }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PrizeZoneComponent {
  readonly prizeCount = input.required<number>();
  readonly isOwn = input.required<boolean>();
  readonly totalPrizeCount = input<number>(6);

  protected readonly cardBackUrl = 'assets/images/card-back.svg';

  get slots(): boolean[] {
    const total = this.totalPrizeCount();
    const remaining = this.prizeCount();
    return Array.from({ length: total }, (_, i) => i < remaining);
  }
}