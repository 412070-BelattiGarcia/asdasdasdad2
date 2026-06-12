import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { DragDropModule } from '@angular/cdk/drag-drop';
import { PrivateHandCardModel } from '../../../../shared/models/game-state.models';
import { SelectionMode } from '../../../../shared/models/ui-state.models';
import { CardImagePipe } from '../../../../shared/pipes/card-image.pipe';

@Component({
  selector: 'app-hand-zone',
  imports: [DragDropModule, CardImagePipe],
  template: `
    <div class="hand-zone" cdkDropList id="hand-zone-droplist" [cdkDropListConnectedTo]="connectedDropListIds()">
      @for (card of hand(); track card.instanceId; let i = $index) {
        <div
          class="hand-card"
          [class.dimmed]="isDimmed()"
          [class.selected]="selectedHandIndex() === i"
          [class.draggable-hand-card]="dragEnabled() && (card.supertype === 'POKEMON' || card.supertype === 'ENERGY')"
          [class.draggable-energy]="dragEnabled() && card.supertype === 'ENERGY'"
          [class.non-draggable]="dragEnabled() && card.supertype !== 'POKEMON' && card.supertype !== 'ENERGY'"
          cdkDrag
          [cdkDragData]="{ handIndex: i, supertype: card.supertype }"
          [cdkDragDisabled]="!dragEnabled() || (card.supertype !== 'POKEMON' && card.supertype !== 'ENERGY')"
          (click)="onCardClick(card, i)"
        >
          <img [src]="card.cardId | cardImage" alt="{{ card.name }}" class="card-img" />
          <span class="card-info">
            <span class="card-name">{{ card.name }}</span>
            <span class="card-supertype" [class]="card.supertype.toLowerCase()">{{ card.supertype }}</span>
          </span>
        </div>
      }
    </div>
  `,
  styles: [`
    .hand-zone {
      display: flex;
      gap: 0.5rem;
      padding: 0.75rem;
      background: #1e293b;
      border-radius: 0.5rem;
      border: 1px solid #334155;
      overflow-x: auto;
      flex-wrap: nowrap;
    }
    .hand-card {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 0.375rem;
      padding: 0.5rem;
      border: 2px solid #475569;
      border-radius: 0.375rem;
      background: #0f172a;
      cursor: pointer;
      transition: opacity 0.15s, border-color 0.15s;
      min-width: 70px;
      width: 70px;
      color: #e2e8f0;
      position: relative;
      user-select: none;
      touch-action: none;
    }
    .hand-card:hover {
      border-color: #60a5fa;
    }
    .hand-card.draggable-hand-card {
      cursor: grab;
    }
    .hand-card.draggable-hand-card:active {
      cursor: grabbing;
    }
    .hand-card.draggable-hand-card.cdk-drag-dragging {
      opacity: 0.3;
    }
    .hand-card.draggable-energy {
      cursor: grab;
      border-color: #a855f7;
    }
    .hand-card.draggable-energy:active {
      cursor: grabbing;
    }
    .hand-card.non-draggable {
      cursor: not-allowed;
      opacity: 0.55;
      border-color: #991b1b;
    }
    .hand-card.dimmed {
      opacity: 0.5;
      cursor: default;
      pointer-events: none;
    }
    .hand-card.selected {
      border-color: #fbbf24;
      box-shadow: 0 0 0 2px #fbbf24;
    }
    .card-img {
      width: 50px;
      aspect-ratio: 3/4;
      object-fit: cover;
      border-radius: 0.25rem;
      background: #334155;
    }
    .card-info {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 0.125rem;
      width: 100%;
    }
    .card-name {
      font-size: 0.625rem;
      font-weight: 600;
      text-align: center;
      line-height: 1.2;
      max-width: 100%;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    .card-supertype {
      font-size: 0.5625rem;
      font-weight: 700;
      text-transform: uppercase;
      padding: 0.0625rem 0.25rem;
      border-radius: 0.25rem;
    }
    .card-supertype.pokemon { background: #3b82f6; color: #fff; }
    .card-supertype.energy  { background: #a855f7; color: #fff; }
    .card-supertype.trainer { background: #22c55e; color: #fff; }

    @media (min-width: 640px) {
      .hand-zone {
        flex-wrap: wrap;
        overflow-x: visible;
      }
    }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HandZoneComponent {
  readonly hand = input<PrivateHandCardModel[]>([]);
  readonly selectionMode = input<SelectionMode>('NONE');
  readonly selectedHandIndex = input<number | null>(null);
  readonly dragEnabled = input(false);
  readonly connectedDropListIds = input<string[]>([]);

  readonly cardClicked = output<{ card: PrivateHandCardModel; handIndex: number }>();

  protected isDimmed(): boolean {
    return this.selectionMode() !== 'NONE';
  }

  protected onCardClick(card: PrivateHandCardModel, handIndex: number): void {
    this.cardClicked.emit({ card, handIndex });
  }
}
